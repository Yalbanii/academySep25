# @Externalized - Guía Completa para Desarrolladores

## 📚 Sistema Bancario Digital - Externalización de Eventos

**Autor:** Sistema de Banco Digital
**Fecha:** Octubre 2025
**Audiencia:** Desarrolladores Academia
**Nivel:** Intermedio-Avanzado

---

## 🎯 ¿Qué es @Externalized?

La anotación `@Externalized` es una característica de **Spring Modulith** que permite publicar eventos **fuera de tu aplicación**, hacia sistemas externos como **Kafka**, **RabbitMQ**, **AWS SQS**, etc.

### Ejemplo Real

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
@Externalized("banco.customer.created::#{#this.email}")
public class CustomerCreatedEvent {
    private Long customerId;
    private String fullName;
    private String email;
    private String phone;
    private LocalDateTime createdAt;
}
```

---

## 📖 Anatomía de @Externalized

La anotación completa se divide en **2 partes**:

```java
@Externalized("banco.customer.created::#{#this.email}")
               └────────┬─────────┘  └──────┬──────┘
                        │                   │
                   Routing Key         Partition Key
                   (Topic/Canal)       (Clave de partición)
```

---

## 🔑 Parte 1: Routing Key

### Sintaxis: `"banco.customer.created"`

Esta es la **ruta o nombre del canal** donde se publicará el evento.

#### Convención de Nombres

```
sistema.modulo.accion
  │      │      │
  │      │      └── Acción que ocurrió (created, updated, deleted)
  │      └── Módulo/Dominio (customer, account, payment)
  └── Nombre del sistema (banco, ecommerce, inventory)
```

#### Ejemplos del Proyecto

| Routing Key | Significado |
|-------------|-------------|
| `banco.customer.created` | Cliente creado en el banco |
| `banco.account.created` | Cuenta bancaria creada |
| `banco.transaction.completed` | Transacción completada |
| `banco.transfer.completed` | Transferencia entre cuentas completada |
| `banco.interest.applied` | Interés mensual aplicado |

#### Analogía

El Routing Key es como el **código postal** en una carta:
- Determina **a dónde** debe ir el mensaje
- Los consumidores se **suscriben** a routing keys específicos
- Puedes tener múltiples consumidores para el mismo routing key

```
Routing Key: "banco.account.created"
    │
    ├─→ Analytics Dashboard (escucha)
    ├─→ Fraud Detection System (escucha)
    ├─→ Mobile App Backend (escucha)
    └─→ Audit Logger (escucha)
```

---

## 🔢 Parte 2: Partition Key (Clave de Partición)

### Sintaxis: `::#{#this.email}`

Esta es una **expresión SpEL** (Spring Expression Language) que extrae un campo del evento para usar como **clave de partición**.

#### Desglose de la Sintaxis

```java
::#{#this.email}
││││ │    └── Campo del evento a usar
││││ └── Referencia al objeto actual (this)
│││└── Expresión SpEL
││└── Delimitador de expresión
│└── Separador entre routing key y partition key
└── Separador entre routing key y partition key
```

#### ¿Para qué sirve la Partition Key?

En sistemas distribuidos con **múltiples particiones** (como Kafka), la partition key determina:
1. **A qué partición** va el mensaje
2. **El orden de procesamiento** de eventos relacionados

```
Kafka Topic: "banco.customer.created"
┌─────────────────────────────────────────┐
│  Partición 0                            │
│  - juan@example.com eventos             │
│  - pedro@example.com eventos            │
└─────────────────────────────────────────┘
┌─────────────────────────────────────────┐
│  Partición 1                            │
│  - maria@example.com eventos            │
│  - ana@example.com eventos              │
└─────────────────────────────────────────┘
┌─────────────────────────────────────────┐
│  Partición 2                            │
│  - carlos@example.com eventos           │
│  - sofia@example.com eventos            │
└─────────────────────────────────────────┘
```

**Importante:** Todos los eventos con la **misma partition key** van a la **misma partición**, garantizando el **orden**.

---

## 🎨 Ejemplos Prácticos

