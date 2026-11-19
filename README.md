# crediya-authentication

Microservicio de usuarios y autenticación de CrediYa. Registra usuarios, emite tokens JWT RS256 y
publica la clave pública con la que otros servicios los verifican.

Sigue la Clean Architecture del scaffold de Bancolombia
([Clean Architecture — Aislando los detalles](https://medium.com/bancolombia-tech/clean-architecture-aislando-los-detalles-4f9530f35d7a)).
Las dependencias solo apuntan hacia el dominio, y `DependenciasEntreCapasTest` (ArchUnit) hace
fallar el build si alguna capa lo incumple.

| Módulo | Capa | Contenido |
|---|---|---|
| `domain/model` | Dominio | `Usuario`, `Rol` y `UsuarioAutenticado`, con sus reglas (validación, quién asigna cada rol, quién consulta a quién) y los gateways (`UsuarioRepository`, `CifradorClaves`, `EmisorTokens`, `VerificadorTokens`, `ProveedorClavesPublicas`) |
| `domain/usecase` | Dominio | `RegistrarUsuarioUseCase`, `IniciarSesionUseCase`, `ConsultarUsuarioPorDocumentoUseCase`, `ConsultarUsuariosPorDocumentosUseCase` y `ObtenerClavesPublicasUseCase` |
| `infrastructure/driven-adapters/jpa-repository` | Infraestructura | Persistencia en PostgreSQL con JPA; esquema con Flyway (`db/migration`) |
| `infrastructure/driven-adapters/security` | Infraestructura | BCrypt, firma y verificación RS256 (Nimbus) y JWKS |
| `infrastructure/entry-points` | Infraestructura | Controladores REST, filtro JWT, seguridad y manejo de errores |
| `applications/app-service` | Aplicación | Arranque, configuración, registro automático de los `*UseCase` y administrador inicial |

## Endpoints

| Método y ruta | Acceso | Descripción |
|---|---|---|
| `POST /api/v1/login` | Público | Inicia sesión con `correo_electronico` y `clave`; devuelve el token |
| `POST /api/v1/usuarios` | ADMIN, ASESOR | Registra un usuario. Un ADMIN asigna cualquier rol; un ASESOR solo CLIENTE |
| `GET /api/v1/usuarios/documento/{documento}` | Autenticado | Consulta un usuario. El personal consulta a cualquiera; un CLIENTE solo a sí mismo |
| `GET /api/v1/usuarios?documentos=a,b` | ADMIN, ASESOR | Consulta por lote (máximo 100 documentos) |
| `GET /.well-known/jwks.json` | Público | Clave pública (JWKS) para verificar los tokens |
| `GET /actuator/health` | Público | Estado de salud (lo usan Docker y docker-compose) |
| `GET /swagger-ui.html` | Público | Documentación, solo con `SWAGGER_ENABLED=true` o en el perfil `local` |

Los errores siempre tienen el formato `{"estado": 409, "codigo": "CORREO_DUPLICADO", "mensaje": "..."}`.

## Configuración

Los secretos **no tienen valor por defecto**: si faltan, el servicio no arranca. Para desarrollo
local activa el perfil `local` (`SPRING_PROFILES_ACTIVE=local`), que trae valores de desarrollo.

| Variable | Obligatoria | Descripción |
|---|---|---|
| `DB_PASSWORD` | sí | Clave de PostgreSQL (`local`: `crediya_auth_pass`, la del `docker-compose.yml`) |
| `JWT_PRIVATE_KEY` | sí | Clave RSA privada en PEM PKCS#8 para firmar tokens RS256 (`local`: se genera una temporal) |
| `ADMIN_PASSWORD` | sí | Clave del administrador inicial (`local`: `Admin12345`). Solo se usa si el administrador todavía no existe |
| `ADMIN_EMAIL` | no | Correo del administrador inicial (`admin@crediya.com`) |
| `JWT_EXPIRATION_SECONDS` | no | Vigencia del token (`3600`) |
| `SWAGGER_ENABLED` | no | Publica Swagger UI y `/v3/api-docs` (`false`; `local`: `true`) |
| `DB_URL` | no | `jdbc:postgresql://localhost:5432/crediya_auth` |
| `DB_USERNAME` | no | `crediya` |
| `DB_DRIVER` / `DB_DIALECT` | no | `org.postgresql.Driver` / `org.hibernate.dialect.PostgreSQLDialect` |
| `DB_DDL_AUTO` | no | `validate`: el esquema lo crean las migraciones de Flyway |

Generar la clave JWT:

```bash
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out jwt-private.pem
```

Levanta las bases y los servicios con `docker compose up -d` desde la carpeta `CREDIYA`.

## Migraciones

| Versión | Cambio |
|---|---|
| V1 | Tabla `usuarios` con correo y documento únicos |
| V2 | `rol` obligatorio (los usuarios sin rol quedan como CLIENTE) |
| V3 | Correos en minúsculas, para que las búsquedas usen el índice único |

Las bases creadas antes de Flyway se registran en la versión 1 (`baseline-on-migrate`).

## Pruebas

```bash
./gradlew test      # unitarias, de adaptadores, de arquitectura e integración de la aplicación completa
./gradlew pitest    # pruebas de mutación (aparte, porque consumen mucha memoria)
```
