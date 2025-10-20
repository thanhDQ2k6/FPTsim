# Development Guide

This guide helps developers get started with FPTsim development and understand common tasks.

## Getting Started

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- MySQL 8.0+
- Node.js 16+ (for Tailwind CSS)
- Git
- IDE (IntelliJ IDEA recommended, VSCode also works)

### Initial Setup

1. **Clone the repository**:
   ```bash
   git clone https://github.com/thanhDQ2k6/FPTsim.git
   cd FPTsim
   ```

2. **Set up MySQL database**:
   ```sql
   CREATE DATABASE fptsim CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

3. **Configure database connection**:
   Edit `src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/fptsim
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   ```

4. **Install Node dependencies**:
   ```bash
   npm install
   ```

5. **Build Tailwind CSS**:
   ```bash
   npm run build:css
   ```

6. **Run the application**:
   ```bash
   mvn spring-boot:run
   ```

7. **Access the application**:
   Open `http://localhost:8080` in your browser

## Development Workflow

### 1. CSS Development (Tailwind + DaisyUI)

#### Watch Mode (Recommended)
```bash
npm run watch:css
```
This will automatically rebuild CSS when you change templates.

#### Manual Build
```bash
npm run build:css
```

#### DaisyUI Components
Always use DaisyUI v5 with `d-` prefix:
```html
<!-- Button -->
<button class="d-btn d-btn-primary">Click me</button>

<!-- Card -->
<div class="d-card bg-base-100">
  <div class="d-card-body">
    <h2 class="d-card-title">Card Title</h2>
    <p>Card content</p>
  </div>
</div>

<!-- Input -->
<input class="d-input d-input-bordered" type="text" />
```

Reference: https://daisyui.com/llms.txt

### 2. Backend Development

#### Hot Reload
Spring Boot DevTools is enabled. Changes to Java files will trigger auto-restart.

#### Database Changes

**Option 1: Auto-generate from entities (Development)**
```properties
spring.jpa.hibernate.ddl-auto=update
```

**Option 2: Manual SQL (Production)**
Use the SQL script in `Tài Nguyên CSDL/db_create.sql`

#### Creating a New Feature

**Example: Adding a new "Review" feature**

1. **Create Entity** (`com.model.Review.java`):
```java
@Entity
@Table(name = "review")
@Getter @Setter
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Integer rating;
    
    @Column(columnDefinition = "TEXT")
    private String comment;
    
    @ManyToOne
    @JoinColumn(name = "user_email")
    private NguoiDung user;
    
    @ManyToOne
    @JoinColumn(name = "sim_iccid")
    private Sim sim;
}
```

2. **Create Repository** (`com.repository.ReviewRepository.java`):
```java
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findBySimIccid(String iccid);
    List<Review> findByUserEmail(String email);
}
```

3. **Create Service** (`com.service.ReviewService.java`):
```java
@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    
    @Transactional
    public Review createReview(String userEmail, String simIccid, 
                              int rating, String comment) {
        // Business logic here
    }
    
    public List<Review> getReviewsForSim(String iccid) {
        return reviewRepository.findBySimIccid(iccid);
    }
}
```

4. **Create Controller** (`com.controller.ReviewController.java`):
```java
@Controller
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;
    
    @GetMapping("/sim/{iccid}")
    public String getSimReviews(@PathVariable String iccid, Model model) {
        List<Review> reviews = reviewService.getReviewsForSim(iccid);
        model.addAttribute("reviews", reviews);
        return "views/reviews";
    }
}
```

5. **Create Template** (`templates/views/reviews.html`):
```html
<!doctype html>
<html th:replace="~{layouts/layout :: view(~{::title}, ~{::main})}" 
      xmlns:th="http://www.thymeleaf.org">
  <title>Reviews</title>
  <main>
    <div class="d-card" th:each="review : ${reviews}">
      <div class="d-card-body">
        <h3 th:text="${review.user.hoTen}"></h3>
        <p th:text="${review.comment}"></p>
      </div>
    </div>
  </main>
</html>
```

6. **Add Translations** (`i18n/messages_en.properties`):
```properties
review.title=Reviews
review.rating=Rating
review.comment=Comment
```

### 3. Testing

#### Unit Tests
```bash
mvn test
```

#### Integration Tests
```bash
mvn verify
```

#### Manual Testing
1. Start the application
2. Test each endpoint
3. Check console for errors

### 4. Common Tasks

#### Add a New Translation Key
1. Edit `src/main/resources/i18n/messages_en.properties`
2. Edit `src/main/resources/i18n/messages_vi.properties`
3. Use in template: `<span th:text="#{your.key}"></span>`

#### Add a New Controller Endpoint
```java
@GetMapping("/my-page")
public String myPage(Model model) {
    model.addAttribute("data", someData);
    return "views/my-page";
}
```

#### Add Authorization Check
```java
@GetMapping("/admin-only")
public String adminPage(HttpSession session) {
    Object user = session.getAttribute("user");
    if (!(user instanceof NguoiDung nguoiDung) 
        || nguoiDung.getVaiTro() != NguoiDung.VaiTro.Admin) {
        return "redirect:/";
    }
    return "views/admin";
}
```

