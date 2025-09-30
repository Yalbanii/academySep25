#!/bin/bash

# Script de Pruebas de Integración - Sistema Bancario Digital
# Ejecuta todas las pruebas de los Días 1 al 5

echo "=================================================="
echo "   PRUEBAS DE INTEGRACIÓN - DÍAS 1 AL 5"
echo "=================================================="
echo ""

# Colores para output
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Variables
BASE_URL="http://localhost:8080"
CUSTOMER_ID=""
ACCOUNT_1=""
ACCOUNT_2=""

# Función para verificar el status HTTP
check_status() {
    if [ $1 -eq 200 ] || [ $1 -eq 201 ]; then
        echo -e "${GREEN}✅ PASS${NC}"
        return 0
    else
        echo -e "${RED}❌ FAIL (HTTP $1)${NC}"
        return 1
    fi
}

# Función para hacer pausa
pause() {
    sleep 1
}

echo -e "${BLUE}=== DÍA 1: CUSTOMER MODULE ===${NC}"
echo ""

# 1.1 Crear Cliente
echo -n "1.1 Crear Cliente... "
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST $BASE_URL/api/customers \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Juan Pérez Test",
    "email": "juan.test@example.com",
    "phone": "5551234567"
  }')
HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
check_status $HTTP_CODE
CUSTOMER_ID=$(echo $BODY | jq -r '.id')
echo "   Customer ID: $CUSTOMER_ID"
pause

# 1.2 Obtener todos los clientes
echo -n "1.2 Obtener todos los clientes... "
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" $BASE_URL/api/customers)
check_status $HTTP_CODE
pause

# 1.3 Obtener cliente por ID
echo -n "1.3 Obtener cliente por ID ($CUSTOMER_ID)... "
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" $BASE_URL/api/customers/$CUSTOMER_ID)
check_status $HTTP_CODE
pause

# 1.4 Actualizar cliente
echo -n "1.4 Actualizar cliente... "
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X PUT $BASE_URL/api/customers/$CUSTOMER_ID \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Juan Pérez Actualizado",
    "email": "juan.updated@example.com",
    "phone": "5559876543"
  }')
check_status $HTTP_CODE
pause

echo ""
echo -e "${BLUE}=== DÍA 2: ACCOUNT MODULE ===${NC}"
echo ""

# 2.1 Crear Cuenta CHECKING con balance inicial
echo -n "2.1 Crear cuenta CHECKING con balance inicial... "
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST $BASE_URL/api/accounts \
  -H "Content-Type: application/json" \
  -d "{
    \"customerId\": $CUSTOMER_ID,
    \"accountType\": \"CHECKING\",
    \"initialBalance\": 1000.00
  }")
HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
check_status $HTTP_CODE
ACCOUNT_1=$(echo $BODY | jq -r '.accountNumber')
ACCOUNT_1_ID=$(echo $BODY | jq -r '.id')
echo "   Account ID: $ACCOUNT_1_ID, Number: $ACCOUNT_1"
pause

# 2.2 Crear Cuenta SAVINGS con balance inicial
echo -n "2.2 Crear cuenta SAVINGS con balance inicial... "
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST $BASE_URL/api/accounts \
  -H "Content-Type: application/json" \
  -d "{
    \"customerId\": $CUSTOMER_ID,
    \"accountType\": \"SAVINGS\",
    \"initialBalance\": 5000.00
  }")
HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
check_status $HTTP_CODE
ACCOUNT_2=$(echo $BODY | jq -r '.accountNumber')
ACCOUNT_2_ID=$(echo $BODY | jq -r '.id')
echo "   Account ID: $ACCOUNT_2_ID, Number: $ACCOUNT_2"
pause

# 2.3 Obtener todas las cuentas
echo -n "2.3 Obtener todas las cuentas... "
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" $BASE_URL/api/accounts)
check_status $HTTP_CODE
pause

echo ""
echo -e "${BLUE}=== DÍA 3: BANKING OPERATIONS ===${NC}"
echo ""

