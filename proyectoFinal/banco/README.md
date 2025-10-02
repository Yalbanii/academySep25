# Sistema Bancario Digital

Proyecto Final - Academia Xideral FullStack

## Descripción

Sistema bancario digital desarrollado con Spring Boot que permite la gestión de clientes, cuentas bancarias, transacciones y notificaciones. Incluye procesamiento batch para el cálculo automático de intereses mensuales.

## Tecnologías

- **Java 17**
- **Spring Boot 3.5.6**
- **MySQL** - Base de datos transaccional
- **MongoDB** - Logs y notificaciones
- **Spring Batch** - Procesamiento de intereses
- **Spring Modulith** - Arquitectura modular event-driven
- **Docker & Docker Compose** - Containerización
- **Maven** - Gestión de dependencias
- **Swagger/OpenAPI** - Documentación de API

## Requisitos Previos

- Java 17 o superior
- Maven 3.6+
- Docker Desktop
- IDE (IntelliJ IDEA, Eclipse, VS Code)

## Instalación

### 1. Clonar el repositorio

```bash
git clone <repository-url>
cd banco
```

### 2. Levantar las bases de datos con Docker Compose

**Asegúrate de que Docker Desktop esté corriendo**

```bash
# Levantar MySQL y MongoDB
docker compose up -d

# Verificar que los contenedores estén corriendo
docker compose ps

# Ver logs (opcional)
docker compose logs -f
```

### 3. Configuración (Opcional)

Las credenciales por defecto están en `docker-compose.yml`. Si deseas personalizarlas:

```bash
# Copiar archivo de ejemplo
cp .env.example .env

# Editar .env con tus credenciales
nano .env
```

### 4. Compilar el proyecto

```bash
mvn clean install
```

### 5. Ejecutar la aplicación

```bash
mvn spring-boot:run
```

O desde tu IDE: Ejecutar `BancoApplication.java`

## Acceso a la Aplicación

- **API REST**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/api-docs

## Estructura de Módulos

```
com.xideral.banco
├── customer/          # Gestión de clientes
├── account/           # Gestión de cuentas y transacciones
├── notification/      # Notificaciones (MongoDB)
├── batch/             # Procesamiento batch de intereses
├── events/            # Eventos del sistema
└── config/            # Configuraciones globales
```

## Endpoints Principales

### Clientes
- `POST /api/customers` - Crear cliente
- `GET /api/customers` - Listar clientes
- `GET /api/customers/{id}` - Obtener cliente
- `PUT /api/customers/{id}` - Actualizar cliente
- `DELETE /api/customers/{id}` - Desactivar cliente

### Cuentas
- `POST /api/accounts` - Crear cuenta
- `GET /api/accounts` - Listar cuentas
- `GET /api/accounts/{id}` - Obtener cuenta
- `POST /api/accounts/deposit` - Realizar depósito
- `POST /api/accounts/withdraw` - Realizar retiro
- `POST /api/accounts/transfer` - Realizar transferencia

### Batch Jobs
- `POST /api/batch/monthly-interest` - Ejecutar cálculo de intereses

### Notificaciones (MongoDB)
- `GET /api/notifications` - Listar notificaciones
- `GET /api/notifications/customer/{customerId}` - Notificaciones por cliente

### Transaction Logs (MongoDB)
- `GET /api/transaction-logs` - Listar logs
- `GET /api/transaction-logs/account/{accountNumber}` - Logs por cuenta

## Base de Datos

### MySQL (Puerto 3306)
- **Base de datos**: `banco_db` (se crea automáticamente)
- **Usuario**: `root`
- **Contraseña**: `xideral1234`

**Tablas:**
- `customers` - Información de clientes
- `accounts` - Cuentas bancarias
- `batch_*` - Tablas de Spring Batch

### MongoDB (Puerto 27017)
- **Base de datos**: `banco_logs`
- **Usuario**: `admin`
- **Contraseña**: `xideral4321`

**Colecciones:**
- `notifications` - Notificaciones enviadas
- `transaction_logs` - Logs de transacciones
- `batch_job_execution_log` - Logs de ejecución de jobs

## Testing

```bash
# Ejecutar todos los tests
mvn test

# Ejecutar tests con reporte de cobertura
mvn clean test jacoco:report

# Ver reporte de cobertura
open target/site/jacoco/index.html
```

## Docker Commands

```bash
# Levantar servicios
docker-compose up -d

# Detener servicios
docker-compose down

# Detener y eliminar volúmenes (¡CUIDADO! Borra datos)
docker-compose down -v

# Ver logs
docker-compose logs -f

# Ver solo logs de MySQL
docker-compose logs -f mysql

# Ver solo logs de MongoDB
docker-compose logs -f mongodb

# Reiniciar un servicio
docker-compose restart mysql

# Verificar estado
docker-compose ps
```

## Acceso Directo a las Bases de Datos

### MySQL
```bash
# Desde terminal
docker exec -it mysql-container mysql -uroot -pxideral1234

# O usando cliente MySQL local
mysql -h 127.0.0.1 -P 3306 -uroot -pxideral1234
```

### MongoDB
```bash
# Desde terminal
docker exec -it mongodb-container mongosh -u admin -p xideral4321 --authenticationDatabase admin

# O usando MongoDB Compass
# Connection String: mongodb://admin:xideral4321@localhost:27017/?authSource=admin
```

## Características Destacadas

### Polimorfismo
- **Calculadoras de Interés**: Factory Pattern para diferentes tipos de cuenta
- **Balance Mínimo**: Estrategia según tipo de cuenta (CHECKING: $0, SAVINGS: $100)

### Event-Driven Architecture
- Uso de Spring Modulith Events
- Comunicación desacoplada entre módulos
- Event listeners para notificaciones y logs

### Spring Batch
- Job mensual de cálculo de intereses
- Procesamiento por chunks (10 cuentas)
- Listeners para auditoría en MongoDB

## Troubleshooting

### Error: "Connection refused" al iniciar la app
**Solución**: Verifica que Docker Desktop esté corriendo y los contenedores estén activos:
```bash
docker-compose ps
```

### Error: "Port 3306 already in use"
**Solución**: Ya tienes MySQL corriendo localmente. Detén el servicio o usa otro puerto en `docker-compose.yml`

### Los datos se pierden al reiniciar
**Solución**: Los datos se persisten en volúmenes Docker. Solo se pierden con `docker-compose down -v`

### No puedo conectar a MongoDB
**Solución**: Verifica las credenciales y que el contenedor esté healthy:
```bash
docker-compose ps
docker-compose logs mongodb
```

## Autores

Academia Xideral - Generación Septiembre 2025

## Licencia

Proyecto educativo - Academia Xideral
