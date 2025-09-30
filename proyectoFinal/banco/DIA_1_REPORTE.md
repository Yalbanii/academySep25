# Día 1: Setup y Configuración Completa
## Sistema Bancario Digital - Academia Xideral FullStack

---

## 📋 Objetivo del Día 1

Establecer la base sólida del proyecto configurando:
- Proyecto Maven con Spring Boot 3.x
- Conexiones a MySQL y MongoDB (bases de datos híbridas)
- Estructura modular del proyecto
- Entidades base (JPA y MongoDB)
- Verificación de conectividad

---

## 🎯 Resultados Esperados

Al finalizar este día, tendrás:
- ✅ Proyecto Maven compilando correctamente
- ✅ Conexión funcional a MySQL
- ✅ Conexión funcional a MongoDB
- ✅ Estructura de packages organizada
- ✅ Entidades base creadas y mapeadas
- ✅ Aplicación Spring Boot iniciando sin errores

---

## 📚 Prerequisitos

### Software Requerido
- **Java 17** o superior
- **Maven 3.6+**
- **Docker** (para MySQL y MongoDB)
- **IDE** (IntelliJ IDEA, Eclipse, o VS Code)

### Contenedores Docker

#### 1. MySQL Container
```bash
docker run --name mysql-container \
  -e MYSQL_ROOT_PASSWORD=xideral1234 \
  -p 3306:3306 \
  -d mysql:latest
```

**Verificar que está corriendo:**
```bash
docker ps | grep mysql
```

#### 2. MongoDB Container
```bash
docker run --name mongodb-container \
  -e MONGO_INITDB_ROOT_USERNAME=admin \
  -e MONGO_INITDB_ROOT_PASSWORD=xideral4321 \
  -p 27017:27017 \
  -d mongo:8
```

**Verificar que está corriendo:**
```bash
docker ps | grep mongo
```

---

## 🚀 Paso a Paso - Día 1

### Paso 1: Crear Proyecto Spring Boot

