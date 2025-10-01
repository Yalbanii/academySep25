# Spring Batch - Guía para Desarrolladores Academia

## 📚 Sistema Bancario Digital - Procesamiento de Intereses Mensuales

**Autor:** Sistema de Banco Digital
**Fecha:** Octubre 2025
**Audiencia:** Desarrolladores Academia
**Nivel:** Intermedio

---

## 🎯 ¿Qué es Spring Batch?

Spring Batch es un framework de Spring que nos permite **procesar grandes cantidades de datos de manera eficiente y confiable**. Piensa en él como una fábrica automatizada que procesa miles de registros siguiendo un flujo ordenado.

### ¿Por qué usamos Spring Batch en nuestro banco?

Imagina que cada mes tenemos que:
1. Revisar **todas las cuentas bancarias** (pueden ser miles o millones)
2. Calcular el **interés mensual** para cada una
3. Actualizar el **balance** en la base de datos
4. Registrar **todas las transacciones** en los logs
5. Enviar **notificaciones** a los clientes

Hacer esto manualmente o con un simple `for` loop sería:
- ❌ Lento
- ❌ Propenso a errores
- ❌ Difícil de monitorear
- ❌ Sin recuperación ante fallos

**Spring Batch** nos da todo esto automáticamente ✅

---

## 🏗️ Arquitectura del Sistema de Intereses

### Flujo General

```
┌─────────────────────────────────────────────────────────────────┐
│                   MONTHLY INTEREST JOB                          │
│                                                                 │
│  1️⃣ Trigger Manual (POST /api/batch/monthly-interest)          │
│                          ↓                                       │
│  2️⃣ STEP 1: Calcular y Aplicar Intereses                        │
│     ┌─────────────────────────────────────────────────┐        │
│     │ Reader → Processor → Writer                      │        │
│     │ (MySQL)  (Cálculo)   (MySQL + Contexto)         │        │
│     └─────────────────────────────────────────────────┘        │
│                          ↓                                       │
│  3️⃣ STEP 2: Publicar Eventos                                    │
│     ┌─────────────────────────────────────────────────┐        │
│     │ Reader → Processor → Writer                      │        │
│     │ (Contexto) (Pass)    (Event Publisher)          │        │
│     └─────────────────────────────────────────────────┘        │
│                          ↓                                       │
│  4️⃣ Event Listener → MongoDB (Transaction Logs)                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## 📦 Componentes Principales

### 1. Job (El Trabajo Completo)

**Archivo:** `MonthlyInterestBatchConfig.java:64-76`

```java
@Bean
public Job monthlyInterestJob() {
    JobBuilder jobBuilder = new JobBuilder("monthlyInterestJob", jobRepository);

    // Listener para guardar logs en MongoDB
    if (batchJobExecutionMongoListener != null) {
        jobBuilder.listener(batchJobExecutionMongoListener);
    }

    return jobBuilder
            .start(calculateAndApplyInterestStep())    // PASO 1
            .next(publishEventsStep())                 // PASO 2
            .build();
}
```

**¿Qué hace?**
- Define el **trabajo completo** llamado "monthlyInterestJob"
- Ejecuta **2 pasos en secuencia**: primero calcula, luego notifica
- Tiene un **listener** que guarda información de la ejecución en MongoDB

**Analogía:** Es como una receta de cocina con 2 pasos principales.

---

### 2. STEP 1: Calculate and Apply Interest

#### 📖 Reader (Lector de Datos)

**Archivo:** `MonthlyInterestBatchConfig.java:91-99`

```java
@Bean
public RepositoryItemReader<Account> accountReader() {
    return new RepositoryItemReaderBuilder<Account>()
            .name("accountReader")
            .repository(accountRepository)
            .methodName("findByActive")           // Solo cuentas activas
            .sorts(Collections.singletonMap("id", Sort.Direction.ASC))
            .pageSize(10)                         // Lee 10 registros a la vez
            .build();
}
```

**¿Qué hace?**
- Lee las cuentas bancarias **de 10 en 10** (paginación)
- Solo lee cuentas **activas** (no cerradas)
- Las ordena por **ID** para procesar siempre en el mismo orden

**Analogía:** Es como un cajero que atiende clientes de 10 en 10, no todos a la vez.

**Ejemplo de datos que lee:**
```java
Account(id=1, accountNumber="400013459224", type=CHECKING, balance=1201.00, status=ACTIVE)
Account(id=2, accountNumber="400068159471", type=SAVINGS, balance=100.42, status=ACTIVE)
// ... hasta 10 cuentas
```

---

#### ⚙️ Processor (Procesador - AQUÍ ESTÁ EL POLIMORFISMO)

**Archivo:** `MonthlyInterestBatchConfig.java:102-133`

```java
@Bean
public ItemProcessor<Account, AccountInterestData> interestCalculatorProcessor() {
    return account -> {
        log.info("Processing account: {} (Type: {}, Balance: ${})",
                account.getAccountNumber(),
                account.getAccountType(),
                account.getBalance());

        try {
            // 🎯 POLIMORFISMO: El factory elige el calculador correcto
            InterestCalculator calculator = calculatorFactory.getCalculator(account.getAccountType());
            BigDecimal interest = calculator.calculateInterest(account);

            if (interest.compareTo(BigDecimal.ZERO) > 0) {
                log.info("Interest calculated for account {}: ${}",
                        account.getAccountNumber(),
                        interest);

                return new AccountInterestData(account, interest);
            } else {
                return null; // No hay interés, se omite
            }
        } catch (Exception e) {
            log.error("Error calculating interest: {}", e.getMessage());
            return null;
        }
    };
}
```

**¿Qué hace?**
1. Recibe una cuenta (`Account`)
2. Usa **polimorfismo** para elegir el calculador correcto:
   - Si es `CHECKING` → usa `CheckingInterestCalculator` (1% anual)
   - Si es `SAVINGS` → usa `SavingsInterestCalculator` (5% anual)
3. Calcula el interés mensual
4. Retorna `AccountInterestData` con el interés calculado

**Analogía:** Es como un chef que prepara platillos diferentes según el tipo de orden (vegetariana, carnívora, vegana).

---

#### 🔢 ¿Cómo funciona el Polimorfismo?

**Interface Base:** `InterestCalculator.java`

```java
public interface InterestCalculator {
    BigDecimal calculateInterest(Account account);
    BigDecimal getInterestRate();
    Account.AccountType getAccountType();
}
```

**Implementación 1: Cuentas de Ahorro (SAVINGS)**

```java
@Component
public class SavingsInterestCalculator implements InterestCalculator {
    // 5% anual = 0.4166667% mensual
    private static final BigDecimal MONTHLY_INTEREST_RATE = new BigDecimal("0.004166667");

