# SyncUp - SPM Orangle Project

A comprehensive task management and collaboration platform built with Spring Boot backend and Next.js frontend.

## 📋 Table of Contents

- [Project Overview](#project-overview)
- [Technology Stack](#technology-stack)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Running the Application](#running-the-application)
- [Testing](#testing)
- [CI/CD Pipeline](#cicd-pipeline)
- [Project Structure](#project-structure)
- [Documentation](#documentation)
- [Contributors](#contributors)

## 🎯 Quick Links

- **[Quick Start Guide](QUICK_START.md)** - Get running in 10 minutes
- **[Testing Summary](TESTING_SUMMARY.md)** - Comprehensive testing documentation
- **[API Documentation](http://localhost:8080/swagger-ui.html)** - Interactive API docs (when backend is running)
- **[E2E Testing Guide](frontend/e2e/PLAYWRIGHT_E2E_GUIDE.md)** - Playwright E2E testing

## 🎯 Project Overview

SyncUp is an enterprise task management system that provides:
- Real-time task tracking and notifications
- Department-based project management
- Role-based access control (HR, Manager, Staff)
- Comprehensive reporting and analytics
- WebSocket-based real-time updates
- Comment threading and collaboration features

## 🛠 Technology Stack

### Backend
- **Framework**: Spring Boot 3.5.5
- **Language**: Java 21
- **Database**: PostgreSQL 16.2
- **Message Queue**: RabbitMQ 3.13
- **Authentication**: AWS Cognito (OAuth2/JWT)
- **Database Migration**: Flyway 11.3.4
- **API Documentation**: SpringDoc OpenAPI (Swagger)
- **Build Tool**: Maven

### Frontend
- **Framework**: Next.js 15.5.3 (React 19)
- **Language**: TypeScript 5
- **Styling**: Tailwind CSS 4
- **State Management**: TanStack Query (React Query)
- **UI Components**: Radix UI
- **Real-time**: WebSocket (STOMP/SockJS)
- **Testing**: Jest + Playwright
- **Build Tool**: npm

### Infrastructure
- **Containerization**: Docker & Docker Compose
- **CI/CD**: GitHub Actions
- **Cloud**: AWS (Cognito, RDS, EC2)

## ✅ Prerequisites

Before running the application, ensure you have the following installed:

### Required Software
- **Node.js**: 22.x or higher
- **Java**: JDK 21
- **Docker Desktop**: Latest version
- **Git**: Latest version

### Optional Tools
- **Maven**: 3.9+ (or use included `mvnw`)
- **PostgreSQL Client**: For database inspection
- **Bruno/Postman**: For API testing

### Environment Setup

Create the following environment files:

#### Frontend `.env.local`
```bash
# Copy from .env.example and fill in values
cp frontend/.env.example frontend/.env.local
```

Required variables:
- `NEXT_PUBLIC_API_BASE_URL`: Backend API URL (e.g., `http://localhost:8080`)
- `NEXT_PUBLIC_WS_BASE_URL`: WebSocket URL (e.g., `http://localhost:8080`)
- AWS Cognito credentials

#### Backend `application-local.yml`
Located at `backend/spmorangle/src/main/resources/application-local.yml`

## 🚀 Quick Start

**New to the project? Start here:** [QUICK_START.md](QUICK_START.md)

For experienced developers, here's the minimal setup:

### 1. Clone the Repository
```bash
git clone <repository-url>
cd spmOrangle
```

### 2. Start Infrastructure Services
```bash
cd backend
docker compose up -d
```

This will start:
- PostgreSQL database (port 5432)
- RabbitMQ message broker (port 5672, management UI at 15672)
- Flyway migrations (automatic database setup)

Verify services are running:
```bash
docker compose ps
```

### 3. Install Frontend Dependencies
```bash
cd ../frontend
npm install
```

This will automatically install Playwright browsers via the `postinstall` script.

### 4. Install Backend Dependencies
```bash
cd ../backend/spmorangle
./mvnw clean install -DskipTests
```

## 🏃 Running the Application

### Development Mode

#### Start Backend (Terminal 1)
```bash
cd backend/spmorangle
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

Backend will be available at: `http://localhost:8080`
- API Documentation: `http://localhost:8080/swagger-ui.html`
- Health Check: `http://localhost:8080/actuator/health`

#### Start Frontend (Terminal 2)
```bash
cd frontend
npm run dev
```

Frontend will be available at: `http://localhost:3000`

### Production Build

#### Backend
```bash
cd backend/spmorangle
./mvnw clean package
java -jar target/backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

#### Frontend
```bash
cd frontend
npm run build
npm start
```

### Docker Services Management

#### Stop Services
```bash
cd backend
docker compose down
```

#### Stop and Remove Volumes (Fresh Start)
```bash
cd backend
docker compose down -v
```

#### View Logs
```bash
docker compose logs -f postgres
docker compose logs -f rabbitmq
```

#### Access RabbitMQ Management UI
Open `http://localhost:15672`
- Username: `admin`
- Password: `admin`

## 🧪 Testing

### Backend Tests

The backend has **68 test files** covering unit and integration tests.

#### Location
```
backend/spmorangle/src/test/java/com/spmorangle/
├── common/          # Common utility tests
├── config/          # Configuration tests
├── crm/             # CRM module tests
│   ├── task/        # Task management tests
│   ├── project/     # Project management tests
│   ├── notification/# Notification tests
│   └── report/      # Reporting tests
└── BackendApplicationTests.java
```

#### Run All Tests
```bash
cd backend/spmorangle
./mvnw test
```

#### Run Specific Test Class
```bash
./mvnw test -Dtest=SecurityContextUtilTest
```

#### Run Tests with Coverage
```bash
./mvnw test jacoco:report
```

Coverage report will be at: `target/site/jacoco/index.html`

#### Test Configuration
- Tests use H2 in-memory database
- Profile: `test` (configured in `application-test.yml`)
- Mocked AWS Cognito and external services

### Frontend Tests

The frontend has **63 unit/integration test files** and **12 E2E test files**.

#### Unit/Integration Tests (Jest)

**Location:**
```
frontend/__tests__/
├── app/             # Page component tests
├── components/      # UI component tests
├── contexts/        # Context provider tests
├── hooks/           # Custom hooks tests
├── lib/             # Utility function tests
├── services/        # API service tests
├── types/           # Type definition tests
└── utils/           # Helper function tests
```

**Run Tests:**
```bash
cd frontend
npm test                    # Run all tests
npm test -- --watch         # Watch mode
npm test -- --coverage      # With coverage
```

Coverage report: `frontend/coverage/lcov-report/index.html`

#### E2E Tests (Playwright)

**Location:**
```
frontend/e2e/
├── tests/           # 12 E2E test specs
├── fixtures/        # Test fixtures and helpers
├── setup/           # Global setup/teardown
├── config/          # Test configuration
└── utils/           # Test utilities
```

**Documentation:**
- `frontend/e2e/DOCUMENTATION_INDEX.md` - Complete E2E documentation
- `frontend/e2e/PLAYWRIGHT_E2E_GUIDE.md` - Playwright guide

**Run E2E Tests:**
```bash
cd frontend

# Run all E2E tests
npm run test:e2e

# Run with UI mode (recommended for development)
npm run test:e2e:ui

# Run in headed mode (see browser)
npm run test:e2e:headed

# Debug tests
npm run test:e2e:debug

# View test report
npm run test:e2e:report
```

**Prerequisites for E2E Tests:**
- Backend and database must be running
- Frontend must be running at `http://localhost:3000`
- Valid AWS Cognito test users configured

## 🔄 CI/CD Pipeline

GitHub Actions workflows are located in `.github/workflows/`

### Backend CI (`backend-ci.yml`)

**Trigger:** Push to `backend/**` or PRs to `main`

**Steps:**
1. ☕ Setup Java 21
2. 🗄️ Start PostgreSQL test database
3. 🔨 Compile application
4. 🧪 Run tests with coverage
5. 📊 Generate JaCoCo coverage report
6. 💬 Post coverage comment on PR
7. 📦 Package JAR file

**Coverage Requirements:**
- Overall: 40% minimum
- Changed files: 60% minimum

### Frontend CI (`frontend-ci.yml`)

**Trigger:** Push to `frontend/**` or PRs to `main`

**Steps:**
1. 📦 Setup Node.js 22.x
2. 📥 Install dependencies
3. 🔍 Run TypeScript type checking
4. 🧪 Run Jest tests with coverage
5. 📊 Generate coverage report
6. 🏗️ Build production bundle

**Coverage Reporting:**
- Uses Vitest coverage report action
- Posts detailed coverage to PR comments

### Database CD (`db-cd.yml`)

**Trigger:** Manual or automated deployment

**Steps:**
1. Run Flyway migrations against production database
2. Validate migration success

### Security Scanning (`snyk-security-scan.yml`)

**Trigger:** Scheduled or on-demand

**Steps:**
1. Scan dependencies for vulnerabilities
2. Report security issues

## 📁 Project Structure

```
spmOrangle/
├── .github/
│   └── workflows/           # CI/CD pipeline definitions
├── backend/
│   ├── database/
│   │   └── migrations/      # Flyway SQL migration scripts (45+ files)
│   ├── docs/                # Bruno API collection
│   ├── rabbitmq/            # RabbitMQ Dockerfile
│   ├── spmorangle/          # Spring Boot application
│   │   ├── src/
│   │   │   ├── main/java/com/spmorangle/
│   │   │   │   ├── common/  # Shared utilities
│   │   │   │   ├── config/  # Configuration classes
│   │   │   │   └── crm/     # Business modules
│   │   │   │       ├── task/
│   │   │   │       ├── project/
│   │   │   │       ├── notification/
│   │   │   │       └── report/
│   │   │   └── test/        # 68 test files
│   │   └── pom.xml
│   ├── docker-compose.yml   # Local infrastructure
│   └── README.md
├── frontend/
│   ├── app/                 # Next.js pages (App Router)
│   ├── components/          # React components (50+ components)
│   ├── contexts/            # React contexts
│   ├── hooks/               # Custom React hooks
│   ├── lib/                 # Utility libraries
│   ├── services/            # API services
│   ├── types/               # TypeScript types
│   ├── __tests__/           # 63 Jest test files
│   ├── e2e/                 # 12 Playwright E2E tests
│   ├── public/              # Static assets
│   ├── package.json
│   ├── playwright.config.ts
│   ├── jest.config.ts
│   └── README.md
├── bruno/                   # API testing collection
├── c4-diagrams/             # Architecture diagrams
├── terraform/               # Infrastructure as Code
├── package.json             # Root package.json (Husky)
└── README.md               # This file
```

## 📚 Documentation

### Getting Started
- **[Quick Start Guide](QUICK_START.md)** - Get running in 10 minutes ⚡
- **[Documentation Index](DOCUMENTATION_INDEX.md)** - Complete guide to all documentation 📚
- **[Testing Summary](TESTING_SUMMARY.md)** - Comprehensive testing guide 🧪

### API Documentation
- **Swagger UI**: `http://localhost:8080/swagger-ui.html` (when backend is running)
- **Bruno Collection**: `bruno/` and `backend/docs/` directories
- **Notification API**: `NOTIFICATION_API.md`

### Architecture
- **C4 Diagrams**: `c4-diagrams/` directory
  - System Context (C1)
  - Container Diagram (C2)
  - Component Diagrams (C3)
  - Code Diagrams (C4)

### Database
- **Migrations**: `backend/database/migrations/`
- **Migration Guide**: `backend/database/README.md`
- **Revert Scripts**: Available for critical migrations

### Testing
- **Testing Overview**: `TESTING_SUMMARY.md` - **68 backend** + **75 frontend** tests
- **E2E Guide**: `frontend/e2e/PLAYWRIGHT_E2E_GUIDE.md`
- **E2E Index**: `frontend/e2e/DOCUMENTATION_INDEX.md`
- **Backend Tests**: `backend/spmorangle/src/test/` - JUnit 5, Spring Boot Test, JaCoCo coverage
- **Frontend Unit Tests**: `frontend/__tests__/` - Jest, React Testing Library
- **Frontend E2E Tests**: `frontend/e2e/tests/` - Playwright

### Infrastructure
- **Terraform**: `terraform/README.md`
- **Docker**: `backend/docker-compose.yml` with service definitions

## 🔧 Troubleshooting

### Backend Issues

**Database Connection Failed:**
```bash
# Check if PostgreSQL is running
docker compose ps
# Restart services
docker compose down && docker compose up -d
```

**Port Already in Use (8080):**
```bash
# Find process using port
lsof -i :8080
# Kill the process or change port in application-local.yml
```

### Frontend Issues

**Node Modules Issues:**
```bash
rm -rf node_modules package-lock.json
npm install
```

**Playwright Browsers Missing:**
```bash
npx playwright install --with-deps chromium
```

### Docker Issues

**Volume Permissions:**
```bash
docker compose down -v
docker volume prune
docker compose up -d
```

## 🤝 Development Workflow

### Git Workflow
1. Create feature branch from `main`
2. Make changes and commit
3. Push and create Pull Request
4. CI/CD checks run automatically
5. Review and merge

### Pre-commit Hooks (Husky)
- Linting (ESLint)
- Type checking (TypeScript)
- Code formatting (Prettier)

### Code Quality
- Backend: JaCoCo coverage + SonarQube
- Frontend: Jest coverage + ESLint
- Security: Snyk scanning

## 📝 Environment Variables Reference

### Backend Environment Variables
Located in `application-local.yml` and `application-prod.yml`

### Frontend Environment Variables
```bash
# API Configuration
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080
NEXT_PUBLIC_WS_BASE_URL=http://localhost:8080

# AWS Cognito
NEXT_PUBLIC_AWS_REGION=
NEXT_PUBLIC_AWS_USER_POOL_ID=
NEXT_PUBLIC_AWS_USER_POOL_CLIENT_ID=

# Feature Flags (Optional)
NEXT_PUBLIC_ENABLE_NOTIFICATIONS=true
```

## 👥 Contributors

Team Orangle - SMU Software Project Management 2025

## 📄 License

This project is part of SMU Software Project Management course.

---

**Last Updated**: November 2025

For detailed setup instructions for specific components, see:
- Backend: `backend/README.md`
- Frontend: `frontend/README.md`
- Database: `backend/database/README.md`
- E2E Testing: `frontend/e2e/PLAYWRIGHT_E2E_GUIDE.md`