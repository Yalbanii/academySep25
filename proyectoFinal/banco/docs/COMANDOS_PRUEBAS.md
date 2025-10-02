# Comandos de Pruebas Manuales
## Sistema Bancario Digital - Días 1 al 5

Este archivo contiene todos los comandos `curl` que puedes ejecutar manualmente para probar el sistema.

---

## 📋 Prerequisitos

1. **Iniciar MongoDB:**
```bash
docker start mongodb-container
# O crear nuevo:
docker run --name mongodb-container \
  -e MONGO_INITDB_ROOT_USERNAME=admin \
  -e MONGO_INITDB_ROOT_PASSWORD=xideral4321 \
  -p 27017:27017 \
  -d mongo:8
```

2. **Iniciar la aplicación:**
```bash
cd /Users/mike/Desarrollo/academiaXidSep25/proyectoFinal/banco
mvn spring-boot:run
```

3. **Verificar que la aplicación está corriendo:**
```bash
curl http://localhost:8080/actuator/health
# O simplemente:
curl http://localhost:8080/api/customers
```

---

## 🗂️ Datos dinámicos que debes guardar

- Los IDs de clientes y las cuentas se generan al vuelo. Guarda los valores que te devuelva la API para usarlos en los pasos siguientes.
- Puedes exportarlos a variables de entorno inmediatamente después de cada creación para reutilizarlos en los `curl`.

Ejemplo general:
```bash
export CUSTOMER_ID=<ID_devuelto_por_el_POST_de_customers>
export ACCOUNT_CHECKING=<accountNumber_devuelto_por_el_POST_de_accounts_CHECKING>
export ACCOUNT_SAVINGS=<accountNumber_devuelto_por_el_POST_de_accounts_SAVINGS>
```
> Si prefieres automatizarlo, captura la respuesta en una variable (`RESP=$(curl ... )`) y usa `jq` para extraer cada campo.

---

## 🧪 Día 1: Customer Module

### 1. Crear un Cliente
```bash
CUSTOMER_RESPONSE=$(curl -s -X POST http://localhost:8080/api/customers \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Patrobas",
    "email": "patrobas.garcia@example.com",
    "phone": "5559876543"
  }')
echo "$CUSTOMER_RESPONSE" | jq .
export CUSTOMER_ID=$(echo "$CUSTOMER_RESPONSE" | jq -r '.id')
echo "ID del cliente guardado en CUSTOMER_ID=$CUSTOMER_ID"
```

**Respuesta esperada:**
```json
{
  "id": 1,
  "name": "Patrobas",
  "email": "patrobas.garcia@example.com",
  "phone": "5559876543",
  "status": "ACTIVE",
  "createdAt": "2025-09-30T...",
  "updatedAt": "2025-09-30T..."
}
```

### 2. Obtener Todos los Clientes
```bash
curl http://localhost:8080/api/customers | jq .
```

### 3. Obtener Cliente por ID
```bash
curl http://localhost:8080/api/customers/$CUSTOMER_ID | jq .
```

### 4. Actualizar Cliente
```bash
curl -X PUT http://localhost:8080/api/customers/$CUSTOMER_ID \
  -H "Content-Type: application/json" \
  -d '{
    "name": "María García Rodríguez",
    "email": "maria.nueva@example.com",
    "phone": "5551112233"
  }' | jq .
```

### 5. Obtener Clientes Activos
```bash
curl http://localhost:8080/api/customers/status/ACTIVE | jq .
```

---

## 🏦 Día 2: Account Module

### 1. Crear Cuenta CHECKING con balance inicial
```bash
ACCOUNT_CHECKING_PAYLOAD=$(jq -n \
  --argjson customerId "$CUSTOMER_ID" \
  --arg accountType "CHECKING" \
  --argjson initialBalance 1000.00 \
  '{customerId: $customerId, accountType: $accountType, initialBalance: $initialBalance}')
ACCOUNT_CHECKING_RESPONSE=$(curl -s -X POST http://localhost:8080/api/accounts \
  -H "Content-Type: application/json" \
  -d "$ACCOUNT_CHECKING_PAYLOAD")
echo "$ACCOUNT_CHECKING_RESPONSE" | jq .
export ACCOUNT_CHECKING=$(echo "$ACCOUNT_CHECKING_RESPONSE" | jq -r '.accountNumber')
export ACCOUNT_CHECKING_ID=$(echo "$ACCOUNT_CHECKING_RESPONSE" | jq -r '.id')
echo "Cuenta CHECKING creada → número=$ACCOUNT_CHECKING (ID=$ACCOUNT_CHECKING_ID)"
```

**Lo que debes ver:** un objeto `AccountResponse` con el `id`, `accountNumber` generado dinámicamente, el `customerId` que acabas de crear, el `balance` inicial y los timestamps.

