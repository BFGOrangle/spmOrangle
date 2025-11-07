# SPM Orangle - CRM & Project Management System

A comprehensive Customer Relationship Management (CRM) and Project Management system built with modern web technologies. SPM Orangle helps teams manage projects, tasks, departments, and team collaboration with real-time notifications and detailed reporting capabilities.

[![Backend CI](https://github.com/BFGOrangle/spmOrangle/actions/workflows/backend-ci.yml/badge.svg)](https://github.com/BFGOrangle/spmOrangle/actions/workflows/backend-ci.yml)
[![Frontend CI](https://github.com/BFGOrangle/spmOrangle/actions/workflows/frontend-ci.yml/badge.svg)](https://github.com/BFGOrangle/spmOrangle/actions/workflows/frontend-ci.yml)
[![Security](https://github.com/BFGOrangle/spmOrangle/actions/workflows/snyk-security-scan.yml/badge.svg)](https://github.com/BFGOrangle/spmOrangle/actions/workflows/snyk-security-scan.yml)

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Technology Stack](#technology-stack)
- [Architecture](#architecture)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Backend Setup](#backend-setup)
  - [Frontend Setup](#frontend-setup)
  - [Database Setup](#database-setup)
- [Project Structure](#project-structure)
- [Development](#development)
  - [Running Locally](#running-locally)
  - [Testing](#testing)
  - [Linting & Code Quality](#linting--code-quality)
- [API Documentation](#api-documentation)
- [Deployment](#deployment)
- [CI/CD Pipeline](#cicd-pipeline)
- [Contributing](#contributing)
- [Additional Resources](#additional-resources)
- [License](#license)

---

## 🎯 Overview

SPM Orangle is a full-stack CRM and project management platform designed for organizations to efficiently manage:
- **Departments & Hierarchies**: Multi-level department structures with role-based access control
- **Project Management**: Track projects, tasks, and deadlines with team collaboration
- **Task Management**: Assign, track, and manage tasks with status tracking and priorities
- **Real-time Notifications**: In-app notifications via WebSocket with support for email and SMS
- **Reporting & Analytics**: Export reports in multiple formats (CSV, Excel, PDF) with customizable templates
- **User Management**: Comprehensive user and role management with AWS Cognito authentication
- **Department Dashboards**: Aggregated metrics and insights for managers across departments and sub-departments

---

## ✨ Features

### Core Functionality
- 🏢 **Hierarchical Department Management** - Create and manage nested department structures
- 📊 **Departmental Dashboards** - View aggregated metrics, project health, team load, and upcoming commitments
- ✅ **Task Management** - Create, assign, and track tasks with various statuses and priorities
- 📁 **Project Tracking** - Manage projects with team members, deadlines, and progress tracking
- 🔔 **Real-time Notifications** - WebSocket-based notifications with multiple channels (in-app, email, SMS)
- 📈 **Advanced Reporting** - Generate and export reports in CSV, Excel, and PDF formats
- 🔐 **Security & Authentication** - OAuth2/JWT with AWS Cognito integration
- 📅 **Calendar Integration** - Task scheduling and calendar view
- 👥 **User Management** - Role-based access control (MANAGER, STAFF, ADMIN)

### Technical Features
- Responsive Next.js frontend with TypeScript
- RESTful API with Spring Boot
- PostgreSQL database with Flyway migrations
- RabbitMQ for asynchronous messaging
- WebSocket for real-time updates
- Comprehensive test coverage (40%+ overall, 60%+ for changed files)
- CI/CD with GitHub Actions
- Infrastructure as Code with Terraform
- Docker containerization support

---

## 🛠️ Technology Stack

### Frontend
- **Framework**: [Next.js 15.5](https://nextjs.org/) (React 19)
- **Language**: TypeScript 5
- **UI Components**: 
  - [Radix UI](https://www.radix-ui.com/) - Accessible component primitives
  - [Tailwind CSS 4](https://tailwindcss.com/) - Utility-first CSS
  - [Lucide Icons](https://lucide.dev/) - Icon library
- **State Management**: 
  - [TanStack Query (React Query)](https://tanstack.com/query) - Server state management
  - React Context - Client state
- **Real-time**: 
  - [SockJS](https://github.com/sockjs/sockjs-client) - WebSocket polyfill
  - [STOMP](https://stomp-js.github.io/stomp-websocket/) - WebSocket messaging protocol
- **Charts**: [Chart.js](https://www.chartjs.org/) with react-chartjs-2
- **Authentication**: [AWS Amplify](https://aws.amazon.com/amplify/)
- **Testing**:
  - [Jest](https://jestjs.io/) - Unit testing
  - [Testing Library](https://testing-library.com/) - Component testing
  - [Playwright](https://playwright.dev/) - E2E testing
- **Code Quality**:
  - [ESLint 9](https://eslint.org/) - Linting
  - [Prettier](https://prettier.io/) - Code formatting
  - [TypeScript](https://www.typescriptlang.org/) - Type checking

### Backend
- **Framework**: [Spring Boot 3.5](https://spring.io/projects/spring-boot)
- **Language**: Java 21
- **Database**: [PostgreSQL 15](https://www.postgresql.org/)
- **Migration**: [Flyway](https://flywaydb.org/) - Database version control
- **Messaging**: [RabbitMQ](https://www.rabbitmq.com/) - Message broker (via Spring AMQP)
- **Real-time**: [Spring WebSocket](https://docs.spring.io/spring-framework/reference/web/websocket.html) - WebSocket support
- **Security**: 
  - [Spring Security](https://spring.io/projects/spring-security)
  - [OAuth2 Resource Server](https://spring.io/guides/tutorials/spring-boot-oauth2)
  - [AWS Cognito](https://aws.amazon.com/cognito/) - User authentication
- **API Documentation**: [SpringDoc OpenAPI 3](https://springdoc.org/) (Swagger)
- **Testing**:
  - JUnit 5
  - Spring Boot Test
  - Spring Security Test
  - JaCoCo - Code coverage
- **Build Tool**: [Maven 3](https://maven.apache.org/)
- **Containerization**: [Docker](https://www.docker.com/)

### Infrastructure
- **Cloud Provider**: [AWS](https://aws.amazon.com/)
- **IaC**: [Terraform](https://www.terraform.io/)
- **CI/CD**: [GitHub Actions](https://github.com/features/actions)
- **Security Scanning**: [Snyk](https://snyk.io/)
- **API Testing**: [Bruno](https://www.usebruno.com/) - API client

### Development Tools
- **Git Hooks**: [Husky](https://typicode.github.io/husky/) - Pre-commit hooks
- **Staged Linting**: [lint-staged](https://github.com/okonet/lint-staged)
- **Architecture Diagrams**: [C4 Model](https://c4model.com/) with PlantUML

---

## 🏗️ Architecture

SPM Orangle follows a modern microservices-inspired architecture with clear separation of concerns:

```
┌─────────────────┐      ┌─────────────────┐      ┌─────────────────┐
│                 │      │                 │      │                 │
│  Next.js        │─────▶│  Spring Boot    │─────▶│   PostgreSQL    │
│  Frontend       │      │  Backend API    │      │   Database      │
│                 │      │                 │      │                 │
└─────────────────┘      └─────────────────┘      └─────────────────┘
         │                       │                         ▲
         │                       │                         │
         │                       ▼                         │
         │              ┌─────────────────┐               │
         │              │                 │               │
         │              │    RabbitMQ     │               │
         │              │  Message Queue  │               │
         │              │                 │               │
         │              └─────────────────┘               │
         │                       │                         │
         ▼                       ▼                         │
┌─────────────────┐      ┌─────────────────┐             │
│                 │      │                 │             │
│   WebSocket     │◀────▶│    Flyway       │─────────────┘
│   Real-time     │      │   Migrations    │
│                 │      │                 │
└─────────────────┘      └─────────────────┘
         │
         ▼
┌─────────────────┐
│                 │
│  AWS Cognito    │
│  Authentication │
│                 │
└─────────────────┘
```

### Key Components

1. **Frontend (Next.js)**: Server-side rendered React application with TypeScript
2. **Backend (Spring Boot)**: RESTful API server with business logic and data access
3. **Database (PostgreSQL)**: Relational database with Flyway migrations
4. **Message Queue (RabbitMQ)**: Asynchronous task processing and event distribution
5. **WebSocket**: Real-time bidirectional communication for notifications
6. **Authentication (AWS Cognito)**: OAuth2/JWT-based user authentication

### Detailed Architecture Documentation

The repository includes comprehensive C4 model diagrams:
- **C1 - System Context**: High-level system overview and external dependencies
- **C2 - Container Diagram**: Application containers and their interactions
- **C3 - Component Diagrams**: Internal component structure for key modules
  - Backend Task Management
  - Backend Security
  - Backend Notification System
  - Backend Reporting System
- **C4 - Code Diagrams**: Implementation details
  - RabbitMQ Topology
  - Event Flow
  - Export Strategy Factory Pattern

📁 View diagrams in `/c4-diagrams/`

---

## 🚀 Getting Started

### Prerequisites

Ensure you have the following installed:

**Required:**
- **Node.js**: v22.x or higher ([Download](https://nodejs.org/))
- **Java**: JDK 21 ([Download](https://adoptium.net/))
- **Maven**: 3.8+ ([Download](https://maven.apache.org/download.cgi))
- **PostgreSQL**: 15+ ([Download](https://www.postgresql.org/download/))
- **Docker**: Latest version ([Download](https://www.docker.com/get-started))
- **Docker Compose**: Included with Docker Desktop

**Optional (for deployment):**
- **Terraform**: Latest version ([Install](https://developer.hashicorp.com/terraform/install))
- **AWS CLI**: Latest version ([Install](https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html))
- **Bruno**: API client for testing ([Download](https://www.usebruno.com/downloads))

---

### Backend Setup

The backend is a Spring Boot application located in `/backend/spmorangle/`.

#### 1. Database Setup with Docker Compose

The easiest way to run PostgreSQL locally is using Docker Compose:

```bash
cd backend
docker-compose up -d
```

This will:
- Start PostgreSQL 15 on port 5432
- Start RabbitMQ with management UI on port 15672
- Automatically run Flyway migrations

For more details, see [backend/database/README.md](backend/database/README.md).

#### 2. Environment Configuration

Create an environment file (optional, defaults work for local development):

```bash
cd backend/spmorangle
cp .env.example .env
```

Edit `.env` with your local configuration if needed.

#### 3. Build and Run

**Option A: Using Maven (Development)**
```bash
cd backend/spmorangle

# Compile the application
mvn compile

# Run with local profile
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

**Option B: Using Docker (Production-like)**
```bash
cd backend/spmorangle

# Build the Docker image
docker build -t spmorangle-backend .

# Run the container
docker run -p 8080:8080 spmorangle-backend
```

The backend API will be available at `http://localhost:8080`.

#### 4. Verify Installation

- API Base URL: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- RabbitMQ Management: `http://localhost:15672` (guest/guest)

**Test with curl:**
```bash
curl http://localhost:8080/actuator/health
```

---

### Frontend Setup

The frontend is a Next.js application located in `/frontend/`.

#### 1. Install Dependencies

```bash
cd frontend
npm ci
```

This will also automatically install Playwright browsers for E2E testing.

#### 2. Environment Configuration

Create environment files for different environments:

```bash
# Development (local backend)
cp .env.example .env.local

# Production
cp .env.example .env.production
```

Edit `.env.local`:
```env
NEXT_PUBLIC_API_URL=http://localhost:8080
NEXT_PUBLIC_WS_URL=ws://localhost:8080/ws
```

#### 3. Run Development Server

```bash
npm run dev
```

The frontend will be available at `http://localhost:3000`.

#### 4. Build for Production

```bash
npm run build
npm start
```

---

### Database Setup

SPM Orangle uses Flyway for database migrations to ensure consistent schema across environments.

#### Migration Files

All migrations are located in `/backend/database/migrations/`.

#### Naming Convention

Migrations follow the format: `V<yyyymmddhhmm>__<description>.sql`

Example: `V202503031230__create_users_table.sql`

#### Creating New Migrations

1. Create a new SQL file in `/backend/database/migrations/`
2. Use the timestamp naming convention
3. Write idempotent SQL where possible
4. Test locally before committing

Example migration:
```sql
-- V202503061045__Add_Product_Table.sql
CREATE TABLE IF NOT EXISTS product (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### Running Migrations

Migrations run automatically when the backend starts with Docker Compose. To run manually:

```bash
cd backend/spmorangle
mvn flyway:migrate -Dflyway.configFiles=flyway.conf
```

For more information, see [backend/database/README.md](backend/database/README.md).

---

## 📁 Project Structure

```
spmOrangle/
├── .github/
│   ├── workflows/          # CI/CD pipeline definitions
│   │   ├── backend-ci.yml
│   │   ├── frontend-ci.yml
│   │   ├── db-cd.yml
│   │   └── snyk-security-scan.yml
│   └── agents/             # Custom GitHub Copilot agents
├── backend/
│   ├── database/           # Flyway migrations
│   │   ├── migrations/
│   │   └── README.md
│   ├── rabbitmq/           # RabbitMQ configuration
│   ├── spmorangle/         # Spring Boot application
│   │   ├── src/
│   │   │   ├── main/
│   │   │   │   ├── java/com/spmorangle/crm/
│   │   │   │   │   ├── departmentmgmt/    # Department management
│   │   │   │   │   ├── projectmgmt/       # Project management
│   │   │   │   │   ├── taskmgmt/          # Task management
│   │   │   │   │   ├── usermanagement/    # User management
│   │   │   │   │   ├── reporting/         # Reporting system
│   │   │   │   │   ├── notification/      # Notification system
│   │   │   │   │   └── security/          # Security & auth
│   │   │   │   └── resources/
│   │   │   │       ├── application.yml    # Main config
│   │   │   │       ├── application-local.yml
│   │   │   │       ├── application-dev.yml
│   │   │   │       ├── application-qa.yml
│   │   │   │       └── application-prod.yml
│   │   │   └── test/
│   │   ├── pom.xml
│   │   └── Dockerfile
│   ├── docker-compose.yml  # Local development services
│   └── README.md
├── frontend/
│   ├── app/                # Next.js app directory
│   │   ├── (app)/          # Authenticated routes
│   │   │   ├── auth/
│   │   │   ├── dashboard/
│   │   │   ├── projects/
│   │   │   ├── tasks/
│   │   │   ├── reports/
│   │   │   ├── calendar/
│   │   │   ├── notifications-test/
│   │   │   ├── profile/
│   │   │   └── user-management/
│   │   ├── globals.css
│   │   ├── layout.tsx
│   │   └── page.tsx
│   ├── components/         # React components
│   ├── contexts/           # React contexts
│   ├── hooks/              # Custom React hooks
│   ├── lib/                # Utility functions
│   ├── services/           # API service layer
│   ├── types/              # TypeScript types
│   ├── __tests__/          # Unit tests
│   ├── e2e/                # E2E tests (Playwright)
│   ├── public/             # Static assets
│   ├── package.json
│   ├── tsconfig.json
│   ├── next.config.ts
│   ├── tailwind.config.js
│   ├── jest.config.ts
│   ├── playwright.config.ts
│   └── README.md
├── terraform/              # Infrastructure as Code
│   ├── global/             # Global resources
│   └── README.md
├── c4-diagrams/            # Architecture diagrams
│   ├── c1-context/
│   ├── c2-containers/
│   ├── c3-components/
│   └── c4-code/
├── bruno/                  # API test collections
│   ├── Auth/
│   ├── Tasks/
│   ├── Reports/
│   └── collection.bru
├── .husky/                 # Git hooks
├── package.json            # Root package.json (Husky)
├── NOTIFICATION_API.md     # Notification API documentation
└── README.md               # This file
```

---

## 💻 Development

### Running Locally

#### Full Stack Development

**Terminal 1 - Backend:**
```bash
cd backend
docker-compose up -d        # Start PostgreSQL & RabbitMQ
cd spmorangle
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

**Terminal 2 - Frontend:**
```bash
cd frontend
npm run dev
```

**Access:**
- Frontend: http://localhost:3000
- Backend API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- RabbitMQ Management: http://localhost:15672

---

### Testing

#### Backend Testing

```bash
cd backend/spmorangle

# Run all tests
mvn test

# Run tests with coverage
mvn test jacoco:report

# View coverage report
open target/site/jacoco/index.html
```

**Coverage Requirements:**
- Overall coverage: 40% minimum
- Changed files: 60% minimum

#### Frontend Testing

```bash
cd frontend

# Unit tests (Jest)
npm test

# Unit tests with coverage
npm test -- --coverage

# E2E tests (Playwright)
npm run test:e2e

# E2E tests with UI
npm run test:e2e:ui

# E2E tests in headed mode
npm run test:e2e:headed

# Debug E2E tests
npm run test:e2e:debug

# View E2E test report
npm run test:e2e:report

# Generate E2E tests interactively
npm run test:e2e:codegen
```

---

### Linting & Code Quality

#### Backend

```bash
cd backend/spmorangle

# Check code style
mvn checkstyle:check

# Run static analysis
mvn verify
```

#### Frontend

```bash
cd frontend

# Run ESLint
npm run lint

# Type checking
npm run typecheck

# Format code with Prettier
npx prettier --write .
```

#### Pre-commit Hooks

The project uses Husky to run linting on staged files before commit:

```bash
# Install hooks (done automatically by npm install)
npm run prepare
```

Hooks will automatically:
1. Run ESLint and Prettier on staged JS/TS files
2. Run TypeScript compiler on staged TS files
3. Prevent commits if checks fail

---

## 📚 API Documentation

### REST API

The backend exposes a RESTful API documented with OpenAPI 3.0 (Swagger).

**Swagger UI:** http://localhost:8080/swagger-ui.html

**OpenAPI Spec:** http://localhost:8080/v3/api-docs

### Notification API

Real-time notifications are delivered via WebSocket and REST API.

📖 **Complete Documentation:** [NOTIFICATION_API.md](NOTIFICATION_API.md)

**Key Endpoints:**
- `GET /api/notifications` - Fetch all notifications
- `GET /api/notifications/unread-count` - Get unread count
- `PATCH /api/notifications/{id}/read` - Mark as read
- `PATCH /api/notifications/mark-all-read` - Mark all as read

**WebSocket:**
- Connection: `ws://localhost:8080/ws/notifications`
- Protocol: STOMP over SockJS
- Topic: `/topic/notifications/{userId}`

### Bruno API Collections

The repository includes pre-configured API requests in Bruno format.

**Location:** `/bruno/`

**Collections:**
- Authentication (`/bruno/Auth/`)
- Tasks (`/bruno/Tasks/`)
- Reports (`/bruno/Reports/`)

**Usage:**
1. Install [Bruno](https://www.usebruno.com/downloads)
2. Open the `/bruno/` folder in Bruno
3. Configure authentication token in `collection.bru`
4. Execute requests

---

## 🚢 Deployment

### Infrastructure Provisioning

SPM Orangle uses Terraform for Infrastructure as Code.

#### Prerequisites

1. Install Terraform ([Guide](https://developer.hashicorp.com/terraform/install))
2. Install AWS CLI ([Guide](https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html))
3. Generate AWS IAM Access Key ([Guide](https://docs.aws.amazon.com/IAM/latest/UserGuide/id_credentials_access-keys.html))

#### Setup

```bash
# Configure AWS CLI (one-time setup)
cd ~/.aws
aws configure
# Enter: Access Key ID, Secret Access Key, Region, Output format

# Navigate to Terraform directory
cd terraform/global

# Initialize Terraform
terraform init

# Preview changes
terraform plan

# Apply infrastructure changes
terraform apply
```

For detailed instructions, see [terraform/README.md](terraform/README.md).

### Docker Deployment

#### Backend

```bash
cd backend/spmorangle

# Build image
docker build -t spmorangle-backend:latest .

# Run container
docker run -d \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/spmorangle \
  -e SPRING_DATASOURCE_USERNAME=postgres \
  -e SPRING_DATASOURCE_PASSWORD=yourpassword \
  --name spmorangle-backend \
  spmorangle-backend:latest
```

#### Frontend

```bash
cd frontend

# Build for production
npm run build

# Deploy to Vercel (recommended)
vercel deploy --prod

# Or build Docker image
docker build -t spmorangle-frontend:latest .
docker run -d -p 3000:3000 spmorangle-frontend:latest
```

---

## 🔄 CI/CD Pipeline

The project uses GitHub Actions for continuous integration and deployment.

### Workflows

#### 1. Backend CI (`.github/workflows/backend-ci.yml`)

**Triggers:**
- Pull requests to `main`
- Push to `backend/spmorangle/**`

**Steps:**
1. Checkout code
2. Set up JDK 21
3. Compile application
4. Run tests with PostgreSQL
5. Generate JaCoCo coverage report
6. Add coverage comment to PR
7. Package application

**Requirements:**
- Overall coverage: ≥40%
- Changed files coverage: ≥60%

#### 2. Frontend CI (`.github/workflows/frontend-ci.yml`)

**Triggers:**
- Pull requests to `main`
- Push to `frontend/**`

**Steps:**
1. Checkout code
2. Set up Node.js 22
3. Install dependencies
4. Run type checking
5. Run tests with coverage
6. Generate coverage report
7. Build application

#### 3. Database CD (`.github/workflows/db-cd.yml`)

**Triggers:**
- Changes to database migrations

**Steps:**
1. Run Flyway migrations
2. Update database schema
3. Validate migrations

#### 4. Security Scan (`.github/workflows/snyk-security-scan.yml`)

**Triggers:**
- Pull requests to `main`
- Scheduled weekly scans

**Steps:**
1. Scan dependencies for vulnerabilities
2. Report security issues
3. Create security advisories

### Branch Protection

The `main` branch is protected with the following rules:
- ✅ Require pull request reviews
- ✅ Require status checks to pass (CI workflows)
- ✅ Require branches to be up to date
- ✅ Require conversation resolution

---

## 🤝 Contributing

We welcome contributions! Please follow these guidelines:

### Development Workflow

1. **Create a feature branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. **Make your changes**
   - Follow existing code style
   - Write tests for new features
   - Update documentation as needed

3. **Run tests locally**
   ```bash
   # Backend
   cd backend/spmorangle && mvn test
   
   # Frontend
   cd frontend && npm test
   ```

4. **Commit your changes**
   ```bash
   git add .
   git commit -m "feat: add new feature"
   ```
   
   Use [Conventional Commits](https://www.conventionalcommits.org/):
   - `feat:` - New feature
   - `fix:` - Bug fix
   - `docs:` - Documentation changes
   - `style:` - Code style changes (formatting)
   - `refactor:` - Code refactoring
   - `test:` - Adding tests
   - `chore:` - Maintenance tasks

5. **Push and create Pull Request**
   ```bash
   git push origin feature/your-feature-name
   ```
   
   Then create a PR on GitHub with:
   - Clear description of changes
   - Link to related issues
   - Screenshots for UI changes

### Code Style

#### Java (Backend)
- Follow [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- Use Lombok for boilerplate reduction
- Write JavaDoc for public APIs
- Maximum line length: 120 characters

#### TypeScript (Frontend)
- Follow project ESLint configuration
- Use Prettier for formatting
- Prefer functional components and hooks
- Use TypeScript types (avoid `any`)
- Maximum line length: 100 characters

### Testing Requirements

- Write unit tests for new features
- Maintain or improve code coverage
- Backend: ≥60% coverage for changed files
- Frontend: Test critical user flows

### Database Migrations

- Always create migrations for schema changes
- Use timestamp-based naming: `V<yyyymmddhhmm>__<description>.sql`
- Test migrations on a clean database
- Write reversible migrations when possible

---

## 📖 Additional Resources

### Documentation

- [Backend README](backend/README.md) - Backend-specific documentation
- [Frontend README](frontend/README.md) - Frontend-specific documentation
- [Terraform README](terraform/README.md) - Infrastructure documentation
- [Database README](backend/database/README.md) - Database migration guide
- [Notification API](NOTIFICATION_API.md) - Real-time notification guide
- [Backend Reporting Architecture](backend/spmorangle/src/main/java/com/spmorangle/crm/reporting/ARCHITECTURE.md) - Reporting system design
- [Backend Reporting README](backend/spmorangle/src/main/java/com/spmorangle/crm/reporting/README.md) - Reporting implementation guide

### External Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Next.js Documentation](https://nextjs.org/docs)
- [Flyway Documentation](https://flywaydb.org/documentation)
- [RabbitMQ Tutorials](https://www.rabbitmq.com/tutorials)
- [AWS Cognito Documentation](https://docs.aws.amazon.com/cognito/)
- [Terraform AWS Provider](https://registry.terraform.io/providers/hashicorp/aws/latest/docs)

### Architecture Diagrams

C4 model diagrams are available in the `/c4-diagrams/` directory:

- **System Context** (`c1-context/`) - How SPM Orangle fits in the bigger picture
- **Container Diagram** (`c2-containers/`) - High-level technology choices
- **Component Diagrams** (`c3-components/`) - Detailed component structure
  - Task Management
  - Security
  - Notification System
  - Reporting System
- **Code Diagrams** (`c4-code/`) - Implementation details
  - RabbitMQ Topology
  - Event Flow
  - Factory Patterns

View diagrams with PlantUML:
```bash
# Install PlantUML
brew install plantuml  # macOS
# or download from https://plantuml.com/download

# Generate PNG from PUML
plantuml c4-diagrams/c1-context/system-context.puml
```

---

## 📄 License

This project is proprietary software. All rights reserved.

---

## 📧 Support

For questions, issues, or contributions:

- **Issues**: [GitHub Issues](https://github.com/BFGOrangle/spmOrangle/issues)
- **Pull Requests**: [GitHub PRs](https://github.com/BFGOrangle/spmOrangle/pulls)
- **Discussions**: [GitHub Discussions](https://github.com/BFGOrangle/spmOrangle/discussions)

---

**Built with ❤️ by the SPM Orangle Team**