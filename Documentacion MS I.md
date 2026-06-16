# DOCUMENTO DE ESPECIFICACIÓN DE REQUISITOS – API REST M3 ALMACENAMIENTO SYSTEM

## 1. INTRODUCCIÓN {#introduccion}

El presente documento describe la especificación de la API REST del sistema **M3 Almacenamiento System**, una aplicación diseñada para la gestión integral de un negocio de alquiler de baúleras. El sistema permite administrar usuarios, baúleras, tipos de baúlera, deudas mensuales, remitos y logs de auditoría, con automatizaciones vía cron y notificaciones por correo electrónico. El documento está dirigido principalmente a los administradores del sistema y al equipo de desarrollo, como guía para el uso y mantenimiento de la API.

### 1.1 Propósito {#proposito}

El propósito de la API es exponer los servicios del backend para que tanto el frontend (web o app) como los administradores puedan interactuar con el negocio. Entre las funcionalidades principales se incluyen: autenticación con JWT, operaciones CRUD completas sobre usuarios y baúleras, cálculo y actualización automática de deudas, generación y descarga de remitos en PDF, dashboards de métricas, y envío automático de correos electrónicos ante eventos clave (alta de usuario, asignación de baúlera, deuda mensual).

### 1.2 Alcance

El sistema M3 Almacenamiento System es una API REST monolítica independiente. No depende de sistemas externos más que del servicio de correo de Google (SMTP) para el envío de emails. Toda la lógica de negocio, base de datos y reglas se ejecutan dentro del mismo entorno.

**_Qué hace el sistema:_**

- CRUD completo de usuarios, baúleras, tipos de baúlera (administrador).

- Gestión de deudas: suma automática mensual mediante cron, reducción manual por parte del administrador.

- Generación automática de remitos cada mes al actualizar deudas.

- Generación manual de remitos en PDF para un usuario específico.

- Dashboards: administrativo (métricas globales) y de usuario (su situación personal).

- Logs de auditoría mediante triggers en base de datos.

- Envío de emails de bienvenida, asignación de baúlera y notificación de deuda.

**_Qué no hace:_**

- No procesa pagos reales (el administrador reduce la deuda manualmente).

- No permite que los usuarios se registren solos (el administrador los da de alta).

**_Beneficios:_**

- Organización total del negocio.

- Reducción de morosidad mediante registro claro de deudas.

- Automatización de tareas repetitivas (cálculo de deuda, envío de emails, generación de remitos).

### 1.3 Definiciones, Acrónimos y Abreviaturas

| Término   | Definición                                                                            |
| :-------- | :------------------------------------------------------------------------------------ |
| API REST  | Interfaz de programación que sigue el estilo arquitectónico REST, usando HTTP y JSON. |
| JWT       | JSON Web Token, utilizado para autenticación stateless.                               |
| CRON      | Tarea programada que se ejecuta automáticamente en una fecha/hora fija.               |
| Remito    | Documento en PDF que detalla la deuda y las baúleras asignadas a un usuario.          |
| ADMIN     | Rol con permisos totales sobre el sistema.                                            |
| SEMIADMIN | Rol con permisos de solo lectura sobre baúleras 1 a 32 y tipos de baúlera.            |
| USER      | Rol con permisos limitados a consultar su propia deuda y baúleras.                    |

### 1.4 Referencias

- Apuntes de la cátedra "Programación III / Metodología de Sistemas I" – desarrollo de API REST.

- Documentación de JWT (jwt.io).

- Documentación de generación de PDF con librerías Java (iText/OpenPDF).

## 2. DESCRIPCIÓN GENERAL DEL SISTEMA

### 2.1 Perspectiva del Producto

El sistema M3 Almacenamiento System es una aplicación independiente. No requiere integración con APIs externas, salvo el servicio SMTP de Google para el envío de correos electrónicos. La lógica de negocio, la base de datos relacional (MySQL/MariaDB) y los procesos programados (cron) residen en el mismo servidor o entorno de ejecución.