#### Opción A: Spring Initializr (Web)
1. Ir a [https://start.spring.io/](https://start.spring.io/)
2. Configurar:
   - **Project:** Maven
   - **Language:** Java
   - **Spring Boot:** 3.5.6 (o la versión estable más reciente)
   - **Group:** `com.xideral`
   - **Artifact:** `banco`
   - **Name:** `Sistema Bancario Digital`
   - **Package name:** `com.xideral.banco`
   - **Packaging:** Jar
   - **Java:** 17

3. **Agregar Dependencias:**
   - Spring Web
   - Spring Data JPA
   - Spring Data MongoDB
   - Spring Batch
   - Validation
   - MySQL Driver
   - Lombok
   - Spring Boot DevTools

4. Generar y descargar el proyecto
5. Descomprimir en tu directorio de trabajo

#### Opción B: Maven Manual
Crear archivo `pom.xml` (ver configuración completa abajo)

---

### Paso 2: Configurar pom.xml

Asegúrate de tener estas dependencias en tu `pom.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
	https://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>

	<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
		<version>3.5.6</version>
		<relativePath />
	</parent>

	<groupId>com.xideral</groupId>
	<artifactId>banco</artifactId>
	<version>1.0.0</version>
	<name>Sistema Bancario Digital</name>
	<description>Proyecto Final Academia Xideral FullStack</description>

	<properties>
		<java.version>17</java.version>
	</properties>

	<dependencies>
		<!-- Spring Boot Starter Web -->
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-web</artifactId>
		</dependency>

		<!-- Spring Boot Starter Data JPA -->
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-data-jpa</artifactId>
		</dependency>

		<!-- Spring Boot Starter Data MongoDB -->
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-data-mongodb</artifactId>
		</dependency>

		<!-- Spring Boot Starter Validation -->
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-validation</artifactId>
		</dependency>

		<!-- Spring Boot Starter Batch -->
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-batch</artifactId>
		</dependency>

		<!-- MySQL Connector -->
		<dependency>
			<groupId>com.mysql</groupId>
			<artifactId>mysql-connector-j</artifactId>
			<scope>runtime</scope>
		</dependency>

		<!-- Lombok -->
		<dependency>
			<groupId>org.projectlombok</groupId>
			<artifactId>lombok</artifactId>
			<optional>true</optional>
		</dependency>

		<!-- SpringDoc OpenAPI (Swagger) -->
		<dependency>
			<groupId>org.springdoc</groupId>
			<artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
			<version>2.3.0</version>
		</dependency>

		<!-- Spring Boot Starter Test -->
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-test</artifactId>
			<scope>test</scope>
		</dependency>

		<!-- Spring Batch Test -->
		<dependency>
			<groupId>org.springframework.batch</groupId>
			<artifactId>spring-batch-test</artifactId>
			<scope>test</scope>
		</dependency>

		<!-- H2 Database (para tests) -->
		<dependency>
			<groupId>com.h2database</groupId>
			<artifactId>h2</artifactId>
			<scope>test</scope>
		</dependency>

		<!-- Spring Boot DevTools -->
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-devtools</artifactId>
			<scope>runtime</scope>
			<optional>true</optional>
		</dependency>
	</dependencies>

	<build>
		<plugins>
			<!-- Maven Compiler Plugin -->
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-compiler-plugin</artifactId>
				<configuration>
					<annotationProcessorPaths>
						<path>
							<groupId>org.projectlombok</groupId>
							<artifactId>lombok</artifactId>
						</path>
					</annotationProcessorPaths>
				</configuration>
			</plugin>

			<!-- Spring Boot Maven Plugin -->
			<plugin>
				<groupId>org.springframework.boot</groupId>
				<artifactId>spring-boot-maven-plugin</artifactId>
				<configuration>
					<excludes>
						<exclude>
							<groupId>org.projectlombok</groupId>
							<artifactId>lombok</artifactId>
						</exclude>
					</excludes>
				</configuration>
			</plugin>

			<!-- JaCoCo Maven Plugin (Code Coverage) -->
			<plugin>
				<groupId>org.jacoco</groupId>
				<artifactId>jacoco-maven-plugin</artifactId>
				<version>0.8.12</version>
				<executions>
					<execution>
						<id>jacoco-prepare</id>
						<goals>
							<goal>prepare-agent</goal>
						</goals>
					</execution>
					<execution>
						<id>jacoco-report</id>
						<phase>test</phase>
						<goals>
							<goal>report</goal>
						</goals>
					</execution>
				</executions>
			</plugin>
		</plugins>
	</build>
</project>
```

**Compilar y verificar dependencias:**
```bash
mvn clean compile
```

---

### Paso 3: Configurar application.properties

Crear/editar el archivo `src/main/resources/application.properties`:

```properties
# Application Name
spring.application.name=Sistema Bancario Digital

# Server Configuration
server.port=8080

# Bean Override Configuration
spring.main.allow-bean-definition-overriding=true

# MySQL Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/banco_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=xideral1234
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
spring.jpa.properties.hibernate.format_sql=true

# MongoDB Configuration
spring.data.mongodb.uri=mongodb://admin:xideral4321@localhost:27017/banco_logs?authSource=admin
spring.data.mongodb.database=banco_logs

# Logging Configuration
logging.level.com.xideral.banco=DEBUG
logging.level.org.springframework.data.mongodb=DEBUG
logging.level.org.hibernate.SQL=DEBUG

# Swagger/OpenAPI Configuration
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.enabled=true

# Batch Configuration
spring.batch.jdbc.initialize-schema=always
spring.batch.job.enabled=false
```

#### 📝 Notas sobre la Configuración:

**MySQL:**
- `createDatabaseIfNotExist=true` - Crea la base de datos automáticamente
- `allowPublicKeyRetrieval=true` - Necesario para autenticación con MySQL 8+
- `spring.jpa.hibernate.ddl-auto=update` - Crea/actualiza tablas automáticamente

**MongoDB:**
- `authSource=admin` - Requerido cuando usas usuario root
- URI incluye credenciales: `mongodb://usuario:password@host:puerto/database`

**Batch:**
- `spring.batch.job.enabled=false` - Deshabilitado por ahora (se activará en Día 5)

---

### Paso 4: Crear Estructura de Packages

Crear la siguiente estructura de directorios en `src/main/java/com/xideral/banco/`:

```
src/main/java/com/xideral/banco/
├── BancoApplication.java
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

**Comandos para crear estructura (Linux/Mac):**
```bash
cd src/main/java/com/xideral/banco
mkdir -p customer/{controller,service,repository,model}
mkdir -p account/{controller,service,repository,model}
mkdir -p notification/{service,model}
mkdir -p batch events config
```

**En Windows (PowerShell):**
```powershell
New-Item -ItemType Directory -Force -Path customer/controller,customer/service,customer/repository,customer/model
New-Item -ItemType Directory -Force -Path account/controller,account/service,account/repository,account/model
New-Item -ItemType Directory -Force -Path notification/service,notification/model
New-Item -ItemType Directory -Force -Path batch,events,config
```

---

### Paso 5: Crear Clase Principal (BancoApplication)

Crear archivo `src/main/java/com/xideral/banco/BancoApplication.java`:

```java
package com.xideral.banco;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = {
    "com.xideral.banco.customer.repository",
    "com.xideral.banco.account.repository"
})
@EnableMongoRepositories(basePackages = {
    "com.xideral.banco.notification.repository"
})
public class BancoApplication {