**🔔 Verifica la notificación:**
```bash
curl http://localhost:8080/api/notifications/customer/$CUSTOMER_ID | jq '.[-1]'
```

### 2. Crear Cuenta SAVINGS con balance inicial
```bash
ACCOUNT_SAVINGS_PAYLOAD=$(jq -n \
  --argjson customerId "$CUSTOMER_ID" \
  --arg accountType "SAVINGS" \
  --argjson initialBalance 5000.00 \
  '{customerId: $customerId, accountType: $accountType, initialBalance: $initialBalance}')
ACCOUNT_SAVINGS_RESPONSE=$(curl -s -X POST http://localhost:8080/api/accounts \
  -H "Content-Type: application/json" \
  -d "$ACCOUNT_SAVINGS_PAYLOAD")
echo "$ACCOUNT_SAVINGS_RESPONSE" | jq .
export ACCOUNT_SAVINGS=$(echo "$ACCOUNT_SAVINGS_RESPONSE" | jq -r '.accountNumber')
export ACCOUNT_SAVINGS_ID=$(echo "$ACCOUNT_SAVINGS_RESPONSE" | jq -r '.id')
echo "Cuenta SAVINGS creada → número=$ACCOUNT_SAVINGS (ID=$ACCOUNT_SAVINGS_ID)"
```

### 3. Obtener Todas las Cuentas
```bash
curl http://localhost:8080/api/accounts | jq .
```

### 4. Obtener Cuentas de un Cliente
```bash
curl http://localhost:8080/api/accounts/customer/$CUSTOMER_ID | jq .
```

### 5. Obtener Cuenta por ID (no por número)
```bash
# Usa el ID de la cuenta, no el número de cuenta
curl http://localhost:8080/api/accounts/$ACCOUNT_CHECKING_ID | jq .
```

### 6. Obtener Cuentas Activas
```bash
curl http://localhost:8080/api/accounts/status/ACTIVE | jq .
```

---

## 💰 Día 3: Banking Operations

### 1. Depósito

**Depositar $1000:**
```bash
DEPOSIT_PAYLOAD=$(jq -n \
  --arg accountNumber "$ACCOUNT_CHECKING" \
  --argjson amount 1000.00 \
  '{accountNumber: $accountNumber, amount: $amount}')
DEPOSIT_RESPONSE=$(curl -s -X POST http://localhost:8080/api/accounts/deposit \
  -H "Content-Type: application/json" \
  -d "$DEPOSIT_PAYLOAD")
echo "$DEPOSIT_RESPONSE" | jq .
```

**Lo que debes ver:** el `AccountResponse` de la cuenta con el nuevo `balance` incrementado en 1000.00 respecto al valor anterior.

**🔔 Verifica la notificación de depósito:**
```bash
curl http://localhost:8080/api/notifications/type/DEPOSIT | jq .
```

### 2. Retiro

**Retirar $300:**
```bash
export ACCOUNT_CHECKING="400053095316"
echo $ACCOUNT_CHECKING

WITHDRAW_PAYLOAD=$(jq -n \
  --arg accountNumber "$ACCOUNT_CHECKING" \
  --argjson amount 300.00 \
  '{accountNumber: $accountNumber, amount: $amount}')

WITHDRAW_RESPONSE=$(curl -s -X POST http://localhost:8080/api/accounts/withdraw \
  -H "Content-Type: application/json" \
  -d "$WITHDRAW_PAYLOAD")

echo "$WITHDRAW_RESPONSE" | jq .
```

```bash
export ACCOUNT_CHECKING="400053095316"
echo $ACCOUNT_CHECKING

WITHDRAW_RESPONSE=$(curl -s -X POST http://localhost:8080/api/accounts/withdraw \
-H "Content-Type: application/json" \
-d "$WITHDRAW_PAYLOAD")

echo "$WITHDRAW_RESPONSE" | jq .

```

**🔔 Verifica la notificación de retiro:**
```bash
curl http://localhost:8080/api/notifications/type/WITHDRAWAL | jq .
```

### 3. Transferencia

**Transferir $250 entre cuentas:**
```bash
export ACCOUNT_CHECKING="400090752141"
export ACCOUNT_SAVINGS="400053095316"

TRANSFER_PAYLOAD=$(jq -n \
  --arg fromAccountNumber "$ACCOUNT_CHECKING" \
  --arg toAccountNumber "$ACCOUNT_SAVINGS" \
  --argjson amount 250.00 \
  '{fromAccountNumber: $fromAccountNumber, toAccountNumber: $toAccountNumber, amount: $amount}')

curl -X POST http://localhost:8080/api/accounts/transfer \
  -H "Content-Type: application/json" \
  -d "$TRANSFER_PAYLOAD"
```

