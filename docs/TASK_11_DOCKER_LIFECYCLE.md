# Task 11 — Docker Image and Container Lifecycle

## Deliverables

| Deliverable | Location |
|---|---|
| Backend Dockerfile | `backend/Dockerfile` |
| Frontend Dockerfile | `frontend/Dockerfile` |
| Lifecycle command log | This document |
| docker-compose.yml (multi-container) | `docker-compose.yml` |

---

## Dockerfiles

### backend/Dockerfile

```dockerfile
FROM eclipse-temurin:21-jre-alpine
LABEL maintainer="23102A0008"
LABEL org.opencontainers.image.title="hostel-cms-backend"
LABEL org.opencontainers.image.version="1.0.0"
WORKDIR /app
COPY target/complaint-system-1.0.0.jar app.jar
EXPOSE 8080
ENV SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/hostel_db
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget -qO- http://localhost:8080/api/auth/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### frontend/Dockerfile

```dockerfile
FROM node:20-alpine AS builder          # Stage 1: build React with Vite
WORKDIR /app
COPY package*.json ./
RUN npm ci --silent
COPY . .
ARG VITE_API_URL=/api
ENV VITE_API_URL=${VITE_API_URL}
RUN npm run build

FROM nginx:alpine                       # Stage 2: serve with Nginx
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

---

## Complete Container Lifecycle

### Step 1 — Pre-requisite: build Maven JAR

```powershell
mvn -B -f backend/pom.xml clean package -DskipTests
# Output: BUILD SUCCESS
# Artifact: backend/target/complaint-system-1.0.0.jar  (48.1 MB)
```

### Step 2 — Build Docker images

```powershell
# Backend
docker build -t hostel-cms-backend:1.0.0 ./backend

# Frontend (Vite build-arg sets API proxy path)
docker build -t hostel-cms-frontend:1.0.0 --build-arg VITE_API_URL=/api ./frontend
```

### Step 3 — Tag images

```powershell
docker tag hostel-cms-backend:1.0.0  hostel-cms-backend:latest
docker tag hostel-cms-backend:1.0.0  hostel-cms-backend:stable
docker tag hostel-cms-frontend:1.0.0 hostel-cms-frontend:latest
docker tag hostel-cms-frontend:1.0.0 hostel-cms-frontend:stable
```

### Step 4 — List images (`docker images`)

```
REPOSITORY            TAG       IMAGE ID       SIZE
hostel-cms-backend    1.0.0     466fa69ac02a   382MB
hostel-cms-backend    latest    4117b4f6c5d2   382MB
hostel-cms-backend    stable    4117b4f6c5d2   382MB
hostel-cms-frontend   1.0.0     31ab5c327d00   93.7MB
hostel-cms-frontend   latest    e645109d8452   93.7MB
hostel-cms-frontend   stable    e645109d8452   93.7MB
```

### Step 5 — Inspect image metadata (`docker inspect`)

```powershell
docker inspect hostel-cms-backend:1.0.0 --format "..."
```

```
ID:       sha256:466fa69ac02a...
Created:  2026-10-29T...
OS/Arch:  linux/amd64
Ports:    {"8080/tcp":{}}
Entrypt:  ["java","-jar","app.jar"]
WorkDir:  /app
Labels:   {"maintainer":"23102A0008",
           "org.opencontainers.image.title":"hostel-cms-backend",
           "org.opencontainers.image.version":"1.0.0",...}
```

### Step 6 — Run containers with port mapping (`docker run -d -p`)

```powershell
# 1. Database (PostgreSQL) — host port 5440 → container 5432
docker run -d --name t11-db \
  -e POSTGRES_DB=hostel_db \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5440:5432 \
  postgres:15-alpine

# 2. Backend (Spring Boot) — host port 8088 → container 8080
docker run -d --name t11-backend \
  --link t11-db:db \
  -p 8088:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/hostel_db \
  -e SPRING_DATASOURCE_USERNAME=postgres \
  -e SPRING_DATASOURCE_PASSWORD=postgres \
  -e APP_JWT_SECRET=<secret> \
  hostel-cms-backend:1.0.0

# 3. Frontend (Nginx) — host port 8090 → container 80
docker run -d --name t11-frontend \
  --link t11-backend:backend \
  -p 8090:80 \
  hostel-cms-frontend:1.0.0
```

### Step 7 — Verify running containers (`docker ps`)