    public static void main(String[] args) {
        SpringApplication.run(BancoApplication.class, args);
    }
}
```

#### 📝 Explicación de Anotaciones:

- `@SpringBootApplication` - Activa autoconfiguración de Spring Boot
- `@EnableJpaRepositories` - Habilita repositorios JPA para MySQL
- `@EnableMongoRepositories` - Habilita repositorios para MongoDB
- `basePackages` - Define dónde buscar los repositorios

---

### Paso 6: Crear Entidad Customer (MySQL)

Crear archivo `src/main/java/com/xideral/banco/customer/model/Customer.java`:

```java
package com.xideral.banco.customer.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "customers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is required")
    @Column(nullable = false, length = 100)
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^\\d{10}$", message = "Phone must be 10 digits")
    @Column(nullable = false, length = 10)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CustomerStatus status = CustomerStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum CustomerStatus {
        ACTIVE,
        INACTIVE
    }
}
```

#### 📝 Conceptos Clave:

**Anotaciones JPA:**
- `@Entity` - Marca la clase como entidad JPA
- `@Table(name = "customers")` - Nombre de la tabla en la base de datos
- `@Id` - Marca el campo como clave primaria
- `@GeneratedValue` - Generación automática de ID
- `@Column` - Configuración de columna (nullable, unique, length)

**Anotaciones de Validación:**
- `@NotBlank` - Campo no puede estar vacío
- `@Email` - Valida formato de email
- `@Pattern` - Valida expresión regular (10 dígitos para teléfono)

**Anotaciones de Lombok:**
- `@Data` - Genera getters, setters, toString, equals, hashCode
- `@NoArgsConstructor` - Constructor sin argumentos
- `@AllArgsConstructor` - Constructor con todos los argumentos

**Timestamps Automáticos:**
- `@CreationTimestamp` - Se establece automáticamente al crear
- `@UpdateTimestamp` - Se actualiza automáticamente al modificar

---

### Paso 7: Crear Entidad Account (MySQL)

Crear archivo `src/main/java/com/xideral/banco/account/model/Account.java`:

```java
package com.xideral.banco.account.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "accounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Account number is required")
    @Column(name = "account_number", nullable = false, unique = true, length = 20)
    private String accountNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false, length = 20)
    private AccountType accountType;

    @NotNull(message = "Balance is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Balance cannot be negative")
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @NotNull(message = "Customer ID is required")
    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountStatus status = AccountStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum AccountType {
        SAVINGS,
        CHECKING
    }

    public enum AccountStatus {
        ACTIVE,
        CLOSED
    }
}
```

#### 📝 Conceptos Importantes:

**BigDecimal para Dinero:**
- Usa `BigDecimal` (NO `double` o `float`) para cantidades monetarias
- `precision = 15, scale = 2` permite números como 9999999999999.99

**Enums:**
- `@Enumerated(EnumType.STRING)` - Guarda el nombre del enum (no el ordinal)
- `AccountType`: SAVINGS (Ahorro), CHECKING (Corriente)
- `AccountStatus`: ACTIVE, CLOSED

---

### Paso 8: Crear Entidad Transaction (MySQL)

Crear archivo `src/main/java/com/xideral/banco/account/model/Transaction.java`:

```java
package com.xideral.banco.account.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Account ID is required")
    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 20)
    private TransactionType transactionType;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @NotNull(message = "Balance is required")
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal balanceAfter;

    @Column(length = 500)
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum TransactionType {
        DEPOSIT,
        WITHDRAWAL,
        TRANSFER_IN,
        TRANSFER_OUT,
        INTEREST
    }
}
```

#### 📝 Tipos de Transacciones:

- `DEPOSIT` - Depósito
- `WITHDRAWAL` - Retiro
- `TRANSFER_IN` - Transferencia recibida
- `TRANSFER_OUT` - Transferencia enviada
- `INTEREST` - Intereses (para el batch del Día 5)

---

### Paso 9: Crear Documento TransactionLog (MongoDB)

Crear archivo `src/main/java/com/xideral/banco/notification/model/TransactionLog.java`:

```java
package com.xideral.banco.notification.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Document(collection = "transaction_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionLog {

    @Id
    private String id;

    private Long transactionId;

    private Long accountId;

    private String accountNumber;

    private String transactionType;

    private BigDecimal amount;

    private BigDecimal balanceAfter;

    private String status;

    private LocalDateTime timestamp;

    private Map<String, Object> metadata;

    private String description;
}
```

#### 📝 Diferencias MongoDB vs JPA:

**MongoDB:**
- `@Document(collection = "transaction_logs")` - Nombre de la colección
- `@Id` de `org.springframework.data.annotation` (no JPA)
- ID es `String` (ObjectId de MongoDB)
- Estructura más flexible (Map para metadata)
- No requiere validaciones (se valida en MySQL)

**Uso:**
- MySQL: Datos transaccionales críticos
- MongoDB: Logs, auditoría, metadata adicional

---

### Paso 10: Compilar y Ejecutar

#### 1. Limpiar y Compilar
```bash
mvn clean compile
```

**Resultado esperado:**
```
[INFO] BUILD SUCCESS
[INFO] Total time: X.XXX s
```

#### 2. Ejecutar la Aplicación
```bash
mvn spring-boot:run
```

#### 3. Verificar Logs

**Conexión MySQL:**
```
INFO com.zaxxer.hikari.HikariDataSource : HikariPool-1 - Starting...
INFO com.zaxxer.hikari.HikariDataSource : HikariPool-1 - Start completed.
```

**Creación de Tablas:**
```
Hibernate: create table customers (...)
Hibernate: create table accounts (...)
Hibernate: create table transactions (...)
```

**Conexión MongoDB:**
```
INFO org.mongodb.driver.client : MongoClient with metadata {...} created with settings ...
INFO org.mongodb.driver.cluster : Monitor thread successfully connected to server
```

**Aplicación Iniciada:**
```
INFO com.xideral.banco.BancoApplication : Started BancoApplication in X.XXX seconds
INFO o.s.b.w.embedded.tomcat.TomcatWebServer : Tomcat started on port 8080
```

---

## ✅ Verificación Final

### 1. Verificar Base de Datos MySQL

```bash
# Conectarse a MySQL
docker exec -it mysql-container mysql -uroot -pxideral1234