#### Add Custom Exception
```java
public class ReviewException extends RuntimeException {
    public ReviewException(String message) {
        super(message);
    }
}
```

Then handle it in `GlobalExceptionHandler`:
```java
@ExceptionHandler(ReviewException.class)
public ResponseEntity<ApiError> handleReview(ReviewException ex, 
                                            HttpServletRequest req) {
    return ResponseEntity.badRequest()
        .body(new ApiError("REVIEW_ERROR", ex.getMessage(), 
                          req.getRequestURI()));
}
```

## Code Style Guidelines

### Naming Conventions
- **Classes**: PascalCase (`UserService`, `SimController`)
- **Methods**: camelCase (`findUserByEmail`, `createSim`)
- **Variables**: camelCase (`userName`, `simList`)
- **Constants**: UPPER_SNAKE_CASE (`MAX_ITEMS`, `DEFAULT_PAGE_SIZE`)

### Java Conventions
```java
// Use Lombok annotations
@Getter @Setter
@RequiredArgsConstructor
public class MyClass {
    private final DependencyClass dependency;
}

// Use meaningful names
List<Sim> availableSims = simRepository.findByTrangThai(TrangThai.SanSang);

// Add JavaDoc for public methods
/**
 * Finds all available SIM cards.
 * @return list of available SIMs
 */
public List<Sim> findAvailableSims() {
    // implementation
}
```

### HTML/Thymeleaf Conventions
```html
<!-- Use semantic HTML -->
<article>
  <header>
    <h1 th:text="${title}"></h1>
  </header>
  <main>
    <!-- Content -->
  </main>
</article>

<!-- Use DaisyUI components -->
<button class="d-btn d-btn-primary" th:text="#{button.save}"></button>

<!-- Use Thymeleaf properly -->
<div th:if="${user != null}">
  <span th:text="${user.hoTen}"></span>
</div>
```

## Debugging

### Enable Debug Logging
Add to `application.properties`:
```properties
logging.level.com.controller=DEBUG
logging.level.com.service=DEBUG
logging.level.org.hibernate.SQL=DEBUG
```

### Common Issues

#### CSS not updating
```bash
# Clear browser cache
# Rebuild CSS
npm run build:css
```

#### Database connection error
```bash
# Check MySQL is running
sudo systemctl status mysql

# Check credentials in application.properties
# Test connection
mysql -u root -p
```

#### Port already in use
```bash
# Find process on port 8080
lsof -i :8080

# Kill process
kill -9 <PID>
```

## Git Workflow

### Branching Strategy
- `main` - Production-ready code
- `develop` - Integration branch
- `feature/*` - New features
- `bugfix/*` - Bug fixes
- `hotfix/*` - Critical fixes

### Commit Messages
```
feat: Add review functionality
fix: Fix cart calculation bug
docs: Update README
style: Format code
refactor: Simplify authentication logic
test: Add unit tests for SimService
```

### Pull Request Process
1. Create feature branch
2. Make changes
3. Test thoroughly
4. Create PR with description
5. Wait for review
6. Address feedback
7. Merge when approved

## Performance Tips

### Database Optimization
```java
// Use pagination
Page<Sim> sims = simRepository.findAll(PageRequest.of(0, 20));

// Use JOIN FETCH for eager loading
@Query("SELECT s FROM Sim s JOIN FETCH s.thongTinChuSim WHERE s.iccid = :id")
Sim findWithOwner(@Param("id") String id);

// Use projections for read-only data
interface SimSummary {
    String getIccid();
    String getMsisdn();
    BigDecimal getGiaBan();
}
```

### Caching
```java
@Service
public class SimService {
    @Cacheable("sims")
    public Sim findById(String id) {
        return simRepository.findById(id).orElse(null);
    }
    
    @CacheEvict("sims")
    public void updateSim(Sim sim) {
        simRepository.save(sim);
    }
}
```

## Deployment

### Build for Production
```bash
# Build WAR file
mvn clean package -DskipTests

# WAR file will be in target/FPTsim-0.0.1-SNAPSHOT.war
```

### Run in Production
```bash
# Run with external config
java -jar target/FPTsim-0.0.1-SNAPSHOT.war \
  --spring.config.location=file:/path/to/application.properties

# Or deploy to Tomcat
cp target/FPTsim-0.0.1-SNAPSHOT.war /path/to/tomcat/webapps/
```

## Resources

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Thymeleaf Documentation](https://www.thymeleaf.org/documentation.html)
- [DaisyUI Documentation](https://daisyui.com/)
- [Tailwind CSS Documentation](https://tailwindcss.com/docs)
- [MySQL Documentation](https://dev.mysql.com/doc/)

## Getting Help

- Check the [README.md](README.md) for project overview
- Check [ARCHITECTURE.md](ARCHITECTURE.md) for design decisions
- Ask team members on Discord/Slack
- Create an issue on GitHub
- Review existing code for examples

## Contributing

See [README.md](README.md) for contribution guidelines.