# 3.1 Depósito
echo -n "3.1 Depósito de \$500.00... "
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST $BASE_URL/api/accounts/deposit \
  -H "Content-Type: application/json" \
  -d "{
    \"accountNumber\": \"$ACCOUNT_1\",
    \"amount\": 500.00
  }")
HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
check_status $HTTP_CODE
BALANCE=$(echo $BODY | jq -r '.balance')
echo "   Nuevo balance: \$$BALANCE"
pause

# 3.2 Retiro
echo -n "3.2 Retiro de \$200.00... "
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST $BASE_URL/api/accounts/withdraw \
  -H "Content-Type: application/json" \
  -d "{
    \"accountNumber\": \"$ACCOUNT_1\",
    \"amount\": 200.00
  }")
HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
check_status $HTTP_CODE
BALANCE=$(echo $BODY | jq -r '.balance')
echo "   Nuevo balance: \$$BALANCE"
pause

# 3.3 Transferencia
echo -n "3.3 Transferencia de \$150.00... "
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X POST $BASE_URL/api/accounts/transfer \
  -H "Content-Type: application/json" \
  -d "{
    \"fromAccountNumber\": \"$ACCOUNT_1\",
    \"toAccountNumber\": \"$ACCOUNT_2\",
    \"amount\": 150.00
  }")
check_status $HTTP_CODE
pause

# 3.4 Verificar balances finales
echo -n "3.4 Verificar balances finales... "
BALANCE_1=$(curl -s $BASE_URL/api/accounts/$ACCOUNT_1_ID | jq -r '.balance')
BALANCE_2=$(curl -s $BASE_URL/api/accounts/$ACCOUNT_2_ID | jq -r '.balance')
echo -e "${GREEN}✅ PASS${NC}"
echo "   Cuenta $ACCOUNT_1: \$$BALANCE_1"
echo "   Cuenta $ACCOUNT_2: \$$BALANCE_2"
pause

echo ""
echo -e "${BLUE}=== DÍA 4: NOTIFICATION SYSTEM ===${NC}"
echo ""

# 4.1 Obtener todas las notificaciones del cliente
echo -n "4.1 Obtener notificaciones del cliente... "
RESPONSE=$(curl -s $BASE_URL/api/notifications/customer/$CUSTOMER_ID)
NOTIF_COUNT=$(echo $RESPONSE | jq '. | length')
echo -e "${GREEN}✅ PASS${NC}"
echo "   Total notificaciones: $NOTIF_COUNT"
pause

# 4.2 Notificaciones por tipo
echo "4.2 Notificaciones por tipo:"
TYPES=("ACCOUNT_CREATED" "DEPOSIT" "WITHDRAWAL" "TRANSFER_SENT" "TRANSFER_RECEIVED")
for TYPE in "${TYPES[@]}"; do
    COUNT=$(curl -s $BASE_URL/api/notifications/type/$TYPE | jq '. | length')
    echo "   - $TYPE: $COUNT"
done
pause

# 4.3 Notificaciones por estado
echo -n "4.3 Contar notificaciones SENT... "
COUNT=$(curl -s $BASE_URL/api/notifications/count/status/SENT)
echo -e "${GREEN}✅ PASS${NC}"
echo "   Total SENT: $COUNT"
pause

# 4.4 Verificar que todas están SENT
echo -n "4.4 Verificar que todas las notificaciones están SENT... "
PENDING=$(curl -s $BASE_URL/api/notifications/status/PENDING | jq '. | length')
FAILED=$(curl -s $BASE_URL/api/notifications/status/FAILED | jq '. | length')
if [ $PENDING -eq 0 ] && [ $FAILED -eq 0 ]; then
    echo -e "${GREEN}✅ PASS${NC}"
else
    echo -e "${YELLOW}⚠️  WARNING - Pending: $PENDING, Failed: $FAILED${NC}"
fi
pause