### Ejemplo 1: CustomerCreatedEvent

```java
@Externalized("banco.customer.created::#{#this.email}")
public class CustomerCreatedEvent {
    private Long customerId;
    private String email;  // ← Se usa como partition key
    private String fullName;
}
```

**Eventos publicados:**

| customerId | email | Routing Key Final |
|------------|-------|-------------------|
| 1 | juan@example.com | `banco.customer.created::juan@example.com` |
| 2 | maria@example.com | `banco.customer.created::maria@example.com` |
| 3 | pedro@example.com | `banco.customer.created::pedro@example.com` |

**¿Por qué usar email?**
- Garantiza que todos los eventos de **un mismo cliente** vayan en orden
- Evita race conditions en el consumidor

```
Cliente: juan@example.com

Evento 1: CustomerCreated      → Partición A (orden: 1)
Evento 2: AccountCreated       → Partición A (orden: 2)
Evento 3: DepositCompleted     → Partición A (orden: 3)

✅ Orden garantizado: Siempre procesará en secuencia 1 → 2 → 3
```

---

### Ejemplo 2: AccountCreatedEvent

```java
@Externalized("banco.account.created::#{#this.accountNumber}")
public class AccountCreatedEvent {
    private Long accountId;
    private String accountNumber;  // ← Se usa como partition key
    private String accountType;
    private Long customerId;
}
```

**Eventos publicados:**

| accountId | accountNumber | Routing Key Final |
|-----------|---------------|-------------------|
| 1 | 400012345678 | `banco.account.created::400012345678` |
| 2 | 400087654321 | `banco.account.created::400087654321` |

**¿Por qué usar accountNumber?**
- Garantiza que todos los eventos de **una misma cuenta** vayan en orden
- Útil para auditoría y trazabilidad

```
Cuenta: 400012345678

Evento 1: AccountCreated       → Partición B (orden: 1)
Evento 2: DepositCompleted     → Partición B (orden: 2)
Evento 3: WithdrawalCompleted  → Partición B (orden: 3)
Evento 4: InterestApplied      → Partición B (orden: 4)

✅ Historia completa de la cuenta en orden cronológico
```

---

### Ejemplo 3: TransactionCompletedEvent

```java
@Externalized("banco.transaction.completed::#{#this.transactionId}")
public class TransactionCompletedEvent {
    private String transactionId;  // ← Se usa como partition key
    private String accountNumber;
    private String transactionType;
    private BigDecimal amount;
}
```

**Eventos publicados:**

| transactionId | accountNumber | Routing Key Final |
|---------------|---------------|-------------------|
| TXN-001 | 400012345678 | `banco.transaction.completed::TXN-001` |
| TXN-002 | 400087654321 | `banco.transaction.completed::TXN-002` |

**¿Por qué usar transactionId?**
- Cada transacción es **única e independiente**
- Permite procesamiento paralelo de transacciones diferentes
- Facilita idempotencia (detectar duplicados)

---

### Ejemplo 4: TransferCompletedEvent

```java
@Externalized("banco.transfer.completed::#{#this.transactionId}")
public class TransferCompletedEvent {
    private String transactionId;  // ← Se usa como partition key
    private String sourceAccountNumber;
    private String targetAccountNumber;
    private BigDecimal amount;
}
```

**¿Por qué usar transactionId en lugar de accountNumber?**

Una transferencia involucra **2 cuentas**, pero tiene **1 transacción única**:

```
Transferencia TXN-TRANSFER-123:
  Cuenta origen: 400012345678 (-$250)
  Cuenta destino: 400087654321 (+$250)

Si usáramos accountNumber como key:
❌ ¿Cuál cuenta? ¿origen o destino?
❌ Podría dividirse en 2 particiones diferentes
❌ Difícil relacionar ambos lados de la transferencia

Usando transactionId como key:
✅ Ambos lados de la transferencia en la misma partición
✅ Fácil relacionar origen y destino
✅ Orden garantizado para transacciones relacionadas
```

---

### Ejemplo 5: InterestAppliedEvent

