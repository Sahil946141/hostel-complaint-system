# 🏠 Hostel Complaint Management System

A full-stack **Containerized Hostel Complaint Management System** built using Java Spring Boot, React, PostgreSQL, Jenkins, Docker, and Ansible — following a complete DevOps lifecycle.

---

## 📌 Problem Statement

Hostel students face delays and lack of transparency in complaint resolution due to manual/unstructured systems. This project automates the entire lifecycle — from complaint submission to resolution — with full CI/CD and container-based deployment.

---

## 🎯 Target Users

| Role | Description |
|------|-------------|
| Student | Raise and track complaints |
| Hostel Staff | Resolve assigned complaints |
| Admin | Monitor, assign and manage all complaints |

---

## 🚀 Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 21 + Spring Boot 3 + Maven |
| Frontend | React 18 (Vite) |
| Database | PostgreSQL 15 |
| Auth | JWT (JSON Web Token) |
| CI/CD | Jenkins |
| Testing | Selenium WebDriver |
| Containers | Docker + Docker Compose |
| Config Mgmt | Ansible |
| Reverse Proxy | Nginx |

---

## 📁 Project Structure

```
hostel-complaint-system/
├── backend/          # Spring Boot REST API
├── frontend/         # React UI
├── tests/            # Selenium test suite
├── docker/           # Dockerfiles + Compose
├── ansible/          # Ansible playbooks
├── jenkins/          # Jenkinsfile
└── docs/             # Documentation
```

---

## 🏃 Quick Start (Local)

### Prerequisites
- Java 21
- Maven 3.9+
- Node.js 18+
- PostgreSQL 15
- Docker Desktop

### Backend
```bash
cd backend
mvn spring-boot:run
```

### Frontend
```bash
cd frontend
npm install
npm run dev
```

### Docker (Full Stack)
```bash
docker-compose up --build
```

---

## 🌿 Branch Naming Convention

| Branch | Purpose |
|--------|---------|
| `main` | Production-ready code |
| `dev` | Integration branch |
| `feature/*` | New features |
| `bugfix/*` | Bug fixes |
| `hotfix/*` | Emergency production fixes |
| `release/*` | Release preparation |

---

## ✅ MVP Scope (15 Tasks)

1. User Registration & Login (JWT)
2. Raise a Complaint
3. Select Complaint Category
4. Add Description & Priority
5. Auto-generate Complaint ID
6. View Complaint List (Student)
7. Track Complaint Status
8. Admin Dashboard
9. Update Complaint Status (Admin)
10. Assign Complaint to Staff
11. Priority Tagging (Low/Medium/High)
12. Complaint History Log
13. Docker Containerization
14. Jenkins CI/CD Pipeline
15. Ansible Provisioning

---

## 📊 API Endpoints

| Method | Endpoint | Description |
|--------|---------|-------------|
| POST | /api/auth/register | Register user |
| POST | /api/auth/login | Login & get JWT |
| POST | /api/complaints | Raise complaint |
| GET | /api/complaints | Get all complaints |
| GET | /api/complaints/{id} | Get complaint by ID |
| PUT | /api/complaints/{id}/status | Update status |
| PUT | /api/complaints/{id}/assign | Assign to staff |
| GET | /api/complaints/user/{userId} | Get user's complaints |

---

## 👨‍💻 Developer

**Roll No:** 23102A0008  
**Project:** Containerized Hostel Complaint Management System  
**Subject:** DevOps

---

## 📄 License

MIT License
