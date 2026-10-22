# 🏠 Hostel Complaint Management System

![DevOps](https://img.shields.io/badge/DevOps-CI%2FCD-blue)
![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-green)
![Docker](https://img.shields.io/badge/Docker-Containerized-blue)
![Jenkins](https://img.shields.io/badge/Jenkins-Automated-red)
![License](https://img.shields.io/badge/License-MIT-yellow)

> A full-stack **Containerized Hostel Complaint Management System** following a complete DevOps lifecycle — from Git to Jenkins CI/CD to Docker to Ansible provisioning.

---

## 📌 Problem Statement

Hostel students face delays and lack of transparency in complaint resolution due to manual/unstructured processes. This system automates the entire complaint lifecycle — from submission to resolution — with full CI/CD and container-based deployment.

---

## 🎯 Target Users

| Role        | Description                                  |
|-------------|----------------------------------------------|
| Student     | Raise, view and track personal complaints    |
| Hostel Staff| Resolve complaints assigned to them          |
| Admin       | Monitor, assign and manage all complaints    |

---

## 🚀 Tech Stack

| Layer         | Technology                          |
|---------------|-------------------------------------|
| Backend       | Java 21 + Spring Boot 3 + Maven     |
| Frontend      | React 18 (Vite)                     |
| Database      | PostgreSQL 15                       |
| Auth          | JWT (JSON Web Token)                |
| CI/CD         | Jenkins                             |
| Testing       | Selenium WebDriver + TestNG         |
| Containers    | Docker + Docker Compose             |
| Config Mgmt   | Ansible                             |
| Reverse Proxy | Nginx                               |

---

## 📁 Project Structure

```
hostel-complaint-system/
├── backend/                    # Spring Boot REST API (Maven)
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/hostel/complaintsystem/
│   │   │   │   ├── controller/     # REST Controllers
│   │   │   │   ├── service/        # Business Logic
│   │   │   │   ├── repository/     # JPA Repositories
│   │   │   │   ├── model/          # Entity Classes
│   │   │   │   ├── dto/            # Data Transfer Objects
│   │   │   │   ├── security/       # JWT Security
│   │   │   │   ├── config/         # Configuration
│   │   │   │   └── exception/      # Global Exception Handler
│   │   │   └── resources/
│   │   │       └── application.properties
│   │   └── test/
│   ├── Dockerfile
│   └── pom.xml
├── frontend/                   # React UI (Vite)
│   ├── src/
│   │   ├── pages/              # Page Components
│   │   ├── api.js              # API Calls
│   │   └── App.jsx             # Root Component
│   ├── Dockerfile
│   └── package.json
├── tests/                      # Selenium WebDriver Test Suite
│   ├── src/test/java/com/hostel/tests/
│   │   ├── BaseTest.java
│   │   ├── LoginTest.java
│   │   ├── RaiseComplaintTest.java
│   │   └── AdminDashboardTest.java
│   └── pom.xml
├── ansible/                    # Ansible Playbooks
│   ├── playbook.yml
│   ├── rollback.yml
│   ├── inventory.ini
│   └── ansible.cfg
├── docs/                       # Documentation & Architecture
│   ├── ARCHITECTURE.md
│   └── TECHNICAL_DOCS.md
├── .github/                    # GitHub Templates
│   ├── ISSUE_TEMPLATE/
│   │   ├── bug_report.md
│   │   └── feature_request.md
│   └── PULL_REQUEST_TEMPLATE.md
├── tools/                      # Local Maven (bundled)
│   └── apache-maven-3.9.6/
├── Jenkinsfile                 # Pipeline as Code
├── docker-compose.yml          # Multi-container orchestration
├── .env.example                # Environment variable template
├── .gitignore
├── BRANCH_POLICY.md            # Branch naming and workflow rules
└── README.md
```

---

## 🏃 Quick Start (Local Setup)

### Prerequisites

| Tool        | Version | Download                              |
|-------------|---------|---------------------------------------|
| Java JDK    | 21+     | https://adoptium.net                  |
| Maven       | 3.9+    | Bundled in `tools/apache-maven-3.9.6` |
| Node.js     | 18+     | https://nodejs.org                    |
| PostgreSQL  | 15+     | https://www.postgresql.org            |
| Docker      | Latest  | https://www.docker.com/products/docker-desktop |

### Step 1 — Clone the Repository

```bash
git clone https://github.com/<your-username>/hostel-complaint-system.git
cd hostel-complaint-system
```

### Step 2 — Setup Environment Variables

```bash
cp .env.example .env
# Edit .env with your PostgreSQL credentials
```

### Step 3 — Start Backend

```bash
cd backend
mvn spring-boot:run
# API runs at http://localhost:8080
```

### Step 4 — Start Frontend

```bash
cd frontend
npm install
npm run dev
# UI runs at http://localhost:5173
```

### Step 5 — Run with Docker (Full Stack)

```bash
docker-compose up --build
# Backend  → http://localhost:8080
# Frontend → http://localhost:3000
# DB       → localhost:5432
```

---

## 📊 API Endpoints

| Method | Endpoint                          | Role    | Description              |
|--------|-----------------------------------|---------|--------------------------|
| POST   | `/api/auth/register`              | Public  | Register user            |
| POST   | `/api/auth/login`                 | Public  | Login & get JWT token    |
| POST   | `/api/complaints`                 | Student | Raise a complaint        |
| GET    | `/api/complaints`                 | Admin   | Get all complaints       |
| GET    | `/api/complaints/{id}`            | Both    | Get complaint by ID      |
| PUT    | `/api/complaints/{id}/status`     | Admin   | Update complaint status  |
| PUT    | `/api/complaints/{id}/assign`     | Admin   | Assign to staff          |
| GET    | `/api/complaints/user/{userId}`   | Student | Get user's complaints    |
| GET    | `/api/admin/stats`                | Admin   | Dashboard statistics     |

---

## ✅ MVP Scope (15 Tasks)

- [x] 1. User Registration & Login (JWT)
- [x] 2. Raise a Complaint
- [x] 3. Select Complaint Category
- [x] 4. Add Description & Priority
- [x] 5. Auto-generate Complaint ID
- [x] 6. View Complaint List (Student)
- [x] 7. Track Complaint Status
- [x] 8. Admin Dashboard
- [x] 9. Update Complaint Status (Admin)
- [x] 10. Assign Complaint to Staff
- [x] 11. Priority Tagging (Low / Medium / High)
- [x] 12. Complaint History Log
- [x] 13. Docker Containerization
- [x] 14. Jenkins CI/CD Pipeline
- [x] 15. Ansible Provisioning

---

## 🌿 Branch Naming Convention

See [BRANCH_POLICY.md](./BRANCH_POLICY.md) for full rules.

| Branch       | Purpose                        |
|--------------|--------------------------------|
| `master`     | Production-ready stable code   |
| `dev`        | Integration / staging branch   |
| `feature/*`  | New feature development        |
| `bugfix/*`   | Bug fixes                      |
| `hotfix/*`   | Emergency production patches   |
| `release/*`  | Release preparation            |

---

## 🐳 DevOps Workflow

```
Developer Push
      │
      ▼
GitHub Repository
      │
      ▼
Jenkins (Triggered by Webhook / Poll SCM)
      │
      ├── Stage 1: Checkout
      ├── Stage 2: Build (Maven)
      ├── Stage 3: Unit Tests
      ├── Stage 4: Package (.jar)
      ├── Stage 5: Docker Build & Tag
      ├── Stage 6: Push to Registry
      └── Stage 7: Deploy Container
```

---

## 🧪 Running Tests

```bash
# Run Selenium tests (requires running application)
cd tests
mvn test

# Jenkins runs this automatically in pipeline
```

---

## 👨‍💻 Developer Info

| Field      | Value                                        |
|------------|----------------------------------------------|
| Roll No    | 23102A0008                                   |
| Project    | Containerized Hostel Complaint Management System |
| Subject    | DevOps                                       |
| Stack      | Java + Spring Boot + React + Docker + Jenkins + Ansible |

---

## 📄 License

MIT License — see [LICENSE](./LICENSE) for details.
