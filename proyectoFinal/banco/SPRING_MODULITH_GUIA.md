# Spring Modulith - Guía para Desarrolladores Academia

## 📚 Sistema Bancario Digital - Arquitectura Modular con Eventos

**Autor:** Sistema de Banco Digital
**Fecha:** Octubre 2025
**Audiencia:** Desarrolladores Academia
**Nivel:** Intermedio

---

## 🎯 ¿Qué es Spring Modulith?

Spring Modulith es un framework que nos ayuda a construir aplicaciones **modulares** donde los módulos se comunican entre sí mediante **eventos**, sin acoplarse directamente.

### ¿Por qué es importante?

Imagina que tienes una aplicación bancaria con:
- Módulo de **Clientes** (Customer)
- Módulo de **Cuentas** (Account)
- Módulo de **Notificaciones** (Notification)
- Módulo de **Batch** (procesamiento de intereses)

**Sin Spring Modulith (código acoplado ❌):**
```java
// En AccountService
public Account createAccount(AccountDTO dto) {
    Account account = accountRepository.save(dto);

    // ❌ Acoplamiento directo con NotificationService
    notificationService.sendEmail(account.getCustomerId(), "Cuenta creada");

    // ❌ Acoplamiento directo con TransactionLogService
    transactionLogService.logAccountCreation(account);

    return account;
}
```

**Problemas:**
- ❌ Si Notification falla, Account también falla
- ❌ Difícil de testear
- ❌ No se puede escalar independientemente
- ❌ Cambios en un módulo afectan a otros

**Con Spring Modulith (código desacoplado ✅):**
```java
// En AccountService
public Account createAccount(AccountDTO dto) {
    Account account = accountRepository.save(dto);

    // ✅ Publica un evento y se olvida
    eventPublisher.publishEvent(new AccountCreatedEvent(account));

    return account;
}

// En NotificationService (módulo separado)
@ApplicationModuleListener
public void handleAccountCreated(AccountCreatedEvent event) {
    sendEmail(event.getCustomerId(), "Cuenta creada");
}

// En TransactionLogService (módulo separado)
@ApplicationModuleListener
public void handleAccountCreated(AccountCreatedEvent event) {
    logAccountCreation(event);
}
```

**Ventajas:**
- ✅ Módulos desacoplados
- ✅ Si Notification falla, Account no se ve afectado
- ✅ Fácil de testear
- ✅ Se pueden agregar nuevos listeners sin modificar código existente

---

## 🏗️ Arquitectura del Sistema Bancario

### Estructura de Módulos

```
src/main/java/com/xideral/banco/
│
├── customer/              ← Módulo CUSTOMER
│   ├── controller/
│   ├── service/
│   ├── repository/
│   └── model/
│
├── account/               ← Módulo ACCOUNT
│   ├── controller/
│   ├── service/
│   ├── repository/
│   └── model/
│
├── notification/          ← Módulo NOTIFICATION
│   ├── controller/
│   ├── service/
│   ├── repository/
│   └── model/
│
├── batch/                 ← Módulo BATCH
│   ├── config/
│   ├── controller/
│   └── model/
│
└── events/                ← EVENTOS COMPARTIDOS
    ├── CustomerCreatedEvent.java
    ├── AccountCreatedEvent.java
    ├── TransactionCompletedEvent.java
    ├── TransferCompletedEvent.java
    └── InterestAppliedEvent.java
```

### Diagrama de Comunicación entre Módulos

