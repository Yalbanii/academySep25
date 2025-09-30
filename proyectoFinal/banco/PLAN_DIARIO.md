# Plan de Ejecución - Sistema Bancario Digital
## Proyecto Final Academia Xideral FullStack

---

## 📅 DÍA 1: Setup y Configuración Completa
**Estado:** ⏳ PENDIENTE
**Objetivo:** Establecer base sólida del proyecto

### Configuración Inicial
- [ ] Inicializar proyecto Maven con Spring Boot 3.x
- [ ] Configurar `pom.xml` con dependencias:
  - [ ] Spring Boot Starter Web
  - [ ] Spring Boot Starter Data JPA
  - [ ] Spring Boot Starter Data MongoDB
  - [ ] MySQL Connector
  - [ ] Spring Boot Starter Test
  - [ ] JUnit 5
  - [ ] Mockito
  - [ ] JaCoCo (coverage)
  - [ ] Lombok (opcional)
  - [ ] Validation API

### Configuración de Bases de Datos
- [ ] Crear base de datos MySQL `banco_db`
- [ ] Configurar MongoDB (conexión local/cloud)
- [ ] Configurar `application.properties`:
  - [ ] MySQL datasource
  - [ ] MongoDB connection
  - [ ] JPA properties
  - [ ] Server port

### Estructura del Proyecto
- [ ] Crear estructura de packages:
  ```
  com.xideral.banco/
  ├── customer/
  │   ├── controller/
  │   ├── service/
  │   ├── repository/
  │   └── model/
  ├── account/
  │   ├── controller/
  │   ├── service/
  │   ├── repository/
  │   └── model/
  ├── notification/
  │   ├── service/
  │   └── model/
  ├── batch/
  ├── events/
  └── config/
  ```

### Entidades Base
- [ ] Crear entidad `Customer` (MySQL)
  - [ ] id, name, email, phone, status, createdAt
  - [ ] Anotaciones JPA
  - [ ] Validaciones
- [ ] Crear entidad `Account` (MySQL)
  - [ ] id, accountNumber, accountType, balance, customerId
  - [ ] Relación con Customer
- [ ] Crear documento `TransactionLog` (MongoDB)
  - [ ] id, transactionType, amount, timestamp, details

### Testing de Conectividad
- [ ] Test conexión MySQL
- [ ] Test conexión MongoDB
- [ ] Verificar application context carga correctamente
- [ ] Ejecutar `mvn clean install`

**Entregable Día 1:** Proyecto compilando, DBs conectadas, entidades base creadas

---

## 📅 DÍA 2: Módulo Customer + Testing Integral
**Estado:** ⏳ PENDIENTE
**Objetivo:** CRUD completo con testing robusto

### Capa de Repositorio
- [ ] `CustomerRepository` extends JpaRepository
- [ ] Query methods:
  - [ ] findByEmail(String email)
  - [ ] findByStatus(String status)
  - [ ] existsByEmail(String email)

### Capa de Servicio
- [ ] `CustomerService` interface
- [ ] `CustomerServiceImpl` implementación
- [ ] Métodos:
  - [ ] createCustomer() - validar email único
  - [ ] updateCustomer()
  - [ ] getCustomerById()
  - [ ] getAllCustomers()
  - [ ] deleteCustomer() - soft delete (cambiar status)
  - [ ] activateCustomer()
  - [ ] deactivateCustomer()

### Validaciones de Negocio
- [ ] Email único en el sistema
- [ ] Email con formato válido
- [ ] Teléfono con formato válido
- [ ] No permitir eliminar customer con cuentas activas
- [ ] Status válidos: ACTIVE, INACTIVE

### Capa de Controller
- [ ] `CustomerController` con endpoints REST:
  - [ ] POST /api/customers - crear cliente
  - [ ] GET /api/customers/{id} - obtener por id
  - [ ] GET /api/customers - listar todos
  - [ ] PUT /api/customers/{id} - actualizar
  - [ ] DELETE /api/customers/{id} - eliminar
  - [ ] PATCH /api/customers/{id}/activate
  - [ ] PATCH /api/customers/{id}/deactivate
- [ ] DTOs para request/response
- [ ] Manejo de excepciones (@ControllerAdvice)