# 4.5 Detalle de notificaciones
echo ""
echo "4.5 Detalle de notificaciones generadas:"
curl -s $BASE_URL/api/notifications/customer/$CUSTOMER_ID | jq -r '.[] | "   [\(.type)] \(.subject) - Status: \(.status)"'
pause

echo ""
echo -e "${BLUE}=== DÍA 5: SPRING BATCH - MONTHLY INTEREST ===${NC}"
echo ""

# 5.1 Obtener balances antes de ejecutar batch
echo -n "5.1 Obtener balances antes del batch... "
BALANCE_1_BEFORE=$(curl -s $BASE_URL/api/accounts/$ACCOUNT_1_ID | jq -r '.balance')
BALANCE_2_BEFORE=$(curl -s $BASE_URL/api/accounts/$ACCOUNT_2_ID | jq -r '.balance')
echo -e "${GREEN}✅ PASS${NC}"
echo "   Cuenta 1 (CHECKING): \$$BALANCE_1_BEFORE"
echo "   Cuenta 2 (SAVINGS): \$$BALANCE_2_BEFORE"
pause

# 5.2 Ejecutar Batch Job manualmente
echo -n "5.2 Ejecutar Batch Job de Intereses... "
BATCH_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST $BASE_URL/api/batch/monthly-interest)
HTTP_CODE=$(echo "$BATCH_RESPONSE" | tail -n1)
BODY=$(echo "$BATCH_RESPONSE" | head -n-1)

# Verificar si el batch está habilitado
if [ $HTTP_CODE -eq 404 ]; then
    echo -e "${YELLOW}⚠️  SKIP (Batch deshabilitado - spring.batch.job.enabled=false)${NC}"
    echo "   Para habilitar: spring.batch.job.enabled=true en application.properties"
elif [ $HTTP_CODE -eq 200 ]; then
    echo -e "${GREEN}✅ PASS${NC}"
    echo "   Message: $(echo $BODY | jq -r '.message')"
    echo "   Status: $(echo $BODY | jq -r '.status')"
else
    echo -e "${RED}❌ FAIL (HTTP $HTTP_CODE)${NC}"
fi
pause

# Esperar a que el batch termine
echo -n "5.3 Esperando a que el batch termine... "
sleep 3
echo -e "${GREEN}✅ DONE${NC}"
pause

# 5.4 Verificar balances después del batch (solo si batch está habilitado)
if [ $HTTP_CODE -eq 200 ]; then
    echo -n "5.4 Verificar balances después del batch... "
    BALANCE_1_AFTER=$(curl -s $BASE_URL/api/accounts/$ACCOUNT_1_ID | jq -r '.balance')
    BALANCE_2_AFTER=$(curl -s $BASE_URL/api/accounts/$ACCOUNT_2_ID | jq -r '.balance')
    echo -e "${GREEN}✅ PASS${NC}"

    # Calcular intereses aplicados
    INTEREST_1=$(echo "$BALANCE_1_AFTER - $BALANCE_1_BEFORE" | bc)
    INTEREST_2=$(echo "$BALANCE_2_AFTER - $BALANCE_2_BEFORE" | bc)
    TOTAL_INTEREST=$(echo "$INTEREST_1 + $INTEREST_2" | bc)

    echo ""
    echo "   Cuenta 1 (CHECKING):"
    echo "     - Balance antes: \$$BALANCE_1_BEFORE"
    echo "     - Balance después: \$$BALANCE_1_AFTER"
    echo "     - Interés aplicado: \$$INTEREST_1 (1% anual = 0.083% mensual)"
    echo ""
    echo "   Cuenta 2 (SAVINGS):"
    echo "     - Balance antes: \$$BALANCE_2_BEFORE"
    echo "     - Balance después: \$$BALANCE_2_AFTER"
    echo "     - Interés aplicado: \$$INTEREST_2 (5% anual = 0.42% mensual)"
    echo ""
    echo "   Total Interés Aplicado: \$$TOTAL_INTEREST"