**🔔 Verifica las 2 notificaciones de transferencia:**
```bash
# Notificación de envío
curl http://localhost:8080/api/notifications/type/TRANSFER_SENT | jq .

# Notificación de recepción
curl http://localhost:8080/api/notifications/type/TRANSFER_RECEIVED | jq .
```

### 4. Verificar Balances Después de Operaciones
```bash
curl http://localhost:8080/api/accounts | jq '[.[] | {accountNumber, balance}]'
```

---

## 🔔 Día 4: Notification System

### 1. Obtener Todas las Notificaciones
```bash
curl http://localhost:8080/api/notifications | jq .
```

### 2. Notificaciones por Cliente
```bash
curl http://localhost:8080/api/notifications/customer/1 | jq .
```

### 3. Notificaciones Ordenadas por Fecha
```bash
curl http://localhost:8080/api/notifications/customer/1/ordered | jq .
```

### 4. Notificaciones por Estado
```bash
# SENT
curl http://localhost:8080/api/notifications/status/SENT | jq .

# PENDING
curl http://localhost:8080/api/notifications/status/PENDING | jq .

# FAILED
curl http://localhost:8080/api/notifications/status/FAILED | jq .
```

### 5. Notificaciones por Tipo
```bash
# Cuentas creadas
curl http://localhost:8080/api/notifications/type/ACCOUNT_CREATED | jq .

# Depósitos
curl http://localhost:8080/api/notifications/type/DEPOSIT | jq .

# Retiros
curl http://localhost:8080/api/notifications/type/WITHDRAWAL | jq .

# Transferencias enviadas
curl http://localhost:8080/api/notifications/type/TRANSFER_SENT | jq .

# Transferencias recibidas
curl http://localhost:8080/api/notifications/type/TRANSFER_RECEIVED | jq .
```

### 6. Notificaciones por Canal
```bash
curl http://localhost:8080/api/notifications/channel/EMAIL | jq .
```

### 7. Notificaciones por Cuenta
```bash
curl http://localhost:8080/api/notifications/account/400012345678 | jq .
```

### 8. Contar Notificaciones por Estado
```bash
curl http://localhost:8080/api/notifications/count/status/SENT
```

### 9. Obtener Notificación por ID
```bash
# Reemplaza {id} con el ID real de MongoDB (ObjectId)
curl http://localhost:8080/api/notifications/674e5a1c3f2a4b0012345678 | jq .
```

### 10. Crear Notificación Manual
```bash
curl -X POST http://localhost:8080/api/notifications \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 1,
    "customerEmail": "maria.garcia@example.com",
    "type": "CUSTOMER_UPDATED",
    "channel": "EMAIL",
    "subject": "Prueba de Notificación Manual",
    "message": "Esta es una notificación de prueba creada manualmente."
  }' | jq .
```

### 11. Enviar Notificación Específica
```bash
# Primero obtén el ID de una notificación PENDING
NOTIF_ID=$(curl -s http://localhost:8080/api/notifications/status/PENDING | jq -r '.[0].id')

# Enviarla
curl -X POST http://localhost:8080/api/notifications/$NOTIF_ID/send
```

### 12. Enviar Todas las Notificaciones Pendientes
```bash
curl -X POST http://localhost:8080/api/notifications/send-pending
```

### 13. Reintentar Notificaciones Fallidas
```bash
curl -X POST http://localhost:8080/api/notifications/retry-failed
```

### 14. Eliminar Notificación
```bash
# Reemplaza {id} con el ID real
curl -X DELETE http://localhost:8080/api/notifications/674e5a1c3f2a4b0012345678
```

---

## 📜 Día 4: Transaction Log System

### 1. Obtener Todos los Transaction Logs
```bash
curl http://localhost:8080/api/transaction-logs | jq .
```

### 2. Transaction Logs por Cuenta
```bash
# Reemplaza con número de cuenta real
curl http://localhost:8080/api/transaction-logs/account/400012345678 | jq .
```

### 3. Transaction Logs por Tipo de Transacción
```bash
# DEPOSIT
curl http://localhost:8080/api/transaction-logs/transaction-type/DEPOSIT | jq .

# WITHDRAWAL
curl http://localhost:8080/api/transaction-logs/transaction-type/WITHDRAWAL | jq .

# TRANSFER
curl http://localhost:8080/api/transaction-logs/transaction-type/TRANSFER | jq .

# INTEREST
curl http://localhost:8080/api/transaction-logs/transaction-type/INTEREST | jq .
```

### 4. Transaction Logs por Cliente
```bash
curl http://localhost:8080/api/transaction-logs/customer/1 | jq .
```

### 5. Transaction Logs por Estado
```bash
# SUCCESS
curl http://localhost:8080/api/transaction-logs/status/SUCCESS | jq .

# FAILED
curl http://localhost:8080/api/transaction-logs/status/FAILED | jq .
```

