# Technical Documentation
# Hostel Complaint Management System
# Roll No: 23102A0008 | DevOps Project

---

## Table of Contents
1. [System Architecture](#architecture)
2. [API Reference](#api)
3. [DevOps Workflow](#devops)
4. [Docker Guide](#docker)
5. [Jenkins Pipeline](#jenkins)
6. [Ansible Provisioning](#ansible)
7. [Selenium Testing](#selenium)
8. [Troubleshooting](#troubleshooting)
9. [Limitations & Future Enhancements](#future)

---

## 1. System Architecture {#architecture}

```
┌─────────────────────────────────────────────────────┐
│                    CLIENT LAYER                       │
│   React 19 (Vite) → Nginx → http://localhost:3000   │
└──────────────────────────┬──────────────────────────┘
                           │ HTTP (REST API)
┌──────────────────────────▼──────────────────────────┐
│                   BACKEND LAYER                       │
│   Spring Boot 3 (Java 21) → http://localhost:8080   │
│   JWT Auth | Spring Security | Spring Data JPA       │
└──────────────────────────┬──────────────────────────┘
                           │ JDBC (PostgreSQL)
┌──────────────────────────▼──────────────────────────┐
│                  DATABASE LAYER                       │
│   PostgreSQL 15 → port 5432                          │
│   Tables: users, complaints                          │
└─────────────────────────────────────────────────────┘

All layers run in Docker containers via Docker Compose
```

---

## 2. API Reference {#api}

### Auth Endpoints
| Method | URL | Auth | Description |
|--------|-----|------|-------------|
| POST | `/api/auth/register` | None | Register new user |
| POST | `/api/auth/login` | None | Login, returns JWT |
| GET | `/api/auth/health` | None | Health check |

### Complaint Endpoints
| Method | URL | Auth | Description |
|--------|-----|------|-------------|
| POST | `/api/complaints` | JWT | Raise complaint |
| GET | `/api/complaints` | JWT | Get all complaints |
| GET | `/api/complaints/my` | JWT | Get my complaints |
| GET | `/api/complaints/{id}` | JWT | Get by ID |
| PUT | `/api/complaints/{id}/status` | JWT | Update status |
| GET | `/api/complaints/status/{status}` | JWT | Filter by status |

### Admin Endpoints
| Method | URL | Auth | Description |
|--------|-----|------|-------------|
| GET | `/api/admin/dashboard` | JWT | Dashboard stats |
| GET | `/api/admin/staff` | JWT | All staff list |
| GET | `/api/admin/users` | JWT | All users |

### Sample Requests

**Register:**
```json
POST /api/auth/register
{
  "name": "Rahul Kumar",
  "email": "rahul@hostel.com",
  "password": "password123",
  "role": "STUDENT",
  "roomNumber": "A-101"
}
```

**Raise Complaint:**
```json
POST /api/complaints
Authorization: Bearer <token>
{
  "title": "WiFi not working in Block A",
  "description": "No internet since morning in room A-101",
  "category": "WIFI",
  "priority": "HIGH"
}
```

---

## 3. DevOps Workflow {#devops}

```
Developer commits code
        │
        ▼
   GitHub Repository
        │
        ▼ (webhook trigger)
   Jenkins Pipeline
        │
   ┌────┴────────────────────────────────────┐
   │  Stage 1: Checkout                       │
   │  Stage 2: Maven Build (compile)          │
   │  Stage 3: Unit Tests (gate ← blocks)    │
   │  Stage 4: Package (JAR artifact)         │
   │  Stage 5: Selenium Tests (gate ← blocks)│
   │  Stage 6: Docker Build                   │
   │  Stage 7: Docker Push to Hub            │
   │  Stage 8: Deploy Container              │
   └────┬────────────────────────────────────┘
        │
        ▼
   Docker Container (running app)
        │
        ▼
   Ansible Provisioning (infra setup)
```

---

## 4. Docker Guide {#docker}

### Build & Run (individual)
```bash
# Build backend image
docker build -t hostelcms/hostel-complaint-system:v1.0.0 ./backend

# Run backend
docker run -d -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/hostel_db \
  hostelcms/hostel-complaint-system:v1.0.0

# View logs
docker logs hostel_backend --tail 50

# Stop container
docker stop hostel_backend

# Remove container
docker rm hostel_backend
```

### Full Stack with Docker Compose
```bash
# Start all services
docker-compose up -d --build

# View status
docker-compose ps

# View logs
docker-compose logs -f backend

# Stop all
docker-compose down

# Stop and remove volumes
docker-compose down -v
```

### Container Lifecycle Commands
```bash
docker start hostel_backend     # Start stopped container
docker stop hostel_backend      # Gracefully stop
docker restart hostel_backend   # Restart
docker inspect hostel_backend   # Detailed info
docker stats hostel_backend     # Live resource usage
docker exec -it hostel_backend sh  # Enter container shell
```

---

## 5. Jenkins Pipeline {#jenkins}

### Starting Jenkins
```bash
cd E:\Devops_23102A0008\jenkins
.\start-jenkins.ps1
# Access: http://localhost:9090
```

### Jenkins Setup Steps
1. Open http://localhost:9090
2. Enter initial admin password (shown in console)
3. Install suggested plugins
4. Create admin user with your credentials
5. Install additional plugins:
   - Pipeline
   - Git
   - Docker Pipeline
   - JUnit
   - Blue Ocean (optional, for UI)
6. Create New Item → Pipeline → Use SCM (GitHub URL)
7. Set Jenkinsfile path: `Jenkinsfile`
8. Add credentials:
   - ID: `docker-hub-credentials`
   - Username: your Docker Hub username
   - Password: your Docker Hub password

### Triggering Build
- Manually: Click "Build with Parameters"
- Automatically: Setup webhook in GitHub:
  - URL: `http://YOUR_IP:9090/github-webhook/`
  - Content type: `application/json`
  - Events: `push`

---

## 6. Ansible Provisioning {#ansible}

### Run Full Provisioning
```bash
# Install Ansible (Linux/Mac)
pip install ansible

# Run playbook
ansible-playbook -i ansible/inventory.ini ansible/playbook.yml

# Dry run (check mode)
ansible-playbook -i ansible/inventory.ini ansible/playbook.yml --check

# Rollback to previous version
ansible-playbook -i ansible/inventory.ini ansible/rollback.yml \
  -e "rollback_tag=v1.0.0"
```

### Idempotency Proof
```bash
# Run twice — should show no changes second time
ansible-playbook -i ansible/inventory.ini ansible/playbook.yml
ansible-playbook -i ansible/inventory.ini ansible/playbook.yml
# Second run: 0 changes = idempotency confirmed
```

---

## 7. Selenium Testing {#selenium}

### Test Cases
| ID | Test | Description |
|----|------|-------------|
| TC-001 | Valid Login | Student login redirects to dashboard |
| TC-002 | Invalid Login | Wrong credentials shows error |
| TC-003 | Admin Login | Admin login redirects to admin dashboard |
| TC-004 | Raise Complaint | Form submit creates complaint with ID |
| TC-005 | Form Validation | Empty form blocked by validation |
| TC-006 | Admin Dashboard Stats | Stats grid shows complaint counts |
| TC-007 | Filter by Status | Status filter tabs work correctly |
| TC-008 | Update Status | Admin can update complaint status |

### Run Tests
```bash
# Local execution
cd tests
mvn test -Dapp.url=http://localhost:3000

# Headless (CI)
mvn test -Dapp.url=http://localhost:3000 -Dheadless=true

# Screenshots saved in: tests/test-screenshots/
```

---

## 8. Troubleshooting {#troubleshooting}

| Problem | Cause | Solution |
|---------|-------|----------|
| Backend won't start | PostgreSQL not running | Start Docker: `docker-compose up db` |
| 401 Unauthorized | JWT expired/missing | Re-login to get new token |
| Maven build fails | Java version mismatch | Set JAVA_HOME to JDK 21 |
| Docker push fails | Not logged in | `docker login` first |
| Selenium tests fail | App not running | Start backend and frontend first |
| Jenkins can't find Maven | PATH not set | Add Maven to Jenkins Global Tools |
| CORS error in browser | Backend CORS config | Already configured in SecurityConfig |

---

## 9. Limitations & Future Enhancements {#future}

### Current Limitations
- No email notifications (only in-app status updates)
- No file/image attachment for complaints
- Single-node deployment (no load balancing)
- Basic role management (no fine-grained permissions)

### Future Enhancements
1. **Email/WhatsApp Notifications** — Send alerts on status change
2. **AI Auto-Categorization** — Auto-tag complaint category using NLP
3. **Mobile App** — React Native app for students
4. **Analytics Dashboard** — Charts for complaint trends
5. **Kubernetes Deployment** — Scale with K8s for large hostels
6. **SLA Tracking** — Alert if complaint not resolved in 24h
7. **QR Code Integration** — Scan room QR to raise complaint
8. **Kubernetes + Helm** — Production-grade deployment

---

## Data Model

### users table
| Column | Type | Description |
|--------|------|-------------|
| id | BIGSERIAL | Primary key |
| name | VARCHAR | Full name |
| email | VARCHAR | Unique email |
| password | VARCHAR | BCrypt hash |
| role | ENUM | STUDENT/STAFF/ADMIN |
| room_number | VARCHAR | Room number (students) |
| created_at | TIMESTAMP | Registration time |

### complaints table
| Column | Type | Description |
|--------|------|-------------|
| id | BIGSERIAL | Primary key |
| complaint_id | VARCHAR | Auto-generated (CMP-YYYYMMDD-XXXXXX) |
| title | VARCHAR | Short title |
| description | TEXT | Detailed description |
| category | ENUM | ELECTRICAL/PLUMBING/WIFI/etc |
| status | ENUM | PENDING/IN_PROGRESS/RESOLVED/REJECTED |
| priority | ENUM | LOW/MEDIUM/HIGH/URGENT |
| student_id | FK→users | Complaint owner |
| assigned_to | FK→users | Assigned staff |
| admin_remarks | TEXT | Staff notes |
| created_at | TIMESTAMP | Submission time |
| updated_at | TIMESTAMP | Last update time |
| resolved_at | TIMESTAMP | Resolution time |