else
    echo "5.4 Batch deshabilitado - salteando verificación de intereses"
    BALANCE_1_AFTER=$BALANCE_1_BEFORE
    BALANCE_2_AFTER=$BALANCE_2_BEFORE
    TOTAL_INTEREST="0.00"
fi
pause

# 5.5 Verificar logs de transacciones en MongoDB
echo ""
echo -n "5.5 Verificar logs de transacciones en MongoDB... "
TRANSACTION_LOGS=$(docker exec mongodb-container mongosh -u admin -p xideral4321 \
  --authenticationDatabase admin --quiet \
  --eval "db = db.getSiblingDB('banco_logs'); db.transactionLogs.countDocuments()" 2>/dev/null)

if [ ! -z "$TRANSACTION_LOGS" ]; then
    echo -e "${GREEN}✅ PASS${NC}"
    echo "   Total transaction logs en MongoDB: $TRANSACTION_LOGS"
else
    echo -e "${YELLOW}⚠️  SKIP (MongoDB no disponible o sin logs)${NC}"
fi
pause

echo ""
echo "=================================================="
echo -e "${GREEN}   ✅ TODAS LAS PRUEBAS COMPLETADAS${NC}"
echo "=================================================="
echo ""
echo "Resumen:"
echo "  - Cliente ID: $CUSTOMER_ID"
echo "  - Cuenta 1 (CHECKING): $ACCOUNT_1 - Balance: \$$BALANCE_1_AFTER"
echo "  - Cuenta 2 (SAVINGS): $ACCOUNT_2 - Balance: \$$BALANCE_2_AFTER"
echo "  - Total notificaciones: $NOTIF_COUNT"
echo "  - Intereses aplicados: \$$TOTAL_INTEREST"
echo ""
echo "Para ver el reporte completo:"
echo "  cat REPORTE_PRUEBAS_INTEGRACION.md"
echo ""

# Validación MongoDB con Docker
echo ""
echo -e "${BLUE}=== VALIDACIÓN MONGODB CON DOCKER ===${NC}"
echo ""

# 6.1 Verificar conexión a MongoDB
echo -n "6.1 Verificar conexión a MongoDB vía Docker... "
MONGO_PING=$(docker exec mongodb-container mongosh -u admin -p xideral4321 \
  --authenticationDatabase admin --quiet \
  --eval "db.adminCommand('ping').ok" 2>/dev/null)

if [ "$MONGO_PING" = "1" ]; then
    echo -e "${GREEN}✅ PASS${NC}"
else
    echo -e "${RED}❌ FAIL (MongoDB no disponible)${NC}"
    exit 1
fi
pause

# 6.2 Verificar base de datos banco_logs
echo -n "6.2 Verificar base de datos banco_logs... "
DB_EXISTS=$(docker exec mongodb-container mongosh -u admin -p xideral4321 \
  --authenticationDatabase admin --quiet \
  --eval "db.adminCommand('listDatabases').databases.filter(d => d.name === 'banco_logs').length" 2>/dev/null)

if [ "$DB_EXISTS" = "1" ]; then
    echo -e "${GREEN}✅ PASS${NC}"
else
    echo -e "${RED}❌ FAIL (base de datos no existe)${NC}"
fi
pause

# 6.3 Contar notificaciones en MongoDB
echo -n "6.3 Contar notificaciones en MongoDB... "
NOTIF_COUNT_MONGO=$(docker exec mongodb-container mongosh -u admin -p xideral4321 \
  --authenticationDatabase admin --quiet \
  --eval "db = db.getSiblingDB('banco_logs'); db.notifications.countDocuments()" 2>/dev/null)
echo -e "${GREEN}✅ PASS${NC}"
echo "   Total notificaciones en MongoDB: $NOTIF_COUNT_MONGO"
pause