### Testing Completo
- [ ] **CustomerRepositoryTest**
  - [ ] Test save customer
  - [ ] Test find by email
  - [ ] Test find by status
  - [ ] Test exists by email
- [ ] **CustomerServiceTest** (con Mockito)
  - [ ] Test create customer exitoso
  - [ ] Test create customer con email duplicado
  - [ ] Test update customer
  - [ ] Test get customer by id
  - [ ] Test get customer no encontrado
  - [ ] Test soft delete customer
- [ ] **CustomerControllerTest** (MockMvc)
  - [ ] Test POST crear cliente
  - [ ] Test GET por id
  - [ ] Test GET todos
  - [ ] Test PUT actualizar
  - [ ] Test DELETE eliminar
  - [ ] Test validaciones (400 Bad Request)

### Coverage
- [ ] Ejecutar `mvn test`
- [ ] Generar reporte JaCoCo
- [ ] Verificar coverage > 85% en módulo Customer

**Entregable Día 2:** CRUD Customer funcional con 85%+ coverage

---

## 📅 DÍA 3: Módulo Account + Polimorfismo
**Estado:** ⏳ PENDIENTE
**Objetivo:** Lógica de negocio bancaria con polimorfismo

### Entidades y Enums
- [ ] Enum `AccountType`: SAVINGS, CHECKING
- [ ] Enum `TransactionType`: DEPOSIT, WITHDRAWAL, TRANSFER_IN, TRANSFER_OUT
- [ ] Entidad `Transaction` (MySQL)
  - [ ] id, accountId, type, amount, balance, timestamp

### Polimorfismo - Calculadoras de Interés
- [ ] Interface `InterestCalculator`
  - [ ] calculateInterest(double balance)
- [ ] `SavingsInterestCalculator` implements InterestCalculator
  - [ ] Tasa: 3.5% anual
- [ ] `CheckingInterestCalculator` implements InterestCalculator
  - [ ] Tasa: 0.5% anual
- [ ] `InterestCalculatorFactory`
  - [ ] getCalculator(AccountType type)

### Capa de Repositorio
- [ ] `AccountRepository` extends JpaRepository
- [ ] `TransactionRepository` extends JpaRepository
- [ ] Query methods:
  - [ ] findByCustomerId(Long customerId)
  - [ ] findByAccountNumber(String accountNumber)
  - [ ] findActiveAccounts()

### Capa de Servicio
- [ ] `AccountService` interface
- [ ] `AccountServiceImpl` implementación
- [ ] Métodos:
  - [ ] createAccount(customerId, accountType)
  - [ ] getAccountById(id)
  - [ ] getAccountsByCustomerId(customerId)
  - [ ] getBalance(accountId)
  - [ ] deposit(accountId, amount)
  - [ ] withdraw(accountId, amount)
  - [ ] transfer(fromAccountId, toAccountId, amount)
  - [ ] closeAccount(accountId)

### Validaciones de Negocio
- [ ] Customer debe existir y estar ACTIVE
- [ ] Balance inicial mínimo: $100
- [ ] No permitir retiros > balance
- [ ] Transfer validar fondos suficientes
- [ ] Transfer validar cuentas diferentes
- [ ] Número de cuenta único (generado automáticamente)
- [ ] Montos deben ser > 0

### Lógica de Transferencias
- [ ] Transacción atómica (@Transactional)
- [ ] Debitar cuenta origen (TRANSFER_OUT)
- [ ] Acreditar cuenta destino (TRANSFER_IN)
- [ ] Registrar ambas transacciones
- [ ] Rollback si falla cualquier paso

### Capa de Controller
- [ ] `AccountController` con endpoints:
  - [ ] POST /api/accounts - crear cuenta
  - [ ] GET /api/accounts/{id}
  - [ ] GET /api/accounts/customer/{customerId}
  - [ ] GET /api/accounts/{id}/balance
  - [ ] POST /api/accounts/{id}/deposit
  - [ ] POST /api/accounts/{id}/withdraw
  - [ ] POST /api/accounts/transfer
  - [ ] DELETE /api/accounts/{id}

### Testing
- [ ] **InterestCalculatorTest**
  - [ ] Test SavingsInterestCalculator
  - [ ] Test CheckingInterestCalculator
  - [ ] Test Factory
