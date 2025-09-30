# Día 5: Spring Batch - Procesamiento Mensual de Intereses

## Academia Xideral - FullStack Development Course
### Sistema Bancario Digital - Proyecto Final

---

## 📋 Tabla de Contenidos

1. [Introducción](#introducción)
2. [Objetivos del Día 5](#objetivos-del-día-5)
3. [Arquitectura del Batch Job](#arquitectura-del-batch-job)
4. [Concepto: Polimorfismo en Calculadores de Interés](#concepto-polimorfismo-en-calculadores-de-interés)
5. [Implementación Paso a Paso](#implementación-paso-a-paso)
6. [Pruebas del Batch Job](#pruebas-del-batch-job)
7. [Conclusiones](#conclusiones)

---

## Introducción

El **Día 5** finaliza el proyecto con la implementación de **Spring Batch** para el procesamiento automático mensual de intereses. Este módulo demuestra:

- ✅ **Spring Batch**: Jobs con steps definidos
- ✅ **Polimorfismo**: Calculadores diferentes por tipo de cuenta
- ✅ **Bases de Datos Híbridas**: MySQL para transacciones, MongoDB para logs
- ✅ **Procesamiento por Lotes**: Lectura, procesamiento y escritura en chunks

### Contexto Bancario

**Requerimiento de Negocio:**
Cada mes, el banco debe calcular y aplicar intereses a todas las cuentas activas de sus clientes, con tasas diferentes según el tipo de cuenta:

- **Cuentas de Ahorro (SAVINGS)**: 5% anual (0.42% mensual)
- **Cuentas Corrientes (CHECKING)**: 1% anual (0.083% mensual)

---

## Objetivos del Día 5

Al finalizar este día, habrás implementado:

✅ Interface `InterestCalculator` con polimorfismo
✅ Implementaciones específicas para SAVINGS y CHECKING
✅ Factory pattern para selección dinámica de calculadores
✅ Spring Batch Job con 1 step
✅ Item Reader, Processor y Writer
✅ Listener para logging en MongoDB
✅ REST endpoint para ejecución manual
✅ Documentación completa del sistema

---

## Arquitectura del Batch Job

### Diagrama General

```
┌──────────────────────────────────────────────────────────────┐
│                    Monthly Interest Job                       │
├──────────────────────────────────────────────────────────────┤
│                                                               │
│  calculateInterestStep                                        │
│  ┌────────────────────────────────────────────────────┐     │
│  │                                                     │     │
│  │  Reader         →    Processor    →    Writer      │     │
│  │    ↓                    ↓               ↓          │     │
│  │  MySQL            Polimorfismo      MySQL          │     │
│  │  (Accounts)       Calculate         Update         │     │
│  │                   Interest          Balance        │     │
│  │                                                     │     │
│  └────────────────────────────────────────────────────┘     │
│                                                               │
│  Logs  →  MongoDB (batch_job_executions)                     │
└──────────────────────────────────────────────────────────────┘
```

### Flujo de Procesamiento

1. **Reader**: Lee cuentas activas de MySQL en chunks de 10
2. **Processor**: Calcula interés usando polimorfismo (SAVINGS vs CHECKING)
3. **Writer**: Actualiza balance en MySQL y registra en MongoDB
4. **Listener**: Registra inicio, fin y estadísticas en MongoDB

---

## Concepto: Polimorfismo en Calculadores de Interés

### ¿Por qué Polimorfismo?

Diferentes tipos de cuentas requieren cálculos diferentes:

- **SAVINGS**: Mayor tasa de interés (5% anual)
- **CHECKING**: Menor tasa de interés (1% anual)

En lugar de usar `if-else` o `switch`, usamos **polimorfismo** para que cada tipo de cuenta tenga su propio calculador.

### Diagrama de Polimorfismo

```
┌─────────────────────────────────┐
│    <<interface>>                 │
│    InterestCalculator            │
├─────────────────────────────────┤
│ + calculateInterest(Account)     │
│ + getInterestRate()              │
│ + getAccountType()               │
└─────────────────────────────────┘
         △                  △
         │                  │
         │                  │
┌────────┴────────┐  ┌─────┴──────────┐
│ Savings         │  │ Checking        │
│ InterestCalc    │  │ InterestCalc    │
├─────────────────┤  ├─────────────────┤
│ Rate: 0.004167  │  │ Rate: 0.000833  │
│ (5% anual)      │  │ (1% anual)      │
└─────────────────┘  └─────────────────┘
```

---

## Implementación Paso a Paso

### Paso 1: Interface InterestCalculator

**Archivo:** `src/main/java/com/xideral/banco/batch/interest/InterestCalculator.java`

```java
public interface InterestCalculator {
    /**
     * Calcula el interés para una cuenta específica.
     */
    BigDecimal calculateInterest(Account account);

    /**
     * Obtiene la tasa de interés aplicable.
     */
    BigDecimal getInterestRate();

    /**
     * Obtiene el tipo de cuenta al que aplica.
     */
    Account.AccountType getAccountType();
}
```

**✅ Ventajas:**
- Define contrato común
- Permite múltiples implementaciones
- Facilita testing y extensión

---

### Paso 2: Implementación para Cuentas de Ahorro

**Archivo:** `src/main/java/com/xideral/banco/batch/interest/SavingsInterestCalculator.java`

```java
@Component
public class SavingsInterestCalculator implements InterestCalculator {

    // 5% anual = 0.42% mensual
    private static final BigDecimal MONTHLY_INTEREST_RATE =
        new BigDecimal("0.004166667");

    @Override
    public BigDecimal calculateInterest(Account account) {
        if (account.getAccountType() != Account.AccountType.SAVINGS) {
            throw new IllegalArgumentException(
                "SavingsInterestCalculator only applies to SAVINGS accounts"
            );
        }

        if (!account.isActive()) {
            return BigDecimal.ZERO;
        }

        BigDecimal balance = account.getBalance();
        if (balance.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        // Interés = Balance * Tasa Mensual
        return balance.multiply(MONTHLY_INTEREST_RATE)
                     .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal getInterestRate() {
        return MONTHLY_INTEREST_RATE;
    }

    @Override
    public Account.AccountType getAccountType() {
        return Account.AccountType.SAVINGS;
    }
}
```

**📊 Ejemplo de Cálculo:**
- Balance: $10,000.00
- Tasa mensual: 0.42%
- Interés = $10,000 × 0.004166667 = **$41.67**

---

### Paso 3: Implementación para Cuentas Corrientes

**Archivo:** `src/main/java/com/xideral/banco/batch/interest/CheckingInterestCalculator.java`

```java
@Component
public class CheckingInterestCalculator implements InterestCalculator {

    // 1% anual = 0.083% mensual
    private static final BigDecimal MONTHLY_INTEREST_RATE =
        new BigDecimal("0.000833333");

    @Override
    public BigDecimal calculateInterest(Account account) {
        if (account.getAccountType() != Account.AccountType.CHECKING) {
            throw new IllegalArgumentException(
                "CheckingInterestCalculator only applies to CHECKING accounts"
            );
        }

        if (!account.isActive()) {
            return BigDecimal.ZERO;
        }

        BigDecimal balance = account.getBalance();
        if (balance.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        return balance.multiply(MONTHLY_INTEREST_RATE)
                     .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal getInterestRate() {
        return MONTHLY_INTEREST_RATE;
    }

    @Override
    public Account.AccountType getAccountType() {
        return Account.AccountType.CHECKING;
    }
}
```

**📊 Ejemplo de Cálculo:**
- Balance: $10,000.00
- Tasa mensual: 0.083%
- Interés = $10,000 × 0.000833333 = **$8.33**

---

### Paso 4: Factory para Selección Dinámica

**Archivo:** `src/main/java/com/xideral/banco/batch/interest/InterestCalculatorFactory.java`

```java
@Component
@RequiredArgsConstructor
public class InterestCalculatorFactory {

    private final List<InterestCalculator> calculators;

    public InterestCalculator getCalculator(Account.AccountType accountType) {
        return calculators.stream()
                .filter(calc -> calc.getAccountType() == accountType)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                    "No interest calculator found for account type: " + accountType
                ));
    }
}
```

**🎯 Polimorfismo en Acción:**

```java
// Spring inyecta automáticamente TODOS los implementadores
List<InterestCalculator> = [
    SavingsInterestCalculator,    // @Component
    CheckingInterestCalculator    // @Component
]

// Factory selecciona el correcto según el tipo
InterestCalculator calc = factory.getCalculator(account.getAccountType());
BigDecimal interest = calc.calculateInterest(account);  // ← Polimorfismo!
```

---

### Paso 5: Configuración de Spring Batch

**Archivo:** `src/main/java/com/xideral/banco/batch/config/MonthlyInterestBatchConfig.java`

```java
@Configuration
@RequiredArgsConstructor
public class MonthlyInterestBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final AccountRepository accountRepository;
    private final InterestCalculatorFactory calculatorFactory;

    // ========== JOB DEFINITION ==========

    @Bean
    public Job monthlyInterestJob() {
        return new JobBuilder("monthlyInterestJob", jobRepository)
                .listener(batchJobExecutionMongoListener)
                .start(calculateInterestStep())
                .build();
    }

    // ========== STEP: CALCULATE & APPLY INTEREST ==========

    @Bean
    public Step calculateInterestStep() {
        return new StepBuilder("calculateInterestStep", jobRepository)
                .<Account, AccountInterestData>chunk(10, transactionManager)
                .reader(accountReader())
                .processor(interestCalculatorProcessor())
                .writer(interestApplierWriter())
                .build();
    }

    @Bean
    public RepositoryItemReader<Account> accountReader() {
        return new RepositoryItemReaderBuilder<Account>()
                .name("accountReader")
                .repository(accountRepository)
                .methodName("findByActive")
                .arguments(List.of(true))
                .sorts(Collections.singletonMap("id", Sort.Direction.ASC))
                .pageSize(10)
                .build();
    }

    @Bean
    public ItemProcessor<Account, AccountInterestData> interestCalculatorProcessor() {
        return account -> {
            // ========== POLIMORFISMO AQUÍ ==========
            InterestCalculator calculator =
                calculatorFactory.getCalculator(account.getAccountType());
            BigDecimal interest = calculator.calculateInterest(account);

            if (interest.compareTo(BigDecimal.ZERO) > 0) {
                return new AccountInterestData(account, interest);
            }
            return null; // Skip accounts with no interest
        };
    }

    @Bean
    public ItemWriter<AccountInterestData> interestApplierWriter() {
        return items -> {
            for (AccountInterestData data : items) {
                if (data != null && data.shouldApplyInterest()) {
                    Account account = accountRepository.findById(data.getAccountId())
                            .orElseThrow();

                    BigDecimal newBalance = account.getBalance()
                                                  .add(data.getCalculatedInterest());
                    account.setBalance(newBalance);
                    account.setUpdatedAt(LocalDateTime.now());

                    accountRepository.save(account);

                    log.info("✅ Interest applied to {}: ${} (New balance: ${})",
                            account.getAccountNumber(),
                            data.getCalculatedInterest(),
                            newBalance);
                }
            }
        };
    }
}
```

---

### Paso 6: Listener para MongoDB

**Archivo:** `src/main/java/com/xideral/banco/batch/listener/BatchJobExecutionMongoListener.java`

```java
@Component
@RequiredArgsConstructor
public class BatchJobExecutionMongoListener implements JobExecutionListener {

    private final BatchJobExecutionLogRepository logRepository;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        BatchJobExecutionLog log = BatchJobExecutionLog.started(
                jobExecution.getId(),
                jobExecution.getJobInstance().getJobName()
        );
        logRepository.save(log);
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        BatchJobExecutionLog log = logRepository.findByJobExecutionId(
                jobExecution.getId());

        if (jobExecution.getStatus().isUnsuccessful()) {
            log.failed(jobExecution.getAllFailureExceptions()...);
        } else {
            log.completed(totalAccounts, accountsWithInterest, totalInterest);
        }

        logRepository.save(log);
    }
}
```

**MongoDB Document:**
```json
{
  "_id": "674f...",
  "jobExecutionId": 1,
  "jobName": "monthlyInterestJob",
  "status": "COMPLETED",
  "startTime": "2025-09-30T10:00:00",
  "endTime": "2025-09-30T10:00:05",
  "duration": 5000,
  "totalAccountsProcessed": 4,
  "accountsWithInterest": 4,
  "totalInterestApplied": "50.00"
}
```

---

### Paso 7: REST Endpoint para Ejecución Manual

**Archivo:** `src/main/java/com/xideral/banco/batch/controller/BatchJobController.java`

```java
@RestController
@RequestMapping("/api/batch")
@RequiredArgsConstructor
public class BatchJobController {

    private final JobLauncher jobLauncher;
    private final Job monthlyInterestJob;

    @PostMapping("/monthly-interest")
    public ResponseEntity<Map<String, Object>> runMonthlyInterestJob() {
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("timestamp", LocalDateTime.now().toString())
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        jobLauncher.run(monthlyInterestJob, jobParameters);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Monthly Interest Job triggered successfully");
        response.put("timestamp", LocalDateTime.now());
        response.put("status", "RUNNING");

        return ResponseEntity.ok(response);
    }
}
```

---

## Pruebas del Batch Job

### Prerequisitos

1. **MongoDB corriendo**
2. **MySQL corriendo**
3. **Aplicación iniciada**
4. **Datos de prueba** (clientes y cuentas con balance)

### Prueba 1: Ejecutar Batch Job Manualmente

```bash
curl -X POST http://localhost:8080/api/batch/monthly-interest | jq .
```

**Respuesta Esperada:**
```json
{
  "message": "Monthly Interest Job triggered successfully",
  "timestamp": "2025-09-30T10:00:00",
  "status": "RUNNING"
}
```

### Prueba 2: Verificar Logs en Aplicación

```bash
# En la terminal donde corre la aplicación, verás:
🚀 Starting batch job: monthlyInterestJob (ID: 1)
Processing account: 400045427676 (Type: CHECKING, Balance: $150.00)
Interest calculated for account 400045427676: $0.12 (Rate: 0.083333%)
✅ Interest applied to 400045427676: $0.12 (Old: $150.00, New: $150.12)

Processing account: 400055441885 (Type: SAVINGS, Balance: $150.00)
Interest calculated for account 400055441885: $0.62 (Rate: 0.416667%)
✅ Interest applied to 400055441885: $0.62 (Old: $150.00, New: $150.62)

✅ Batch job completed: monthlyInterestJob (Duration: 234ms, Accounts: 2, Interest Applied: $0.74)
```

### Prueba 3: Verificar Balances Actualizados

```bash
curl -s http://localhost:8080/api/accounts | jq '[.[] | {accountNumber, accountType, balance}]'
```

**Respuesta Esperada:**
```json
[
  {
    "accountNumber": "400045427676",
    "accountType": "CHECKING",
    "balance": 150.12  // ← Incrementó $0.12
  },
  {
    "accountNumber": "400055441885",
    "accountType": "SAVINGS",
    "balance": 150.62  // ← Incrementó $0.62
  }
]
```

### Prueba 4: Verificar Logs en MongoDB

```bash
# Conectar a MongoDB
docker exec -it mongodb-container mongosh -u admin -p xideral4321 --authenticationDatabase admin

# Dentro de mongosh
use banco_logs
db.batch_job_executions.find().pretty()
```

**Documento en MongoDB:**
```json
{
  "_id": ObjectId("674f5a1c..."),
  "jobExecutionId": 1,
  "jobName": "monthlyInterestJob",
  "status": "COMPLETED",
  "startTime": ISODate("2025-09-30T16:00:00.000Z"),
  "endTime": ISODate("2025-09-30T16:00:05.234Z"),
  "duration": 5234,
  "totalAccountsProcessed": 2,
  "accountsWithInterest": 2,
  "totalInterestApplied": "0.74"
}
```

---

## Tabla de Tasas de Interés

| Tipo de Cuenta | Tasa Anual | Tasa Mensual | Ejemplo (Balance $1,000) |
|----------------|------------|--------------|--------------------------|
| **SAVINGS** | 5.00% | 0.42% | Interés = $4.17/mes |
| **CHECKING** | 1.00% | 0.083% | Interés = $0.83/mes |

### Cálculos Ejemplo

#### Cuenta SAVINGS con $10,000
```
Tasa mensual: 0.004166667
Interés = $10,000 × 0.004166667
Interés = $41.67 por mes
Balance nuevo = $10,041.67
```

#### Cuenta CHECKING con $10,000
```
Tasa mensual: 0.000833333
Interés = $10,000 × 0.000833333
Interés = $8.33 por mes
Balance nuevo = $10,008.33
```

---

## Estructura de Archivos Creados

```
src/main/java/com/xideral/banco/batch/
├── interest/
│   ├── InterestCalculator.java              ← Interface
│   ├── SavingsInterestCalculator.java       ← Impl SAVINGS
│   ├── CheckingInterestCalculator.java      ← Impl CHECKING
│   └── InterestCalculatorFactory.java       ← Factory
├── dto/
│   └── AccountInterestData.java             ← DTO
├── model/
│   └── BatchJobExecutionLog.java            ← MongoDB Model
├── repository/
│   └── BatchJobExecutionLogRepository.java  ← MongoDB Repo
├── config/
│   └── MonthlyInterestBatchConfig.java      ← Batch Config
├── listener/
│   └── BatchJobExecutionMongoListener.java  ← Listener
└── controller/
    └── BatchJobController.java              ← REST API
```

---

## Conceptos Aplicados del Curso

### 1. ✅ Polimorfismo

**Implementación:**
```java
// Interface define contrato
interface InterestCalculator {
    BigDecimal calculateInterest(Account account);
}

// Implementaciones específicas
class SavingsInterestCalculator implements InterestCalculator { ... }
class CheckingInterestCalculator implements InterestCalculator { ... }

// Uso polimórfico
InterestCalculator calc = factory.getCalculator(account.getAccountType());
BigDecimal interest = calc.calculateInterest(account);  // ← Llamada polimórfica
```

### 2. ✅ Inyección de Dependencias

**Spring inyecta automáticamente:**
```java
@Component
@RequiredArgsConstructor  // ← Constructor injection
public class InterestCalculatorFactory {
    private final List<InterestCalculator> calculators;  // ← Spring encuentra TODAS las implementaciones
}
```

### 3. ✅ Spring Batch

**Job con Steps:**
```java
Job → monthlyInterestJob
  └─ Step → calculateInterestStep
      ├─ Reader:    Leer cuentas de MySQL (chunk 10)
      ├─ Processor: Calcular intereses (polimorfismo)
      └─ Writer:    Actualizar MySQL + Log MongoDB
```

### 4. ✅ Bases de Datos Híbridas

- **MySQL**: Almacena cuentas y transacciones (ACID)
- **MongoDB**: Almacena logs de batch executions (escalabilidad)

### 5. ✅ Factory Pattern

```java
// Factory selecciona implementación correcta dinámicamente
InterestCalculator calc = factory.getCalculator(AccountType.SAVINGS);
// ↑ Retorna SavingsInterestCalculator

InterestCalculator calc2 = factory.getCalculator(AccountType.CHECKING);
// ↑ Retorna CheckingInterestCalculator
```

---

## Ventajas del Diseño

### 1. Extensibilidad ⭐
Agregar nuevo tipo de cuenta es fácil:
```java
@Component
public class PremiumInterestCalculator implements InterestCalculator {
    private static final BigDecimal RATE = new BigDecimal("0.01");  // 12% anual
    // ... implementación
}
```

### 2. Testabilidad ⭐
Cada calculador se puede probar independientemente:
```java
@Test
void shouldCalculateCorrectInterestForSavings() {
    SavingsInterestCalculator calc = new SavingsInterestCalculator();
    Account account = new Account();
    account.setBalance(new BigDecimal("10000"));

    BigDecimal interest = calc.calculateInterest(account);

    assertEquals(new BigDecimal("41.67"), interest);
}
```

### 3. Mantenibilidad ⭐
Cambiar tasa de interés solo requiere modificar una clase:
```java
// Solo cambiar esta constante
private static final BigDecimal MONTHLY_INTEREST_RATE = new BigDecimal("0.005");  // Nueva tasa
```

### 4. Separación de Responsabilidades ⭐
- **Reader**: Solo lee datos
- **Processor**: Solo calcula
- **Writer**: Solo persiste
- **Listener**: Solo registra logs

---

## Troubleshooting

### Problema 1: Job no inicia

**Error:**
```
No qualifying bean of type 'org.springframework.batch.core.launch.JobLauncher'
```

**Solución:**
Verificar que Spring Batch esté habilitado en `application.properties`:
```properties
spring.batch.job.enabled=true
spring.batch.jdbc.initialize-schema=always
```

### Problema 2: Logs no se guardan en MongoDB

**Error:**
```
No bean named 'batchJobExecutionLogRepository'
```

**Solución:**
Verificar que MongoDB esté configurado correctamente y el repository esté anotado con `@Repository`.

### Problema 3: Balance no se actualiza

**Síntoma:**
El job se ejecuta pero los balances no cambian.

**Solución:**
Verificar que las cuentas tengan `active = true`:
```sql
SELECT * FROM accounts WHERE active = true;
```

---

## Conclusiones

### ✅ Implementación Completada

1. **Interface InterestCalculator** con polimorfismo
2. **Dos implementaciones**: SavingsInterestCalculator y CheckingInterestCalculator
3. **Factory pattern** para selección dinámica
4. **Spring Batch Job** con 1 step funcional
5. **Logging en MongoDB** con estadísticas completas
6. **REST endpoint** para ejecución manual

### 📊 Estadísticas del Día 5

- **Archivos creados**: 8
- **Líneas de código**: ~600
- **Conceptos aplicados**: 7
- **Patrones de diseño**: 3 (Strategy, Factory, Template Method)

### 🎯 Objetivos Alcanzados

| Objetivo | Estado |
|----------|--------|
| Polimorfismo en calculadores | ✅ COMPLETADO |
| Spring Batch configurado | ✅ COMPLETADO |
| Procesamiento por lotes | ✅ COMPLETADO |
| Logging en MongoDB | ✅ COMPLETADO |
| REST API para batch | ✅ COMPLETADO |

---

## Próximos Pasos (Opcionales)

### Mejoras Posibles

1. **Scheduling Automático**
   ```java
   @Scheduled(cron = "0 0 2 1 * ?")  // 2 AM el día 1 de cada mes
   public void runMonthlyInterest() {
       // Ejecutar job automáticamente
   }
   ```

2. **Notificaciones de Interés**
   ```java
   // Enviar notificación al cliente cuando se aplica interés
   notificationService.notifyInterestApplied(customerId, interest);
   ```

3. **Dashboard de Batch Jobs**
   - Ver historial de ejecuciones
   - Estadísticas por mes
   - Gráficas de intereses aplicados

4. **Tests de Integración**
   ```java
   @SpringBatchTest
   class MonthlyInterestJobTest {
       @Test
       void shouldApplyInterestCorrectly() {
           // Test completo del job
       }
   }
   ```

---

## Recursos Adicionales

### Documentación Oficial

- [Spring Batch Reference](https://docs.spring.io/spring-batch/docs/current/reference/html/)
- [Spring Batch Tutorial](https://spring.io/guides/gs/batch-processing/)

### Comandos Útiles

```bash
# Ver jobs en ejecución
curl http://localhost:8080/actuator/batch/jobs

# Ver ejecuciones completadas
curl http://localhost:8080/actuator/batch/executions

# Logs de batch en MongoDB
docker exec -it mongodb-container mongosh -u admin -p xideral4321 --authenticationDatabase admin
use banco_logs
db.batch_job_executions.find().pretty()
```

---

**¡Felicidades por completar el Día 5!** 🎉

Has construido un sistema de procesamiento batch completo con:
- ✅ Polimorfismo bien implementado
- ✅ Spring Batch funcional
- ✅ Bases de datos híbridas
- ✅ Logging robusto

**Academia Xideral - FullStack Development Course**
**Proyecto Final - Sistema Bancario Digital**
**Día 5: Spring Batch - Procesamiento Mensual de Intereses** ✅