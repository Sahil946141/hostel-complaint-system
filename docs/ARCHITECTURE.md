# Architecture — Hostel Complaint Management System

## System Architecture Overview

```
┌─────────────────────────────────────────────────────────┐
│                    CLIENT LAYER                          │
│   Browser → React 18 (Vite) → http://localhost:3000     │
└─────────────────────┬───────────────────────────────────┘
                      │ HTTP / REST API
                      ▼
┌─────────────────────────────────────────────────────────┐
│                  REVERSE PROXY (Nginx)                   │
│         Routes /api/* → Backend, /* → Frontend          │
└─────────────────────┬───────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────┐
│              BACKEND LAYER (Spring Boot 3)               │
│                  Java 21 + Maven                        │
│   ┌──────────┐  ┌──────────┐  ┌──────────────────────┐  │
│   │Controller│→ │ Service  │→ │    Repository (JPA)  │  │
│   └──────────┘  └──────────┘  └──────────────────────┘  │
│        JWT Security Filter (Spring Security)            │
│                  Port: 8080                             │
└─────────────────────┬───────────────────────────────────┘
                      │ JDBC / JPA
                      ▼
┌─────────────────────────────────────────────────────────┐
│              DATABASE LAYER (PostgreSQL 15)              │
│                    Port: 5432                           │
│   Tables: users, complaints, complaint_status_log       │
└─────────────────────────────────────────────────────────┘
```

## Data Model

### `users` Table
| Column     | Type         | Constraints          |
|------------|--------------|----------------------|
| id         | BIGINT (PK)  | AUTO_INCREMENT       |
| name       | VARCHAR(100) | NOT NULL             |
| email      | VARCHAR(150) | UNIQUE, NOT NULL     |
| password   | VARCHAR(255) | NOT NULL (BCrypt)    |
| role       | ENUM         | STUDENT / ADMIN      |
| created_at | TIMESTAMP    | DEFAULT NOW()        |

### `complaints` Table
| Column      | Type         | Constraints           |
|-------------|--------------|-----------------------|
| id          | BIGINT (PK)  | AUTO_INCREMENT        |
| title       | VARCHAR(200) | NOT NULL              |
| description | TEXT         | NOT NULL              |
| category    | ENUM         | WATER/ELECTRIC/WIFI…  |
| status      | ENUM         | PENDING/IN_PROGRESS/RESOLVED |
| priority    | ENUM         | LOW/MEDIUM/HIGH       |
| user_id     | BIGINT (FK)  | references users(id)  |
| assigned_to | VARCHAR(100) | Nullable              |
| created_at  | TIMESTAMP    | DEFAULT NOW()         |
| updated_at  | TIMESTAMP    | ON UPDATE NOW()       |

## DevOps Architecture

```
Developer (local)
       │
       │  git push
       ▼
┌─────────────────┐
│  GitHub Repo    │◄── Branch: feature/* → PR → dev → master
└────────┬────────┘
         │ Webhook / Poll SCM
         ▼
┌─────────────────┐
│    Jenkins CI   │
│  - Checkout     │
│  - mvn build    │
│  - mvn test     │
│  - Selenium     │
│  - Docker build │
│  - Docker push  │
│  - Deploy       │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  Docker Hub /   │
│  Local Registry │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  Docker Compose │
│  - backend      │
│  - frontend     │
│  - postgres     │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Ansible Playbook│  (provisions target server)
│  - Install Java │
│  - Install Nginx│
│  - Deploy app   │
│  - Health check │
└─────────────────┘
```

## API Flow Example (Raise Complaint)

```
Student Browser
    │
    │  POST /api/complaints
    │  Authorization: Bearer <JWT>
    │  Body: { title, description, category, priority }
    │
    ▼
Spring Security Filter
    │  Validate JWT → extract user
    ▼
ComplaintController.createComplaint()
    │
    ▼
ComplaintService.createComplaint()
    │  Validate input
    │  Set status = PENDING
    │  Set user = logged-in user
    ▼
ComplaintRepository.save()
    │  JPA → PostgreSQL INSERT
    ▼
Response: 201 Created
    { id, title, status: "PENDING", createdAt }
```
