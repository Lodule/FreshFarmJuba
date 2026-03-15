# Fresh Farm Juba 🌾

[![CI/CD](https://github.com/yourusername/FreshFarmJuba/actions/workflows/ci-cd.yml/badge.svg)](https://github.com/yourusername/FreshFarmJuba/actions)
[![Java Version](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.3-brightgreen.svg)](https://spring.io/projects/spring-boot)

A modern, scalable E-commerce platform for agricultural products in South Sudan. Built with Spring Boot, Thymeleaf, and PostgreSQL.

## 🚀 Features

- **Customer App**: Browse products, manage wishlist, and secure checkout.
- **Admin Dashboard**: Professional analytics, product management, and order tracking.
- **Business Accounts**: Specialized features for bulk orders and wholesale.
- **Secure Authentication**: Spring Security with email verification.
- **Responsive Design**: Fully optimized for mobile and desktop using Bootstrap 5.

## 🛠 Tech Stack

- **Backend**: Java 21, Spring Boot 3.2.3, Spring Data JPA, Spring Security, Spring Mail
- **Frontend**: Thymeleaf, Bootstrap 5.3, AOS (Animate on Scroll)
- **Database**: PostgreSQL 15
- **DevOps**: Docker, Docker Compose, Kubernetes, GitHub Actions

## 📦 Getting Started

### Prerequisites

- Docker & Docker Compose
- JDK 21 (optional for local dev)

### Running Locally with Docker

```bash
# Clone the repository
git clone https://github.com/yourusername/FreshFarmJuba.git
cd FreshFarmJuba

# Start the application and database
docker-compose up -d
```

The application will be available at `http://localhost:8080`.

## 🚢 Deployment

The project is ready for Kubernetes deployment. Manifests are located in the `/kubernetes` directory.

```bash
./scripts/deploy.sh
```

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---
Built with ❤️ for Fresh Farm Juba.
