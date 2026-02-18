# Documentación API - M3 Almacenamiento

## 1. Descripción general

La API de **M3 Almacenamiento** está desarrollada con Spring Boot y expone servicios REST para la gestión integral de:

- Autenticación de usuarios.
- Usuarios y su estado operativo.
- Bauleras y su asignación.
- Tipos de baulera.
- Métricas y dashboards.
- Logs operativos.

El backend utiliza JWT para autenticación stateless, Spring Security para autorización por rol y Spring Data JPA para persistencia.

---

## 2. Convenciones de consumo

- **Formato de intercambio:** JSON.
- **Header de autenticación:**
  - `Authorization: Bearer <token_jwt>`
- **Roles operativos:** `ADMIN`, `USER`, `SEMIADMIN`.
- **Estados de usuario:** `activo`, `inactivo`.
- **Estados de baulera:** `disponible`, `ocupada`, `mantenimiento`, `reservada`.

---

## 3. Seguridad y autenticación

### 3.1 Endpoints públicos

Los endpoints bajo `/auth/**` son públicos para login/registro/validación de token.

### 3.2 Endpoints protegidos

La API opera con autenticación JWT. Cuando el token es válido, se carga el usuario autenticado en contexto de seguridad.

### 3.3 Flujo recomendado de autenticación

1. Consumir `POST /auth/login` (o `POST /auth/register`).
2. Guardar `token` devuelto en `AuthResponse`.
3. Enviar `Authorization: Bearer <token>` en las siguientes llamadas.
4. Opcionalmente, validar/renovar token con `GET /auth/validate`.

---

## 4. Modelos principales

## 4.1 Requests

### `LoginRequest`

```json
{
  "email": "usuario@empresa.com",
  "password": "miPassword"
}
```

### `UsuarioRequest`

```json
{
  "dni": "30111222",
  "nombreCompleto": "Nombre Apellido",
  "email": "usuario@empresa.com",
  "telefono": "+5491122334455",
  "password": "Password123",
  "rol": "USER"
}
```

### `TipoBauleraRequest`

```json
{
  "tipoBauleraNombre": "Mediana",
  "descripcion": "Baulera mediana para uso general",
  "precioMensual": 45000.0
}
```

### `BauleraRequest`

```json
{
  "nroBaulera": "101",
  "idTipoBaulera": 1,
  "idUsuario": 25,
  "diaVencimientoPago": 10,
  "observaciones": "Asignación inicial"
}
```

## 4.2 Responses

### `AuthResponse`

```json
{
  "token": "jwt_token",
  "usuario": {
    "id": 25,
    "dni": "30111222",
    "nombreCompleto": "Nombre Apellido",
    "email": "usuario@empresa.com",
    "telefono": "+5491122334455",
    "rol": "USER",
    "estado": "activo"
  }
}
```

### `PaginacionResponse<T>`

```json
{
  "contenido": [],
  "pagina": 1,
  "tamanio": 15,
  "totalElementos": 100,
  "totalPaginas": 7,
  "esUltima": false,
  "esPrimera": true
}
```

---

## 5. Endpoints

## 5.1 Autenticación (`/auth`)

### `POST /auth/login`

Autentica un usuario por email y contraseña.

- **Body:** `LoginRequest`
- **Response:** `AuthResponse`

### `POST /auth/register`

Registra un usuario y devuelve token de sesión.

- **Body:** `UsuarioRequest`
- **Response:** `AuthResponse`

### `GET /auth/validate`

Valida token vigente y retorna token renovado.

- **Header requerido:** `Authorization: Bearer <token>`
- **Response OK:** token renovado (string)
- **Response error:** `401 Unauthorized`

---

## 5.2 Usuarios (`/users`)

### `GET /users/admin/getAll`

Obtiene todos los usuarios.

- **Rol:** ADMIN
- **Response:** `List<UsuarioResponse>`

### `GET /users/admin/getPagina`

Obtiene usuarios paginados y opcionalmente filtrados.

- **Rol:** ADMIN
- **Query params:**
  - `pagina` (default `1`)
  - `tamanio` (default `15`)
  - `sortBy` (default `idUsuario`; también `email`, `deudaAcumulada`)
  - `direction` (`asc`/`desc`, default `desc`)
  - `filter` (texto de búsqueda)
- **Response:** `PaginacionResponse<UsuarioResponse>`

