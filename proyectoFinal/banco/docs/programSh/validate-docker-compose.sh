#!/bin/bash

# Validación Completa de Docker Compose Setup
# Sistema Bancario Digital

set -e

echo "=========================================="
echo "🧪 VALIDACIÓN DOCKER COMPOSE"
echo "=========================================="
echo ""

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if docker compose is running
echo "📦 1. Verificando servicios Docker Compose..."
if ! docker compose ps | grep -q "healthy"; then
    echo -e "${RED}❌ Error: Los servicios no están corriendo o no están healthy${NC}"
    echo "Ejecuta: docker compose up -d"
    exit 1
fi
echo -e "${GREEN}✅ Servicios corriendo correctamente${NC}"
echo ""

# Check MySQL
echo "🐬 2. Verificando MySQL..."
MYSQL_VERSION=$(docker exec mysql-container mysql -uroot -pxideral1234 -e "SELECT VERSION();" 2>/dev/null | grep -v "Warning" | tail -1)
echo -e "${GREEN}✅ MySQL versión: $MYSQL_VERSION${NC}"

# Check MySQL tables
TABLES=$(docker exec mysql-container mysql -uroot -pxideral1234 -e "USE banco_db; SHOW TABLES;" 2>/dev/null | grep -v "Warning" | grep -v "Tables_in" | wc -l | xargs)
echo -e "${GREEN}✅ MySQL tiene $TABLES tablas creadas${NC}"
echo ""

# Check MongoDB
echo "🍃 3. Verificando MongoDB..."
MONGO_PING=$(docker exec mongodb-container mongosh -u admin -p xideral4321 --authenticationDatabase admin --quiet --eval "db.adminCommand('ping').ok" 2>/dev/null)
if [ "$MONGO_PING" == "1" ]; then
    echo -e "${GREEN}✅ MongoDB conectado exitosamente${NC}"
else
    echo -e "${RED}❌ Error al conectar a MongoDB${NC}"
    exit 1
fi

MONGO_COLLECTIONS=$(docker exec mongodb-container mongosh -u admin -p xideral4321 --authenticationDatabase admin --quiet --eval "db = db.getSiblingDB('banco_logs'); db.getCollectionNames().join(', ')" 2>/dev/null)
echo -e "${GREEN}✅ MongoDB colecciones: $MONGO_COLLECTIONS${NC}"
echo ""

# Check Spring Boot App
echo "🚀 4. Verificando aplicación Spring Boot..."
if curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
    echo -e "${GREEN}✅ Aplicación corriendo en http://localhost:8080${NC}"
else
    echo -e "${YELLOW}⚠️  Aplicación no está corriendo${NC}"
    echo "   Ejecuta: mvn spring-boot:run"
    APP_RUNNING=false
fi
echo ""