```
┌─────────────────────────────────────────────────────────────────┐
│                     SISTEMA BANCARIO                            │
│                                                                 │
│  ┌──────────────┐                          ┌─────────────────┐ │
│  │   CUSTOMER   │                          │   NOTIFICATION  │ │
│  │    Module    │                          │     Module      │ │
│  │              │                          │                 │ │
│  │ - Create     │  CustomerCreatedEvent    │ - Send Email    │ │
│  │ - Update     │ ─────────────────────────→ - Create Log    │ │
│  │ - Delete     │                          │                 │ │
│  └──────────────┘                          └─────────────────┘ │
│         │                                          ↑            │
│         │                                          │            │
│         │ CustomerCreatedEvent                     │            │
│         │                                          │            │
│         ↓                                          │            │
│  ┌──────────────┐                                 │            │
│  │   ACCOUNT    │                                 │            │
│  │    Module    │                                 │            │
│  │              │                                 │            │
│  │ - Create     │  AccountCreatedEvent            │            │
│  │ - Deposit    │ ────────────────────────────────┤            │
│  │ - Withdraw   │                                 │            │
│  │ - Transfer   │  TransactionCompletedEvent      │            │
│  │              │ ────────────────────────────────┤            │
│  └──────────────┘                                 │            │
│         │                                          │            │
│         │ TransferCompletedEvent                   │            │
│         │ ────────────────────────────────────────┤            │
│         ↓                                          │            │
│  ┌──────────────┐                                 │            │
│  │    BATCH     │                                 │            │
│  │    Module    │                                 │            │
│  │              │                                 │            │
│  │ - Monthly    │  InterestAppliedEvent           │            │
│  │   Interest   │ ────────────────────────────────┘            │
│  │   Job        │                                               │
│  └──────────────┘                                               │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

**Nota:** Las flechas representan eventos, NO llamadas directas. Los módulos NO se conocen entre sí.

---

## 📦 Eventos del Sistema

### 1. CustomerCreatedEvent

**Archivo:** `events/CustomerCreatedEvent.java`

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

**¿Cuándo se publica?**
Cuando se crea un nuevo cliente en el sistema.

**¿Quién lo publica?**
`CustomerService` (módulo Customer)

**¿Quién lo escucha?**
`NotificationService` (módulo Notification)

**Ejemplo de flujo:**
```
1. Usuario crea cliente → POST /api/customers
2. CustomerService guarda en BD
3. CustomerService PUBLICA CustomerCreatedEvent
4. NotificationService ESCUCHA el evento
5. NotificationService envía email de bienvenida
```

---

### 2. AccountCreatedEvent

**Archivo:** `events/AccountCreatedEvent.java`

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
    private Long customerId;
    private String customerEmail;
    private LocalDateTime createdAt;
}
```

**¿Cuándo se publica?**
Cuando se crea una nueva cuenta bancaria.

**¿Quién lo publica?**
`AccountService` (módulo Account)

**¿Quién lo escucha?**
- `NotificationService` → Envía notificación al cliente
- `TransactionLogService` → Registra en MongoDB

**Ejemplo de flujo:**
```
1. Cliente solicita crear cuenta → POST /api/accounts
2. AccountService crea cuenta en MySQL
3. AccountService PUBLICA AccountCreatedEvent
4. NotificationService envía email: "Cuenta 400012345678 creada"
5. TransactionLogService guarda log en MongoDB
```

---

### 3. TransactionCompletedEvent

**Archivo:** `events/TransactionCompletedEvent.java`

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
@Externalized("banco.transaction.completed::#{#this.transactionId}")
public class TransactionCompletedEvent {
    private String transactionId;
    private String accountNumber;
    private String transactionType;  // "DEPOSIT", "WITHDRAWAL"
    private BigDecimal amount;
    private BigDecimal newBalance;
    private String customerEmail;
    private LocalDateTime timestamp;
}
```

**¿Cuándo se publica?**
Cuando se completa un depósito o retiro.

**¿Quién lo publica?**
`AccountService` (módulo Account)

**¿Quién lo escucha?**
- `NotificationService` → Envía notificación de depósito/retiro
- `TransactionLogService` → Registra en MongoDB

**Ejemplo de flujo (Depósito):**
```
1. Cliente deposita $1000 → POST /api/accounts/deposit
2. AccountService actualiza balance en MySQL
3. AccountService PUBLICA TransactionCompletedEvent(type=DEPOSIT)
4. NotificationService envía email: "Depósito de $1000 recibido"
5. TransactionLogService guarda log en MongoDB
```

**Ejemplo de flujo (Retiro con saldo bajo):**
```
1. Cliente retira $500 → POST /api/accounts/withdraw
2. AccountService actualiza balance (queda $150)
3. AccountService PUBLICA TransactionCompletedEvent(type=WITHDRAWAL)
4. NotificationService detecta balance < $200
5. NotificationService envía ALERTA de saldo bajo ⚠️
6. TransactionLogService guarda log en MongoDB
```

---

### 4. TransferCompletedEvent

**Archivo:** `events/TransferCompletedEvent.java`

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
@Externalized("banco.transfer.completed::#{#this.transactionId}")
public class TransferCompletedEvent {
    private String transactionId;
    private String sourceAccountNumber;
    private String targetAccountNumber;
    private BigDecimal amount;
    private String sourceCustomerEmail;
    private String targetCustomerEmail;
    private LocalDateTime timestamp;
}
```