```java
@Externalized("banco.interest.applied::#{#this.accountNumber}")
public class InterestAppliedEvent {
    private String accountNumber;  // ← Se usa como partition key
    private BigDecimal interestAmount;
    private BigDecimal newBalance;
}
```

**¿Por qué usar accountNumber?**
- Los intereses se aplican **por cuenta**
- Debe estar en orden con otras operaciones de la misma cuenta

```
Cuenta: 400012345678

Evento 1 (Sept): InterestApplied (+$41.67) → Partición C
Evento 2 (Oct):  InterestApplied (+$41.84) → Partición C
Evento 3 (Nov):  InterestApplied (+$42.01) → Partición C

✅ Historial de intereses en orden cronológico
```

---

## 🌍 Eventos Internos vs Externos

### Sin @Externalized (Solo Internos)

```java
public class CustomerCreatedEvent {
    private Long customerId;
    private String email;
}
```

**Flujo:**
```
┌──────────────────────────────────────────┐
│  APLICACIÓN SPRING BOOT                  │
│                                          │
│  CustomerService                         │
│       │                                  │
│       │ publishEvent()                   │
│       ↓                                  │
│  Spring Application Events               │
│       │                                  │
│       ├─→ NotificationService (interno) │
│       └─→ TransactionLogService (interno)│
│                                          │
└──────────────────────────────────────────┘

✅ Rápido (en memoria)
✅ Transaccional
❌ Solo dentro de esta aplicación
```

---

### Con @Externalized (Internos + Externos)

```java
@Externalized("banco.customer.created::#{#this.email}")
public class CustomerCreatedEvent {
    private Long customerId;
    private String email;
}
```

**Flujo:**
```
┌──────────────────────────────────────────────────────────┐
│  APLICACIÓN SPRING BOOT                                  │
│                                                          │
│  CustomerService                                         │
│       │                                                  │
│       │ publishEvent()                                   │
│       ↓                                                  │
│  Spring Modulith Event Publication                      │
│       │                                                  │
│       ├─→ Internal Listeners (en memoria)               │
│       │   ├─→ NotificationService                       │
│       │   └─→ TransactionLogService                     │
│       │                                                  │
│       └─→ External Broker                               │
│           ├─→ Kafka Topic                               │
│           ├─→ RabbitMQ Exchange                         │
│           └─→ AWS SQS Queue                             │
│                                                          │
└──────────────────────────────────────────────────────────┘
                    ↓
┌──────────────────────────────────────────────────────────┐
│  SISTEMAS EXTERNOS                                       │
│                                                          │
│  ├─→ Analytics Dashboard                                │
│  ├─→ Fraud Detection System                             │
│  ├─→ Mobile App Backend                                 │
│  ├─→ Email Marketing Service                            │
│  ├─→ Data Warehouse (BigQuery)                          │
│  └─→ Audit & Compliance Logger                          │
│                                                          │
└──────────────────────────────────────────────────────────┘

✅ Escalable
✅ Múltiples consumidores externos
✅ Desacoplamiento total
✅ Resiliencia (retry, dead letter queues)
❌ Más lento (red)
❌ Requiere infraestructura adicional
```

---

## ⚙️ Configuración Necesaria

Para que `@Externalized` funcione, necesitas configurar un **message broker**.

### Opción 1: Kafka

```properties
# application.properties

# Habilitar externalización
spring.modulith.events.externalization.enabled=true

# Configuración Kafka
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer

# Estrategia de routing
spring.modulith.events.externalization.routing-strategy=topic-per-event
```

**Dependencia Maven:**
```xml
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
</dependency>
```

---

### Opción 2: RabbitMQ

```properties
# application.properties

# Habilitar externalización
spring.modulith.events.externalization.enabled=true

# Configuración RabbitMQ
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest

# Exchange y routing
spring.modulith.events.externalization.exchange=banco-events
```