# Ver bases de datos
SHOW DATABASES;

# Usar base de datos
USE banco_db;

# Ver tablas
SHOW TABLES;

# Describir tabla
DESCRIBE customers;

# Salir
EXIT;
```

**Deberías ver:**
```
+-------------------+
| Tables_in_banco_db|
+-------------------+
| accounts          |
| customers         |
| transactions      |
| batch_*           |
+-------------------+
```

### 2. Verificar MongoDB

```bash
# Conectarse a MongoDB
docker exec -it mongodb-container mongosh -u admin -p xideral4321 --authenticationDatabase admin

# Ver bases de datos
show dbs

# Usar base de datos
use banco_logs

# Ver colecciones (aún vacía, se usará en Día 4)
show collections

# Salir
exit
```

### 3. Verificar Swagger UI

Abrir navegador en:
```
http://localhost:8080/swagger-ui.html
```

Deberías ver la interfaz de Swagger (sin endpoints aún, se crearán en Día 2)

### 4. Verificar Estructura de Proyecto

```bash
# Listar estructura
tree src/main/java/com/xideral/banco/
```

**Estructura esperada:**
```
src/main/java/com/xideral/banco/
├── BancoApplication.java
├── account
│   └── model
│       ├── Account.java
│       └── Transaction.java
├── customer
│   └── model
│       └── Customer.java
└── notification
    └── model
        └── TransactionLog.java