**¿Cuándo se publica?**
Cuando se completa una transferencia entre cuentas.

**¿Quién lo publica?**
`AccountService` (módulo Account)

**¿Quién lo escucha?**
- `NotificationService` → Envía 2 notificaciones (emisor y receptor)
- `TransactionLogService` → Registra 2 logs en MongoDB

**Ejemplo de flujo:**
```
1. Cliente transfiere $250 → POST /api/accounts/transfer
2. AccountService actualiza ambas cuentas en MySQL
3. AccountService PUBLICA TransferCompletedEvent
4. NotificationService envía email al emisor: "Transferencia enviada"
5. NotificationService envía email al receptor: "Transferencia recibida"
6. TransactionLogService guarda 2 logs en MongoDB
```

---

### 5. InterestAppliedEvent

**Archivo:** `events/InterestAppliedEvent.java`

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
@Externalized("banco.interest.applied::#{#this.accountNumber}")
public class InterestAppliedEvent {
    private String accountNumber;
    private String accountType;
    private BigDecimal interestAmount;
    private BigDecimal previousBalance;
    private BigDecimal newBalance;
    private String customerEmail;
    private LocalDateTime timestamp;
}
```

**¿Cuándo se publica?**
Cuando Spring Batch aplica intereses mensuales a una cuenta.

**¿Quién lo publica?**
`MonthlyInterestBatchConfig` (módulo Batch)

**¿Quién lo escucha?**
- `TransactionLogService` → Registra en MongoDB

**Ejemplo de flujo:**
```
1. Batch Job ejecuta → POST /api/batch/monthly-interest
2. Step 1: Calcula intereses y actualiza balances en MySQL
3. Step 2: PUBLICA InterestAppliedEvent por cada cuenta
4. TransactionLogService guarda log en MongoDB
```

---

## 🎧 Listeners (Escuchadores de Eventos)

### ¿Qué es un Listener?

Un **listener** es un método que **escucha** eventos y reacciona cuando ocurren.

**Analogía:** Es como tener el teléfono en espera. Cuando suena (evento), atiendes (ejecutas lógica).

### Anotación: @ApplicationModuleListener

Esta anotación especial de Spring Modulith indica que un método escucha eventos.

```java
@ApplicationModuleListener
public void handleAccountCreated(AccountCreatedEvent event) {
    // Lógica que se ejecuta cuando se crea una cuenta
}
```

**Características:**
- ✅ **Asíncrono por defecto** (no bloquea al publicador)
- ✅ **Transaccional** (si falla, se puede reintentar)
- ✅ **Desacoplado** (el publicador no sabe quién escucha)

---

## 📢 Módulo Notification - Listeners Completos

**Archivo:** `notification/service/NotificationServiceImpl.java`

### Listener 1: Customer Created

```java
@ApplicationModuleListener
public void handleCustomerCreated(CustomerCreatedEvent event) {
    log.debug("Handling CustomerCreatedEvent for customer: {}", event.getEmail());

    // Enviar notificación de bienvenida
    notifyCustomerRegistered(
        event.getCustomerId(),
        event.getEmail(),
        event.getFullName()
    );
}
```

**¿Qué hace?**
1. Escucha cuando se crea un cliente
2. Envía email de bienvenida
3. Guarda notificación en MongoDB

**Notificación creada:**
```json
{
  "type": "CUSTOMER_REGISTERED",
  "channel": "EMAIL",
  "subject": "Bienvenido al Banco Digital",
  "message": "Gracias por registrarte, Juan Pérez",
  "status": "SENT"
}
```

---

### Listener 2: Account Created

```java
@ApplicationModuleListener
public void handleAccountCreated(AccountCreatedEvent event) {
    log.debug("Handling AccountCreatedEvent for account: {}", event.getAccountNumber());

    // Enviar notificación de cuenta creada
    notifyAccountCreated(
        event.getCustomerId(),
        event.getCustomerEmail(),
        event.getAccountNumber(),
        event.getAccountType()
    );
}
```

**¿Qué hace?**
1. Escucha cuando se crea una cuenta
2. Envía email: "Cuenta 400012345678 de tipo SAVINGS creada"
3. Guarda notificación en MongoDB

**Notificación creada:**
```json
{
  "type": "ACCOUNT_CREATED",
  "channel": "EMAIL",
  "subject": "Cuenta Creada Exitosamente",
  "message": "Su cuenta 400012345678 de tipo SAVINGS ha sido creada",
  "accountNumber": "400012345678",
  "status": "SENT"
}
```

---

### Listener 3: Transaction Completed

```java
@ApplicationModuleListener
public void handleTransactionCompleted(TransactionCompletedEvent event) {
    log.debug("Handling TransactionCompletedEvent for account: {}", event.getAccountNumber());

    switch (event.getTransactionType()) {
        case "DEPOSIT" -> {
            notifyDeposit(
                customerId,
                event.getCustomerEmail(),
                event.getAccountNumber(),
                event.getAmount().toString()
            );
        }
        case "WITHDRAWAL" -> {
            notifyWithdrawal(
                customerId,
                event.getCustomerEmail(),
                event.getAccountNumber(),
                event.getAmount().toString()
            );

            // ⚠️ ALERTA DE SALDO BAJO
            if (event.getNewBalance().compareTo(new BigDecimal("200.00")) < 0) {
                notifyLowBalance(
                    customerId,
                    event.getCustomerEmail(),
                    event.getAccountNumber(),
                    event.getNewBalance().toString()
                );
            }
        }
    }
}
```

**¿Qué hace?**
1. Escucha transacciones (depósitos/retiros)
2. Envía notificación según el tipo
3. **Si el saldo queda < $200, envía alerta adicional** ⚠️

**Ejemplo - Retiro con saldo bajo:**
```
Balance antes: $250
Retiro: $100
Balance después: $150 ← Menor a $200