**Dependencia Maven:**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-amqp</artifactId>
</dependency>
```

---

## 🔬 Ejemplo Real Completo con Kafka

### 1. Configuración

```properties
# application.properties
spring.modulith.events.externalization.enabled=true
spring.kafka.bootstrap-servers=localhost:9092
```

### 2. Evento con @Externalized

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
@Externalized("banco.account.created::#{#this.accountNumber}")
public class AccountCreatedEvent {
    private Long accountId;
    private String accountNumber;
    private String accountType;
    private BigDecimal initialBalance;
    private String customerEmail;
    private LocalDateTime createdAt;
}
```

### 3. Publicar Evento (Producer)

```java
@Service
@RequiredArgsConstructor
public class AccountService {

    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public AccountResponse createAccount(AccountCreateRequest request) {
        // 1. Guardar cuenta en MySQL
        Account account = accountRepository.save(newAccount);

        // 2. Publicar evento
        AccountCreatedEvent event = new AccountCreatedEvent(
            account.getId(),
            account.getAccountNumber(),
            account.getAccountType().toString(),
            account.getBalance(),
            account.getCustomer().getEmail(),
            LocalDateTime.now()
        );

        eventPublisher.publishEvent(event);  // ← Se publica a Kafka también

        return AccountResponse.from(account);
    }
}
```

### 4. Consumir Evento Externo (Consumer)

**Aplicación Externa - Analytics Dashboard:**

```java
@Service
public class AccountAnalyticsConsumer {

    @KafkaListener(topics = "banco.account.created", groupId = "analytics-group")
    public void handleAccountCreated(AccountCreatedEvent event) {
        log.info("📊 Analytics: New account created {}", event.getAccountNumber());

        // Guardar métricas
        analyticsRepository.save(new AccountMetric(
            event.getAccountNumber(),
            event.getAccountType(),
            event.getInitialBalance(),
            event.getCreatedAt()
        ));

        // Actualizar dashboard
        dashboardService.incrementAccountCount(event.getAccountType());
    }
}
```

**Aplicación Externa - Fraud Detection:**

```java
@Service
public class FraudDetectionConsumer {

    @KafkaListener(topics = "banco.account.created", groupId = "fraud-detection-group")
    public void handleAccountCreated(AccountCreatedEvent event) {
        log.warn("🔍 Fraud Detection: Analyzing account {}", event.getAccountNumber());

        // Validar contra listas negras
        if (blacklistService.isBlacklisted(event.getCustomerEmail())) {
            alertService.sendAlert("⚠️ Blacklisted customer attempting to create account");
        }

        // Detectar patrones sospechosos
        if (event.getInitialBalance().compareTo(new BigDecimal("100000")) > 0) {
            alertService.sendAlert("🚨 Large initial balance: " + event.getInitialBalance());
        }
    }
}
```

---

## 📊 Flujo Completo End-to-End

```
┌────────────────────────────────────────────────────────────────┐
│  PASO 1: Cliente crea cuenta                                  │
│  POST /api/accounts                                            │
│  { customerId: 1, accountType: "SAVINGS", balance: 50000 }    │
└────────────────────────────────────────────────────────────────┘
                            ↓
┌────────────────────────────────────────────────────────────────┐
│  PASO 2: AccountService guarda en MySQL                       │
│  - ID: 1                                                       │
│  - Account Number: 400012345678                                │
│  - Balance: $50,000.00                                         │
└────────────────────────────────────────────────────────────────┘
                            ↓
┌────────────────────────────────────────────────────────────────┐
│  PASO 3: AccountService publica evento                        │
│  eventPublisher.publishEvent(AccountCreatedEvent)              │
└────────────────────────────────────────────────────────────────┘
                            ↓
         ┌──────────────────┴───────────────────┐
         ↓                                       ↓
┌──────────────────────┐            ┌───────────────────────────┐
│  LISTENERS INTERNOS  │            │  KAFKA TOPIC              │
│  (misma app)         │            │  "banco.account.created"  │
│                      │            │                           │
│  - NotificationSvc   │            │  Partition Key:           │
│  - TransactionLogSvc │            │  "400012345678"           │
└──────────────────────┘            └───────────────────────────┘
         ↓                                       ↓
         ↓                          ┌────────────┴─────────────┐
         ↓                          ↓                          ↓
         ↓              ┌───────────────────┐   ┌────────────────────┐
         ↓              │ Analytics System  │   │ Fraud Detection    │
         ↓              │ (Consumer 1)      │   │ (Consumer 2)       │
         ↓              │                   │   │                    │
         ↓              │ - Store metrics   │   │ - Check blacklist  │
         ↓              │ - Update charts   │   │ - Detect patterns  │
         ↓              └───────────────────┘   │ - Alert if needed  │
         ↓                                      └────────────────────┘
         ↓                          ↓
┌────────────────────────┐          ↓
│  Email enviado         │  ┌───────────────────┐
│  Log guardado MongoDB  │  │ Mobile App Backend│
└────────────────────────┘  │ (Consumer 3)      │
                            │                   │
                            │ - Send push notif │
                            │ - Update app cache│
                            └───────────────────┘
```