```

---

## 🐛 Troubleshooting (Problemas Comunes)

### Error: "Cannot connect to MySQL"

**Problema:**
```
java.sql.SQLNonTransientConnectionException: Public Key Retrieval is not allowed
```

**Solución:**
Agregar `allowPublicKeyRetrieval=true` a la URL de MySQL en `application.properties`

---

### Error: "MongoDB authentication failed"

**Problema:**
```
com.mongodb.MongoSecurityException: Exception authenticating
```

**Solución:**
Verificar:
1. Credenciales correctas en `application.properties`
2. Agregar `?authSource=admin` a la URI de MongoDB
3. Contenedor MongoDB corriendo: `docker ps | grep mongo`

---

### Error: "Port 8080 already in use"

**Problema:**
```
Web server failed to start. Port 8080 was already in use.
```

**Solución:**
Cambiar puerto en `application.properties`:
```properties
server.port=8081
```

O detener proceso usando el puerto:
```bash
# Linux/Mac
lsof -ti:8080 | xargs kill -9

# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

---

### Error: "Lombok not working"

**Problema:**
Getters/Setters no generados, errores de compilación

**Solución IntelliJ IDEA:**
1. File → Settings → Plugins
2. Buscar "Lombok"
3. Instalar plugin
4. Reiniciar IDE
5. File → Settings → Build, Execution, Deployment → Compiler → Annotation Processors
6. Activar "Enable annotation processing"