    @Override
    public BigDecimal calculateInterest(Account account) {
        if (account.getStatus() != Account.AccountStatus.ACTIVE) {
            return BigDecimal.ZERO;
        }

        BigDecimal balance = account.getBalance();

        // Interés = Balance × 0.004166667
        return balance.multiply(MONTHLY_INTEREST_RATE)
                     .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public Account.AccountType getAccountType() {
        return Account.AccountType.SAVINGS;
    }
}
```

**Implementación 2: Cuentas Corrientes (CHECKING)**

```java
@Component
public class CheckingInterestCalculator implements InterestCalculator {
    // 1% anual = 0.0833333% mensual
    private static final BigDecimal MONTHLY_INTEREST_RATE = new BigDecimal("0.000833333");

    @Override
    public BigDecimal calculateInterest(Account account) {
        if (account.getStatus() != Account.AccountStatus.ACTIVE) {
            return BigDecimal.ZERO;
        }

        BigDecimal balance = account.getBalance();

        // Interés = Balance × 0.000833333
        return balance.multiply(MONTHLY_INTEREST_RATE)
                     .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public Account.AccountType getAccountType() {
        return Account.AccountType.CHECKING;
    }
}
```

**Factory que elige el calculador:**

```java
@Component
@RequiredArgsConstructor
public class InterestCalculatorFactory {
    // Spring inyecta TODAS las implementaciones de InterestCalculator
    private final List<InterestCalculator> calculators;