### 2.2 Funciones del Sistema

Las funciones difieren según el rol del usuario:

**_Usuario Administrador (ADMIN):_**

- CRUD completo sobre usuarios, baúleras, tipos de baúlera.

- Asignar y desasignar baúleras a usuarios.

- Reducir manualmente la deuda acumulada de un usuario.

- Visualizar dashboard con métricas (total usuarios, baúleras totales/ocupadas, valor ocupadas, deuda total).

- Generar manualmente un remito en PDF para un usuario específico.

- Consultar logs de auditoría.

**_Usuario SemiAdministrador (SEMIADMIN):_**

- Consultar listado de baúleras (solo las numeradas del 1 al 32) y tipos de baúlera.

- No puede realizar operaciones de escritura (INSERT, UPDATE, DELETE).

**_Usuario Estándar (USER):_**

- Visualizar su propio dashboard (deuda actual, baúleras asignadas, monto mensual a pagar).

- Descargar sus propios remitos en PDF.

- No puede modificar datos.

**_Sistema (automatizado):_**

**Ejecutar tarea programada (cron) el primer día de cada mes a las 6:00 AM para:**

- Calcular la deuda mensual de cada usuario según las baúleras que tiene asignadas.
- Acumular la deuda en el campo `deuda_acumulada` del usuario.
- Generar un registro en la tabla `remito` por cada usuario afectado.
- Enviar un email de notificación a cada usuario con el detalle de la deuda.

### 2.3 Objetivos

El objetivo principal es la organización de los datos del negocio, brindando control total al administrador y transparencia al usuario, reduciendo la morosidad y la carga operativa manual. El sistema permite conocer en todo momento quién debe plata, qué baúleras están ocupadas, y generar métricas para la toma de decisiones.

### 2.4 Características de los Usuarios

- **ADMIN:** Dueño del negocio o asistente administrativo con conocimientos básicos de informática. Acceso total.

- **SEMIADMIN:** Socio del dueño, con acceso restringido a solo lectura de un subconjunto de baúleras (1 a 32).

- **USER:** Cliente que alquila baúleras. No requiere conocimientos técnicos; accede mediante una interfaz web/app sencilla para consultar su situación.

## 3. DEFINICIÓN DE REQUISITOS DEL SISTEMA

### 3.1 Requisitos Funcionales (RF)

Se presentan a continuación los requisitos funcionales más representativos del sistema, organizados por operación principal.

| ID    | Nombre                                | Descripción                                                                                                             | Rol                                                             |
| :---- | :------------------------------------ | :---------------------------------------------------------------------------------------------------------------------- | :-------------------------------------------------------------- |
| RF-01 | Iniciar sesión                        | El usuario se autentica con email y contraseña; el sistema devuelve un JWT.                                             | ADMIN, SEMIADMIN, USER                                          |
| RF-02 | Registrar usuario (alta)              | El administrador crea un usuario. Se genera contraseña automática = DNI. Se envía email de bienvenida con credenciales. | ADMIN                                                           |
| RF-03 | Actualizar datos de usuario           | El administrador modifica nombre, teléfono, email, etc.                                                                 | ADMIN                                                           |
| RF-04 | Asignar baúlera a usuario             | El administrador asigna una baúlera a un usuario. Se envía email de confirmación.                                       | ADMIN                                                           |
| RF-05 | Listar baúleras (con filtro)          | El sistema permite listar baúleras paginadas y filtradas por número, usuario o tipo.                                    | ADMIN, SEMIADMIN (solo baúleras 1-32), USER (solo sus baúleras) |
| RF-06 | Crear baúleras en lote                | El administrador crea múltiples baúleras del mismo tipo de una sola vez (rango de números).                             | ADMIN                                                           |
| RF-07 | Calcular deuda mensual (cron)         | El sistema, el día 1 de cada mes a las 6:00 AM, suma la deuda mensual a cada usuario según las baúleras asignadas.      | Sistema                                                         |
| RF-08 | Generar remito automático             | Al actualizar la deuda (cron), se genera un registro de remito asociado al usuario.                                     | Sistema                                                         |
| RF-09 | Descargar remito en PDF               | El administrador o el usuario pueden descargar el PDF de un remito (usuario solo el suyo).                              | ADMIN, USER                                                     |
| RF-10 | Ver dashboard administrativo          | El administrador visualiza métricas: total de usuarios, baúleras totales/ocupadas, valor ocupadas, deuda total, etc.    | ADMIN                                                           |
| RF-11 | Ver dashboard de usuario              | El usuario visualiza su deuda actual, sus baúleras asignadas y el monto a pagar mensual.                                | USER                                                            |
| RF-12 | Validar unicidad de número de baúlera | El sistema no permite crear o actualizar una baúlera con un número que ya exista.                                       | Sistema (backend)                                               |
| RF-13 | Devolver errores estructurados        | Todos los endpoints responden con un formato JSON consistente en caso de error (código, mensaje, timestamp, path).      | Sistema                                                         |

