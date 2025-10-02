# Guía de Docker Compose para el Proyecto Bancario

**Audiencia:** Participantes Developers
**Nivel:** Intermedio
**Última actualización:** Octubre 2025

---

## 📋 Tabla de Contenidos

1. [¿Qué es Docker Compose?](#qué-es-docker-compose)
2. [¿Por qué lo implementamos?](#por-qué-lo-implementamos)
3. [Antes vs Después](#antes-vs-después)
4. [Beneficios](#beneficios)
5. [Ventajas](#ventajas)
6. [Desventajas](#desventajas)
7. [Cómo Funciona en Nuestro Proyecto](#cómo-funciona-en-nuestro-proyecto)
8. [Comandos Esenciales](#comandos-esenciales)
9. [Casos de Uso Reales](#casos-de-uso-reales)
10. [Mejores Prácticas](#mejores-prácticas)
11. [Troubleshooting](#troubleshooting)
12. [Conclusión](#conclusión)

---

## 🤔 ¿Qué es Docker Compose?

**Docker Compose** es una herramienta que permite definir y ejecutar aplicaciones multi-contenedor usando un archivo de configuración YAML (`docker-compose.yml`).

### Analogía Simple:
Imagina que vas a preparar una cena completa:
- **Sin Docker Compose**: Tienes que cocinar cada platillo uno por uno, recordar todos los ingredientes y tiempos de cocción
- **Con Docker Compose**: Tienes una receta maestra que dice exactamente qué cocinar, en qué orden, y con qué ingredientes

### En el Mundo del Software:
- Define múltiples servicios (bases de datos, servidores, etc.)
- Configura cómo se comunican entre sí
- Los levanta todos con un solo comando
- Mantiene la configuración versionada

---

## 🎯 ¿Por qué lo implementamos?

### Problema Original

Nuestro proyecto tiene 2 bases de datos:
- **MySQL** → Para datos transaccionales (clientes, cuentas)
- **MongoDB** → Para logs y notificaciones

**Antes**, para iniciar el proyecto necesitabas:

```bash
# 1. Abrir terminal y ejecutar MySQL
docker run --name mysql-container \
  -e MYSQL_ROOT_PASSWORD=xideral1234 \
  -p 3306:3306 \
  -d mysql:latest

# 2. Abrir otra terminal y ejecutar MongoDB
docker run --name mongodb-container \
  -e MONGO_INITDB_ROOT_USERNAME=admin \
  -e MONGO_INITDB_ROOT_PASSWORD=xideral4321 \
  -p 27017:27017 \
  -d mongo:8

# 3. Verificar que ambos estén corriendo
docker ps

# 4. Finalmente, correr la aplicación Spring Boot
mvn spring-boot:run
```

**Problemas:**
- ❌ 3 comandos largos y difíciles de recordar
- ❌ Fácil olvidar credenciales o puertos
- ❌ No hay persistencia de datos configurada
- ❌ Difícil compartir setup con el equipo
- ❌ Propenso a errores de tipeo

---

## 📊 Antes vs Después

### ANTES: Comandos Manuales

```bash
# Terminal 1
docker run --name mysql-container -e MYSQL_ROOT_PASSWORD=xideral1234 -p 3306:3306 -d mysql:latest

# Terminal 2
docker run --name mongodb-container -e MONGO_INITDB_ROOT_USERNAME=admin -e MONGO_INITDB_ROOT_PASSWORD=xideral4321 -p 27017:27017 -d mongo:8

# Terminal 3
mvn spring-boot:run
```

**Resultado:**
- 🔴 3 pasos manuales
- 🔴 Fácil de olvidar
- 🔴 No documentado
- 🔴 No versionado en git

---

### DESPUÉS: Docker Compose

```bash
# Solo 1 comando
docker compose up -d

# Listo! Todo corriendo
mvn spring-boot:run
```

**Resultado:**
- 🟢 1 solo comando
- 🟢 Todo documentado en `docker-compose.yml`
- 🟢 Versionado en git
- 🟢 Fácil de compartir

---

## ✅ Beneficios

### 1. **Simplicidad Operacional**
Un solo archivo define toda la infraestructura:

```yaml
services:
  mysql:
    # Configuración MySQL
  mongodb:
    # Configuración MongoDB
```

### 2. **Reproducibilidad**
Todos los developers del equipo tienen el mismo ambiente:
- Mismo MySQL
- Mismo MongoDB
- Mismas credenciales
- Mismos puertos

### 3. **Versionamiento**
El archivo `docker-compose.yml` se guarda en Git:
```bash
git add docker-compose.yml
git commit -m "Add Docker Compose configuration"
```

### 4. **Documentación Implícita**
El archivo ES la documentación:
```yaml
mysql:
  image: mysql:latest              # ¿Qué versión? → latest
  ports:
    - "3306:3306"                   # ¿Qué puerto? → 3306
  environment:
    MYSQL_ROOT_PASSWORD: xideral1234  # ¿Contraseña? → xideral1234
```

### 5. **Persistencia de Datos**
Volúmenes automáticos:
```yaml
volumes:
  mysql-data:    # Los datos NO se pierden al reiniciar
  mongo-data:
```

### 6. **Healthchecks**
Verificación automática de que las BD estén listas:
```yaml
healthcheck:
  test: ["CMD", "mysqladmin", "ping"]
  interval: 10s
```

---

## 🚀 Ventajas

### Para Developers

#### 1. **Onboarding Rápido**
Nuevo developer en el equipo:
```bash
git clone <repo>
cd banco
docker compose up -d
mvn spring-boot:run
# ¡Listo en 2 minutos!
```

#### 2. **Menos Errores**
No hay riesgo de:
- Olvidar una variable de entorno
- Usar puerto incorrecto
- Perder datos al reiniciar

#### 3. **Aprendizaje**
El archivo `docker-compose.yml` enseña:
- Cómo configurar bases de datos
- Networking entre contenedores
- Volúmenes y persistencia
- Variables de entorno

#### 4. **Ambiente Limpio**
```bash
# Limpiar todo
docker compose down -v

# Empezar de cero
docker compose up -d
```

### Para el Proyecto

#### 1. **Escalabilidad**
Fácil agregar más servicios:
```yaml
services:
  mysql:
    # ...
  mongodb:
    # ...
  redis:       # ¡Nuevo servicio!
    image: redis:alpine
    ports:
      - "6379:6379"
```

#### 2. **Ambientes Múltiples**
```bash
# Desarrollo
docker compose up -d

# Testing
docker compose -f docker-compose.test.yml up -d

# Producción
docker compose -f docker-compose.prod.yml up -d
```

#### 3. **CI/CD Ready**
En GitHub Actions o Jenkins:
```yaml
# .github/workflows/test.yml
- name: Start databases
  run: docker compose up -d

- name: Run tests
  run: mvn test
```

#### 4. **Networking Automático**
Los contenedores se comunican por nombre:
```java
// Futuro: cuando la app esté en Docker
spring.datasource.url=jdbc:mysql://mysql:3306/banco_db
// En lugar de localhost
```

---

## ⚠️ Desventajas

### 1. **Curva de Aprendizaje**
**Problema:** Necesitas entender:
- Sintaxis YAML
- Conceptos de Docker (imágenes, contenedores, volúmenes)
- Comandos de Docker Compose

**Solución:**
- Documentación del proyecto (este archivo)
- Práctica gradual
- Pair programming con seniors

### 2. **Overhead de Recursos**
**Problema:** Docker consume:
- RAM (cada contenedor usa memoria)
- CPU (procesos adicionales)
- Disco (imágenes y volúmenes)

**Ejemplo real:**
```
MySQL container:    ~400 MB RAM
MongoDB container:  ~200 MB RAM
Docker Desktop:     ~500 MB RAM
Total:             ~1.1 GB RAM
```

**Solución:**
- Computadoras modernas (8GB+ RAM) no tienen problema
- Cerrar otros programas pesados mientras desarrollas

### 3. **Dependencia de Docker Desktop**
**Problema:** Docker Desktop debe estar corriendo SIEMPRE

**Impacto:**
- Si se cierra → contenedores se detienen
- Si falla → no hay BD disponibles
- Consume batería en laptops

**Solución:**
- Configurar Docker para iniciar automáticamente
- Verificar con `docker ps` antes de trabajar

### 4. **Complejidad Adicional para Casos Simples**
**Problema:** Para un solo servicio, puede ser excesivo

**Ejemplo:**
```yaml
# Solo MySQL
services:
  mysql:
    image: mysql:latest
    # ... 15 líneas de config

# vs comando simple
docker run -p 3306:3306 mysql
```

**En nuestro caso:** Tenemos 2 servicios, así que SÍ vale la pena

### 5. **Debugging Más Complejo**
**Problema:** Los logs están "dentro" de los contenedores

**Antes:**
```bash
# App corre en terminal, ves logs directamente
mvn spring-boot:run
```

**Ahora:**
```bash
# Logs en contenedor separado
docker compose logs mysql
docker compose logs mongodb
```

**Solución:** Usar comandos de compose para logs:
```bash
docker compose logs -f        # Todos los logs en tiempo real
docker compose logs mysql     # Solo MySQL
```

### 6. **Cambios Requieren Rebuild**
**Problema:** Modificar `docker-compose.yml` requiere reiniciar:

```bash
# Editaste docker-compose.yml
docker compose down
docker compose up -d
```

**Impacto:**
- 30-60 segundos de downtime
- Puede interrumpir desarrollo

### 7. **Networking Oculto**
**Problema:** La red `banco-network` es "invisible"

**Confusión común:**
```
¿Por qué no puedo hacer ping a mysql-container desde mi Mac?
→ Porque está en red Docker interna
```

**Solución:**
- Usar `localhost` desde la máquina host
- Usar nombres de servicio solo entre contenedores

---

## 🔧 Cómo Funciona en Nuestro Proyecto

### Arquitectura Actual

```
┌─────────────────────────────────────────┐
│         Tu Computadora (macOS)          │
│                                         │
│  ┌────────────────────────────────┐    │
│  │   Spring Boot App              │    │
│  │   (corre localmente)           │    │
│  │   Puerto: 8080                 │    │
│  └────────────┬───────────────────┘    │
│               │                         │
│               │ Conecta via localhost   │
│               ├─────────────┐           │
│               │             │           │
│  ┌────────────▼──┐    ┌────▼─────────┐ │
│  │   MySQL        │    │   MongoDB    │ │
│  │   (Docker)     │    │   (Docker)   │ │
│  │   Puerto: 3306 │    │   Puerto:    │ │
│  │                │    │   27017      │ │
│  └────────────────┘    └──────────────┘ │
│         ▲                      ▲         │
│         │                      │         │
│         └──────────────────────┘         │
│         Red: banco-network               │
│         (Docker internal)                │
└─────────────────────────────────────────┘
```

### Flujo de Inicio

1. **Developer ejecuta:**
   ```bash
   docker compose up -d
   ```

2. **Docker Compose:**
   - Lee `docker-compose.yml`
   - Crea red `banco-network`
   - Crea volúmenes `mysql-data` y `mongo-data`
   - Descarga imágenes (si no existen)
   - Inicia contenedor MySQL
   - Inicia contenedor MongoDB
   - Ejecuta healthchecks

3. **Developer ejecuta:**
   ```bash
   mvn spring-boot:run
   ```

4. **Spring Boot:**
   - Lee `application.properties`
   - Conecta a `localhost:3306` (MySQL)
   - Conecta a `localhost:27017` (MongoDB)
   - Inicia aplicación

### Configuración Detallada

```yaml
# docker-compose.yml
services:
  mysql:
    image: mysql:latest                    # Imagen oficial de Docker Hub
    container_name: mysql-container        # Nombre fijo del contenedor
    environment:
      MYSQL_ROOT_PASSWORD: xideral1234     # Contraseña root
    ports:
      - "3306:3306"                        # Puerto host:contenedor
    volumes:
      - mysql-data:/var/lib/mysql          # Persistencia
    networks:
      - banco-network                      # Red compartida
    healthcheck:
      test: ["CMD", "mysqladmin", "ping"]  # Verifica que esté vivo
      interval: 10s                        # Cada 10 segundos
      timeout: 5s
      retries: 5

  mongodb:
    image: mongo:8
    container_name: mongodb-container
    environment:
      MONGO_INITDB_ROOT_USERNAME: admin
      MONGO_INITDB_ROOT_PASSWORD: xideral4321
    ports:
      - "27017:27017"
    volumes:
      - mongo-data:/data/db
    networks:
      - banco-network
    healthcheck:
      test: ["CMD", "mongosh", "--eval", "db.adminCommand('ping')"]
      interval: 10s
      timeout: 5s
      retries: 5

volumes:
  mysql-data:      # Volume para MySQL (datos persisten)
  mongo-data:      # Volume para MongoDB (datos persisten)

networks:
  banco-network:   # Red privada para comunicación
    driver: bridge
```

---

## 📝 Comandos Esenciales

### Inicio y Parada

```bash
# Iniciar todo (modo background)
docker compose up -d

# Iniciar y ver logs en tiempo real
docker compose up

# Parar todo (sin borrar datos)
docker compose down

# Parar y BORRAR datos (¡CUIDADO!)
docker compose down -v
```

### Monitoreo

```bash
# Ver estado de contenedores
docker compose ps

# Ver logs de todos los servicios
docker compose logs

# Ver logs en tiempo real
docker compose logs -f

# Ver logs solo de MySQL
docker compose logs -f mysql

# Ver logs solo de MongoDB
docker compose logs -f mongodb
```

### Gestión

```bash
# Reiniciar un servicio específico
docker compose restart mysql

# Parar un servicio específico
docker compose stop mongodb

# Iniciar un servicio específico
docker compose start mongodb

# Reconstruir servicios (si cambió docker-compose.yml)
docker compose up -d --force-recreate
```

### Debugging

```bash
# Ejecutar comando dentro del contenedor MySQL
docker compose exec mysql mysql -uroot -pxideral1234

# Ejecutar comando dentro del contenedor MongoDB
docker compose exec mongodb mongosh -u admin -p xideral4321

# Ver uso de recursos
docker stats

# Inspeccionar red
docker network inspect banco_banco-network
```

---

## 💼 Casos de Uso Reales

### Caso 1: Nuevo Developer en el Equipo

**Situación:** Juan acaba de unirse al equipo

**Pasos:**
```bash
# 1. Clonar repo
git clone <repo-url>
cd banco

# 2. Levantar infraestructura
docker compose up -d

# 3. Verificar que todo esté corriendo
docker compose ps

# 4. Correr la aplicación
mvn spring-boot:run

# ✅ Listo en 3 minutos
```

### Caso 2: Limpiar Datos de Prueba

**Situación:** Hiciste muchas pruebas y quieres empezar de cero

```bash
# 1. Parar y eliminar TODO (incluye datos)
docker compose down -v

# 2. Levantar de nuevo (BD vacías)
docker compose up -d

# 3. Volver a correr app
mvn spring-boot:run

# ✅ Base de datos completamente limpia
```

### Caso 3: Problema de Conexión

**Situación:** La app no conecta a MySQL

```bash
# 1. Verificar estado
docker compose ps

# 2. Ver logs de MySQL
docker compose logs mysql

# 3. Verificar healthcheck
docker inspect mysql-container | grep -A 10 Health

# 4. Reintentar conexión
docker compose restart mysql

# 5. Si persiste, recrear
docker compose down
docker compose up -d
```

### Caso 4: Actualizar Versión de MongoDB

**Situación:** Quieres usar MongoDB 9

```yaml
# Editar docker-compose.yml
mongodb:
  image: mongo:9    # Cambiar de 8 a 9
```

```bash
# Aplicar cambios
docker compose down
docker compose up -d

# Verificar nueva versión
docker compose exec mongodb mongosh --version
```

### Caso 5: Trabajar Offline

**Situación:** No tienes internet pero necesitas desarrollar

```bash
# Las imágenes ya están descargadas
docker images

# Levantar normalmente
docker compose up -d

# ✅ Funciona sin internet
```

---

## 🎓 Mejores Prácticas

### 1. **Siempre Usa Volúmenes para Datos**
```yaml
# ✅ BIEN - Los datos persisten
volumes:
  - mysql-data:/var/lib/mysql

# ❌ MAL - Los datos se pierden
# Sin volumen
```

### 2. **Define Healthchecks**
```yaml
# ✅ BIEN - Sabe cuándo la BD está lista
healthcheck:
  test: ["CMD", "mysqladmin", "ping"]
  interval: 10s

# ❌ MAL - No sabe si está lista
# Sin healthcheck
```

### 3. **Usa .env para Credenciales Sensibles**
```bash
# .env (NO commitear a git)
MYSQL_ROOT_PASSWORD=super_secreto_123
MONGO_PASSWORD=otra_clave_456
```

```yaml
# docker-compose.yml
environment:
  MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
```

### 4. **Documenta los Puertos**
```yaml
# ✅ BIEN - Claro qué hace
ports:
  - "3306:3306"  # MySQL: host 3306 → container 3306

# ❌ MAL - No se entiende
ports:
  - "3306:3306"
```

### 5. **Agregar al .gitignore**
```bash
# .gitignore
.env                 # Credenciales locales
docker-compose.override.yml  # Overrides personales
```

### 6. **Usar container_name Consistentes**
```yaml
# ✅ BIEN - Fácil de encontrar
container_name: mysql-container

# ❌ MAL - Nombre aleatorio
# Sin container_name → banco_mysql_1
```

---

## 🔍 Troubleshooting

### Problema 1: "Port already in use"

**Error:**
```
Error: bind: address already in use (port 3306)
```

**Causa:** Ya tienes MySQL corriendo localmente

**Solución:**
```bash
# Opción A: Parar MySQL local
brew services stop mysql

# Opción B: Cambiar puerto en docker-compose.yml
ports:
  - "3307:3306"  # Usa puerto 3307 en tu Mac
```

### Problema 2: "Connection refused"

**Error:**
```
Connection refused: localhost:3306
```

**Diagnóstico:**
```bash
# 1. ¿Está corriendo?
docker compose ps

# 2. ¿Está healthy?
docker compose ps | grep healthy

# 3. Ver logs
docker compose logs mysql
```

**Solución:**
```bash
# Reiniciar servicio
docker compose restart mysql

# O recrear
docker compose down
docker compose up -d
```

### Problema 3: "No space left on device"

**Error:**
```
Error: no space left on device
```

**Causa:** Docker usa mucho disco

**Solución:**
```bash
# Ver uso de disco
docker system df

# Limpiar imágenes viejas
docker system prune -a

# Limpiar volúmenes sin usar
docker volume prune
```

### Problema 4: Contenedor se reinicia continuamente

**Síntoma:**
```bash
docker compose ps
# STATUS: Restarting (1) 2 seconds ago
```

**Diagnóstico:**
```bash
# Ver por qué falla
docker compose logs mysql
```

**Causas comunes:**
- Contraseña incorrecta
- Puerto ocupado
- Falta de permisos
- Imagen corrupta

**Solución:**
```bash
# Recrear desde cero
docker compose down -v
docker compose up -d
```

### Problema 5: Datos perdidos después de reinicio

**Síntoma:** Al hacer `docker compose down` pierdes datos

**Causa:** No usaste volúmenes o usaste `-v` flag

```bash
# ❌ Esto BORRA datos
docker compose down -v

# ✅ Esto mantiene datos
docker compose down
```

---

## 📚 Conclusión

### Para Developers

#### ¿Vale la pena aprender Docker Compose?

**SÍ, absolutamente.** Aquí está el porqué:

#### Razones Profesionales:
1. **Estándar de la Industria:** 90% de empresas tech usan Docker
2. **Habilidad Valiosa:** Se busca en job descriptions
3. **Facilita Entrevistas:** "¿Sabes Docker?" → ✅
4. **Trabajo Remoto:** Ambientes consistentes en cualquier laptop

#### Razones Prácticas:
1. **Ahorra Tiempo:** Setup en 2 minutos vs 30 minutos
2. **Menos Bugs:** Mismo ambiente = menos "en mi máquina funciona"
3. **Fácil Experimentar:** Prueba y borra sin ensuciar tu sistema
4. **Mejor Colaboración:** Todo el equipo usa mismo setup

### Camino de Aprendizaje Sugerido

#### Semana 1: Básicos
- ✅ Entender contenedores vs máquinas virtuales
- ✅ Instalar Docker Desktop
- ✅ Correr tu primer `docker run`

#### Semana 2: Docker Compose
- ✅ Crear tu primer `docker-compose.yml`
- ✅ Usar volúmenes
- ✅ Debugging con logs

#### Semana 3: Proyecto Real
- ✅ Integrar con proyecto actual
- ✅ Configurar múltiples servicios
- ✅ Compartir con equipo

#### Semana 4: Avanzado
- ✅ Healthchecks
- ✅ Networking
- ✅ Variables de entorno

### Recursos Adicionales

#### Documentación Oficial:
- [Docker Docs](https://docs.docker.com/)
- [Docker Compose Reference](https://docs.docker.com/compose/compose-file/)

#### Tutoriales:
- [Docker for Beginners](https://docker-curriculum.com/)
- [Play with Docker](https://labs.play-with-docker.com/)

#### Comunidad:
- [Stack Overflow - Docker Tag](https://stackoverflow.com/questions/tagged/docker)
- [r/docker](https://reddit.com/r/docker)

### Mensaje Final

> "Docker Compose no es magia, es simplemente una forma inteligente de organizar y automatizar lo que harías manualmente. Como developer, dominar esta herramienta te dará habilidades para configurar proyectos complejos en segundos."

**Recuerda:**
- No tengas miedo de experimentar (puedes borrar y reiniciar)
- Lee los mensajes de error (Docker es muy descriptivo)
- Pregunta al equipo (todos pasamos por esto)
- Practica con proyectos personales

---


**Autores:**
Academia Xideral - Equipo de Arquitectura
Generación Septiembre 2025

**Última revisión:** Octubre 2025
