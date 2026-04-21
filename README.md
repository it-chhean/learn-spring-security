
# Spring Security JWT — Best Practice Demo

A production-ready Spring Boot 3 + Spring Security 6 project with JWT authentication,
role-based access control, and method-level security.

---

## Tech stack

| Layer       | Technology                          |
|-------------|-------------------------------------|
| Framework   | Spring Boot 3.2 / Spring Security 6 |
| Auth        | JWT (jjwt 0.12.5, HS256)            |
| Database    | H2 in-memory (swap for PostgreSQL)  |
| ORM         | Spring Data JPA / Hibernate         |
| Java        | 21                                  |
| Build       | Maven                               |

---

## Folder structure

```
src/main/java/com/yourapp/
├── config/
│   ├── SecurityConfig.java          # SecurityFilterChain, BCrypt, AuthManager
│   └── CorsConfig.java              # Allowed origins, methods, headers
├── security/
│   ├── JwtService.java              # Generate & validate JWT tokens
│   ├── JwtAuthFilter.java           # OncePerRequestFilter — reads Bearer token
│   └── CustomUserDetailsService.java
├── domain/user/
│   ├── User.java                    # Entity + UserDetails implementation
│   ├── Role.java                    # Enum: USER, ADMIN, MODERATOR
│   └── UserRepository.java
├── auth/
│   ├── AuthController.java          # POST /api/auth/register & /login
│   ├── AuthService.java
│   └── AuthDtos.java                # RegisterRequest, AuthRequest, AuthResponse
├── item/
│   ├── ItemController.java          # @PreAuthorize per endpoint
│   ├── ItemService.java
│   ├── Item.java
│   ├── ItemRepository.java
│   ├── ItemDto.java
│   └── ItemSecurity.java            # Custom SpEL bean for ownership checks
└── exception/
    └── GlobalExceptionHandler.java  # 400 / 401 / 403 / 404 / 500 JSON responses
```

---

## Quick start

### 1. Set the JWT secret environment variable

```bash
export JWT_SECRET=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
```

Generate your own for production:
```bash
openssl rand -base64 64
```

### 2. Run the application

```bash
./mvnw spring-boot:run
```

App starts on `http://localhost:8080`.

### 3. Run tests

```bash
./mvnw test
```

---

## API usage

### Register a new user

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"fullName":"Alice","email":"alice@example.com","password":"Password1!"}'
```

Response:
```json
{ "token": "eyJhbGciOiJIUzI1NiJ9..." }
```

---

### Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@example.com","password":"Password1!"}'
```

---

### Call a protected endpoint

```bash
curl http://localhost:8080/api/items \
  -H "Authorization: Bearer <token>"
```

---

### Create an item (owner set from JWT automatically)

```bash
curl -X POST http://localhost:8080/api/items \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"name":"My Item","description":"A test item"}'
```

---

## Role summary

| Endpoint                  | USER | ADMIN |
|---------------------------|------|-------|
| POST /api/auth/**         | open | open  |
| GET  /api/items           | yes  | yes   |
| GET  /api/items/{id}      | own  | any   |
| POST /api/items           | yes  | yes   |
| DELETE /api/items/{id}    | no   | yes   |
| GET  /api/admin/**        | no   | yes   |

---

## Switching to PostgreSQL

1. Replace the H2 dependency in `pom.xml` with:

```xml
<dependency>
  <groupId>org.postgresql</groupId>
  <artifactId>postgresql</artifactId>
  <scope>runtime</scope>
</dependency>
```

2. Update `application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/yourdb
    username: ${DB_USER}
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: validate   # use Flyway/Liquibase in production
    database-platform: org.hibernate.dialect.PostgreSQLDialect
```

---

## Security decisions

- **Stateless sessions** — no `HttpSession`, no cookies. Every request carries a JWT.
- **BCrypt strength 12** — ~300 ms/hash on modern hardware, safe against brute force.
- **Secret from env** — `JWT_SECRET` is never committed to source code.
- **CORS explicit** — `allowedOrigins` is an explicit list, never `"*"` with credentials.
- **Method security** — `@PreAuthorize` + custom `@itemSecurity` bean for row-level checks.
- **ProblemDetail** — RFC 7807 structured error responses, no stack traces to the client.