### 6. Transaction Logs por Rango de Fechas
```bash
# Formato: YYYY-MM-DD
curl "http://localhost:8080/api/transaction-logs/date-range?startDate=2025-09-01&endDate=2025-09-30" | jq .
```

### 7. Transaction Logs por Rango de Montos
```bash
# Transacciones entre $100 y $1000
curl "http://localhost:8080/api/transaction-logs/amount-range?minAmount=100&maxAmount=1000" | jq .
```

### 8. Contar Transaction Logs
```bash
# Por tipo
curl http://localhost:8080/api/transaction-logs/count/transaction-type/DEPOSIT

# Por estado
curl http://localhost:8080/api/transaction-logs/count/status/SUCCESS

# Por cuenta
curl http://localhost:8080/api/transaction-logs/count/account/400012345678
```

### 9. Obtener Transaction Log por ID
```bash
# Reemplaza {id} con el ID real de MongoDB (ObjectId)
curl http://localhost:8080/api/transaction-logs/674e5a1c3f2a4b0012345678 | jq .
```

### 10. Verificar Transaction Logs en MongoDB Directamente
```bash
# Ver todos los transaction logs
docker exec mongodb-container mongosh \
  -u admin -p xideral4321 --authenticationDatabase admin \
  --eval "db = db.getSiblingDB('banco_logs'); db.transactionLogs.find().pretty()"

# Contar transaction logs
docker exec mongodb-container mongosh \
  -u admin -p xideral4321 --authenticationDatabase admin \
  --eval "db = db.getSiblingDB('banco_logs'); db.transactionLogs.countDocuments()"

# Ver últimos 5 transaction logs
docker exec mongodb-container mongosh \
  -u admin -p xideral4321 --authenticationDatabase admin \
  --eval "db = db.getSiblingDB('banco_logs'); db.transactionLogs.find().sort({timestamp: -1}).limit(5).pretty()"

# Estadísticas por tipo de transacción
docker exec mongodb-container mongosh \
  -u admin -p xideral4321 --authenticationDatabase admin \
  --eval "db = db.getSiblingDB('banco_logs'); db.transactionLogs.aggregate([{\$group: {_id: '\$transactionType', count: {\$sum: 1}, totalAmount: {\$sum: {\$toDouble: '\$amount'}}}}, {\$sort: {count: -1}}])"
```

---

## 📊 Consultas Útiles

### Ver Resumen de Cuentas
```bash
curl -s http://localhost:8080/api/accounts | jq '[.[] | {
  accountNumber,
  accountType,
  balance,
  active
}]'
```

### Ver Resumen de Notificaciones
```bash
curl -s http://localhost:8080/api/notifications | jq '[.[] | {
  type,
  status,
  subject,
  createdAt
}]'
```

### Contar Notificaciones por Tipo
```bash
echo "ACCOUNT_CREATED: $(curl -s http://localhost:8080/api/notifications/type/ACCOUNT_CREATED | jq 'length')"
echo "DEPOSIT: $(curl -s http://localhost:8080/api/notifications/type/DEPOSIT | jq 'length')"
echo "WITHDRAWAL: $(curl -s http://localhost:8080/api/notifications/type/WITHDRAWAL | jq 'length')"
echo "TRANSFER_SENT: $(curl -s http://localhost:8080/api/notifications/type/TRANSFER_SENT | jq 'length')"
echo "TRANSFER_RECEIVED: $(curl -s http://localhost:8080/api/notifications/type/TRANSFER_RECEIVED | jq 'length')"
```

### Ver Balance Total por Cliente
```bash
CUSTOMER_ID=1
curl -s http://localhost:8080/api/accounts/customer/$CUSTOMER_ID | \
  jq '[.[] | .balance] | add'
```

---

## 🧪 Escenarios de Prueba Completos

### Escenario 1: Nuevo Cliente y Primera Cuenta
```bash
# 1. Crear cliente
CUSTOMER=$(curl -s -X POST http://localhost:8080/api/customers \
  -H "Content-Type: application/json" \
  -d '{"name": "Tercio López","email": "tercio@example.com","phone": "5551234567"}')
CUSTOMER_ID=$(echo $CUSTOMER | jq -r '.id')
echo $CUSTOMER_ID

# 2. Crear cuenta
ACCOUNT=$(curl -s -X POST http://localhost:8080/api/accounts \
  -H "Content-Type: application/json" \
  -d "{\"customerId\": $CUSTOMER_ID,\"accountType\": \"CHECKING\"}")
ACCOUNT_NUMBER=$(echo $ACCOUNT | jq -r '.accountNumber')

# 3. Ver notificación generada
curl http://localhost:8080/api/notifications/customer/$CUSTOMER_ID | jq .
```

