# 🎓 EducAlba · Backend

![Java](https://img.shields.io/badge/Java-25-orange)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.1-6DB33F)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-336791)
![Tests](https://img.shields.io/badge/tests-203_pasados-brightgreen)
![Cobertura](https://img.shields.io/badge/cobertura-99,5%25-brightgreen)

API REST de **EducAlba**, academia de refuerzo escolar y talleres extraescolares para niños de 1 a 12 años y alumnos de ESO. Gestiona el registro y el login de familias y administradores, los talleres y sus inscripciones, los alumnos, la galería de fotos y vídeos, el equipo de profesores y los formularios públicos de la web.

> 📖 **Documentación completa en la [Wiki](https://github.com/duran-ni/web-educAlba-backend/wiki).**
> 🎤 **Presentación:** _(enlace pendiente)_

---

## 🗂️ Estructura del proyecto

El proyecto está dividido en dos repositorios:

| Repositorio | Contenido |
|-------------|-----------|
| **web-educAlba-backend** (este) | API REST con Spring Boot, seguridad y base de datos |
| [web-educAlba-frontend](https://github.com/duran-ni/web-educAlba-frontend) | Aplicación web en Vue 3 que consume esta API |

---

## 💻 Tecnologías

| Categoría | Tecnología |
|-----------|------------|
| Lenguaje y framework | Java 25 · Spring Boot 4.1 (Web MVC, Data JPA, Validation, Mail) |
| Seguridad | Spring Security (HTTP Basic + sesión por cookie + remember-me) |
| Base de datos | PostgreSQL 17 en Docker (Docker Compose) |
| Servicios externos | Cloudinary (fotos y vídeos) · Mailtrap (correo de pruebas) |
| Testing | JUnit 5 · MockMvc · Testcontainers · JaCoCo |
| Build y CI | Maven (wrapper incluido) · GitHub Actions |

---

## 🚀 Inicio rápido

Requisitos: **Java 25** y **Docker Desktop** abierto.

```bash
git clone https://github.com/duran-ni/web-educAlba-backend.git
cd web-educAlba-backend

# Windows
mvnw.cmd spring-boot:run

# Linux / macOS / Git Bash
./mvnw spring-boot:run
```

Al arrancar, Spring Boot levanta PostgreSQL con Docker, crea las tablas y carga los datos iniciales. La API queda en `http://localhost:8080/api`.

> ⚠️ Para enviar correos y subir fotos hay que definir antes las variables de entorno de Mailtrap y Cloudinary.

📖 Paso a paso y variables de entorno: [Guía de instalación](https://github.com/duran-ni/web-educAlba-backend/wiki/Guía-de-instalación)

---

## ✨ Funcionalidades

**👀 Visitantes**: consultar los talleres, la galería y el equipo de profesores, inscribirse en un taller sin cuenta y enviar los formularios de contacto e interés.

**👨‍👩‍👧 Familias (`ALUMNO_FAMILIA`)**: registrarse con los datos del alumno, iniciar sesión (con opción "recordarme"), recuperar la contraseña por correo y consultar su panel con los talleres del alumno.

**🛠️ Administración (`ADMINISTRADOR`)**: indicadores de la academia en tiempo real, y gestión de talleres, agenda, alumnos, inscripciones, materiales, notas, avisos, profesores (con foto) y galería (carpetas, portadas, fotos y vídeos).

📖 Todos los endpoints con ejemplos: [API REST](https://github.com/duran-ni/web-educAlba-backend/wiki/API-REST)

---

## 🧱 Arquitectura

Código **organizado por funcionalidad** (talleres, galería, alumnos…), con capas **Controller → Service → Repository** dentro de cada módulo, DTOs con `record`, gestión global de errores y el patrón **Facade** para las contraseñas. Los indicadores del administrador se actualizan en tiempo real con **Server-Sent Events**, y las fotos y vídeos se guardan en **Cloudinary**.

📖 [Arquitectura](https://github.com/duran-ni/web-educAlba-backend/wiki/Arquitectura) · [Modelo de datos](https://github.com/duran-ni/web-educAlba-backend/wiki/Modelo-de-datos) · [Tiempo real con SSE](https://github.com/duran-ni/web-educAlba-backend/wiki/Tiempo-real-con-SSE) · [Galería y Cloudinary](https://github.com/duran-ni/web-educAlba-backend/wiki/Galería-y-Cloudinary)

---

## 🛡️ Seguridad

- Login con HTTP Basic una sola vez y, después, **sesión por cookie `HttpOnly`**, con opción de mantenerla 14 días.
- **Permisos por rol**: `/api/admin/**` para el administrador y `/api/dashboard/**` para las familias.
- **Contraseñas cifradas con BCrypt** y credenciales fuera del código, en variables de entorno.
- **Recuperación de contraseña** con token de un solo uso que caduca en una hora.

📖 [Seguridad](https://github.com/duran-ni/web-educAlba-backend/wiki/Seguridad)

---

## 📐 Diagramas

Casos de uso, entidad-relación (Chen), base de datos (pata de gallo), clases y secuencia.

📖 [Diagramas](https://github.com/duran-ni/web-educAlba-backend/wiki/Diagramas)

---

## ✅ Tests

**203 tests, todos pasados**: 129 de integración, 19 de repositorio y 55 unitarios. Los de integración y repositorio se ejecutan contra un PostgreSQL real con **Testcontainers**.

| Instrucciones | Ramas | Líneas | Métodos |
|---------------|-------|--------|---------|
| 99,5 % | 100 % | 99,8 % | 98,9 % |

```bash
mvnw.cmd test      # ejecutar los tests
mvnw.cmd verify    # tests + comprobación de cobertura mínima (70 %)
```

📖 [Testing](https://github.com/duran-ni/web-educAlba-backend/wiki/Testing)

---

## 📊 Gestión del proyecto

Proyecto individual gestionado con metodología ágil en **Jira**: 17 épicas, 59 historias de usuario con criterios de aceptación en formato Gherkin y 217 tareas y subtareas, en 3 sprints.

📖 [Gestión del proyecto](https://github.com/duran-ni/web-educAlba-backend/wiki/Gestión-del-proyecto)

---

## 🌿 Control de versiones

- **Git Flow simplificado**: `main` (estable) ← `dev` (integración) ← una rama por tarea.
- **Commits atómicos** con [Conventional Commits](https://www.conventionalcommits.org/es/) (`feat(gallery):`, `test(teacher):`…).
- **Integración continua** con GitHub Actions: tests y empaquetado en cada `push` a `dev` y `main`.

📖 [Flujo de trabajo](https://github.com/duran-ni/web-educAlba-backend/wiki/Flujo-de-trabajo)

---

## 📖 Documentación

| Página de la wiki | Contenido |
|-------------------|-----------|
| [Guía de instalación](https://github.com/duran-ni/web-educAlba-backend/wiki/Guía-de-instalación) | Requisitos, variables de entorno, arranque y comprobaciones |
| [Gestión del proyecto](https://github.com/duran-ni/web-educAlba-backend/wiki/Gestión-del-proyecto) | Jira, épicas, historias de usuario y sprints |
| [Arquitectura](https://github.com/duran-ni/web-educAlba-backend/wiki/Arquitectura) | Tecnologías, módulos, capas, DTOs, patrón Facade y errores |
| [Modelo de datos](https://github.com/duran-ni/web-educAlba-backend/wiki/Modelo-de-datos) | Tablas, relaciones y datos iniciales |
| [Diagramas](https://github.com/duran-ni/web-educAlba-backend/wiki/Diagramas) | Casos de uso, Chen, base de datos, clases y secuencia |
| [API REST](https://github.com/duran-ni/web-educAlba-backend/wiki/API-REST) | Todos los endpoints con ejemplos |
| [Seguridad](https://github.com/duran-ni/web-educAlba-backend/wiki/Seguridad) | Login, roles, sesión, CORS, CSRF y contraseñas |
| [Tiempo real con SSE](https://github.com/duran-ni/web-educAlba-backend/wiki/Tiempo-real-con-SSE) | Indicadores del administrador en tiempo real |
| [Galería y Cloudinary](https://github.com/duran-ni/web-educAlba-backend/wiki/Galería-y-Cloudinary) | Subida de fotos y vídeos, portadas y paginación |
| [Testing](https://github.com/duran-ni/web-educAlba-backend/wiki/Testing) | Tipos de test, Testcontainers y cobertura |
| [Cómo añadir un módulo](https://github.com/duran-ni/web-educAlba-backend/wiki/Cómo-añadir-un-módulo) | Receta para crear un recurso nuevo |
| [Decisiones técnicas](https://github.com/duran-ni/web-educAlba-backend/wiki/Decisiones-técnicas) | Por qué se eligió cada solución |
| [Flujo de trabajo](https://github.com/duran-ni/web-educAlba-backend/wiki/Flujo-de-trabajo) | Ramas, commits e integración continua |
| [Limitaciones y próximos pasos](https://github.com/duran-ni/web-educAlba-backend/wiki/Limitaciones-y-próximos-pasos) | Lo que queda por hacer |
| [Solución de problemas](https://github.com/duran-ni/web-educAlba-backend/wiki/Solución-de-problemas) | Errores frecuentes |

---

## ✍️ Autora

**Nieves Durán** — [@duran-ni](https://github.com/duran-ni)

Proyecto final del Bootcamp Java Fullstack de Factoría F5.
