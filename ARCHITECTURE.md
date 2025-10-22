# Project Architecture & Design Decisions

## Overview
This document explains the architectural decisions and design patterns used in the FPTsim project.

## Architecture Pattern

### MVC (Model-View-Controller)
The project follows the classic MVC pattern adapted for Spring Boot:
- **Model**: JPA entities representing database tables
- **View**: Thymeleaf templates for HTML rendering
- **Controller**: Spring MVC controllers handling HTTP requests

### Layered Architecture
```
Presentation Layer (Controllers)
        ↓
Business Logic Layer (Services)
        ↓
Data Access Layer (Repositories)
        ↓
Database (MySQL)
```

## Package Structure

### Why This Structure?
The package structure follows Spring Boot best practices:

1. **`com.config`**: Configuration classes
   - Separates configuration from business logic
   - Makes it easy to find and modify application settings
   - Example: I18n configuration, security configuration

2. **`com.controller`**: Web controllers
   - All HTTP request handlers in one place
   - Clear separation of concerns
   - Easy to understand application endpoints

3. **`com.service`**: Business logic
   - Encapsulates business rules
   - Reusable across multiple controllers
   - Transactional boundaries

4. **`com.repository`**: Data access
   - Spring Data JPA repositories
   - Database operations abstraction
   - Query methods

5. **`com.model`**: Domain entities
   - JPA entities representing tables
   - Domain logic and relationships
   - Single source of truth for data model

6. **`com.exception`**: Custom exceptions
   - Business-specific exceptions
   - Consistent error handling

7. **`com.web`**: Web layer utilities
   - DTOs for request/response
   - Exception handling advice
   - API-specific classes

## Design Patterns

### 1. Repository Pattern
**Purpose**: Abstraction over data access
```java
public interface NguoiDungRepository extends JpaRepository<NguoiDung, String> {
    Optional<NguoiDung> findByEmail(String email);
}
```
**Benefits**:
- Decouples business logic from data access
- Easy to test with mocks
- Consistent query interface

### 2. Service Layer Pattern
**Purpose**: Encapsulate business logic
```java
@Service
@RequiredArgsConstructor
public class AuthService {
    private final NguoiDungRepository userRepository;
    
    @Transactional
    public NguoiDung registerCustomer(...) {
        // Business logic here
    }
}
```
**Benefits**:
- Transactional boundaries
- Reusable business logic
- Testable

### 3. DTO Pattern
**Purpose**: Data transfer between layers
```java
public record SimCreateRequest(
    String iccid,
    String msisdn,
    // ... other fields
) {}
```
**Benefits**:
- Decouples API from domain model
- Input validation
- Version control for APIs

### 4. Controller Advice Pattern
**Purpose**: Global exception handling
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> notFound(...) {
        // Handle exception
    }
}
```
**Benefits**:
- Centralized error handling
- Consistent error responses
- Reduces boilerplate in controllers

### 5. Template Method Pattern (Thymeleaf Layouts)
**Purpose**: Reusable page layouts
```html
<html th:fragment="view(title, content)">
  <head>...</head>
  <body>
    <nav>...</nav>
    <main th:include="${content}"></main>
  </body>