### Escenario 2: Ciclo Completo de Transacciones
```bash
ACCOUNT_NUMBER="400087947135"

# 1. Depósito inicial
curl -X POST http://localhost:8080/api/accounts/deposit \
  -H "Content-Type: application/json" \
  -d "{\"accountNumber\": \"$ACCOUNT_NUMBER\",\"amount\": 1000.00}" | jq .

# 2. Retiro
curl -X POST http://localhost:8080/api/accounts/withdraw \
  -H "Content-Type: application/json" \
  -d "{\"accountNumber\": \"$ACCOUNT_NUMBER\",\"amount\": 200.00}" | jq .

# 3. Verificar balance final
curl http://localhost:8080/api/accounts/number/$ACCOUNT_NUMBER | jq '{accountNumber, balance}'

# 4. Ver todas las notificaciones generadas
curl http://localhost:8080/api/notifications/account/$ACCOUNT_NUMBER | jq .
```

### Escenario 3: Transferencia entre Cuentas
```bash
ACCOUNT_FROM="400087947135"
ACCOUNT_TO="400039701907"

# 1. Verificar balances iniciales
echo "Balance cuenta origen:"
curl -s http://localhost:8080/api/accounts/number/$ACCOUNT_FROM | jq '.balance'
echo "Balance cuenta destino:"
curl -s http://localhost:8080/api/accounts/number/$ACCOUNT_TO | jq '.balance'

# 2. Realizar transferencia
curl -X POST http://localhost:8080/api/accounts/transfer \
  -H "Content-Type: application/json" \
  -d "{
    \"fromAccountNumber\": \"$ACCOUNT_FROM\",
    \"toAccountNumber\": \"$ACCOUNT_TO\",
    \"amount\": 100.00
  }"

# 3. Verificar balances finales
echo "Nuevo balance cuenta origen:"
curl -s http://localhost:8080/api/accounts/number/$ACCOUNT_FROM | jq '.balance'
echo "Nuevo balance cuenta destino:"
curl -s http://localhost:8080/api/accounts/number/$ACCOUNT_TO | jq '.balance'

# 4. Ver notificaciones de transferencia
curl http://localhost:8080/api/notifications/type/TRANSFER_SENT | jq '.[-1]'
curl http://localhost:8080/api/notifications/type/TRANSFER_RECEIVED | jq '.[-1]'
```

---

## 🔄 Día 5: Spring Batch - Monthly Interest Processing

### ⚠️ IMPORTANTE: Habilitar Batch

El batch está deshabilitado por defecto. Para habilitarlo:

1. **Editar `application.properties`:**
```properties
spring.batch.job.enabled=true
```

2. **Reiniciar la aplicación**

### 1. Ejecutar Batch Job Manualmente

**Trigger el job:**
```bash
curl -X POST http://localhost:8080/api/batch/monthly-interest | jq .
```

**Respuesta esperada:**
```json
{
  "message": "Monthly Interest Job triggered successfully",
  "timestamp": "2025-09-30T03:45:23.456",
  "status": "RUNNING"
}
```

**Nota:** Si el batch está deshabilitado (`spring.batch.job.enabled=false`), este endpoint retornará 404.

---

### 2. Verificar Balances Antes y Después del Batch

**Obtener balance de una cuenta ANTES del batch (usa ID, no número de cuenta):**
```bash
ACCOUNT_ID=1
curl http://localhost:8080/api/accounts/$ACCOUNT_ID | jq '{id, accountNumber, accountType, balance}'
```

**Ejecutar batch:**
```bash
curl -X POST http://localhost:8080/api/batch/monthly-interest
```

**Esperar 3 segundos y verificar balance DESPUÉS:**
```bash
sleep 3
curl http://localhost:8080/api/accounts/$ACCOUNT_ID | jq '{id, accountNumber, accountType, balance}'
```

**Ejemplo de resultado:**
```
ANTES:  {"id": 1, "accountNumber": "400045427676", "accountType": "CHECKING", "balance": 150.00}
DESPUÉS: {"id": 1, "accountNumber": "400045427676", "accountType": "CHECKING", "balance": 150.12}
INTERÉS: $0.12 (0.083% mensual)
```

---

### 3. Cálculos de Intereses por Tipo de Cuenta

**CHECKING (1% anual = 0.083% mensual):**
```bash
# Para balance de $10,000.00
# Interés mensual: $10,000.00 × 0.000833333 = $8.33
```

**SAVINGS (5% anual = 0.42% mensual):**
```bash
# Para balance de $10,000.00
# Interés mensual: $10,000.00 × 0.004166667 = $41.67
```

**Fórmula:**
```
Interés Mensual = Balance × (Tasa Anual / 12 / 100)

CHECKING: Balance × 0.000833333
SAVINGS:  Balance × 0.004166667
```

---

### 4. Verificar Logs de Transacciones en MongoDB

**Ver todos los logs de transacciones:**
```bash
docker exec mongodb-container mongosh \
  -u admin -p xideral4321 --authenticationDatabase admin \
  --eval "db = db.getSiblingDB('banco_logs'); db.transactionLogs.find().pretty()"
```

