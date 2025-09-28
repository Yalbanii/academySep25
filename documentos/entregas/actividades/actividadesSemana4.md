# Academia Xideral FullStack: Sistema Bancario Digital
## Proyecto Final Integrador 

**Plan de Trabajo Semanal**  
*5 días aplicando todos los conceptos del curso*

**Tecnologías:** Spring Boot •  SpringData JPA • Maven •  Spring Rest • SpringBatch • MongoDB • MySQL • Testing

---

## Objetivos del Proyecto

### Objetivos Técnicos
- Aplicar inyección de dependencias
- Demostrar polimorfismo efectivo
- Configurar bases de datos híbridas
- Desarrollar APIs REST completas
- Implementar arquitectura modular

### Objetivos Funcionales
- Gestión completa de clientes
- Administración de cuentas bancarias
- Sistema de transferencias
- Procesamiento batch automatizado
- Notificaciones en tiempo real

### Métricas de Éxito
| Métrica | Objetivo |
|---------|----------|
| **Coverage de Testing** | 85% mínimo |
| **APIs REST** | 15+ implementadas |
| **Módulos Comunicándose** | 3 módulos |

---

## Stack Tecnológico

### Herramientas Core
| Tecnología | Propósito |
|------------|-----------|
| **Maven** | Gestión completa de dependencias y plugins |
| **Spring Boot 3.x** | Framework principal con Spring Data JPA |
| **MySQL + MongoDB** | Bases de datos híbridas (relacional + documental) |
| **JUnit 5 + Mockito** | Testing integral |
| **Spring Batch** | Procesos automatizados |
| **Spring Modulith** | Arquitectura orientada a eventos |

### ¿Por qué esta combinación?

**Realismo Empresarial**  
Simula entornos reales donde coexisten múltiples tecnologías de bases de datos

**Aprendizaje Integral**  
Cubre todos los conceptos del curso en un proyecto  práctico

**Preparación Profesional**  
Prepara a los participantes para proyectos empresariales complejos

---

## Arquitectura del Sistema

### Módulos del Sistema

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│ Customer Service│    │ Account Service │    │Notification Srv │
│                 │    │                 │    │                 │
│ Gestión de      │    │ Cuentas, saldos │    │ Alertas y       │
│ clientes y KYC  │    │ y transferencias│    │ comunicaciones  │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

### Estrategia de Datos Híbrida

#### MySQL (Datos Relacionales)
- Información de clientes
- Relaciones entre entidades
- Transacciones
- Reportes 

#### MongoDB (Datos Documentales)
- Logs de transacciones
- Datos de clientes
- Datos de transacciones

---

## Plan de Trabajo - 5 Días

### **Día 1: Setup y Configuración Completa**
**Objetivo:** Establecer base sólida del proyecto
- Maven, Spring Boot, MySQL, MongoDB
- Estructura de proyecto y entidades base
- Configuración de conexiones duales
- Testing de conectividad

### **Día 2: Módulo Customer + Testing Integral**
**Objetivo:** CRUD completo con testing robusto
- CRUD completo de clientes
- Spring Data JPA con MySQL
- Validaciones de negocio
- Testing en todas las capas (Controller, Service, Repository)

### **Día 3: Módulo Account + Polimorfismo**
**Objetivo:** Lógica de negocio bancaria
- Gestión de cuentas bancarias
- Calculadoras de interés (polimorfismo)
- Transferencias y validaciones
- Lógica de negocio completa

### **Día 4: Eventos + Notificaciones + Transacciones**
**Objetivo:** Comunicación entre módulos
- Sistema de eventos Spring
- Comunicación entre módulos
- Historial transaccional
- Notificaciones automáticas

### **Día 5: Spring Batch + Coverage Final**
**Objetivo:** Job: Procesamiento Mensual de Intereses
Contexto Bancario: Cada mes, el banco debe calcular y aplicar intereses a todas las cuentas de ahorro activas de sus clientes.

