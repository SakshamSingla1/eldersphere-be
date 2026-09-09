# ElderSphere Backend (eldersphere-be)

Spring Boot backend for **ElderSphere**, an elderly-care marketplace: family members discover
and book verified caretakers for elders (nursing, physiotherapy, medication assistance,
companion care), share medical records, and trigger emergency alerts.

Architecture is deliberately mirrored from an existing internal portfolio-builder backend
(`portfolio-be`) — same package layout, layering style (`entity -> repository -> dao -> dto ->
service -> controller`), `ApiResponse`/`ResponseModel` response envelope, `GenericException` +
`GlobalExceptionHandler` error handling, and JWT + refresh-token auth approach — applied to the
elder-care domain with PostgreSQL and env-var-driven configuration.

## Tech stack

- Java 17, Spring Boot 3.5.7
- Spring Web, Spring Data JPA, Spring Security, Bean Validation (`jakarta.validation`)
- PostgreSQL (via env vars, no hardcoded credentials) + Flyway migrations
- JWT auth (jjwt) with httpOnly access/refresh cookies, BCrypt password hashing
- springdoc-openapi / Swagger UI
- Caffeine cache (public landing page), Bucket4j rate limiting on sensitive endpoints
- Local-disk file storage (no external object storage dependency)

## Running locally

1. Provide a PostgreSQL database and export the required environment variables (or copy
   `src/main/resources/application-dev.properties.example` to `application-dev.properties`
   and fill it in — that file is git-ignored).
2. Run `./mvnw spring-boot:run` (or `mvn spring-boot:run`).

### Required environment variables

| Variable | Purpose | Example |
|---|---|---|
| `DATABASE_URL` | JDBC URL for PostgreSQL | `jdbc:postgresql://localhost:5432/eldersphere` |
| `DATABASE_USER` | DB username | `eldersphere` |
| `DATABASE_PASSWORD` | DB password | `changeme` |
| `JWT_SECRET` | Base64-encoded HMAC secret, 32+ bytes | (generate with `openssl rand -base64 32`) |
| `APP_FRONTEND_URL` | Comma-separated allowed CORS origins | `http://localhost:5173,http://localhost:3000` |

### Optional environment variables (sensible defaults exist)

| Variable | Default | Purpose |
|---|---|---|
| `SERVER_PORT` | `8080` | HTTP port |
| `APP_URL` | `http://localhost:8080` | Public base URL of this API, used to build file URLs |
| `JWT_ACCESS_TOKEN_EXPIRATION` | `36000000` (10h, ms) | Access token lifetime |
| `JWT_REFRESH_TOKEN_EXPIRATION_DAYS` | `7` | Refresh token lifetime |
| `MAIL_HOST` / `MAIL_PORT` / `MAIL_USERNAME` / `MAIL_PASSWORD` | Gmail SMTP defaults | Outbound email (password reset) |
| `MAIL_FROM_ADDRESS` | `no-reply@eldersphere.app` | From address on emails |
| `PASSWORD_RESET_TOKEN_EXPIRY_MINUTES` | `30` | Password reset link lifetime |
| `PASSWORD_RESET_URL` | `http://localhost:5173/reset-password` | Frontend page the reset link points to |
| `FILE_UPLOAD_DIR` | `uploads` | Local disk directory for uploaded files |
| `FILE_PUBLIC_BASE_URL` | `${APP_URL}/uploads` | Public URL prefix for stored files |
| `EMERGENCY_RESPONSE_SLA_MINUTES` | `5` | Fallback SLA shown if no `PlatformSettings` row exists |
| `VAPID_PUBLIC_KEY` / `VAPID_PRIVATE_KEY` | *(generated)* | Base64url (unpadded) VAPID EC key pair used to authenticate outbound Web Push messages. If unset, a fresh key pair is generated at startup and logged at WARN (fine for local dev; production deployments should pin real values so browser subscriptions survive a restart) |
| `VAPID_SUBJECT` | `mailto:admin@eldersphere.app` | A `mailto:`/`https:` URL identifying the operator, required by the Web Push spec |
| `SENTRY_DSN` | *(unset)* | Free-tier error tracking (sentry.io) — the starter no-ops entirely when this is blank |
| `SENTRY_ENVIRONMENT` | `development` | Environment tag attached to reported Sentry events |
| `GEOCODING_CONTACT_EMAIL` | `admin@eldersphere.app` | Contact identifier sent in the User-Agent header of every OpenStreetMap Nominatim request (their usage policy requires this), see `GeocodingServiceImpl` |
| `DB_POOL_MAX_SIZE` / `DB_POOL_MIN_IDLE` | `10` / `2` | HikariCP pool sizing |