# Integration Test (only if app is running)
if [ "$APP_RUNNING" != "false" ]; then
    echo "🧪 5. Prueba de Integración Completa..."
    echo ""

    # Create Customer
    echo "   📝 Creando cliente de prueba..."
    CUSTOMER_RESPONSE=$(curl -s -X POST http://localhost:8080/api/customers \
      -H "Content-Type: application/json" \
      -d '{
        "name": "Test Docker Compose",
        "email": "test-compose@example.com",
        "phone": "5559999999"
      }')

    CUSTOMER_ID=$(echo "$CUSTOMER_RESPONSE" | jq -r '.id')
    if [ "$CUSTOMER_ID" != "null" ]; then
        echo -e "   ${GREEN}✅ Cliente creado con ID: $CUSTOMER_ID${NC}"
    else
        echo -e "   ${RED}❌ Error al crear cliente${NC}"
        exit 1
    fi

    # Create Account
    echo "   💳 Creando cuenta de prueba..."
    ACCOUNT_RESPONSE=$(curl -s -X POST http://localhost:8080/api/accounts \
      -H "Content-Type: application/json" \
      -d "{\"customerId\": $CUSTOMER_ID, \"accountType\": \"CHECKING\", \"balance\": 1000.00}")

    ACCOUNT_NUMBER=$(echo "$ACCOUNT_RESPONSE" | jq -r '.accountNumber')
    if [ "$ACCOUNT_NUMBER" != "null" ]; then
        echo -e "   ${GREEN}✅ Cuenta creada: $ACCOUNT_NUMBER${NC}"
    else
        echo -e "   ${RED}❌ Error al crear cuenta${NC}"
        exit 1
    fi

    # Deposit
    echo "   💰 Realizando depósito..."
    DEPOSIT_RESPONSE=$(curl -s -X POST http://localhost:8080/api/accounts/deposit \
      -H "Content-Type: application/json" \
      -d "{\"accountNumber\": \"$ACCOUNT_NUMBER\", \"amount\": 500.00}")

    NEW_BALANCE=$(echo "$DEPOSIT_RESPONSE" | jq -r '.balance')
    if [ "$NEW_BALANCE" != "null" ]; then
        echo -e "   ${GREEN}✅ Depósito exitoso. Nuevo balance: \$$NEW_BALANCE${NC}"
    else
        echo -e "   ${RED}❌ Error en depósito${NC}"
        exit 1
    fi

    # Verify Notifications in MongoDB
    echo "   🔔 Verificando notificaciones en MongoDB..."
    sleep 2
    NOTIF_COUNT=$(docker exec mongodb-container mongosh -u admin -p xideral4321 --authenticationDatabase admin --quiet --eval "db = db.getSiblingDB('banco_logs'); db.notifications.countDocuments({customerId: $CUSTOMER_ID})" 2>/dev/null)

    if [ "$NOTIF_COUNT" -ge "3" ]; then
        echo -e "   ${GREEN}✅ $NOTIF_COUNT notificaciones creadas en MongoDB${NC}"
    else
        echo -e "   ${YELLOW}⚠️  Solo $NOTIF_COUNT notificaciones encontradas${NC}"
    fi

    # Verify Transaction Logs in MongoDB
    echo "   📜 Verificando transaction logs en MongoDB..."
    TXLOG_COUNT=$(docker exec mongodb-container mongosh -u admin -p xideral4321 --authenticationDatabase admin --quiet --eval "db = db.getSiblingDB('banco_logs'); db.transactionLogs.countDocuments({accountNumber: '$ACCOUNT_NUMBER'})" 2>/dev/null || echo "0")

    if [ "$TXLOG_COUNT" != "0" ]; then
        echo -e "   ${GREEN}✅ $TXLOG_COUNT transaction logs creados en MongoDB${NC}"
    else
        echo -e "   ${YELLOW}⚠️  No se encontraron transaction logs${NC}"
    fi

    echo ""
fi

# Volume Check
echo "💾 6. Verificando volúmenes persistentes..."
VOLUMES=$(docker volume ls --filter "name=banco" --format "{{.Name}}" | wc -l | xargs)
echo -e "${GREEN}✅ Volúmenes del proyecto: $VOLUMES${NC}"
docker volume ls --filter "name=banco" --format "   - {{.Name}}"
echo ""

# Network Check
echo "🌐 7. Verificando red Docker..."
NETWORK=$(docker network ls --filter "name=banco" --format "{{.Name}}" | head -1)
if [ -n "$NETWORK" ]; then
    echo -e "${GREEN}✅ Red activa: $NETWORK${NC}"
else
    echo -e "${RED}❌ Red no encontrada${NC}"
fi
echo ""

# Summary
echo "=========================================="
echo "📊 RESUMEN DE VALIDACIÓN"
echo "=========================================="
echo ""
docker compose ps
echo ""
echo -e "${GREEN}✅ Docker Compose está funcionando correctamente${NC}"
echo ""
echo "Comandos útiles:"
echo "  docker compose ps          # Ver estado"
echo "  docker compose logs -f     # Ver logs en tiempo real"
echo "  docker compose down        # Detener servicios"
echo "  docker compose up -d       # Iniciar servicios"
echo ""