**Tiempo estimado:**
- MySQL write: 50ms
- Event publish interno: 5ms
- Kafka publish: 20ms
- Consumers externos: 100-500ms (asíncrono, no bloquea)

**Total percibido por usuario:** ~75ms ✅

---

## 🎓 Ventajas y Desventajas

### ✅ Ventajas de @Externalized

1. **Desacoplamiento Total**
   - Servicios externos NO dependen de tu aplicación
   - Puedes agregar/quitar consumidores sin cambiar código

2. **Escalabilidad**
   - Múltiples consumidores en paralelo
   - Balanceo de carga automático

3. **Resiliencia**
   - Retry automático en caso de fallo
   - Dead Letter Queues para mensajes fallidos
   - Los eventos persisten en Kafka (no se pierden)

4. **Auditoría**
   - Historial completo de eventos
   - Replay de eventos si es necesario

5. **Integración con Ecosistema**
   - Conecta con sistemas legacy
   - Microservicios pueden comunicarse
   - Integración con Cloud (AWS, Azure, GCP)

### ❌ Desventajas de @Externalized

1. **Complejidad Adicional**
   - Requiere infraestructura (Kafka, RabbitMQ)
   - Monitoreo y operación más complejo

2. **Latencia**
   - Red introduce latencia (10-100ms)
   - No apto para comunicación síncrona

3. **Consistencia Eventual**
   - Los consumidores procesan "eventualmente"
   - No hay garantía de tiempo exacto

4. **Costo**
   - Infraestructura adicional
   - Almacenamiento de eventos

---

## 🚀 Cuándo Usar @Externalized

### ✅ USA @Externalized cuando:

1. **Múltiples Aplicaciones Consumen el Evento**
   ```
   Evento: CustomerCreated
   Consumidores:
   - App de Marketing (envía emails)
   - App de Analytics (métricas)
   - App de Fraud (detección)
   - App de CRM (actualiza perfil)
   ```

2. **Necesitas Auditoría Completa**
   ```
   Todos los eventos quedan registrados en Kafka
   Puedes "replay" eventos de hace meses
   ```

3. **Integración con Sistemas Legacy**
   ```
   Sistema nuevo → Kafka → Sistema viejo (lee de Kafka)
   ```

4. **Arquitectura de Microservicios**
   ```
   Servicio A → Evento → Servicio B, C, D
   Sin dependencias directas entre servicios
   ```

### ❌ NO USES @Externalized cuando:

1. **Solo Listeners Internos**
   ```
   Si solo NotificationService escucha, NO necesitas Kafka
   Los eventos internos de Spring son suficientes
   ```

2. **Necesitas Respuesta Inmediata**
   ```
   Validación de tarjeta de crédito → Síncrono
   NO usar eventos
   ```

3. **Aplicación Pequeña/Monolítica**
   ```
   Overhead de Kafka no justifica el beneficio
   Eventos internos son más simples
   ```

---

## 🔍 Estado Actual del Proyecto

### En Nuestro Sistema Bancario

**Actualmente:**
```java
@Externalized("banco.customer.created::#{#this.email}")
public class CustomerCreatedEvent { ... }
```

**Estado:** ⚠️ **Anotado pero NO activo**

