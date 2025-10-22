# FPTsim - SIM Card Sales Management System

A Spring Boot web application for managing SIM card sales, built with Thymeleaf and DaisyUI v5.

## Project Overview

FPTsim is a comprehensive SIM card sales platform designed for college students to learn and practice modern web development with Spring Boot. The system allows managing SIM inventory, customer accounts, sales transactions, and promotional offers.

## Technology Stack

- **Backend Framework**: Spring Boot 3.5.6
- **Frontend Template Engine**: Thymeleaf
- **CSS Framework**: Tailwind CSS 4.1.14 + DaisyUI v5.3.1
- **Database**: MySQL 8.0
- **Build Tool**: Maven
- **Java Version**: 17
- **ORM**: Spring Data JPA (Hibernate)

## Project Structure

```
src/main/java/com/
├── app/                    # Application entry point
│   ├── FpTsimApplication.java
│   └── ServletInitializer.java
├── config/                 # Configuration classes
│   ├── I18nConfig.java                  # Internationalization setup
│   └── GlobalModelAttributeAdvice.java  # Global model attributes
├── controller/             # MVC Controllers
│   ├── HomeController.java              # Landing page
│   ├── AuthController.java              # Authentication (sign in/up/out)
│   ├── CartController.java              # Shopping cart
│   ├── DashboardController.java         # Dashboard pages
│   ├── AccountsController.java          # User account management
│   ├── SimController.java               # SIM management
│   ├── UuDaiController.java             # Promotions management
│   └── GlobalErrorController.java       # Error handling
├── service/                # Business logic layer
│   ├── AuthService.java
│   ├── SimService.java
│   └── UuDaiService.java
├── repository/             # Data access layer (Spring Data JPA)
│   ├── NguoiDungRepository.java
│   ├── SimRepository.java
│   ├── GioHangRepository.java
│   └── ... (other repositories)
├── model/                  # JPA Entities
│   ├── NguoiDung.java      # Users/Customers
│   ├── Sim.java            # SIM cards
│   ├── GioHang.java        # Shopping cart
│   ├── HoaDon.java         # Orders/Invoices
│   └── ... (other entities)
├── exception/              # Custom exceptions
│   ├── BusinessException.java
│   └── NotFoundException.java
└── web/                    # Web layer DTOs and advice
    ├── dto/
    │   ├── ApiError.java
    │   ├── SimCreateRequest.java
    │   └── ... (other DTOs)
    └── advice/
        └── GlobalExceptionHandler.java

src/main/resources/
├── application.properties  # Database and JPA configuration
├── application.yaml        # Additional Spring configuration
├── i18n/                   # Internationalization files
│   ├── messages_en.properties  # English translations
│   └── messages_vi.properties  # Vietnamese translations
├── static/                 # Static assets
│   ├── styles/
│   │   └── tailwind.css
│   ├── scripts/
│   │   └── langSelector.js
│   └── images/
│       └── logo.svg
└── templates/              # Thymeleaf templates
    ├── layouts/
    │   ├── layout.html     # Main layout
    │   └── dashboard.html  # Dashboard layout
    ├── forms/
    │   ├── signIn.html
    │   └── signUp.html
    ├── views/
    │   ├── index.html      # Home page
    │   ├── cart.html
    │   ├── uudai/
    │   └── dashboard/
    └── error.html          # Error page
```

## Key Features

### 1. User Management
- **Customer Registration & Authentication**: Sign up, sign in, sign out functionality
- **Role-Based Access**: Support for Customers, Staff, and Admin roles
- **User Profile Management**: Complete CRUD operations for user accounts

### 2. SIM Card Management
- **Inventory Management**: Track SIM cards by ICCID, MSISDN, provider, type, and status
- **Batch Import**: Import multiple SIM cards with purchase history
- **Provider Support**: Viettel, Mobifone, Vinaphone
- **SIM Types**: Prepaid, Postpaid, International

