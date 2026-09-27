# FAE Admin Portal - Backend API

A robust, modular Spring Boot backend application designed to power the FAE (Field Application Engineer) Admin Portal. This project follows Domain-Driven Design (DDD) principles, organizing code by feature rather than technical layer, ensuring high maintainability and scalability.

---

## 🚀 Tech Stack

* **Framework:** Java 21 / Spring Boot 3
* **Security:** Spring Security with JWT (JSON Web Tokens)
* **Database & ORM:** MySQL, Spring Data JPA / Hibernate
* **Documentation:** OpenAPI 3 / Swagger UI
* **Build Tool:** Maven

---

## 🏗️ Architecture & Project Structure

The project is structured using Domain-Driven Design (DDD) to separate core business logic from infrastructure concerns:

### 1. `config` (Infrastructure)
Contains system-wide configurations:
* **`SecurityConfig`:** Configures CORS, CSRF, session management, and endpoint authorization rules.
* **`JwtAuthenticationFilter`:** Intercepts incoming requests to validate JWT tokens and populate the Security Context.
* **`SwaggerConfig`:** Configures interactive API documentation via OpenAPI.
* **`FileStorageConfig`:** Manages local or cloud storage properties for file uploads.

### 2. `common` (Cross-Cutting Concerns)
Shared utilities and standardized response formats used across all domains:
* **Exception Handling:** Centralized error handling (`GlobalExceptionHandler`) delivering consistent JSON responses (e.g., `ResourceNotFoundException`).
* **DTOs:** Standardized API response wrappers (`ApiResponse<T>`, `PagedResponse<T>`) for uniform client-side handling.
* **Utils:** Helper utilities for common operations such as slug generation and retrieving security context details.

### 3. `domain` (Core Business Logic)
Encapsulated feature modules containing their own Controllers, Services, Repositories, Entities, and DTOs:
* **`auth`:** Handles user login, registration, password management, and JWT issuance.
* **`user`:** Comprehensive management of Users and Roles using Role-Based Access Control (RBAC).
  * Uses `RoleSlug` for granular permission checks.
  * Supports custom user projections and summary models.
* **`content`:** Manages dynamic content entries and customizable content types.
* **`file`:** Handles asset management and file uploads, integrating with relational mapping (`content_files`).
* **`contact`:** Handles incoming "Contact Us" inquiries and administrative review workflows.

---

## ✨ Key Features

* 🔐 **Stateless Authentication:** Secure REST API protected via JWT tokens.
* 🛡️ **Role-Based Access Control (RBAC):** Granular permission management driven by dynamic role entities and `RoleSlug` definitions.
* ⚠️ **Global Error Handling:** Clean, unified error structures ensuring easy integration with frontend apps.
* 📁 **File & Asset Management:** Dedicated module for processing file uploads and attaching media to content entities.
* 📖 **Interactive API Documentation:** Auto-generated Swagger UI for exploring and testing endpoints live.

---

## 📂 Directory Tree

```text
src/main/java/com/fae/adminportal/
├── config/           # Security, Swagger, and Application Configurations
├── common/           # Exception Handling, Shared DTOs, and Utilities
├── domain/           # DDD Feature Modules
│   ├── auth/         # Authentication (Login, Signup, Token Management)
│   ├── user/         # User & Role Management (RBAC)
│   ├── content/      # Content & Content Types Management
│   ├── file/         # File Uploads & Asset Attachments
│   └── contact/      # Contact Us Inquiries & Workflows
└── AdminPortalApplication.java