- [ ] **AccountServiceTest**
  - [ ] Test crear cuenta
  - [ ] Test deposit
  - [ ] Test withdraw exitoso
  - [ ] Test withdraw sin fondos
  - [ ] Test transfer exitoso
  - [ ] Test transfer sin fondos
  - [ ] Test validaciones
- [ ] **AccountControllerTest**
  - [ ] Test todos los endpoints
  - [ ] Test validaciones y errores

### Coverage
- [ ] Coverage > 85% módulo Account

**Entregable Día 3:** Módulo Account con transferencias y polimorfismo funcionando

---

## 📅 DÍA 4: Eventos + Notificaciones + Transacciones
**Estado:** ⏳ PENDIENTE
**Objetivo:** Comunicación entre módulos vía eventos

### Sistema de Eventos
- [ ] Crear package `events`
- [ ] Definir eventos:
  - [ ] `CustomerCreatedEvent`
  - [ ] `AccountCreatedEvent`
  - [ ] `TransactionExecutedEvent`
  - [ ] `TransferCompletedEvent`

### Event Publishers
- [ ] Publicar eventos en:
  - [ ] CustomerService.createCustomer()
  - [ ] AccountService.createAccount()
  - [ ] AccountService.deposit()
  - [ ] AccountService.withdraw()
  - [ ] AccountService.transfer()

### Módulo Notification
- [ ] `NotificationService` interface
- [ ] `EmailNotificationService` implementación
- [ ] Métodos:
  - [ ] sendWelcomeEmail(customer)
  - [ ] sendAccountCreatedEmail(account)
  - [ ] sendTransactionConfirmation(transaction)
  - [ ] sendTransferConfirmation(transfer)

### Event Listeners
- [ ] `NotificationEventListener`
  - [ ] @EventListener CustomerCreatedEvent
  - [ ] @EventListener AccountCreatedEvent
  - [ ] @EventListener TransactionExecutedEvent
  - [ ] @EventListener TransferCompletedEvent
- [ ] Cada listener invoca NotificationService

### Historial Transaccional (MongoDB)
- [ ] `TransactionLogRepository` (MongoDB)
- [ ] Documento `TransactionLog`:
  - [ ] transactionId, accountId, type, amount
  - [ ] timestamp, status, metadata
- [ ] `TransactionLogService`
  - [ ] logTransaction(transaction)
  - [ ] getTransactionHistory(accountId)
  - [ ] getTransactionsByDateRange(accountId, from, to)

### Event Listener para Logs
- [ ] `TransactionLogListener`
  - [ ] @EventListener TransactionExecutedEvent
  - [ ] Guardar en MongoDB cada transacción

### Controller para Historial
- [ ] `TransactionController`
  - [ ] GET /api/transactions/account/{accountId}
  - [ ] GET /api/transactions/account/{accountId}/history
  - [ ] GET /api/transactions/{transactionId}

### Testing
- [ ] **EventPublisherTest**
  - [ ] Verificar eventos se publican
- [ ] **NotificationServiceTest**
  - [ ] Test envío de notificaciones
- [ ] **TransactionLogServiceTest**
  - [ ] Test guardado en MongoDB
  - [ ] Test queries de historial
- [ ] **Integration Tests**
  - [ ] Test flujo completo: transfer -> evento -> notificación -> log

### Coverage
- [ ] Coverage > 85% módulos Notification y TransactionLog

**Entregable Día 4:** Sistema de eventos funcionando, notificaciones automáticas, logs en MongoDB

---

## 📅 DÍA 5: Spring Batch + Coverage Final
**Estado:** ⏳ PENDIENTE
**Objetivo:** Job de procesamiento mensual de intereses

### Configuración Spring Batch
- [ ] Agregar dependencia Spring Batch
- [ ] Configurar `@EnableBatchProcessing`
- [ ] Configurar DataSource para metadata de Batch

### Job: Procesamiento Mensual de Intereses
- [ ] `MonthlyInterestJob` configuración
- [ ] **Step 1: Leer Cuentas Activas**
  - [ ] `AccountItemReader` (JpaPagingItemReader)
  - [ ] Leer todas las cuentas SAVINGS activas
- [ ] **Step 2: Calcular y Aplicar Intereses**
  - [ ] `InterestItemProcessor`
    - [ ] Usar InterestCalculator por tipo
    - [ ] Calcular interés mensual
  - [ ] `InterestItemWriter`
    - [ ] Actualizar balance
    - [ ] Crear Transaction de tipo INTEREST
    - [ ] Publicar evento InterestAppliedEvent
    - [ ] Log en MongoDB

