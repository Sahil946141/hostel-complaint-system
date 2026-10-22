# ============================================================
#   Jenkins Setup Script for Hostel CMS
#   Task 6: Jenkins Installation and CI Job Configuration
#   Run this script as Administrator in PowerShell
# ============================================================

Write-Host "=== Hostel CMS — Jenkins Setup ===" -ForegroundColor Cyan

# ── Step 1: Download Jenkins WAR ────────────────────────────
Write-Host "`n[1/5] Downloading Jenkins WAR..." -ForegroundColor Yellow
$jenkinsUrl = "https://get.jenkins.io/war-stable/latest/jenkins.war"
$jenkinsDir = "E:\Devops_23102A0008\jenkins"
New-Item -ItemType Directory -Path $jenkinsDir -Force | Out-Null
$jenkinsWar = "$jenkinsDir\jenkins.war"

if (-Not (Test-Path $jenkinsWar)) {
    Invoke-WebRequest -Uri $jenkinsUrl -OutFile $jenkinsWar -UseBasicParsing
    Write-Host "   Jenkins WAR downloaded to $jenkinsWar" -ForegroundColor Green
} else {
    Write-Host "   Jenkins WAR already exists. Skipping download." -ForegroundColor Gray
}

# ── Step 2: Create Jenkins Home ─────────────────────────────
Write-Host "`n[2/5] Creating Jenkins home directory..." -ForegroundColor Yellow
$jenkinsHome = "E:\Devops_23102A0008\jenkins\jenkins_home"
New-Item -ItemType Directory -Path $jenkinsHome -Force | Out-Null
$env:JENKINS_HOME = $jenkinsHome
Write-Host "   JENKINS_HOME set to: $jenkinsHome" -ForegroundColor Green

# ── Step 3: Start Jenkins ────────────────────────────────────
Write-Host "`n[3/5] Starting Jenkins on port 9090..." -ForegroundColor Yellow
Write-Host "   IMPORTANT: Jenkins will show initial admin password on first run." -ForegroundColor Magenta
Write-Host "   Look for: 'Please use the following password to proceed to installation'" -ForegroundColor Magenta
Write-Host ""
Write-Host "   After Jenkins starts, go to: http://localhost:9090" -ForegroundColor Cyan
Write-Host "   Username: admin" -ForegroundColor Cyan  
Write-Host "   Password: (shown in the console below)" -ForegroundColor Cyan
Write-Host ""
Write-Host "   Press Ctrl+C to stop Jenkins" -ForegroundColor Red
Write-Host "`n[Starting Jenkins...]" -ForegroundColor Yellow

$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.12.8-hotspot"
& "$env:JAVA_HOME\bin\java" `
    -DJENKINS_HOME="$jenkinsHome" `
    -jar "$jenkinsWar" `
    --httpPort=9090 `
    --prefix=/jenkins

Write-Host "`n=== Jenkins stopped ===" -ForegroundColor Red