**Contar logs de transacciones:**
```bash
docker exec mongodb-container mongosh \
  -u admin -p xideral4321 --authenticationDatabase admin \
  --eval "db = db.getSiblingDB('banco_logs'); db.transactionLogs.countDocuments()"
```

**Ver últimas transacciones:**
```bash
docker exec mongodb-container mongosh \
  -u admin -p xideral4321 --authenticationDatabase admin \
  --eval "db = db.getSiblingDB('banco_logs'); db.transactionLogs.find().sort({timestamp: -1}).limit(5).pretty()"
```

**Respuesta esperada:**
```json
{
  "_id": ObjectId("66fa8c5b7f4e2a1b3c9d8e7f"),
  "jobExecutionId": 1,
  "jobName": "monthlyInterestJob",
  "status": "COMPLETED",
  "startTime": "2025-09-30T03:45:23.500Z",
  "endTime": "2025-09-30T03:45:23.589Z",
  "duration": 89,
  "totalAccounts": 2,
  "accountsWithInterest": 2,
  "totalInterest": "0.75",
  "errorMessage": null
}
```

---

### 5. Ver Spring Batch Metadata en MySQL

**Verificar tablas de Spring Batch:**
```bash
mysql -u root -pxideral1234 banco_db -e "SHOW TABLES LIKE 'BATCH%';"
```

**Ver ejecuciones de jobs:**
```bash
mysql -u root -pxideral1234 banco_db -e "SELECT * FROM BATCH_JOB_EXECUTION ORDER BY CREATE_TIME DESC LIMIT 5;"
```

**Ver parámetros del job:**
```bash
mysql -u root -pxideral1234 banco_db -e "SELECT * FROM BATCH_JOB_EXECUTION_PARAMS ORDER BY JOB_EXECUTION_ID DESC;"
```

---

### 6. Escenarios de Prueba

#### Escenario 1: Batch con 2 Cuentas

```bash
# 1. Verificar que existen cuentas activas
curl http://localhost:8080/api/accounts | jq '[.[] | select(.active == true) | {accountNumber, accountType, balance}]'

# 2. Ejecutar batch
curl -X POST http://localhost:8080/api/batch/monthly-interest | jq .

# 3. Esperar y verificar
sleep 3
curl http://localhost:8080/api/accounts | jq '[.[] | {accountNumber, accountType, balance}]'
```

#### Escenario 2: Ejecutar Batch Múltiples Veces

```bash
# Primera ejecución
curl -X POST http://localhost:8080/api/batch/monthly-interest | jq .
sleep 3

# Segunda ejecución (aplicará interés sobre nuevo balance)
curl -X POST http://localhost:8080/api/batch/monthly-interest | jq .
sleep 3

# Tercera ejecución (interés compuesto)
curl -X POST http://localhost:8080/api/batch/monthly-interest | jq .
```

**Nota:** Cada ejecución aplica interés sobre el balance actual, creando interés compuesto.

#### Escenario 3: Validar Polimorfismo

```bash
# 1. Crear cuenta CHECKING con $1,000
CHECKING=$(curl -s -X POST http://localhost:8080/api/customers/1/accounts \
  -H "Content-Type: application/json" \
  -d '{"accountType": "CHECKING","balance": 1000.00}')
CHECKING_NUM=$(echo $CHECKING | jq -r '.accountNumber')

# 2. Depositar $1,000 (total: $1,000)
curl -X POST http://localhost:8080/api/accounts/deposit \
  -H "Content-Type: application/json" \
  -d "{\"accountNumber\": \"$CHECKING_NUM\",\"amount\": 1000.00}"

# 3. Crear cuenta SAVINGS con $1,000
SAVINGS=$(curl -s -X POST http://localhost:8080/api/customers/1/accounts \
  -H "Content-Type: application/json" \
  -d '{"accountType": "SAVINGS","balance": 1000.00}')
SAVINGS_NUM=$(echo $SAVINGS | jq -r '.accountNumber')

# 4. Depositar $1,000 (total: $1,000)
curl -X POST http://localhost:8080/api/accounts/deposit \
  -H "Content-Type: application/json" \
  -d "{\"accountNumber\": \"$SAVINGS_NUM\",\"amount\": 1000.00}"

# 5. Ver balances antes
echo "CHECKING antes:"
curl -s http://localhost:8080/api/accounts/$CHECKING_NUM | jq '.balance'
echo "SAVINGS antes:"
curl -s http://localhost:8080/api/accounts/$SAVINGS_NUM | jq '.balance'

# 6. Ejecutar batch
curl -X POST http://localhost:8080/api/batch/monthly-interest
sleep 3

# 7. Ver balances después
echo "CHECKING después (1% anual):"
curl -s http://localhost:8080/api/accounts/$CHECKING_NUM | jq '.balance'
# Esperado: $1000.83 (interés: $0.83)

echo "SAVINGS después (5% anual):"
curl -s http://localhost:8080/api/accounts/$SAVINGS_NUM | jq '.balance'
# Esperado: $1004.17 (interés: $4.17)
```