### 3.2 Requisitos No Funcionales (RNF)

| ID      | Descripción                                                                                                                                                                                                                                   |
| :------ | :-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| RNF-001 | La API será implementada como RESTful, con comunicación vía HTTPS, formato JSON, y códigos de estado HTTP estándar.                                                                                                                           |
| RNF-002 | La autenticación será stateless mediante JWT, con expiración configurable (8 horas por defecto).                                                                                                                                              |
| RNF-003 | El sistema ejecutará una tarea programada (cron) el primer día de cada mes a las 6:00 AM (huso horario del servidor) para actualizar deudas y generar remitos.                                                                                |
| RNF-004 | Los correos electrónicos se enviarán mediante SMTP de Google (Gmail) usando una cuenta de servicio configurada.                                                                                                                               |
| RNF-005 | Los logs de auditoría se registrarán automáticamente mediante triggers en la base de datos para las operaciones de INSERT, UPDATE y DELETE sobre las tablas `usuarios`, `baulera`, `tipo_baulera`.                                            |
| RNF-006 | La generación de PDF se realizará del lado del servidor con una librería Java (iText/OpenPDF). Los remitos generados mensualmente se persistirán en la tabla `remito`; los descargables se generan bajo demanda sin almacenamiento adicional. |
| RNF-007 | El acceso a los endpoints será restringido según rol mediante un filtro/middleware que valide el JWT y los permisos.                                                                                                                          |
| RNF-008 | La API deberá configurar CORS para permitir peticiones desde un frontend específico (dominio configurable).                                                                                                                                   |
| RNF-009 | La base de datos será relacional (MySQL/MariaDB) con el esquema reflejado en el diagrama DER (Figura 2).                                                                                                                                      |
| RNF-010 | Todos los endpoints devolverán errores en un formato estructurado: `{ "timestamp": "...", "codigo": 400, "error": "mensaje", "path": "/..." }`.                                                                                               |

## 4. DIAGRAMA DE CASO DE USO

El diagrama de casos de uso del sistema se presenta en la Figura 1 (elaborado por el alumno). A continuación se especifica en detalle el caso de uso “Actualización automática de deuda mensual (cron)”, por ser el proceso central automatizado del sistema.

### 4.1 Especificación del Caso de Uso: Actualización automática de deuda mensual