```
CONTAINER ID  IMAGE                      COMMAND              CREATED     STATUS              PORTS
3255cfe789d6  hostel-cms-frontend:1.0.0  "/docker-entrypoint" 36s ago     Up 36 seconds       0.0.0.0:8090->80/tcp
f7aacf4a51dc  hostel-cms-backend:1.0.0   "java -jar app.jar"  5m ago      Up 5m (healthy)     0.0.0.0:8088->8080/tcp
1da765bee2e3  postgres:15-alpine         "docker-entrypoint"  7m ago      Up 7 minutes        0.0.0.0:5440->5432/tcp
```

### Step 8 — Port mapping (`docker port`)

```powershell
docker port t11-backend    # 8080/tcp -> 0.0.0.0:8088
docker port t11-frontend   # 80/tcp   -> 0.0.0.0:8090
docker port t11-db         # 5432/tcp -> 0.0.0.0:5440
```

### Step 9 — Smoke tests (deployed application URLs)

```
Backend  http://localhost:8088/api/auth/health  →  "Hostel Complaint System is running!"
Frontend http://localhost:8090/                 →  HTTP 200  (470 bytes)
```

### Step 10 — Inspect logs (`docker logs`)

```powershell
docker logs t11-backend    # Spring Boot startup + health-check requests
docker logs t11-frontend   # Nginx startup + access log lines
docker logs t11-db         # PostgreSQL checkpoint logs
```

Sample backend log:
```
Started ComplaintSystemApplication in 8.3 seconds
Securing GET /api/auth/health
```

Sample frontend log:
```
2026/10/29 16:18:17 [notice] nginx/1.27.3
172.17.0.1 - - [29/Oct/2026] "GET / HTTP/1.1" 200 470
```

### Step 11 — Resource stats (`docker stats --no-stream`)

```
NAME           CPU %   MEM USAGE / LIMIT     MEM %   NET I/O           PIDS
t11-backend    0.30%   231.3MiB / 7.613GiB   2.97%   19.4kB / 24.4kB   47
t11-frontend   0.00%   10.3MiB  / 7.613GiB   0.13%   2.25kB / 2.39kB   13
t11-db         0.00%   56.1MiB  / 7.613GiB   0.72%   24.5kB / 17.2kB   16
```

### Step 12 — Stop a container (`docker stop`)

```powershell
docker stop t11-frontend
# t11-frontend: Exited (0)
```

### Step 13 — Restart a container (`docker restart`)

```powershell
docker restart t11-frontend
# t11-frontend: Up 12 seconds | 0.0.0.0:8090->80/tcp
```

### Step 14 — Stop all containers

```powershell
docker stop t11-frontend t11-backend t11-db
# t11-frontend: Exited (0)
# t11-backend:  Exited (143)
# t11-db:       Exited (0)
```

### Step 15 — Remove containers (`docker rm`)

```powershell
docker rm t11-frontend t11-backend t11-db
docker ps -a --filter "name=t11"
# (empty — all containers removed)
```

### Step 16 — Remove images (`docker rmi`)

```powershell
docker rmi hostel-cms-backend:1.0.0 hostel-cms-backend:latest hostel-cms-backend:stable
docker rmi hostel-cms-frontend:1.0.0 hostel-cms-frontend:latest hostel-cms-frontend:stable

# Output:
# Untagged: hostel-cms-backend:latest
# Untagged: hostel-cms-backend:stable
# Deleted: sha256:4117b4f6c5d2...
# Untagged: hostel-cms-frontend:latest
# Deleted: sha256:e645109d8452...

docker images --filter "reference=hostel-cms-*"
# (empty — all images removed)
```

---

## Lifecycle Summary Diagram

```
JAR (mvn package)
      │
      ▼
docker build ──► Image: hostel-cms-backend:1.0.0 ──► docker tag ──► :latest / :stable
      │
      ▼
docker run -d -p 8088:8080     ←── port mapping (host:container)
      │
      ├── docker ps              ← list running
      ├── docker port            ← verify port bindings
      ├── docker logs            ← inspect output
      ├── docker stats           ← resource usage
      ├── docker inspect         ← full metadata
      │
      ├── docker stop            ← graceful shutdown (SIGTERM)
      ├── docker restart         ← stop + start
      │
      ├── docker stop            ← final stop
      ├── docker rm              ← remove container
      └── docker rmi             ← remove image
```

---

## Fix applied in Task 11

The Spring Boot circular dependency between `SecurityConfig` and
`JwtAuthenticationFilter` was resolved by injecting the filter with
`@Lazy` in the constructor:

```java
public SecurityConfig(@Lazy JwtAuthenticationFilter jwtAuthFilter, ...) { ... }
```

This defers filter instantiation until after all beans are initialized,
breaking the cycle without changing the application logic.
