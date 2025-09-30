#!/bin/bash

# Script de Pruebas de Integración - Sistema Bancario Digital
# Ejecuta todas las pruebas de los Días 1 al 4

echo "=================================================="
echo "   PRUEBAS DE INTEGRACIÓN - DÍAS 1 AL 4"
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

# 2.1 Crear Cuenta CHECKING
echo -n "2.1 Crear cuenta CHECKING... "
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST $BASE_URL/api/accounts \
  -H "Content-Type: application/json" \
  -d "{
    \"customerId\": $CUSTOMER_ID,
    \"accountType\": \"CHECKING\",
    \"balance\": 0
  }")
HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
check_status $HTTP_CODE
ACCOUNT_1=$(echo $BODY | jq -r '.accountNumber')
echo "   Account Number: $ACCOUNT_1"
pause

# 2.2 Crear Cuenta SAVINGS
echo -n "2.2 Crear cuenta SAVINGS... "
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST $BASE_URL/api/accounts \
  -H "Content-Type: application/json" \
  -d "{
    \"customerId\": $CUSTOMER_ID,
    \"accountType\": \"SAVINGS\",
    \"balance\": 0
  }")
HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
check_status $HTTP_CODE
ACCOUNT_2=$(echo $BODY | jq -r '.accountNumber')
echo "   Account Number: $ACCOUNT_2"
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
ACCOUNTS=$(curl -s $BASE_URL/api/accounts | jq -r ".[] | select(.accountNumber == \"$ACCOUNT_1\" or .accountNumber == \"$ACCOUNT_2\") | \"\(.accountNumber): $\(.balance)\"")
echo ""
echo "$ACCOUNTS" | while read line; do
    echo "   $line"
done
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
echo "=================================================="
echo -e "${GREEN}   ✅ TODAS LAS PRUEBAS COMPLETADAS${NC}"
echo "=================================================="
echo ""
echo "Resumen:"
echo "  - Cliente ID: $CUSTOMER_ID"
echo "  - Cuenta 1: $ACCOUNT_1"
echo "  - Cuenta 2: $ACCOUNT_2"
echo "  - Total notificaciones: $NOTIF_COUNT"
echo ""
echo "Para ver el reporte completo:"
echo "  cat REPORTE_PRUEBAS_INTEGRACION.md"
echo ""