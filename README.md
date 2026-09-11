#  API Movies

## 🔍 Índice

- [Descripción](#-descripción)
- [Tecnologías utilizadas](#-tecnologías-utilizadas)
- [Instalación](#%EF%B8%8F-instalación)
- [Estructura de carpetas](#-estructura-de-carpetas)
- [Diagrama de base de datos](#-diagrama-de-base-de-datos)
- [Pruebas](#-pruebas)
- [Tests](#-tests)
- [Autora](#%EF%B8%8F-autora)

---

## 📝 Descripción

API REST del backend de **EducAlba**, academia de refuerzo educativo y talleres extraescolares. Desarrollado con **Spring Boot** y **Spring Security** (autenticación mediante Basic Auth con sesión por cookies), persistencia con **Spring Data JPA** sobre una base de datos **PostgreSQL dockerizada**, y tests unitarios y de integración con **Testcontainers**.

Este backend da servicio al frontend en Vue 3 ([web-educAlba-frontend](https://github.com/duran-ni/web-educAlba-frontend)), gestionando la autenticación de usuarios (familias y administradores), talleres, inscripciones, materiales, notas y avisos de la academia.
---

## 💻 Tecnologías utilizadas

---

## 🛠️ Instalación

**1.** Clonar el repositorio:

```bash

```

**2.** Compilar el proyecto:

```bash

```

**3.** Ejecutar la aplicación:

```bash

```

---

## 📁 Estructura de carpetas

```

```
---


## Diagrama de base de datos

```mermaid
erDiagram
    ROLE ||--o{ USER_ROLE : "asignado a"
    USER ||--o{ USER_ROLE : "tiene"
    USER ||--o| ALUMNO : "representa a"
    ALUMNO ||--o{ INSCRIPCION : "se inscribe en"
    TALLER ||--o{ INSCRIPCION : "recibe"
    ALUMNO ||--o{ MATERIAL : "tiene asignado"
    ALUMNO ||--o{ NOTA : "recibe"
    USER ||--o{ NOTA : "escribe"
    TALLER ||--o{ EVENTO_AGENDA : "genera"
    USER ||--o{ AVISO : "publica"

    ROLE {
        Long id PK
        String name
    }
    USER {
        Long id PK
        String email
        String password
        boolean enabled
    }
    ALUMNO {
        Long id PK
        String nombre
        String apellidos
        String etapaEducativa
        String servicioInteres
        String estado
        Long user_id FK "nullable"
    }
    TALLER {
        Long id PK
        String nombre
        String descripcion
        LocalDate fecha
        String edadRecomendada
        String sala
        boolean activo
    }
    INSCRIPCION {
        Long id PK
        Long alumno_id FK
        Long taller_id FK
        LocalDate fechaInscripcion
        String progreso
    }
    MATERIAL {
        Long id PK
        String nombre
        String asignatura
        Long tamanoArchivo
        String rutaArchivo
        Long alumno_id FK
    }
    NOTA {
        Long id PK
        String contenido
        LocalDate fecha
        boolean fijada
        Long alumno_id FK
        Long autor_id FK
    }
    EVENTO_AGENDA {
        Long id PK
        LocalDate fecha
        String hora
        String actividad
        String sala
        Long taller_id FK "nullable"
    }
    AVISO {
        Long id PK
        String titulo
        String contenido
        LocalDate fecha
        Long autor_id FK
    }
```

---

## 📷 Pruebas

---

## ✅ Tests

---

## ✍️ Autora

duran-ni