| Campo               | Valor                                                                                                                                                                                                                                                                                                                                                                                                 |
| :------------------ | :---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Identificador**   | RF-07 / UC-001                                                                                                                                                                                                                                                                                                                                                                                        |
| **Nombre**          | Actualización automática de deuda mensual                                                                                                                                                                                                                                                                                                                                                             |
| **Actor**           | Sistema (tarea programada - cron)                                                                                                                                                                                                                                                                                                                                                                     |
| **Prioridad**       | Alta                                                                                                                                                                                                                                                                                                                                                                                                  |
| **Objetivo**        | Calcular y acumular la deuda mensual de cada usuario en función de las baúleras que tiene asignadas al inicio de cada mes, generar el remito correspondiente y notificar por email.                                                                                                                                                                                                                   |
| **Precondiciones**  | - Es el primer día del mes, 6:00 AM.<br>- Existen usuarios activos.<br>- Existen baúleras ocupadas (con `usuario_asignado` no nulo).<br>- La base de datos está operativa.                                                                                                                                                                                                                            |
| **Postcondiciones** | - Para cada usuario con baúleras asignadas:<br> &nbsp;&nbsp;• Se incrementa `deuda_acumulada` en el monto mensual de sus baúleras.<br> &nbsp;&nbsp;• Se genera un registro en la tabla `remito` con el periodo, importe total y detalle.<br> &nbsp;&nbsp;• Se envía un correo electrónico con el resumen de deuda.<br>- Si un usuario no tiene baúleras, no se modifica su deuda ni se genera remito. |

#### Camino de éxito (flujo principal)

| Paso | Acción del actor (sistema)                                                            | Acción del sistema (backend)                                                                                                    |
| :--: | :------------------------------------------------------------------------------------ | :------------------------------------------------------------------------------------------------------------------------------ |
|  1   | El scheduler ejecuta el método `actualizarDeudas()` según cron `0 0 6 1 * *`          | Se inicia la transacción y se registra el inicio en log.                                                                        |
|  2   | Consulta todas las baúleras con estado "ocupada" (usuario asignado no nulo).          | El repositorio `bauleraRepositorio.findAllOcupadas()` devuelve la lista.                                                        |
|  3   | Por cada baúlera ocupada, obtiene el usuario asignado y el precio mensual de su tipo. | Calcula el monto a sumar y acumula por usuario en un mapa (`InfoDeudaEmail`).                                                   |
|  4   | Para cada usuario con al menos una baúlera:                                           | - Toma la deuda actual (o 0 si no tenía).<br>- Suma el monto mensual.<br>- Actualiza `deuda_acumulada` en la tabla `usuarios`.  |
|  5   | Llama a `emailService.enviarNotificacionDeuda(usuario, info)`                         | Construye y envía un email HTML con detalle de baúleras, deuda anterior, nuevo cargo y total.                                   |
|  6   | Llama a `remitoService.generarRemito(usuario, info)`                                  | Inserta un registro en la tabla `remito` con el periodo (ej. "2025-06"), importe total, y un string con los números de baúlera. |
|  7   | Finaliza la iteración y cierra la transacción.                                        | Registra en log la cantidad de usuarios actualizados y el total de deuda generada.                                              |

#### Flujos alternativos y excepciones

| Paso  | Disparador                                                                                                 | Acción del sistema                                                                                                                    |
| :---: | :--------------------------------------------------------------------------------------------------------- | :------------------------------------------------------------------------------------------------------------------------------------ |
| 1-EXC | No hay baúleras ocupadas (`baulerasActivas` vacío)                                                         | Se registra en log "No hay baúleras ocupadas" y finaliza la ejecución sin cambios.                                                    |
| 2-EXC | Una baúlera tiene `usuarioAsignado == null` (inconsistencia)                                               | Se registra una advertencia (warn) y se salta esa baúlera, continuando con las siguientes.                                            |
| 3-EXC | Error al actualizar la deuda de un usuario (ej. excepción de base de datos)                                | Se captura la excepción, se registra el error en log, y se continúa con el siguiente usuario (no se cancela todo).                    |
| 4-EXC | Fallo en el servicio de email para un usuario (mail inválido, servidor SMTP caído, etc.)                   | Se registra el error, pero la deuda y el remito ya fueron guardados. El usuario no recibe la notificación.                            |
| 5-EXC | Fallo al generar el remito (error de base de datos)                                                        | Se registra el error; la deuda ya fue actualizada, pero no queda registro de remito. El administrador deberá regenerarlo manualmente. |
| ALT-1 | El administrador ejecuta manualmente el método (por endpoint no expuesto originalmente, solo para pruebas) | El sistema ejecuta el mismo proceso fuera de la fecha programada, respetando las mismas reglas de negocio.                            |

