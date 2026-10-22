# Task 11 — Docker Container Lifecycle Commands

# ─── BUILD IMAGE ──────────────────────────────────────────────────
# Build backend image from Dockerfile
docker build -t hostelcms/hostel-complaint-system:v1.0.0 ./backend

# Tag as latest
docker tag hostelcms/hostel-complaint-system:v1.0.0 hostelcms/hostel-complaint-system:latest

# List images (verify image created)
docker images | grep hostel-complaint-system

# ─── RUN CONTAINER ────────────────────────────────────────────────
# Run backend container with port mapping
docker run -d \
  --name hostel-backend \
  -p 8080:8080 \
  -e DB_HOST=host.docker.internal \
  -e DB_PORT=5432 \
  -e DB_NAME=hostel_db \
  -e DB_USERNAME=postgres \
  -e DB_PASSWORD=postgres \
  -e JWT_SECRET=mySecretKey123456789 \
  hostelcms/hostel-complaint-system:v1.0.0

# ─── INSPECT / LOGS ───────────────────────────────────────────────
# View running containers
docker ps

# View all containers (including stopped)
docker ps -a

# View container logs (live)
docker logs -f hostel-backend

# Inspect container details (IP, ports, env vars)
docker inspect hostel-backend

# View resource usage
docker stats hostel-backend --no-stream

# ─── STOP / START / RESTART ───────────────────────────────────────
# Stop container gracefully
docker stop hostel-backend

# Start a stopped container
docker start hostel-backend

# Restart container
docker restart hostel-backend

# ─── REMOVE ───────────────────────────────────────────────────────
# Remove container (must be stopped first)
docker rm hostel-backend

# Force remove running container
docker rm -f hostel-backend

# Remove image
docker rmi hostelcms/hostel-complaint-system:v1.0.0

# ─── FULL STACK VIA DOCKER COMPOSE ────────────────────────────────
# Start all services (backend + frontend + postgres)
docker-compose up -d --build

# Check all services status
docker-compose ps

# View combined logs
docker-compose logs -f

# Stop all services
docker-compose down

# Stop and remove volumes (wipes DB data)
docker-compose down -v

# ─── CLEANUP ──────────────────────────────────────────────────────
# Remove all stopped containers
docker container prune -f

# Remove dangling images
docker image prune -f

# Remove everything (USE WITH CAUTION)
docker system prune -a -f
