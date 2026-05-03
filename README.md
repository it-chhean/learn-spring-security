
# Spring Security JWT — Best Practice Demo

A production-ready Spring Boot 3 + Spring Security 6 project with JWT authentication,
role-based access control, and method-level security.

---

## Tech stack

| Layer       | Technology                          |
|-------------|-------------------------------------|
| Framework   | Spring Boot 3.2 / Spring Security 6 |
| Auth        | JWT (jjwt 0.12.6, HS256)            |
| Database    | H2 in-memory (swap for PostgreSQL)  |
| ORM         | Spring Data JPA / Hibernate         |
| Java        | 21                                  |
| Build       | gradle                               |

---

## Folder structure

```
src/main/java/com/example/security/
├── controller/
│   └── AuthController.java          
├── dto/
│   ├── RegisterRequest.java         
│   └── RegisterResponse.java        
├── entity/
│   ├── Role.java
│   └── UserEntity.java
├── exception/
│   ├── GlobalExceptionHandler.java  
│   ├── UserAlreadyExistsException.java 
│   └── RoleNotFoundException.java   
├── repository/
│   ├── UserRepository.java
│   └── RoleRepository.java          
└── service/
    ├── AuthService.java             
    └── AuthServiceImpl.java 
```

---

## Quick start

### 1. Set the JWT secret environment variable

Generate your own for production:
```bash
openssl rand -base64 64
```

### 2. Run the application

```bash
./gradlew bootRun
```

App starts on `http://localhost:8080`.

### 3. Run tests

```bash
./gradlew test
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