    public InterestCalculator getCalculator(Account.AccountType accountType) {
        return calculators.stream()
                .filter(calc -> calc.getAccountType() == accountType)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                    "No calculator found for: " + accountType));
    }
}
```

**🌟 Ventaja del Polimorfismo:**

Sin polimorfismo (código acoplado ❌):
```java
BigDecimal interest;
if (account.getAccountType() == SAVINGS) {
    interest = balance.multiply(new BigDecimal("0.004166667"));
} else if (account.getAccountType() == CHECKING) {
    interest = balance.multiply(new BigDecimal("0.000833333"));
} else if (account.getAccountType() == BUSINESS) {
    interest = balance.multiply(new BigDecimal("0.01"));
}
```

Con polimorfismo (código desacoplado ✅):
```java
InterestCalculator calculator = factory.getCalculator(account.getAccountType());
BigDecimal interest = calculator.calculateInterest(account);
```

**Para agregar un nuevo tipo de cuenta solo necesitas:**
```java
@Component
public class BusinessInterestCalculator implements InterestCalculator {
    private static final BigDecimal MONTHLY_INTEREST_RATE = new BigDecimal("0.01");

    // ... implementación
}
```

¡No necesitas modificar ningún otro código! 🎉

---

#### ✍️ Writer (Escritor de Datos)

**Archivo:** `MonthlyInterestBatchConfig.java:136-173`

```java
@Bean
public ItemWriter<AccountInterestData> interestApplierWriter() {
    return items -> {
        for (AccountInterestData data : items) {
            if (data != null && data.shouldApplyInterest()) {
                // 1. Buscar la cuenta en la base de datos
                Account account = accountRepository.findById(data.getAccountId())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Account not found: " + data.getAccountId()));

                // 2. Calcular nuevo balance
                BigDecimal previousBalance = account.getBalance();
                BigDecimal newBalance = previousBalance.add(data.getCalculatedInterest());

                // 3. Actualizar cuenta
                account.setBalance(newBalance);
                account.setUpdatedAt(LocalDateTime.now());

                // 4. Guardar en MySQL
                accountRepository.save(account);

                log.info("✅ Interest applied to account {}: ${} (Balance: ${} → ${})",
                        account.getAccountNumber(),
                        data.getCalculatedInterest(),
                        previousBalance,
                        newBalance);

                // 5. Guardar en contexto para Step 2
                executionContext.put("interest_" + account.getId(), data);
            }
        }
    };
}
```

**¿Qué hace?**
1. Recibe una lista de `AccountInterestData` (hasta 10)
2. Para cada cuenta:
   - Actualiza el balance: `nuevo_balance = balance_anterior + interés`
   - Guarda los cambios en **MySQL**
   - Guarda los datos en el **ExecutionContext** (memoria compartida entre steps)

**Ejemplo:**
```
Cuenta: 400013459224
Balance anterior: $1,201.00
Interés calculado: $1.00
Nuevo balance: $1,202.00 ← Se guarda en MySQL
```

---

### 3. STEP 2: Publish Events

#### 📖 Reader (Lee del Contexto)

**Archivo:** `MonthlyInterestBatchConfig.java:188-217`

```java
@Bean
public ItemReader<AccountInterestData> interestDataReader() {
    return new ItemReader<>() {
        private final List<AccountInterestData> processedAccounts = new ArrayList<>();
        private int currentIndex = 0;

        @Override
        public AccountInterestData read() {
            // Primera lectura: cargar del contexto del Step 1
            if (processedAccounts.isEmpty()) {
                ExecutionContext context = stepExecution.getExecutionContext();
                for (String key : context.keySet()) {
                    if (key.startsWith("interest_")) {
                        AccountInterestData data = (AccountInterestData) context.get(key);
                        processedAccounts.add(data);
                    }
                }
            }

            // Leer siguiente item
            if (currentIndex < processedAccounts.size()) {
                return processedAccounts.get(currentIndex++);
            }

            return null; // Fin de lectura
        }
    };
}
```

**¿Qué hace?**
- Lee los datos que el **Step 1 guardó en el ExecutionContext**
- No lee de la base de datos, lee de **memoria**
- Retorna cada `AccountInterestData` uno por uno

**Analogía:** Es como leer notas que dejaste en una pizarra compartida.

---

#### ✍️ Writer (Publica Eventos)

**Archivo:** `MonthlyInterestBatchConfig.java:225-249`

```java
@Bean
public ItemWriter<AccountInterestData> eventPublisherWriter() {
    return items -> {
        for (AccountInterestData data : items) {
            Optional<Customer> customer = customerRepository.findById(data.getCustomerId());

            if (customer.isPresent()) {
                // PUBLICAR EVENTO
                InterestAppliedEvent event = new InterestAppliedEvent(
                        data.getAccountId(),
                        data.getAccountNumber(),
                        data.getCalculatedInterest(),
                        data.getNewBalance(),
                        customer.get().getEmail(),
                        LocalDateTime.now()
                );

                eventPublisher.publishEvent(event);

                log.info("📢 InterestAppliedEvent published for account: {} (Interest: ${})",
                        data.getAccountNumber(),
                        data.getCalculatedInterest());
            }
        }
    };
}
```

**¿Qué hace?**
1. Para cada cuenta procesada
2. Busca el cliente dueño de la cuenta
3. **Publica un evento** `InterestAppliedEvent`
4. El evento es escuchado por `TransactionLogService`

---

### 4. Event Listener (Guarda Logs en MongoDB)

**Archivo:** `TransactionLogServiceImpl.java:155-172`

```java
@ApplicationModuleListener
public void handleInterestApplied(InterestAppliedEvent event) {
    log.debug("Logging InterestAppliedEvent for account: {}", event.getAccountNumber());

    TransactionLog log = TransactionLog.builder()
            .transactionId(UUID.randomUUID().toString())
            .accountNumber(event.getAccountNumber())
            .transactionType("INTEREST_APPLIED")
            .amount(event.getInterestAmount())
            .balanceAfter(event.getNewBalance())
            .timestamp(event.getTimestamp())
            .description(String.format("Monthly interest applied: %s", event.getInterestAmount()))
            .customerId(0L)
            .status("SUCCESS")
            .build();

    createTransactionLog(log);  // Guarda en MongoDB
}
```

**¿Qué hace?**
- Escucha el evento `InterestAppliedEvent`
- Crea un registro de transacción
- Lo guarda en **MongoDB** (colección `transaction_logs`)

**Documento guardado en MongoDB:**
```json
{
  "_id": ObjectId("..."),
  "transactionId": "550e8400-e29b-41d4-a716-446655440001",
  "accountNumber": "400013459224",
  "transactionType": "INTEREST_APPLIED",
  "amount": 1.00,
  "balanceAfter": 1202.00,
  "timestamp": ISODate("2025-10-01T06:57:39Z"),
  "description": "Monthly interest applied: 1.00",
  "status": "SUCCESS"
}
```

---

## 🚀 ¿Cómo Ejecutar el Batch?

### Opción 1: Manualmente vía API

```bash
curl -X POST http://localhost:8080/api/batch/monthly-interest
```

**Respuesta:**
```json
{
  "message": "Monthly Interest Job triggered successfully",
  "timestamp": "2025-10-01T06:57:39.760468",
  "status": "RUNNING"
}
```

### Opción 2: Programado (Futuro)

```java
@Scheduled(cron = "0 0 2 1 * ?")  // 2 AM el día 1 de cada mes
public void runMonthlyInterest() {
    JobParameters jobParameters = new JobParametersBuilder()
            .addLong("timestamp", System.currentTimeMillis())
            .toJobParameters();

    jobLauncher.run(monthlyInterestJob, jobParameters);
}
```

---

## 📊 Resultados Reales

### Antes del Batch

| Cuenta | Tipo | Balance |
|--------|------|---------|
| 400013459224 | CHECKING | $1,201.00 |
| 400068159471 | SAVINGS | $100.42 |
| 400084675118 | SAVINGS | $2,158.96 |

### Después del Batch

| Cuenta | Tipo | Balance | Interés |
|--------|------|---------|---------|
| 400013459224 | CHECKING | $1,202.00 | **$1.00** |
| 400068159471 | SAVINGS | $100.84 | **$0.42** |
| 400084675118 | SAVINGS | $2,167.96 | **$9.00** |

### Validación del Polimorfismo

```
CHECKING con $1,201.00 → Interés: $1.00 (0.083%)
SAVINGS con $100.42 → Interés: $0.42 (0.417%)