**Solución Eclipse:**
1. Descargar `lombok.jar` de [https://projectlombok.org/](https://projectlombok.org/)
2. Ejecutar: `java -jar lombok.jar`
3. Seleccionar tu Eclipse
4. Install/Update
5. Reiniciar Eclipse

---

### Error: "Bean 'transactionManager' conflict"

**Problema:**
```
Cannot register bean definition 'transactionManager'
```

**Solución:**
Agregar en `application.properties`:
```properties
spring.main.allow-bean-definition-overriding=true
```

---

## 📊 Resultado del Día 1

### Archivos Creados

| Archivo | Descripción |
|---------|-------------|
| `pom.xml` | Configuración Maven con dependencias |
| `application.properties` | Configuración de bases de datos |
| `BancoApplication.java` | Clase principal Spring Boot |
| `Customer.java` | Entidad JPA para clientes |
| `Account.java` | Entidad JPA para cuentas |
| `Transaction.java` | Entidad JPA para transacciones |
| `TransactionLog.java` | Documento MongoDB para logs |

### Tablas en MySQL

| Tabla | Campos Principales |
|-------|-------------------|
| `customers` | id, name, email, phone, status |
| `accounts` | id, account_number, account_type, balance, customer_id |
| `transactions` | id, account_id, transaction_type, amount, balance_after |

### Tecnologías Configuradas

- ✅ Spring Boot 3.5.6
- ✅ Spring Data JPA (MySQL)
- ✅ Spring Data MongoDB
- ✅ Spring Batch (preparado para Día 5)
- ✅ Spring Validation
- ✅ Hibernate ORM
- ✅ Lombok
- ✅ Swagger/OpenAPI
- ✅ JaCoCo (coverage)

---

## 🎓 Conceptos Aprendidos

### 1. Arquitectura Híbrida de Datos
- **MySQL (Relacional):** Datos transaccionales críticos
- **MongoDB (Documental):** Logs, auditoría, metadata flexible

### 2. Mapeo Objeto-Relacional (ORM)
- Entidades JPA con anotaciones
- Relaciones entre tablas (aunque no explícitas aún)
- Generación automática de esquema

### 3. Validaciones
- Validaciones de Bean Validation (JSR-303)
- Validaciones a nivel de base de datos (constraints)

### 4. Lombok
- Reducción de código boilerplate
- Generación automática de getters, setters, constructores

### 5. Configuración Externalizada
- `application.properties` para configuración
- Separación de credenciales y lógica

---

## 📝 Checklist Final Día 1

Antes de continuar con el Día 2, verifica que:

- [ ] Docker containers (MySQL y MongoDB) están corriendo
- [ ] Proyecto Maven compila sin errores: `mvn clean compile`
- [ ] Aplicación Spring Boot inicia correctamente: `mvn spring-boot:run`
- [ ] Tablas creadas en MySQL (`customers`, `accounts`, `transactions`)
- [ ] Conexión a MongoDB exitosa (ver logs)
- [ ] Estructura de packages completa
- [ ] Todas las entidades creadas (Customer, Account, Transaction, TransactionLog)
- [ ] Swagger UI accesible en http://localhost:8080/swagger-ui.html
- [ ] Sin errores en los logs de la aplicación

---

## 🚀 Próximos Pasos

**Día 2:** Módulo Customer + Testing Integral
- Crear repositories (CustomerRepository)
- Crear services con lógica de negocio
- Crear REST controllers
- Testing completo (Unit + Integration)
- Coverage > 85%

---

## 📚 Referencias

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Data JPA](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
- [Spring Data MongoDB](https://docs.spring.io/spring-data/mongodb/docs/current/reference/html/)
- [Project Lombok](https://projectlombok.org/)
- [Hibernate ORM](https://hibernate.org/orm/documentation/)
- [Bean Validation](https://beanvalidation.org/)
- [SpringDoc OpenAPI](https://springdoc.org/)

---

## 💡 Tips Profesionales

### 1. Git desde el inicio
```bash
git init
git add .
git commit -m "Día 1: Setup y configuración completa"
```

### 2. .gitignore
Crear archivo `.gitignore`:
```
target/
.mvn/
.idea/
*.iml
.DS_Store
application-local.properties
```

### 3. Backup de Configuración
Guardar copia de `application.properties` como `application.properties.example` sin credenciales reales

### 4. Docker Compose (Opcional)
Para facilitar el inicio de contenedores, crear `docker-compose.yml`:
```yaml
version: '3.8'
services:
  mysql:
    image: mysql:latest
    container_name: mysql-container
    environment:
      MYSQL_ROOT_PASSWORD: xideral1234
    ports:
      - "3306:3306"

  mongodb:
    image: mongo:8
    container_name: mongodb-container
    environment:
      MONGO_INITDB_ROOT_USERNAME: admin
      MONGO_INITDB_ROOT_PASSWORD: xideral4321
    ports:
      - "27017:27017"
```

Iniciar ambos:
```bash
docker-compose up -d
```

---

**¡Felicidades! Has completado el Día 1 del Sistema Bancario Digital** 🎉

Ahora tienes una base sólida para construir el resto de la aplicación en los próximos 4 días.

---

*Documento generado para: Academia Xideral FullStack*
*Fecha: Septiembre 2025*
*Versión: 1.0*