### 3. Shopping & Sales
- **Shopping Cart**: Session-based cart (can be extended to database-backed)
- **Order Processing**: Complete order lifecycle management
- **Transaction History**: Track all sales transactions

### 4. Promotions
- **Discount Management**: Create and manage promotional offers
- **Customer-Specific Offers**: Apply promotions to individual customers

### 5. Dashboard
- **Admin Dashboard**: Comprehensive management interface for staff and admin
- **Sales Statistics**: View sales performance and trends
- **Import History**: Track SIM card import batches

## Configuration

### Database Configuration
Edit `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/fptsim
spring.datasource.username=root
spring.datasource.password=root
```

### Internationalization
The application supports English and Vietnamese. Language files are in `src/main/resources/i18n/`:
- `messages_en.properties` - English translations
- `messages_vi.properties` - Vietnamese translations

Users can switch languages using the language selector in the navigation bar.

## Database Schema

The database schema is generated from JPA entities. You can:
1. Use `spring.jpa.hibernate.ddl-auto=create` to auto-generate tables
2. Or use the SQL script in `Tài Nguyên CSDL/db_create.sql`

### Main Entities
- **nguoidung**: User accounts (customers, staff, admin)
- **sim**: SIM card inventory
- **giohang**: Shopping cart items
- **hoadon**: Orders/invoices
- **hoadonchitiet**: Order line items
- **uudai**: Promotional offers
- **napsim**: SIM import batches
- **chitietnhapsim**: Import batch details

## Building and Running

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- MySQL 8.0+
- Node.js (for Tailwind CSS compilation)

### Build Steps

1. **Install Node dependencies**:
   ```bash
   npm install
   ```

2. **Compile Tailwind CSS**:
   ```bash
   npm run build:css
   ```
   Or for development with watch mode:
   ```bash
   npm run watch:css
   ```

3. **Build the Spring Boot application**:
   ```bash
   mvn clean package
   ```

4. **Run the application**:
   ```bash
   mvn spring-boot:run
   ```
   Or run the generated WAR file:
   ```bash
   java -jar target/FPTsim-0.0.1-SNAPSHOT.war
   ```

5. **Access the application**:
   Open your browser and navigate to `http://localhost:8080`

## Development Guidelines

### Code Style
- Use Lombok annotations to reduce boilerplate code
- Follow standard Spring Boot package structure
- Use meaningful variable and method names
- Add JavaDoc comments for public methods and classes

### DaisyUI v5 Components
The application uses DaisyUI v5 for UI components. Always prefix DaisyUI classes with `d-`:
- Buttons: `d-btn`, `d-btn-primary`, `d-btn-ghost`
- Cards: `d-card`, `d-card-body`, `d-card-title`
- Navigation: `d-navbar`, `d-menu`, `d-dropdown`
- Forms: `d-input`, `d-label`, `d-fieldset`
- Modals: `d-modal`, `d-modal-box`

Reference: https://daisyui.com/llms.txt

### Exception Handling
The application has comprehensive exception handling:
1. **Business Exceptions**: Use `BusinessException` for business logic errors
2. **Not Found Exceptions**: Use `NotFoundException` for missing resources
3. **Global Handler**: `GlobalExceptionHandler` catches and formats all exceptions
4. **Error Pages**: User-friendly error pages for browser requests
5. **API Errors**: JSON error responses for API requests

### Adding New Features
1. Create entity classes in `com.model`
2. Create repositories in `com.repository`
3. Implement business logic in `com.service`
4. Create controllers in `com.controller`
5. Add Thymeleaf templates in `src/main/resources/templates`
6. Add translations to `messages_en.properties` and `messages_vi.properties`

## Demo Data

The system includes a stub user account for testing:
- **Email**: root@mail.com
- **Password**: root

You can insert additional test data through the admin dashboard or directly into the database.

## Contributing

This project is maintained by college students. Contributions are welcome! Please follow these steps:
1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## License

This project is licensed for educational purposes.

## Support

For issues, questions, or suggestions, please contact the development team or open an issue in the repository.