Ratio: SAVINGS recibe 5x más interés que CHECKING ✅
```

---

## 🎨 Chunk-Oriented Processing

Spring Batch procesa datos en **chunks (bloques)**:

```
┌─────────────────────────────────────────────────────────┐
│  CHUNK 1 (10 cuentas)                                   │
│  ┌──────────┐   ┌──────────┐   ┌──────────┐           │
│  │  Reader  │ → │ Processor│ → │  Writer  │           │
│  └──────────┘   └──────────┘   └──────────┘           │
│  Lee 10        Procesa 10      Guarda 10              │
│                                                         │
│  ✅ COMMIT - Todo o nada                                │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│  CHUNK 2 (10 cuentas)                                   │
│  ┌──────────┐   ┌──────────┐   ┌──────────┐           │
│  │  Reader  │ → │ Processor│ → │  Writer  │           │
│  └──────────┘   └──────────┘   └──────────┘           │
│  Lee 10        Procesa 10      Guarda 10              │
│                                                         │
│  ✅ COMMIT - Todo o nada                                │
└─────────────────────────────────────────────────────────┘
```

**Ventajas:**
- ✅ **Transaccional:** Si falla el chunk 2, el chunk 1 ya está guardado
- ✅ **Eficiente:** No carga todas las cuentas en memoria
- ✅ **Recuperable:** Puede reintentar chunks fallidos

---

## 🔍 Configuración

**Archivo:** `application.properties`

```properties
# Batch Configuration
spring.batch.jdbc.initialize-schema=always   # Crea tablas de metadatos
spring.batch.job.enabled=false               # Deshabilitado por defecto
```

**Para habilitar:**
```properties
spring.batch.job.enabled=true
```

---

## 📈 Metadatos en MySQL

Spring Batch guarda información de ejecución en MySQL:

### Tablas creadas automáticamente:

```sql
BATCH_JOB_INSTANCE          -- Instancias de jobs
BATCH_JOB_EXECUTION         -- Ejecuciones de jobs
BATCH_JOB_EXECUTION_PARAMS  -- Parámetros de ejecución
BATCH_STEP_EXECUTION        -- Ejecuciones de steps
BATCH_STEP_EXECUTION_CONTEXT -- Contexto de steps
BATCH_JOB_EXECUTION_CONTEXT  -- Contexto de jobs
```

### Consultar última ejecución:

```sql
SELECT
    job_execution_id,
    status,
    start_time,
    end_time,
    exit_code
