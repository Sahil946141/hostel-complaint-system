# ============================================================
#   Docker Container Lifecycle Script
#   Task 11: Docker Image and Container Lifecycle
# ============================================================

Write-Host "=== Hostel CMS — Docker Container Lifecycle ===" -ForegroundColor Cyan

$IMAGE_NAME  = "hostelcms/hostel-complaint-system"
$TAG         = "v1.0.0"
$CONTAINER   = "hostel_backend"

# ── STEP 1: Build Image ──────────────────────────────────────
Write-Host "`n[1] Building Docker image..." -ForegroundColor Yellow
docker build -t "${IMAGE_NAME}:${TAG}" ./backend
docker tag "${IMAGE_NAME}:${TAG}" "${IMAGE_NAME}:latest"
Write-Host "  Image built: ${IMAGE_NAME}:${TAG}" -ForegroundColor Green

# ── STEP 2: Inspect Image ───────────────────────────────────
Write-Host "`n[2] Image details:" -ForegroundColor Yellow
docker image inspect "${IMAGE_NAME}:${TAG}" --format "ID: {{.Id}}`nSize: {{.Size}} bytes`nCreated: {{.Created}}"

# ── STEP 3: Run Container ───────────────────────────────────
Write-Host "`n[3] Running container (port 8080 → 8080)..." -ForegroundColor Yellow
docker run -d `
    --name $CONTAINER `
    -p 8080:8080 `
    -e SPRING_DATASOURCE_URL="jdbc:postgresql://host.docker.internal:5432/hostel_db" `
    -e SPRING_DATASOURCE_USERNAME="postgres" `
    -e SPRING_DATASOURCE_PASSWORD="postgres" `
    "${IMAGE_NAME}:${TAG}"
Write-Host "  Container started: $CONTAINER" -ForegroundColor Green

# ── STEP 4: Check Logs ──────────────────────────────────────
Write-Host "`n[4] Container logs (last 20 lines):" -ForegroundColor Yellow
Start-Sleep -Seconds 5
docker logs $CONTAINER --tail 20

# ── STEP 5: Inspect Container ───────────────────────────────
Write-Host "`n[5] Container inspection:" -ForegroundColor Yellow
docker inspect $CONTAINER --format "Name: {{.Name}}`nStatus: {{.State.Status}}`nIP: {{.NetworkSettings.IPAddress}}"

# ── STEP 6: List Running Containers ─────────────────────────
Write-Host "`n[6] Running containers:" -ForegroundColor Yellow
docker ps

# ── STEP 7: Stop Container ──────────────────────────────────
Write-Host "`n[7] Stopping container..." -ForegroundColor Yellow
docker stop $CONTAINER
Write-Host "  Container stopped" -ForegroundColor Green

# ── STEP 8: Restart Container ───────────────────────────────
Write-Host "`n[8] Restarting container..." -ForegroundColor Yellow
docker start $CONTAINER
Start-Sleep -Seconds 3
docker ps --filter "name=$CONTAINER"

# ── STEP 9: Remove Container ────────────────────────────────
Write-Host "`n[9] Removing container..." -ForegroundColor Yellow
docker stop $CONTAINER
docker rm $CONTAINER
Write-Host "  Container removed" -ForegroundColor Green

# ── STEP 10: Full Docker Compose (all services) ─────────────
Write-Host "`n[10] Starting full stack with Docker Compose..." -ForegroundColor Yellow
docker-compose up -d --build
Start-Sleep -Seconds 10
docker-compose ps

Write-Host "`n=== Docker lifecycle complete ===" -ForegroundColor Green
Write-Host "Frontend : http://localhost:3000" -ForegroundColor Cyan
Write-Host "Backend  : http://localhost:8080" -ForegroundColor Cyan