→ Notificación 1: "Retiro de $100 realizado"
→ Notificación 2: "⚠️ Alerta: Su saldo es bajo ($150)"
```

---

### Listener 4: Transfer Completed

```java
@ApplicationModuleListener
public void handleTransferCompleted(TransferCompletedEvent event) {
    log.debug("Handling TransferCompletedEvent from {} to {}",
        event.getSourceAccountNumber(),
        event.getTargetAccountNumber());

    // Notificación para el emisor
    notifyTransferSent(
        sourceCustomerId,
        event.getSourceCustomerEmail(),
        event.getSourceAccountNumber(),
        event.getTargetAccountNumber(),
        event.getAmount().toString()
    );

    // Notificación para el receptor
    notifyTransferReceived(
        targetCustomerId,
        event.getTargetCustomerEmail(),
        event.getSourceAccountNumber(),
        event.getTargetAccountNumber(),
        event.getAmount().toString()
    );
}
```

**¿Qué hace?**
1. Escucha cuando se completa una transferencia
2. Envía **2 notificaciones**:
   - Una al emisor: "Transferencia enviada"
   - Una al receptor: "Transferencia recibida"

**Ejemplo:**
```
Transferencia: $250
De: 400012345678
A:  400087654321

→ Email a cuenta origen: "Enviaste $250 a 400087654321"
→ Email a cuenta destino: "Recibiste $250 de 400012345678"
```

---

## 📝 Módulo TransactionLog - Listeners

**Archivo:** `notification/service/TransactionLogServiceImpl.java`

### Listener 1: Transaction Completed

```java
@ApplicationModuleListener
public void handleTransactionCompleted(TransactionCompletedEvent event) {
    log.debug("Logging TransactionCompletedEvent for account: {}", event.getAccountNumber());

    TransactionLog log = TransactionLog.builder()
            .transactionId(event.getTransactionId())
            .accountNumber(event.getAccountNumber())
            .transactionType(event.getTransactionType())
            .amount(event.getAmount())
            .balanceAfter(event.getNewBalance())
            .timestamp(event.getTimestamp())
            .description(String.format("%s transaction completed", event.getTransactionType()))
            .status("SUCCESS")
            .build();

    createTransactionLog(log);  // Guarda en MongoDB
}
```

**¿Qué hace?**
Guarda un log de la transacción en MongoDB.

**Documento guardado:**
```json
{
  "_id": ObjectId("..."),
  "transactionId": "550e8400-e29b-41d4-a716-446655440001",
  "accountNumber": "400012345678",
  "transactionType": "DEPOSIT",
  "amount": 1000.00,
  "balanceAfter": 11000.00,
  "timestamp": "2025-10-01T08:00:00Z",
  "description": "DEPOSIT transaction completed",
  "status": "SUCCESS"
}
```

---

### Listener 2: Transfer Completed

```java
@ApplicationModuleListener
public void handleTransferCompleted(TransferCompletedEvent event) {
    log.debug("Logging TransferCompletedEvent from {} to {}",
        event.getSourceAccountNumber(),
        event.getTargetAccountNumber());

    // Log para cuenta origen (monto negativo)
    TransactionLog sourceLog = TransactionLog.builder()
            .transactionId(event.getTransactionId())
            .accountNumber(event.getSourceAccountNumber())
            .transactionType("TRANSFER_SENT")
            .amount(event.getAmount().negate())  // ← Negativo
            .timestamp(event.getTimestamp())
            .description(String.format("Transfer sent to %s", event.getTargetAccountNumber()))
            .status("SUCCESS")
            .build();

    createTransactionLog(sourceLog);

    // Log para cuenta destino (monto positivo)
    TransactionLog targetLog = TransactionLog.builder()
            .transactionId(event.getTransactionId())
            .accountNumber(event.getTargetAccountNumber())
            .transactionType("TRANSFER_RECEIVED")
            .amount(event.getAmount())  // ← Positivo
            .timestamp(event.getTimestamp())
            .description(String.format("Transfer received from %s", event.getSourceAccountNumber()))
            .status("SUCCESS")
            .build();

    createTransactionLog(targetLog);
}
```

**¿Qué hace?**
Guarda **2 logs** en MongoDB (uno para cada cuenta).

**Documentos guardados:**
```json
// Log cuenta origen
{
  "accountNumber": "400012345678",
  "transactionType": "TRANSFER_SENT",
  "amount": -250.00,  // ← Negativo
  "description": "Transfer sent to 400087654321"
}