FROM BATCH_JOB_EXECUTION
ORDER BY start_time DESC
LIMIT 1;
```

**Resultado:**
```
job_execution_id | status    | start_time          | end_time            | exit_code
2                | COMPLETED | 2025-10-01 06:57:39 | 2025-10-01 06:57:40 | COMPLETED
```

---

## 🧪 Testing

### Test del Processor

```java
@Test
void testInterestCalculatorProcessor_Savings() {
    // Given
    Account savingsAccount = new Account();
    savingsAccount.setAccountType(Account.AccountType.SAVINGS);
    savingsAccount.setBalance(new BigDecimal("1000.00"));
    savingsAccount.setStatus(Account.AccountStatus.ACTIVE);

    // When
    AccountInterestData result = processor.process(savingsAccount);

    // Then
    assertNotNull(result);
    assertEquals(new BigDecimal("4.17"), result.getCalculatedInterest());
}

@Test
void testInterestCalculatorProcessor_Checking() {
    // Given
    Account checkingAccount = new Account();
    checkingAccount.setAccountType(Account.AccountType.CHECKING);
    checkingAccount.setBalance(new BigDecimal("1000.00"));
    checkingAccount.setStatus(Account.AccountStatus.ACTIVE);

    // When
    AccountInterestData result = processor.process(checkingAccount);

    // Then
    assertNotNull(result);
    assertEquals(new BigDecimal("0.83"), result.getCalculatedInterest());
}
```

---

## 🎓 Conceptos Clave

### 1. **Job**
Es el trabajo completo que quieres ejecutar. Ejemplo: "Calcular intereses mensuales"

### 2. **Step**
Es un paso dentro del Job. Ejemplo: "Calcular intereses" es un step, "Enviar notificaciones" es otro step

### 3. **Reader**
Lee datos de algún lugar (base de datos, archivo, API)

### 4. **Processor**
Procesa/transforma cada registro leído

### 5. **Writer**
Guarda los resultados procesados

### 6. **Chunk**
Grupo de registros procesados juntos (en nuestro caso: 10 cuentas)

### 7. **JobRepository**
Almacén de metadatos del batch (quién ejecutó qué, cuándo, resultado)

### 8. **ExecutionContext**
Memoria compartida entre steps (como una pizarra compartida)

### 9. **JobLauncher**
El que ejecuta el Job

### 10. **Polimorfismo**
Usar una interface común para múltiples implementaciones diferentes

---

## 💡 Mejores Prácticas

### ✅ DO (Hacer)

1. **Usar chunks pequeños** (10-100 registros) para balance entre memoria y commits
2. **Validar datos en el Processor** antes de escribir
3. **Usar transacciones** (Spring Batch lo hace automáticamente)
4. **Loggear información importante** para debug
5. **Usar polimorfismo** para lógica que varía por tipo

### ❌ DON'T (No Hacer)

1. **No cargar todo en memoria** (usa paginación)
2. **No hardcodear tasas de interés** (usa constantes o configuración)
3. **No mezclar lógica de negocio en el Writer** (eso va en el Processor)
4. **No ignorar errores** (maneja excepciones apropiadamente)
5. **No usar if-else para tipos** (usa polimorfismo)

---

## 🐛 Troubleshooting Común

### Problema 1: Job no ejecuta
```
Error: No static resource api/batch/monthly-interest
```

**Solución:**
```properties
spring.batch.job.enabled=true
```

### Problema 2: Duplicate key violation
```
Error: Job instance already exists
```

**Solución:** Usar parámetros únicos
```java
JobParameters params = new JobParametersBuilder()
    .addLong("timestamp", System.currentTimeMillis())  // ← Siempre diferente
    .toJobParameters();
