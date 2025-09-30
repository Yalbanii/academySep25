# Comandos de Pruebas Manuales
## Sistema Bancario Digital - Días 1 al 4

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

## 🧪 Día 1: Customer Module

### 1. Crear un Cliente
```bash
curl -X POST http://localhost:8080/api/customers \
  -H "Content-Type: application/json" \
  -d '{
    "name": "María García",
    "email": "maria.garcia@example.com",
    "phone": "5559876543"
  }' | jq .
```

**Respuesta esperada:**
```json
{
  "id": 1,
  "name": "María García",
  "email": "maria.garcia@example.com",
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
# Reemplaza {id} con el ID real del cliente
curl http://localhost:8080/api/customers/1 | jq .
```

### 4. Actualizar Cliente
```bash
curl -X PUT http://localhost:8080/api/customers/1 \
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

### 1. Crear Cuenta CHECKING
```bash
curl -X POST http://localhost:8080/api/accounts \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 1,
    "accountType": "CHECKING"
  }' | jq .
```

**Respuesta esperada:**
```json
{
  "id": 1,
  "accountNumber": "400012345678",
  "accountType": "CHECKING",
  "balance": 0,
  "active": true
}
```

**🔔 Verifica la notificación:**
```bash
curl http://localhost:8080/api/notifications/customer/1 | jq '.[-1]'
```

### 2. Crear Cuenta SAVINGS
```bash
curl -X POST http://localhost:8080/api/accounts \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 1,
    "accountType": "SAVINGS"
  }' | jq .
```

### 3. Obtener Todas las Cuentas
```bash
curl http://localhost:8080/api/accounts | jq .
```

### 4. Obtener Cuentas de un Cliente
```bash
curl http://localhost:8080/api/accounts/customer/1 | jq .
```

### 5. Obtener Cuenta por Número
```bash
# Reemplaza con el número de cuenta real
curl http://localhost:8080/api/accounts/400012345678 | jq .
```

### 6. Obtener Cuentas Activas
```bash
curl http://localhost:8080/api/accounts/active | jq .
```

---

## 💰 Día 3: Banking Operations

### 1. Depósito

**Depositar $1000:**
```bash
curl -X POST http://localhost:8080/api/accounts/deposit \
  -H "Content-Type: application/json" \
  -d '{
    "accountNumber": "400012345678",
    "amount": 1000.00
  }' | jq .
```

**Respuesta esperada:**
```json
{
  "accountNumber": "400012345678",
  "balance": 1000.00
}
```

**🔔 Verifica la notificación de depósito:**
```bash
curl http://localhost:8080/api/notifications/type/DEPOSIT | jq .
```

### 2. Retiro

**Retirar $300:**
```bash
curl -X POST http://localhost:8080/api/accounts/withdraw \
  -H "Content-Type: application/json" \
  -d '{
    "accountNumber": "400012345678",
    "amount": 300.00
  }' | jq .
```

**🔔 Verifica la notificación de retiro:**
```bash
curl http://localhost:8080/api/notifications/type/WITHDRAWAL | jq .
```

### 3. Transferencia

**Transferir $250 entre cuentas:**
```bash
curl -X POST http://localhost:8080/api/accounts/transfer \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccountNumber": "400012345678",
    "toAccountNumber": "400087654321",
    "amount": 250.00
  }'
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
  -d '{"name": "Pedro López","email": "pedro@example.com","phone": "5551234567"}')
CUSTOMER_ID=$(echo $CUSTOMER | jq -r '.id')

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
ACCOUNT_NUMBER="400012345678"

# 1. Depósito inicial
curl -X POST http://localhost:8080/api/accounts/deposit \
  -H "Content-Type: application/json" \
  -d "{\"accountNumber\": \"$ACCOUNT_NUMBER\",\"amount\": 1000.00}" | jq .

# 2. Retiro
curl -X POST http://localhost:8080/api/accounts/withdraw \
  -H "Content-Type: application/json" \
  -d "{\"accountNumber\": \"$ACCOUNT_NUMBER\",\"amount\": 200.00}" | jq .

# 3. Verificar balance final
curl http://localhost:8080/api/accounts/$ACCOUNT_NUMBER | jq '{accountNumber, balance}'

# 4. Ver todas las notificaciones generadas
curl http://localhost:8080/api/notifications/account/$ACCOUNT_NUMBER | jq .
```

### Escenario 3: Transferencia entre Cuentas
```bash
ACCOUNT_FROM="400012345678"
ACCOUNT_TO="400087654321"

# 1. Verificar balances iniciales
echo "Balance cuenta origen:"
curl -s http://localhost:8080/api/accounts/$ACCOUNT_FROM | jq '.balance'
echo "Balance cuenta destino:"
curl -s http://localhost:8080/api/accounts/$ACCOUNT_TO | jq '.balance'

# 2. Realizar transferencia
curl -X POST http://localhost:8080/api/accounts/transfer \
  -H "Content-Type: application/json" \
  -d "{
    \"fromAccountNumber\": \"$ACCOUNT_FROM\",
    \"toAccountNumber\": \"$ACCOUNT_TO\",
    \"amount\": 300.00
  }"

# 3. Verificar balances finales
echo "Nuevo balance cuenta origen:"
curl -s http://localhost:8080/api/accounts/$ACCOUNT_FROM | jq '.balance'
echo "Nuevo balance cuenta destino:"
curl -s http://localhost:8080/api/accounts/$ACCOUNT_TO | jq '.balance'

# 4. Ver notificaciones de transferencia
curl http://localhost:8080/api/notifications/type/TRANSFER_SENT | jq '.[-1]'
curl http://localhost:8080/api/notifications/type/TRANSFER_RECEIVED | jq '.[-1]'
```

---

## 🔍 Verificación de MongoDB

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

**Academia Xideral - FullStack Development Course**
**Proyecto Final - Sistema Bancario Digital**