</html>
```
**Benefits**:
- Consistent UI across pages
- DRY (Don't Repeat Yourself)
- Easy to maintain

## Key Decisions

### 1. Session-Based Authentication
**Current**: Simple session-based auth
**Why**: Easy to understand for students
**Future**: Migrate to Spring Security

### 2. No Password Encryption
**Current**: Plain text passwords
**Why**: Simplified for learning
**Future**: Add BCrypt hashing

### 3. Session-Based Cart
**Current**: Cart stored in HTTP session
**Why**: Quick to implement
**Future**: Database-backed cart with GioHang entity

### 4. Lombok Usage
**Why**: Reduces boilerplate code
**Benefits**:
- Automatic getters/setters with `@Getter/@Setter`
- Constructor injection with `@RequiredArgsConstructor`
- Cleaner code

### 5. JPA Entity First
**Why**: Database schema generated from entities
**Benefits**:
- Type-safe database access
- Automatic schema generation
- Object-oriented approach

### 6. Bilingual Support (i18n)
**Why**: Practice internationalization
**Benefits**:
- Professional approach
- Easy to add more languages
- Better user experience

### 7. DaisyUI v5 for UI
**Why**: Modern, accessible components
**Benefits**:
- Consistent design system
- Responsive by default
- Tailwind CSS integration

## Error Handling Strategy

### Three-Tier Error Handling

1. **Custom Business Exceptions**
   ```java
   throw new BusinessException("EMAIL_EXISTS", "Email đã tồn tại");
   ```

2. **Global Exception Handler**
   - Catches all exceptions
   - Formats responses
   - Logs errors

3. **Error Pages/JSON**
   - HTML error pages for browser requests
   - JSON responses for API requests

## Database Design

### Naming Conventions
- Table names: lowercase (e.g., `nguoidung`, `sim`)
- Column names: CamelCase (e.g., `HoTen`, `DiaChi`)
- Foreign keys: table_id (e.g., `khachHang_email`)

### Relationships
- **One-to-Many**: User → Orders
- **Many-to-One**: Order → User
- **Many-to-Many**: Customer → Promotions (through ApDungUuDai)
- **One-to-One**: SIM → Owner Info (ThongTinChuSim)

### Indexes
Strategic indexes on:
- SIM MSISDN (phone number lookups)
- SIM provider (filtering)
- SIM status (filtering)

## Testing Strategy

### Current State
Limited testing infrastructure

### Recommended Approach
1. **Unit Tests**: Test services with mocked repositories
2. **Integration Tests**: Test repositories with H2 database
3. **Controller Tests**: Test with MockMvc
4. **E2E Tests**: Test with Selenium/Playwright

## Performance Considerations

### JPA Optimizations
```yaml
hibernate.jdbc.batch_size: 50
hibernate.order_inserts: true
hibernate.order_updates: true
```

### Lazy Loading
- Most relationships use lazy loading
- Prevents N+1 query problems
- Use `@EntityGraph` or JOIN FETCH when needed

### Pagination
- All list endpoints should support pagination
- Prevents memory issues with large datasets

## Security Considerations

### Current Gaps (To Be Addressed)
1. ❌ No password encryption
2. ❌ No CSRF protection
3. ❌ No SQL injection prevention beyond JPA
4. ❌ No rate limiting
5. ❌ No input sanitization

### Recommended Improvements
1. ✅ Add Spring Security
2. ✅ Use BCrypt for passwords
3. ✅ Enable CSRF tokens
4. ✅ Add validation annotations
5. ✅ Implement rate limiting

## Deployment Strategy

### Development
```bash
mvn spring-boot:run
```

### Production
1. Build WAR file: `mvn clean package`
2. Deploy to Tomcat/standalone
3. Configure external `application.properties`
4. Set up MySQL database
5. Configure reverse proxy (Nginx)

## Monitoring & Logging

### Current
- Spring Boot default logging
- Console output

### Recommended
- Add Actuator endpoints
- Implement structured logging (Logback)
- Add application metrics
- Set up health checks

## Future Architecture

### Microservices Consideration
Current monolith can be split into:
1. **User Service**: Authentication & user management
2. **Inventory Service**: SIM card management
3. **Order Service**: Shopping cart & orders
4. **Promotion Service**: Discounts & offers

### API-First Approach
- Separate REST API from web UI
- Enable mobile app development
- Third-party integrations

### Event-Driven Architecture
- Use message queues (RabbitMQ/Kafka)
- Async processing for imports
- Event sourcing for orders

## Conclusion

This architecture provides a solid foundation for learning Spring Boot development while following industry best practices. The structure is simple enough for students to understand but professional enough to be extended for real-world use.