**¿Por qué?**
```properties
# application.properties
# NO hay esta configuración:
# spring.modulith.events.externalization.enabled=true
# spring.kafka.bootstrap-servers=localhost:9092

# Por lo tanto:
# - Los eventos solo funcionan INTERNAMENTE
# - NotificationService y TransactionLogService escuchan correctamente ✅
# - NO se publica a Kafka (no está configurado)
```

### ¿Cómo Activarlo?

**Paso 1:** Levantar Kafka
```bash
docker run -d \
  --name kafka \
  -p 9092:9092 \
  apache/kafka:latest
```

**Paso 2:** Agregar dependencia
```xml
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
</dependency>
```

**Paso 3:** Configurar
```properties
spring.modulith.events.externalization.enabled=true
spring.kafka.bootstrap-servers=localhost:9092
```

**Paso 4:** Reiniciar aplicación

Ahora los eventos se publicarán **tanto internamente como a Kafka** 🎉

---

## 📝 Resumen de @Externalized

| Aspecto | Detalle |
|---------|---------|
| **Propósito** | Publicar eventos fuera de la aplicación |
| **Destinos** | Kafka, RabbitMQ, AWS SQS, Azure Service Bus |
| **Routing Key** | `banco.customer.created` (topic/canal) |
| **Partition Key** | `#{#this.email}` (para ordenamiento) |
| **Ventaja Principal** | Desacoplamiento total entre servicios |
| **Desventaja Principal** | Complejidad adicional de infraestructura |
| **Cuándo usar** | Múltiples aplicaciones consumen el evento |
| **Cuándo NO usar** | Solo listeners internos |
| **Estado Actual** | Anotado pero no activo (sin Kafka) |

---

## 🎯 Ejercicio Práctico

**Desafío:** Simula cómo se vería el evento en Kafka.

**Dado este evento:**
```java
@Externalized("banco.transfer.completed::#{#this.transactionId}")
public class TransferCompletedEvent {
    private String transactionId = "TXN-ABC-123";
    private String sourceAccountNumber = "400012345678";
    private String targetAccountNumber = "400087654321";
    private BigDecimal amount = new BigDecimal("250.00");
}
```

**Pregunta 1:** ¿Cuál será el routing key completo?

<details>
<summary>Ver respuesta</summary>

```
banco.transfer.completed::TXN-ABC-123
```

- Topic: `banco.transfer.completed`
- Partition Key: `TXN-ABC-123`
</details>

**Pregunta 2:** Si publicas 3 eventos con transactionIds diferentes, ¿irán a la misma partición?

<details>
<summary>Ver respuesta</summary>

NO, cada uno irá a una partición diferente (posiblemente):
- `TXN-001` → Partición 0
- `TXN-002` → Partición 1
- `TXN-003` → Partición 0

Kafka usa un hash del partition key para determinar la partición.
</details>

**Pregunta 3:** ¿Qué pasaría si cambiamos la partition key a `sourceAccountNumber`?

<details>
<summary>Ver respuesta</summary>

```java
@Externalized("banco.transfer.completed::#{#this.sourceAccountNumber}")
```

Todos los eventos de **la misma cuenta origen** irían a la misma partición:
- Cuenta `400012345678`:
  - Transfer 1 → Partición A
  - Transfer 2 → Partición A
  - Transfer 3 → Partición A

✅ Ventaja: Orden garantizado para todas las transferencias de esa cuenta
❌ Desventaja: No agrupa transferencias relacionadas (misma transactionId)
</details>

---

## 📚 Recursos Adicionales

- [Spring Modulith Documentation - Event Externalization](https://docs.spring.io/spring-modulith/reference/events.html#externalization)
- [Apache Kafka Documentation](https://kafka.apache.org/documentation/)
- [Spring Kafka Documentation](https://docs.spring.io/spring-kafka/docs/current/reference/html/)
- [RabbitMQ Documentation](https://www.rabbitmq.com/documentation.html)

---

**¿Preguntas?** Revisa el código fuente en `src/main/java/com/xideral/banco/events/`

**Happy Event Streaming!** 🚀📡