### `GET /users/user/contrasenia-dni`

Indica si la contraseña actual coincide con el DNI del usuario autenticado.

- **Rol:** USER/ADMIN
- **Response:** `Boolean`

### `POST /users/admin/alta/usuario`

Crea un usuario en estado activo.

- **Rol:** ADMIN
- **Body:** `UsuarioRequest`
- **Response:** `UsuarioResponse`

### `PATCH /users/admin/alta/usuarioCreado/{idUsuario}`

Activa un usuario existente.

- **Rol:** ADMIN
- **Path param:** `idUsuario`
- **Response:** `UsuarioResponse`

### `PATCH /users/admin/baja/usuario/{idUsuario}`

Pasa un usuario a estado inactivo y desasigna sus bauleras.

- **Rol:** ADMIN
- **Path param:** `idUsuario`
- **Response:** `UsuarioResponse`

### `PATCH /users/admin/asignarBauleras/{idUsuario}`

Asigna múltiples bauleras a un usuario.

- **Rol:** ADMIN
- **Path param:** `idUsuario`
- **Body:** `List<Long>` con IDs de bauleras
- **Response:** `UsuarioResponse`

### `PATCH /users/admin/reducirDeuda/{idUsuario}?montoAReducir=...`

Reduce deuda acumulada del usuario.

- **Rol:** ADMIN
- **Path param:** `idUsuario`
- **Query param:** `montoAReducir` (entero > 0)
- **Response:** `UsuarioResponse`

### `PATCH /users/user/cambiarContrasenia`

Actualiza contraseña del usuario autenticado.

- **Rol:** USER/ADMIN
- **Body:** string con nueva contraseña
- **Response:** `Boolean`

---

## 5.3 Bauleras (`/baulera`)

### `GET /baulera/admin/obtenerTodos`

Lista todas las bauleras.

- **Rol:** ADMIN
- **Response:** `List<BauleraResponse>`

### `GET /baulera/admin/obtenerDisponibles`

Lista bauleras en estado disponible.

- **Rol:** ADMIN
- **Response:** `List<BauleraResponse>`

### `GET /baulera/user/obtener-x-id/{idBaulera}`

Obtiene baulera por ID.

- **Rol:** ADMIN (validación interna de servicio)
- **Path param:** `idBaulera`
- **Response:** `BauleraResponse`

### `GET /baulera/user/obtenerTodo/{idUsuario}`

Obtiene bauleras de un usuario.

- **Rol:** ADMIN o mismo usuario
- **Path param:** `idUsuario`
- **Response:** `List<BauleraResponse>`

### `GET /baulera/admin/obtenerTodo/pagina`

Obtiene bauleras paginadas y filtrables.

- **Rol:** ADMIN
- **Query params:**
  - `pagina` (default `1`)
  - `tamanio` (default `15`)
  - `sortBy` (default `idBaulera`; también `nroBaulera`, `tipoBauleraNombre`, `estadoBaulera`, `nombreUsuario`)
  - `direction` (`asc`/`desc`, default `desc`)
  - `filter` (texto de búsqueda)
- **Response:** `PaginacionResponse<BauleraResponse>`

### `GET /baulera/semiadmin/obtenerTodo`

Lista de bauleras para operación semiadmin.

- **Rol:** SEMIADMIN
- **Response:** `List<BauleraResponse>`

### `POST /baulera/admin/new-baulera`

Crea una baulera individual.

- **Rol:** ADMIN
- **Body:** `BauleraRequest`
- **Response:** `BauleraResponse`

### `POST /baulera/admin/crear-lote?cantidad=...&tipoBauleraId=...`

Crea bauleras en lote con numeración secuencial.

- **Rol:** ADMIN
- **Query params:**
  - `cantidad` (>0 y <=50)
  - `tipoBauleraId`
- **Response:** `List<BauleraResponse>`

### `PATCH /baulera/admin/desasignar/{idBaulera}`

Desasigna usuario de una baulera y la vuelve disponible.

- **Rol:** ADMIN
- **Path param:** `idBaulera`
- **Response:** `BauleraResponse`

### `PATCH /baulera/admin/asignarBaulera?idBaulera=...&idUsuario=...`

Asigna una baulera a un usuario.

- **Rol:** ADMIN
- **Query params:** `idBaulera`, `idUsuario`
- **Response:** `BauleraResponse`

### `DELETE /baulera/admin/eliminar/{idBaulera}`