Uses `hibernate.ddl-auto=validate` + **Flyway** (matching the reference project) — schema is
defined entirely by the migrations in `src/main/resources/db/migration/`.

Swagger UI: `http://localhost:8080/swagger-ui.html` · OpenAPI JSON: `/v3/api-docs`

## REST endpoints by module

All routes are prefixed `/api/v1` unless noted. Admin-only routes require a JWT for a user
whose effective role is `ADMIN`.

**Auth** (`/auth`) — public
`POST /register` · `POST /login` · `POST /refresh` · `POST /logout` ·
`POST /forgot-password` · `GET /validate-reset-token` · `POST /reset-password` ·
`PUT /change-password` (authenticated)

**Admin — Users** (`/admin/users`) — admin only
`POST /` create · `GET /` list/filter (search, userType, status, paged) · `GET /{id}` ·
`PUT /{id}/status` · `PUT /bulk-status` · `PUT /{id}/role` · `DELETE /{id}`

**Roles** (`/roles`) — admin only: `POST /` · `PUT /{id}` · `GET /{id}` · `GET /` · `DELETE /{id}`

**Permissions** (`/permissions`) — admin only
`POST /` · `GET /` · `DELETE /{id}` · `POST /assign` (role+permission) ·
`DELETE /roles/{roleId}/{permissionId}` · `GET /roles/{roleId}`

**Caretakers** (`/caretakers`)
`PUT /me` create/update own profile (caretaker) · `GET /me` · `GET /{id}` ·
`PUT /{id}/verification` (admin)

**Elder Profiles** (`/elder-profiles`) — authenticated (family)
`POST /` · `PUT /{id}` · `GET /{id}` · `GET /` (mine) · `DELETE /{id}`

**Service Offerings** (`/services`) — catalog is publicly readable, writes are admin-only
`POST /` (admin) · `PUT /{id}` (admin) · `GET /{id}` · `GET /` (filter by category, paged) ·
`DELETE /{id}` (admin)

**Bookings** (`/bookings`) — authenticated
`POST /` · `PUT /{id}/status` · `GET /{id}` · `GET /` (filter by familyUserId/caretakerId/status, paged)

**Medical Records** (`/medical-records`) — authenticated
`POST /` · `PUT /{id}` · `GET /{id}` · `GET /elder/{elderProfileId}` (`sharedOnly` flag) · `DELETE /{id}`

**Reviews** (`/reviews`)
`POST /` (authenticated, one per completed booking) · `GET /caretaker/{caretakerId}` (public, paged)

**Emergency Alerts** (`/emergency-alerts`) — authenticated
`POST /` trigger · `PUT /{id}/status` acknowledge/resolve · `GET /{id}` ·
`GET /` (filter by elderProfileId/status, paged)

**Notifications** (`/notifications`) — authenticated
`GET /` · `GET /unread-count` · `PUT /{id}/read` · `PUT /read-all` · `DELETE /{id}`

**Notification preferences & Web Push** (`/users/me`) — authenticated unless noted
`GET /notification-preferences` · `PUT /notification-preferences` ·
`POST /push-subscriptions` register a browser Web Push subscription (idempotent on `endpoint`) ·
`DELETE /push-subscriptions` unsubscribe (body `{"endpoint": "..."}`, no-op if absent)
`GET /push/vapid-public-key` (public) — the VAPID public key the frontend passes to
`pushManager.subscribe({ applicationServerKey })`

**Landing Page** (`/landing`)
`GET /page` (public, cached aggregate of config+features+faqs+testimonials) ·
admin-only CRUD under `/config`, `/features`, `/faqs`, `/testimonials`

**Contact Us** (`/contact-us`)
`POST /` (public submission) · `GET /` (admin, search/filter/paged) ·
`PUT /{id}/status` (admin) · `DELETE /{id}` (admin)

**Files** (`/files`) — authenticated
`POST /` multipart upload (`file`, `resourceType`) · `GET /{id}` · `DELETE /{id}`
Uploaded files are served statically from `GET /uploads/**`.

**Platform Settings** (`/platform-settings`)
`GET /` (public) · `PUT /` (admin)