**Observación:** SAVINGS recibe 5x más interés que CHECKING, demostrando el polimorfismo.

---

## 🔍 Verificación de MongoDB (No aplica Día 5)

### Ver notificaciones directamente en MongoDB
```bash
docker exec -it mongodb-container mongosh \
  -u admin -p xideral4321 --authenticationDatabase admin

# Dentro de mongosh:
use banco_logs
db.notifications.find().pretty()
db.notifications.countDocuments()
db.notifications.find({status: "SENT"}).count()
db.notifications.find({type: "DEPOSIT"}).pretty()

# Ver logs de batch
db.batch_job_executions.find().pretty()
db.batch_job_executions.find({status: "COMPLETED"}).count()
```

---

## 📝 Notas

- Todos los comandos usan `jq` para formatear el JSON. Si no lo tienes instalado:
  ```bash
  brew install jq
  ```
- La aplicación debe estar corriendo en `http://localhost:8080`
- MongoDB debe estar corriendo en `localhost:27017`
- Reemplaza los IDs y números de cuenta con valores reales de tu sistema

---

## 🚀 Ejecución Rápida

Para ejecutar todas las pruebas automáticamente:
```bash
./run-integration-tests.sh
```

---

## 🗄️ Verificación Directa de MongoDB con Docker

### Conectarse a MongoDB mediante Docker

**Conexión interactiva:**
```bash
docker exec -it mongodb-container mongosh \
  -u admin -p xideral4321 --authenticationDatabase admin
```

**Dentro de mongosh:**
```javascript
// Cambiar a la base de datos banco_logs
use banco_logs

// Ver colecciones
show collections

// Contar notificaciones
db.notifications.countDocuments()

// Ver últimas 5 notificaciones
db.notifications.find().sort({createdAt:-1}).limit(5).pretty()

// Ver notificaciones por tipo
db.notifications.find({type: "DEPOSIT"}).pretty()

// Ver notificaciones por estado
db.notifications.find({status: "SENT"}).count()

// Ver estadísticas por tipo
db.notifications.aggregate([
  {$group: {_id: "$type", count: {$sum: 1}}},
  {$sort: {count: -1}}
])

// Ver logs de batch jobs
db.batch_job_executions.find().pretty()

// Ver último batch ejecutado
db.batch_job_executions.find().sort({startTime:-1}).limit(1).pretty()

// Contar ejecuciones de batch
db.batch_job_executions.countDocuments()

// Ver batch jobs completados
db.batch_job_executions.find({status: "COMPLETED"}).pretty()

// Ver batch jobs fallidos
db.batch_job_executions.find({status: "FAILED"}).pretty()

// Salir
exit
```

---

### Comandos Docker No Interactivos

**Ver todas las bases de datos:**
```bash
docker exec mongodb-container mongosh -u admin -p xideral4321 \
  --authenticationDatabase admin \
  --eval "db.adminCommand('listDatabases')"
```

**Contar notificaciones:**
```bash
docker exec mongodb-container mongosh -u admin -p xideral4321 \
  --authenticationDatabase admin \
  --eval "db = db.getSiblingDB('banco_logs'); db.notifications.countDocuments()"
```

**Ver últimas notificaciones:**
```bash
docker exec mongodb-container mongosh -u admin -p xideral4321 \
  --authenticationDatabase admin \
  --eval "db = db.getSiblingDB('banco_logs'); db.notifications.find().sort({createdAt:-1}).limit(3).pretty()"
```

**Estadísticas de notificaciones por tipo:**
```bash
docker exec mongodb-container mongosh -u admin -p xideral4321 \
  --authenticationDatabase admin \
  --eval "db = db.getSiblingDB('banco_logs'); db.notifications.aggregate([{\$group: {_id: '\$type', count: {\$sum: 1}}}, {\$sort: {count: -1}}])"
```

**Ver logs de batch jobs:**
```bash
docker exec mongodb-container mongosh -u admin -p xideral4321 \
  --authenticationDatabase admin \
  --eval "db = db.getSiblingDB('banco_logs'); db.batch_job_executions.find().pretty()"
```

**Ver último batch job ejecutado:**
```bash
docker exec mongodb-container mongosh -u admin -p xideral4321 \
  --authenticationDatabase admin \
  --eval "db = db.getSiblingDB('banco_logs'); db.batch_job_executions.find().sort({startTime:-1}).limit(1).pretty()"
```

**Contar batch jobs:**
```bash
docker exec mongodb-container mongosh -u admin -p xideral4321 \
  --authenticationDatabase admin \
  --eval "db = db.getSiblingDB('banco_logs'); db.batch_job_executions.countDocuments()"
```