// Log cuenta destino
{
  "accountNumber": "400087654321",
  "transactionType": "TRANSFER_RECEIVED",
  "amount": 250.00,  // ← Positivo
  "description": "Transfer received from 400012345678"
}
```

---

### Listener 3: Interest Applied

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
            .status("SUCCESS")
            .build();

    createTransactionLog(log);
}
```

**¿Qué hace?**
Guarda log de intereses aplicados por Spring Batch.

**Documento guardado:**
```json
{
  "accountNumber": "400012345678",
  "transactionType": "INTEREST_APPLIED",
  "amount": 41.67,
  "balanceAfter": 10041.67,
  "description": "Monthly interest applied: 41.67",
  "status": "SUCCESS"
}
```

---

## 🔄 Flujo Completo: Crear Cuenta

Veamos paso a paso qué pasa cuando un cliente crea una cuenta.

### Código en AccountService

```java
@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public AccountResponse createAccount(AccountCreateRequest request) {
        // 1. Buscar cliente
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        // 2. Crear cuenta
        Account account = new Account();
        account.setCustomer(customer);
        account.setAccountType(request.getAccountType());
        account.setBalance(request.getInitialBalance());
        account.setStatus(Account.AccountStatus.ACTIVE);
        account.setCreatedAt(LocalDateTime.now());

        // 3. Guardar en MySQL
        Account savedAccount = accountRepository.save(account);

        // 4. PUBLICAR EVENTO
        AccountCreatedEvent event = new AccountCreatedEvent(
            savedAccount.getId(),
            savedAccount.getAccountNumber(),
            savedAccount.getAccountType().toString(),
            savedAccount.getBalance(),
            customer.getId(),
            customer.getEmail(),
            savedAccount.getCreatedAt()
        );

        eventPublisher.publishEvent(event);  // ← MAGIC HAPPENS HERE

        // 5. Retornar respuesta
        return AccountResponse.from(savedAccount);
    }
}
```