**Dashboard** (`/dashboard`) — admin only
`GET /summary` — total bookings, active (verified) caretakers, pending emergency alerts,
total elders/families, revenue this month & last 30 days, recent activity feed

**Search** (`/search`) — public
`GET /caretakers` — filter by service category, minimum rating, verification status (paged)

**Health**
`GET /api/v1/health` — public liveness check

All list endpoints accept standard Spring Data `Pageable` params (`page`, `size`, `sort`).
All write endpoints validate request bodies via `jakarta.validation` annotations and return a
`400` with field-level messages on failure (via `GlobalExceptionHandler`).

## Scope decisions

This is a deliberate trim of the reference project's ~30-module portfolio-CMS surface down to
what an elder-care marketplace MVP actually needs. Everything below was intentionally cut or
simplified, and why:

- **OTP / 2FA / TOTP / phone-login dropped.** The reference project's phone+OTP login, email OTP
  verification, TOTP-based 2FA setup, and QR-code generation (zxing) add real complexity
  (Resend/SMS integration, OTP store, rate-limited resend flows) that isn't core to validating
  an elder-care booking marketplace. Auth here is register/login/JWT+refresh/forgot-password/
  reset-password/change-password only. `UserStatusEnum.PENDING_VERIFICATION` is kept in the
  enum for a future email-verification flow, but self-registration currently activates
  accounts immediately.
- **Email change flow dropped** for the same reason (it depended on the OTP infrastructure above).
- **Cloudinary → local disk.** File uploads are stored under `app.file-storage.upload-dir`
  (default `uploads/`) and served via a Spring `ResourceHandler` at `/uploads/**`, instead of
  the reference project's Cloudinary SDK integration. This removes an external service
  dependency for local development and this MVP's scope; swapping in S3/Cloudinary later only
  touches `FileStorageServiceImpl`.
- **Blog, Resume, GitHub integration, Publication, TestimonialRequestLink dropped** — these are
  portfolio-builder-specific content modules (blog posts, resume PDF export/download tracking,
  GitHub OAuth + repo stats, academic publications, testimonial-request magic links) with no
  equivalent in an elder-care marketplace.
- **`Review` vs `LandingTestimonial` kept distinct**, matching the reference project's split:
  `Review` is the operational, booking-linked rating a family leaves for a caretaker; the
  `Landing*` tables are curated, admin-authored homepage content (which may or may not be
  sourced from real reviews).
- **No SMS.** SMS delivery is simulated (a DB delivery-log row + INFO log line, no real Twilio
  call) - see `NotificationChannelEnum`. **Web Push is real**, however: a per-user
  `PushSubscription` (the standard browser Push API subscription shape) is sent a genuine
  VAPID-authenticated push via `nl.martijndwars:web-push`, gated by that user's `WEB_PUSH`
  notification preference; a subscription the push service reports as gone (404/410) is deleted
  automatically, and any send failure is swallowed so it never breaks notification creation.
- **Thymeleaf email templates dropped** in favor of small inline HTML strings in
  `EmailServiceImpl` — there is only one transactional email (password reset) in this MVP, so a
  templating engine dependency wasn't justified.
- **`java.version` set to 17, not 21** (the reference project's setting) — 21 wasn't available
  in this build environment; Spring Boot 3.5.7 fully supports 17, so this is a build-environment
  accommodation, not a scope cut.

## Project layout

```
src/main/java/com/eldersphere/
  audit/          Auditable base entity + AuditorAwareImpl (JPA auditing)
  config/         Security, Swagger, CORS, cache, async, static file serving
  controllers/    REST controllers (one per module)
  dao/            Thin repository-wrapping DAO layer (per-module subpackages)
  dtos/           Request/response DTOs (per-module subpackages)
  entities/       JPA entities
  enums/          Domain + exception-code enums
  exceptions/     GenericException + GlobalExceptionHandler
  filter/         AuditFilter, NotFoundFilter, RateLimitingFilter
  payload/        ApiResponse / ResponseModel response envelope
  repositories/   Spring Data JPA repositories
  security/       JwtUtil, JwtAuthFilter, CustomUserDetailsService
  services/       Service interfaces + impl/ subpackage
  utils/          Helper (auth-context extraction from JWT/SecurityContext)
src/main/resources/
  application.properties               env-var-driven base config
  application-{dev,prod}.properties.example   copy to .properties locally (git-ignored)
  db/migration/                        Flyway migrations V1..V22
```