```

### Problema 3: OutOfMemoryError
```
Error: Java heap space
```

**Solución:** Reducir chunk size
```java
.chunk(5, transactionManager)  // ← Reduce de 10 a 5
```

---

## 📚 Recursos Adicionales

- [Spring Batch Documentation](https://docs.spring.io/spring-batch/docs/current/reference/html/)
- [Baeldung Spring Batch Tutorial](https://www.baeldung.com/introduction-to-spring-batch)
- Código fuente: `src/main/java/com/xideral/banco/batch/`

---

## ✅ Checklist de Aprendizaje

Marca cuando entiendas cada concepto:

- [ ] Entiendo qué es un Job y para qué sirve
- [ ] Entiendo qué es un Step y cómo se relaciona con el Job
- [ ] Puedo explicar la diferencia entre Reader, Processor y Writer
- [ ] Entiendo qué es chunk-oriented processing
- [ ] Puedo explicar cómo funciona el polimorfismo en los calculadores
- [ ] Sé cómo ejecutar el batch manualmente
- [ ] Entiendo cómo se guardan los datos en MySQL y MongoDB
- [ ] Puedo agregar un nuevo tipo de cuenta con su calculador
- [ ] Sé cómo verificar los resultados del batch
- [ ] Entiendo el flujo completo de principio a fin

---

## 🎯 Ejercicio Práctico

**Desafío:** Agrega un nuevo tipo de cuenta llamada `BUSINESS` con las siguientes características:

1. Tasa de interés: **12% anual** (1% mensual)
2. Solo aplica interés si el balance es mayor a $5,000
3. Los logs deben indicar "Business interest applied"

**Pasos:**
1. Crea `BusinessInterestCalculator implements InterestCalculator`
2. Define la tasa: `0.01` (1% mensual)
3. Agrega lógica para validar balance mínimo
4. Agrega el enum `BUSINESS` a `Account.AccountType`
5. Ejecuta el batch y valida resultados

**Solución:**
¡Inténtalo primero! La factory automáticamente detectará tu nueva implementación sin modificar código existente. Esa es la magia del polimorfismo. 🎩✨

---

**¿Preguntas?** Revisa el código fuente o consulta con el senior.

**Happy Batching!** 🚀