### Flujo Completo

```
┌─────────────────────────────────────────────────────────────────┐
│ 1. Cliente hace request                                         │
│    POST /api/accounts                                           │
│    {                                                            │
│      "customerId": 1,                                           │
│      "accountType": "SAVINGS",                                  │
│      "initialBalance": 5000.00                                  │
│    }                                                            │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│ 2. AccountController recibe request                            │
│    @PostMapping                                                 │
│    public ResponseEntity<AccountResponse> createAccount(...)    │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│ 3. AccountService crea cuenta                                   │
│    - Valida customer existe                                     │
│    - Crea entidad Account                                       │
│    - Guarda en MySQL                                            │
│    - Genera número de cuenta: 400012345678                      │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│ 4. AccountService PUBLICA evento                                │
│    eventPublisher.publishEvent(AccountCreatedEvent)             │
│                                                                 │
│    ✅ Cuenta guardada exitosamente                              │
│    ✅ Response enviado al cliente inmediatamente                │
└─────────────────────────────────────────────────────────────────┘
                            ↓
         ┌──────────────────┴──────────────────┐
         ↓                                      ↓
┌──────────────────────────┐      ┌───────────────────────────┐
│ 5a. NotificationService  │      │ 5b. TransactionLogService │
│     ESCUCHA evento       │      │     ESCUCHA evento        │
│                          │      │                           │
│ @ApplicationModuleListener│      │ @ApplicationModuleListener│
│ handleAccountCreated()   │      │ handleAccountCreated()    │
│                          │      │                           │
│ - Crea notificación      │      │ - Crea transaction log    │
│ - Envía email            │      │ - Guarda en MongoDB       │
│ - Guarda en MongoDB      │      │                           │
└──────────────────────────┘      └───────────────────────────┘
         ↓                                      ↓
┌──────────────────────────┐      ┌───────────────────────────┐
│ MongoDB - notifications  │      │ MongoDB - transaction_logs│
│                          │      │                           │
│ {                        │      │ {                         │
│   "type": "ACCOUNT_      │      │   "type": "ACCOUNT_       │
│           CREATED",      │      │           CREATED",       │
│   "subject": "Cuenta     │      │   "accountNumber":        │
│              Creada",    │      │     "400012345678",       │
│   "status": "SENT"       │      │   "status": "SUCCESS"     │
│ }                        │      │ }                         │
└──────────────────────────┘      └───────────────────────────┘
```

**Tiempo total:** ~200ms
- MySQL write: ~50ms
- Event publish: ~5ms
- Listeners (asíncronos): ~100ms cada uno

---

## 🎨 Ventajas de la Arquitectura con Eventos

### 1. Desacoplamiento ✅