### Scheduling
- [ ] `@EnableScheduling`
- [ ] `BatchScheduler`
  - [ ] @Scheduled ejecutar job mensualmente
  - [ ] Configurar cron expression
  - [ ] Manual trigger para testing: POST /api/batch/run-interest-job

### Listeners y Logging
- [ ] `JobExecutionListener`
  - [ ] beforeJob: log inicio
  - [ ] afterJob: log resumen (cuentas procesadas, total intereses)
- [ ] `StepExecutionListener`
  - [ ] Logs por step

### Reporting
- [ ] Endpoint para ver ejecuciones:
  - [ ] GET /api/batch/jobs
  - [ ] GET /api/batch/jobs/{jobExecutionId}
- [ ] Guardar resumen en MongoDB:
  - [ ] BatchExecutionReport
  - [ ] totalAccounts, totalInterest, timestamp, status

### Testing Batch
- [ ] **MonthlyInterestJobTest**
  - [ ] Test job execution
  - [ ] Test step 1: lectura correcta
  - [ ] Test step 2: procesamiento correcto
  - [ ] Verificar intereses calculados
  - [ ] Verificar balances actualizados
  - [ ] Verificar transacciones creadas
  - [ ] Verificar eventos publicados
- [ ] **Integration Test completo**
  - [ ] Crear cuentas de prueba
  - [ ] Ejecutar job
  - [ ] Verificar resultados end-to-end

### Coverage Final del Proyecto
- [ ] Ejecutar `mvn clean test`
- [ ] Generar reporte JaCoCo completo
- [ ] Verificar coverage global > 85%
- [ ] Revisar coverage por módulo:
  - [ ] Customer module
  - [ ] Account module
  - [ ] Notification module
  - [ ] Batch module
  - [ ] Events module
- [ ] Corregir gaps de coverage si es necesario

### Documentación Final
- [ ] Configurar Swagger/OpenAPI
- [ ] Documentar todos los endpoints
- [ ] Verificar respuestas y códigos HTTP
- [ ] Probar endpoints desde Swagger UI

### Testing de Integración Final
- [ ] Flujo completo 1: Crear cliente -> Crear cuenta -> Depositar -> Transferir
- [ ] Flujo completo 2: Ejecutar batch job -> Verificar intereses
- [ ] Flujo completo 3: Verificar notificaciones y logs

### Comandos Maven Finales
- [ ] `mvn clean install`
- [ ] `mvn test`
- [ ] `mvn verify`
- [ ] `mvn jacoco:report`

**Entregable Día 5:** Proyecto completo con Batch funcionando y 85%+ coverage global

---

## 📊 Métricas de Éxito

### Al finalizar los 5 días:
- ✅ **Coverage:** > 85% (verificado con JaCoCo)
- ✅ **APIs REST:** 15+ endpoints documentados
- ✅ **Módulos:** 3 módulos comunicándose vía eventos
- ✅ **Bases de datos:** MySQL + MongoDB funcionando
- ✅ **Batch:** Job de intereses ejecutándose correctamente
- ✅ **Testing:** Tests en todas las capas (unit + integration)
- ✅ **Build:** `mvn clean install` exitoso

---

## 🛠️ Comandos Maven Útiles

```bash
# Compilar
mvn clean compile

# Ejecutar tests
mvn test

# Generar reporte de coverage
mvn test jacoco:report

# Ver reporte (abrir en navegador)
open target/site/jacoco/index.html

# Build completo
mvn clean install

# Ejecutar aplicación
mvn spring-boot:run

# Skip tests (no recomendado)
mvn clean install -DskipTests
```

---

## 📝 Notas Importantes

- **Commits frecuentes:** Hacer commit al completar cada checklist importante
- **Testing continuo:** No dejar testing para el final
- **Validar diariamente:** Cada día debe tener un entregable funcional
- **Coverage mínimo:** 85% es el mínimo, apuntar a 90%+
- **Code review:** Revisar código antes de pasar al siguiente módulo
- **Documentación:** Documentar decisiones técnicas importantes

---

*Última actualización: Septiembre 2025*