**Ver notificaciones de un cliente específico:**
```bash
# Reemplaza 5 con el ID del cliente
docker exec mongodb-container mongosh -u admin -p xideral4321 \
  --authenticationDatabase admin \
  --eval "db = db.getSiblingDB('banco_logs'); db.notifications.find({customerId: 5}).pretty()"
```

**Ver notificaciones de una cuenta específica:**
```bash
# Reemplaza con el número de cuenta real
docker exec mongodb-container mongosh -u admin -p xideral4321 \
  --authenticationDatabase admin \
  --eval "db = db.getSiblingDB('banco_logs'); db.notifications.find({accountNumber: '400084675118'}).pretty()"
```

**Eliminar todas las notificaciones (CUIDADO):**
```bash
docker exec mongodb-container mongosh -u admin -p xideral4321 \
  --authenticationDatabase admin \
  --eval "db = db.getSiblingDB('banco_logs'); db.notifications.deleteMany({})"
```

**Eliminar todos los logs de batch (CUIDADO):**
```bash
docker exec mongodb-container mongosh -u admin -p xideral4321 \
  --authenticationDatabase admin \
  --eval "db = db.getSiblingDB('banco_logs'); db.batch_job_executions.deleteMany({})"
```

---

### Gestión del Contenedor Docker

**Ver estado del contenedor:**
```bash
docker ps --filter "name=mongodb-container"
```

**Detener el contenedor:**
```bash
docker stop mongodb-container
```

**Iniciar el contenedor:**
```bash
docker start mongodb-container
```

**Ver logs del contenedor:**
```bash
docker logs mongodb-container
docker logs -f mongodb-container  # Seguir logs en tiempo real
```

**Reiniciar el contenedor:**
```bash
docker restart mongodb-container
```

**Eliminar el contenedor (CUIDADO - perderás los datos):**
```bash
docker rm -f mongodb-container
```

**Ver estadísticas del contenedor:**
```bash
docker stats mongodb-container
```

---

### Escenario Completo: Validar MongoDB End-to-End

```bash
echo "=== PRUEBA COMPLETA DE MONGODB ==="

# 1. Verificar conexión
echo "1. Verificando conexión a MongoDB..."
docker exec mongodb-container mongosh -u admin -p xideral4321 \
  --authenticationDatabase admin \
  --eval "db.adminCommand('ping')"

# 2. Contar notificaciones antes
BEFORE=$(docker exec mongodb-container mongosh -u admin -p xideral4321 \
  --authenticationDatabase admin --quiet \
  --eval "db = db.getSiblingDB('banco_logs'); db.notifications.countDocuments()")
echo "2. Notificaciones antes: $BEFORE"

# 3. Crear cliente y cuenta
echo "3. Creando cliente y cuenta..."
CUSTOMER=$(curl -s -X POST http://localhost:8080/api/customers \
  -H "Content-Type: application/json" \
  -d '{"name":"MongoDB Test","email":"mongo@test.com","phone":"5555555555"}')
CUSTOMER_ID=$(echo $CUSTOMER | jq -r '.id')

ACCOUNT=$(curl -s -X POST http://localhost:8080/api/accounts \
  -H "Content-Type: application/json" \
  -d "{\"customerId\": $CUSTOMER_ID,\"accountType\": \"CHECKING\"}")
ACCOUNT_NUMBER=$(echo $ACCOUNT | jq -r '.accountNumber')

# 4. Hacer depósito
echo "4. Realizando depósito..."
curl -s -X POST http://localhost:8080/api/accounts/deposit \
  -H "Content-Type: application/json" \
  -d "{\"accountNumber\": \"$ACCOUNT_NUMBER\",\"amount\": 1000.00}" > /dev/null

# 5. Esperar y contar notificaciones después
sleep 2
AFTER=$(docker exec mongodb-container mongosh -u admin -p xideral4321 \
  --authenticationDatabase admin --quiet \
  --eval "db = db.getSiblingDB('banco_logs'); db.notifications.countDocuments()")
echo "5. Notificaciones después: $AFTER"

# 6. Calcular diferencia
DIFF=$((AFTER - BEFORE))
echo "6. Nuevas notificaciones generadas: $DIFF"

# 7. Ver últimas notificaciones
echo "7. Últimas notificaciones:"
docker exec mongodb-container mongosh -u admin -p xideral4321 \
  --authenticationDatabase admin --quiet \
  --eval "db = db.getSiblingDB('banco_logs'); db.notifications.find({customerId: $CUSTOMER_ID}).forEach(n => print('   - ' + n.type + ': ' + n.subject))"

echo "=== PRUEBA COMPLETADA ==="
```

---

**Academia Xideral - FullStack Development Course**
**Proyecto Final - Sistema Bancario Digital**