**Sin eventos:**
```java
// AccountService depende de NotificationService
public class AccountService {
    @Autowired
    private NotificationService notificationService;  // ❌ Acoplamiento

    public void createAccount() {
        // ...
        notificationService.sendEmail();  // ❌ Dependencia directa
    }
}
```

**Con eventos:**
```java
// AccountService NO conoce a NotificationService
public class AccountService {
    @Autowired
    private ApplicationEventPublisher eventPublisher;  // ✅ Solo conoce eventos

    public void createAccount() {
        // ...
        eventPublisher.publishEvent(new AccountCreatedEvent());  // ✅ Publicar y olvidar
    }
}
```

---

### 2. Escalabilidad ✅

Puedes agregar nuevos listeners sin modificar código existente:

```java
// Nuevo listener para Analytics
@ApplicationModuleListener
public void handleAccountCreated(AccountCreatedEvent event) {
    // Enviar métricas a sistema de analytics
    analyticsService.trackAccountCreation(event);
}

// Nuevo listener para Auditoría
@ApplicationModuleListener
public void handleAccountCreated(AccountCreatedEvent event) {
    // Guardar en sistema de auditoría
    auditService.logAccountCreation(event);
}
```

**No se modifica AccountService** ✅

---

### 3. Resiliencia ✅

Si un listener falla, no afecta al publicador:

```java
// Si NotificationService falla...
@ApplicationModuleListener
public void handleAccountCreated(AccountCreatedEvent event) {
    throw new RuntimeException("Email service down!");  // ❌ Falla
}

// ... AccountService continúa funcionando
// La cuenta YA FUE CREADA exitosamente ✅
```

Spring Modulith puede **reintentar** el listener más tarde.

---

### 4. Testing Más Fácil ✅

**Test de AccountService sin dependencies:**
```java
@Test
void testCreateAccount() {
    // Given
    AccountCreateRequest request = new AccountCreateRequest();

    // When
    AccountResponse response = accountService.createAccount(request);

    // Then
    assertNotNull(response.getAccountNumber());
    assertEquals(AccountStatus.ACTIVE, response.getStatus());

    // NO necesitas mockear NotificationService ✅
}
```

---

## 📊 Monitoreo de Eventos

### Ver eventos publicados

Spring Modulith guarda metadatos de eventos. Puedes consultar:

```sql
-- Ver últimos eventos publicados
SELECT * FROM event_publication
ORDER BY publication_date DESC
LIMIT 10;
```

### Ver eventos pendientes de procesar

```sql
-- Ver eventos que no se han completado
SELECT * FROM event_publication
WHERE completion_date IS NULL;
```

---

## 🎓 Conceptos Clave

### 1. **Evento**
Un mensaje que indica que "algo pasó". Ejemplo: "Se creó una cuenta"

### 2. **Publisher (Publicador)**
El módulo que publica el evento. Ejemplo: `AccountService`

### 3. **Listener (Escuchador)**
El módulo que escucha y reacciona al evento. Ejemplo: `NotificationService`

### 4. **@ApplicationModuleListener**
Anotación que marca un método como listener de eventos

### 5. **Desacoplamiento**
Los módulos NO se conocen entre sí, solo conocen los eventos

### 6. **Asincronía**
Los listeners se ejecutan en paralelo, no bloquean al publisher

### 7. **@Externalized**
Anotación para publicar eventos fuera de la aplicación (Kafka, RabbitMQ)

### 8. **Event Store**
Almacén de eventos para auditoría y replay

---

## 💡 Mejores Prácticas

### ✅ DO (Hacer)

1. **Eventos inmutables** - Usa `@Data` y `@AllArgsConstructor`
2. **Nombres descriptivos** - `AccountCreatedEvent`, no `Event1`
3. **Incluye contexto** - customerId, email, timestamp, etc.
4. **Un listener, una responsabilidad** - No hagas todo en un listener
5. **Loggea eventos** - Para debugging y monitoreo

### ❌ DON'T (No Hacer)