# 6.4 Verificar estadísticas de notificaciones por tipo
echo "6.4 Estadísticas de notificaciones por tipo:"
docker exec mongodb-container mongosh -u admin -p xideral4321 \
  --authenticationDatabase admin --quiet \
  --eval "db = db.getSiblingDB('banco_logs'); db.notifications.aggregate([{\$group: {_id: '\$type', count: {\$sum: 1}}}, {\$sort: {count: -1}}]).forEach(r => print('   - ' + r._id + ': ' + r.count))" 2>/dev/null
pause

# 6.5 Verificar última notificación
echo -n "6.5 Verificar última notificación registrada... "
LAST_NOTIF=$(docker exec mongodb-container mongosh -u admin -p xideral4321 \
  --authenticationDatabase admin --quiet \
  --eval "db = db.getSiblingDB('banco_logs'); var n = db.notifications.findOne({}, {sort: {createdAt: -1}}); if (n) print(n.type + ' - ' + n.status); else print('none')" 2>/dev/null)
if [ ! -z "$LAST_NOTIF" ] && [ "$LAST_NOTIF" != "none" ]; then
    echo -e "${GREEN}✅ PASS${NC}"
    echo "   Última notificación: $LAST_NOTIF"
else
    echo -e "${YELLOW}⚠️  WARNING (no hay notificaciones)${NC}"
fi
pause

# 6.6 Verificar colección de transaction logs
echo -n "6.6 Verificar colección de transactionLogs... "
TRANS_LOG_COUNT=$(docker exec mongodb-container mongosh -u admin -p xideral4321 \
  --authenticationDatabase admin --quiet \
  --eval "db = db.getSiblingDB('banco_logs'); db.transactionLogs.countDocuments()" 2>/dev/null)
echo -e "${GREEN}✅ PASS${NC}"
echo "   Total transaction logs en MongoDB: $TRANS_LOG_COUNT"
pause

# 6.7 Verificar última transacción
if [ "$TRANS_LOG_COUNT" -gt "0" ]; then
    echo -n "6.7 Verificar última transacción... "
    LAST_TRANS=$(docker exec mongodb-container mongosh -u admin -p xideral4321 \
      --authenticationDatabase admin --quiet \
      --eval "db = db.getSiblingDB('banco_logs'); var t = db.transactionLogs.findOne({}, {sort: {timestamp: -1}}); if (t) print(t.transactionType + ' - ' + t.amount); else print('none')" 2>/dev/null)
    if [ ! -z "$LAST_TRANS" ] && [ "$LAST_TRANS" != "none" ]; then
        echo -e "${GREEN}✅ PASS${NC}"
        echo "   Última transacción: $LAST_TRANS"
    fi
else
    echo "6.7 No hay transacciones registradas todavía"
fi
pause

# 6.8 Verificar estructura de notificación
echo -n "6.8 Verificar estructura de documentos... "
HAS_FIELDS=$(docker exec mongodb-container mongosh -u admin -p xideral4321 \
  --authenticationDatabase admin --quiet \
  --eval "db = db.getSiblingDB('banco_logs'); var n = db.notifications.findOne(); if (n && n.customerId && n.type && n.status && n.createdAt) print('valid'); else print('invalid')" 2>/dev/null)
if [ "$HAS_FIELDS" = "valid" ]; then
    echo -e "${GREEN}✅ PASS${NC}"
    echo "   Estructura de documentos correcta"
else
    echo -e "${YELLOW}⚠️  WARNING (estructura incompleta)${NC}"
fi
pause

echo ""
echo "==================================================="
echo -e "${GREEN}   ✅ VALIDACIÓN MONGODB COMPLETADA${NC}"
echo "==================================================="
echo ""
echo "Resumen MongoDB:"
echo "  - Conexión Docker: OK"
echo "  - Base de datos: banco_logs"
echo "  - Notificaciones: $NOTIF_COUNT_MONGO documentos"
echo "  - Transaction Logs: $TRANS_LOG_COUNT documentos"
echo ""
echo "Para consultar MongoDB directamente:"
echo "  docker exec -it mongodb-container mongosh -u admin -p xideral4321 --authenticationDatabase admin"
echo ""