## 5. DIAGRAMA DE CLASES CON LA ARQUITECTURA IMPLEMENTADA

El sistema está desarrollado en Java con Spring Boot, siguiendo una arquitectura en capas: controladores (Controllers), servicios (Services), repositorios (Repositories), entidades (Entities), mappers (Mappers) y aspectos de auditoría. A continuación se muestra el diagrama de clases generado desde el IDE IntelliJ IDEA (formato Mermaid). Figura 2 (insertar imagen convertida a PNG a partir del código Mermaid adjunto en el anexo o incrustar aquí).

**Principales entidades:**

- `Usuario`, `Baulera`, `TipoBaulera`, `Remito`, `Log`

- Controladores: `AuthController`, `UsuarioController`, `BauleraController`, `RemitoController`, `DashBoardController`

- Servicios: `UsuarioService`, `BauleraService`, `RemitoService`, `EmailService`, `PagosScheduler`

- Repositorios JPA: `UsuarioRepositorio`, `BauleraRepositorio`, `RemitoRepositorio`, `TipoBauleraRepositorio`

- Seguridad: `JwtService`, `JwtAuthFilter`, `CustomUserDetailsService`

- Auditoría: `AuditAspect`, `LogService` (con triggers en BD como respaldo).

## 6. DIAGRAMA DE ENTIDADES Y RELACIONES

La base de datos relacional MySQL está compuesta por las tablas que se muestran en la Figura 3. El diagrama fue generado con ingenieria reversa del MySQL Workbench e incluye las tablas `tipobaulera`, `baulera`, `usuarios`, `logs` y `remito`.

Descripción de las principales tablas y relaciones:

- `usuarios` : Almacena los datos de login, rol (ADMIN, SEMIADMIN, USER), estado, deuda acumulada, etc. Se relaciona con baulera (un usuario puede tener múltiples baúleras) y con remito (un usuario puede tener múltiples remitos).

- `tipobaulera` : Define los tipos de baúlera (nombre, precio mensual, descripción). Se relaciona con baulera (una baúlera pertenece a un tipo).

- `baulera` : Contiene el número, estado (ocupada/disponible/mantenimiento/reservada), referencia al usuario asignado (FK a usuarios), referencia al tipo (FK a tipobaulera), fecha de asignación, día de vencimiento, observaciones.

- `remito` : Registra cada deuda mensual generada automáticamente o remito manual. Columnas: id_remito (PK), id_usuario (FK), periodo (ej. "2025-06"), fecha_emision, importe_total, bauleras_string (concatenación de números de baúlera), deuda_anterior, id_publico.

- `logs` : Tabla de auditoría llenada por triggers después de cada operación de escritura sobre usuarios, baulera, tipobaulera. Guarda acción, valores anteriores/nuevos, usuario afectado, timestamp.

Figura 3: Diagrama de entidades y relaciones (DER) – insertar imagen proporcionada por el alumno (image.png) que incluye tipobaulera, baulera, usuarios, logs; y agregar manualmente la tabla remito según el esquema adjunto en la respuesta.

**Relaciones clave:**

- `baulera(usuario_asignado)` → `usuarios(id_usuario)` (0..\* a 1)

- `baulera(tipo_baulera)` → `tipobaulera(id_tipo_baulera)` (muchos a 1)

- `remito(id_usuario)` → `usuarios(id_usuario)` (muchos a 1)

- `logs(usuario)` → `usuarios(id_usuario)` (muchos a 1)