Elimina una baulera.

- **Rol:** ADMIN
- **Path param:** `idBaulera`
- **Response:** `204 No Content` en éxito

---

## 5.4 Tipos de baulera (`/tipo-baulera`)

### `GET /tipo-baulera/admin/obtener-todos`

Lista tipos de baulera.

- **Response:** `List<TipoBauleraResponse>`

### `GET /tipo-baulera/user/obtener-id/{idTipoBaulera}`

Obtiene tipo de baulera por ID.

- **Path param:** `idTipoBaulera`
- **Response:** `TipoBauleraResponse`

### `POST /tipo-baulera/admin/new-tipo`

Crea nuevo tipo de baulera.

- **Body:** `TipoBauleraRequest`
- **Response:** `TipoBauleraResponse`

### `DELETE /tipo-baulera/admin/delete/{idTipoBaulera}/cascade`

Elimina tipo de baulera y sus dependencias operativas.

- **Path param:** `idTipoBaulera`
- **Response:** `204 No Content` en éxito

---

## 5.5 Logs y dashboard (`/logs`)

### `GET /logs/admin/obtenerLogsPaginados?pagina=...&tamanio=...`

Obtiene logs paginados.

- **Response:** `PaginacionResponse<LogResponse>`

### `GET /logs/admin/obtenerLogs/insert`

Obtiene últimos logs de inserción.

- **Response:** `List<LogResponse>`

### `GET /logs/admin/obtenerLogs/update`

Obtiene últimos logs de actualización.

- **Response:** `List<LogResponse>`

### `GET /logs/admin/obtenerLogs/delete`

Obtiene últimos logs de eliminación.

- **Response:** `List<LogResponse>`

### `GET /logs/user/obtenerDashBoard/{idUsuario}`

Dashboard individual del usuario.

- **Path param:** `idUsuario`
- **Response:** `UserDashBoardResponse`

### `GET /logs/admin/obtenerDashBoard`

Dashboard general del sistema.

- **Response:** `DashBoardResponse`

---

## 6. Procesos automáticos (scheduler)

La API ejecuta tareas programadas para operación mensual:

- **Actualización de deuda mensual** el día 1 de cada mes a las 06:00.
- **Generación de notificaciones por email** con detalle de deuda.
- **Limpieza de caché de dashboard** de forma diaria (madrugada), para asegurar consistencia de métricas.

---

## 7. Notificaciones por email

El servicio de email utiliza plantillas HTML y envío con `JavaMailSender` para:

- Bienvenida al crear usuarios.
- Notificación al asignar baulera.
- Notificación de actualización de deuda mensual.

Plantillas incluidas:

- `EmailBienvenida.html`
- `EmailAsignacionDeBauleraTemplate.html`
- `EmailDeudaTemplate.html`

---

## 8. Estructura de respuestas

## 8.1 Ejemplo `BauleraResponse`

```json
{
  "idBaulera": 101,
  "nroBaulera": "101",
  "estadoBaulera": "ocupada",
  "observaciones": "Asignada",
  "idTipoBaulera": 2,
  "tipoBauleraNombre": "Grande",
  "tipoBauleraPrecio": 70000.0,
  "idUsuario": 25,
  "nombreUsuario": "Nombre Apellido",
  "emailUsuario": "usuario@empresa.com",
  "fechaAsignacion": "2026-01-10",
  "isDisponible": false,
  "isOcupada": true
}
```

## 8.2 Ejemplo `LogResponse`

```json
{
  "idLog": 889,
  "usuario": "admin@empresa.com",
  "accion": "UPDATE",
  "tablaAfectada": "baulera",
  "descripcion": "Se asignó baulera 101 al usuario 25",
  "fecha": "2026-01-10T14:22:30"
}
```

---

## 9. Códigos HTTP utilizados

- `200 OK`: operación exitosa con contenido.
- `204 No Content`: eliminación exitosa sin body.
- `401 Unauthorized`: token inválido o ausente.
- `404 Not Found`: recurso inexistente (según operación).
- `500 Internal Server Error`: error no controlado de ejecución.

---

## 10. Notas de operación

- Los endpoints con prefijo `/admin` están destinados al perfil administrativo.
- Los endpoints con prefijo `/user` operan sobre contexto autenticado o validación de identidad de usuario.
- La API integra auditoría de operaciones en entidades clave y consolidación en logs.