- Spring Batch: Job con 2 steps claramente definidos
- Procesamiento automático de intereses
- Reportes de testing y coverage
- Bases de Datos: MySQL para transacciones, MongoDB para logs

---

## Metodología de Desarrollo

### Desarrollo Iterativo
- Entregables diarios validables
- Revisiones de código continuas
- Integración progresiva de módulos
- Feedback inmediato

### Testing integral
- Testing paralelo al desarrollo
- Coverage mínimo del 85%
- Testing en todas las capas
- Reportes automáticos con JaCoCo

### Herramientas de Seguimiento
- **Maven Commands:** Comandos específicos para compilación, testing y reportes
- **Coverage Reports:** Reportes diarios de cobertura por módulo

---

## Casos de Uso del Negocio

### Funcionalidades Core

#### Gestión de Clientes
- Registro con Email
- Actualización de datos personales
- Estados de cliente (Activo/Inactivo)
- Búsquedas y filtros

#### Operaciones Bancarias
- Creación de cuentas (Ahorro/Corriente)
- Consulta de saldos en tiempo real
- Transferencias entre cuentas
- Validaciones de límites y fondos

### Procesos Automatizados

#### Cálculo de Intereses
Procesamiento batch mensual con diferentes tasas según tipo de cuenta (polimorfismo aplicado)

#### Notificaciones Automáticas
Sistema de eventos que envía confirmaciones por email en cada transacción

#### Auditoría Completa
Registro detallado de todas las operaciones para trazabilidad

---

## Resultados Esperados

### Al finalizar la semana, cada desarrollador tendrá:

| Resultado | Detalle |
|-----------|---------|
| **100%** | Conceptos del Curso Aplicados |
| **1** | Proyecto Portfolio Completo |
| **15+** | APIs REST Documentadas |
| **85%** | Coverage de Testing Alcanzado |

### Beneficios para el Equipo

#### Experiencia Práctica
Aplicación real de conceptos en un proyecto empresarial completo y funcional

#### Habilidades Técnicas
Dominio de herramientas y frameworks utilizados en proyectos profesionales

#### Preparación Avanzada
Capacidad para enfrentar proyectos más complejos con confianza y conocimiento

#### Proyecto Profesional
Proyecto demostrable para futuras oportunidades y crecimiento profesional

---

## Conceptos del Curso Cubiertos

### Inyección de Dependencias
- Constructor injection en todos los services
- Uso de interfaces para desacoplamiento
- Configuración con anotaciones Spring

### Polimorfismo
- Calculadoras de interés por tipo de cuenta
- Strategy pattern implementado
- Interfaces con múltiples implementaciones

### Spring Core & MVC
- Controllers REST completos
- Services con lógica de negocio
- Configuración y manejo de beans

### Spring Data JPA
- Repositories personalizados
- Queries derivadas y personalizadas
- Agregaciones y operaciones complejas

### Maven
- Gestión completa de dependencias
- Plugins para testing y coverage
- Build lifecycle completo

### MongoDB
- Configuración y conexión
- Documentos y collections
- Queries y operaciones NoSQL

### MySQL
- Base de datos relacional
- JPA entities y relaciones
- Transacciones ACID

### Spring REST
- APIs RESTful completas
- DTOs y validaciones
- Manejo de errores y excepciones

### JUnit
- Tests unitarios completos
- Testing de lógica de negocio
- Assertions y validaciones

### Mockito
- Mocking en todas las capas
- Verification y stubbing
- Testing de integración

### Spring Batch
- Jobs con múltiples steps
- Procesamiento por lotes
- Scheduling y configuración

### Manejo de Eventos
- Spring Events para comunicación
- Publishers y listeners
- Arquitectura orientada a eventos

---

## Conclusión

**5 días • 1 proyecto integral**

Este proyecto final integra todos los conceptos aprendidos durante la academia en un contexto bancario realista, preparando a los participantes para enfrentar desafíos empresariales reales y conocimiento técnico sólido.

---

*Preparado para: Participanes Academia Fullstack*  
*Fecha: Septiembre 2025*  
*Duración: 5 días*