1. **No modifiques el evento** - Son inmutables
2. **No hagas llamadas síncronas lentas** - Usa async
3. **No captures excepciones y las ignores** - Déjalas propagarse
4. **No publiques eventos en constructores** - Hazlo en métodos de servicio
5. **No mezcles lógica de negocio en listeners** - Solo orquestación

---

## 🐛 Troubleshooting Común

### Problema 1: Evento no se escucha

```
Publico evento pero listener no se ejecuta
```

**Solución:**
1. Verifica que el listener tenga `@ApplicationModuleListener`
2. Verifica que el evento sea la misma clase (no herencia)
3. Verifica que el listener esté en un `@Service` o `@Component`

---

### Problema 2: Evento se ejecuta múltiples veces

```
Listener se ejecuta 2 o 3 veces para el mismo evento
```

**Solución:**
1. Verifica que solo tengas UN listener con ese método
2. Usa `@Transactional` para evitar duplicados
3. Implementa idempotencia (usar el transactionId)

---

### Problema 3: Listener falla y rompe el flujo

```
Si listener falla, la operación principal falla
```

**Solución:**
1. Los listeners deben ser **asíncronos** por defecto
2. Spring Modulith reintentará automáticamente
3. Implementa manejo de errores en el listener

---

## ✅ Checklist de Aprendizaje

- [ ] Entiendo qué es un evento y para qué sirve
- [ ] Puedo explicar la diferencia entre Publisher y Listener
- [ ] Sé cómo publicar un evento con `ApplicationEventPublisher`
- [ ] Sé cómo crear un listener con `@ApplicationModuleListener`
- [ ] Entiendo las ventajas del desacoplamiento
- [ ] Puedo agregar un nuevo listener sin modificar código existente
- [ ] Entiendo cómo funcionan los eventos en las transferencias
- [ ] Sé cómo se guardan los logs en MongoDB mediante eventos
- [ ] Puedo explicar el flujo completo de crear una cuenta
- [ ] Entiendo la diferencia entre síncrono y asíncrono

---

## 🎯 Ejercicio Práctico

**Desafío:** Agrega un nuevo listener que envíe SMS cuando el saldo es menor a $100.

**Pasos:**
1. El evento `TransactionCompletedEvent` ya existe
2. En `NotificationService`, agrega un nuevo método:
```java
@ApplicationModuleListener
public void handleLowBalanceAlert(TransactionCompletedEvent event) {
    if (event.getNewBalance().compareTo(new BigDecimal("100.00")) < 0) {
        // Enviar SMS de alerta
        notifySMS(event.getCustomerEmail(), "⚠️ Saldo bajo: " + event.getNewBalance());
    }
}
```
3. Implementa `notifySMS()` que guarde una notificación con canal SMS
4. Prueba haciendo un retiro que deje el saldo < $100

**Solución:**
¡Inténtalo primero! No necesitas modificar `AccountService` ni ningún otro código. 🎩✨

---

## 📚 Resumen

### Módulos del Sistema
- **Customer** → Gestión de clientes
- **Account** → Gestión de cuentas y transacciones
- **Notification** → Notificaciones y alertas
- **Batch** → Procesamiento de intereses

### Eventos Implementados
1. `CustomerCreatedEvent` → Cliente registrado
2. `AccountCreatedEvent` → Cuenta creada
3. `TransactionCompletedEvent` → Depósito/retiro
4. `TransferCompletedEvent` → Transferencia entre cuentas
5. `InterestAppliedEvent` → Interés mensual aplicado

### Listeners Implementados
- **NotificationService** → 4 listeners (envía emails)
- **TransactionLogService** → 3 listeners (guarda en MongoDB)

### Ventajas
✅ Módulos desacoplados
✅ Fácil de escalar
✅ Resiliente a fallos
✅ Fácil de testear
✅ Asíncrono por defecto

---

**¿Preguntas?** Revisa el código fuente en `src/main/java/com/xideral/banco/`

**Happy Eventing!** 🚀
