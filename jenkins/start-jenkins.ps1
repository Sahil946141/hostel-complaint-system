# ============================================================
#   Jenkins Setup Script for Hostel CMS
#   Task 6: Jenkins Installation and CI Job Configuration
#   Run this script in PowerShell; administrator access is not required.
# ============================================================

$ErrorActionPreference = "Stop"

Write-Host "=== Hostel CMS — Jenkins Setup ===" -ForegroundColor Cyan

# ── Step 1: Download Jenkins WAR ────────────────────────────
Write-Host "`n[1/5] Downloading Jenkins WAR..." -ForegroundColor Yellow
$downloadUrl = "https://get.jenkins.io/war-stable/latest/jenkins.war"
$installDirectory = "E:\Devops_23102A0008\jenkins"
New-Item -ItemType Directory -Path $installDirectory -Force | Out-Null
$warFile = Join-Path $installDirectory "jenkins.war"

if (-Not (Test-Path $warFile)) {
    Invoke-WebRequest -Uri $downloadUrl -OutFile $warFile -UseBasicParsing
    Write-Host "   Jenkins WAR downloaded to $warFile" -ForegroundColor Green
} else {
    Write-Host "   Jenkins WAR already exists. Skipping download." -ForegroundColor Gray
}

$checksumResponse = Invoke-WebRequest -Uri "$downloadUrl.sha256" -UseBasicParsing
$checksumText = [System.Text.Encoding]::UTF8.GetString($checksumResponse.Content)
$expectedHash = $checksumText.Trim().Split()[0].ToLowerInvariant()
$actualHash = (Get-FileHash -Path $warFile -Algorithm SHA256).Hash.ToLowerInvariant()
if ($actualHash -ne $expectedHash) {
    throw "Jenkins WAR checksum verification failed."
}

# ── Step 2: Create Jenkins Home ─────────────────────────────
Write-Host "`n[2/5] Creating Jenkins home directory..." -ForegroundColor Yellow
$homeDirectory = Join-Path $installDirectory "jenkins_home"
New-Item -ItemType Directory -Path $homeDirectory -Force | Out-Null
Write-Host "   JENKINS_HOME set to: $homeDirectory" -ForegroundColor Green

# ── Step 3: Start Jenkins ────────────────────────────────────
Write-Host "`n[3/5] Starting Jenkins on port 9090..." -ForegroundColor Yellow
Write-Host "   Open http://localhost:9090/jenkins/ to complete setup and create your account." -ForegroundColor Cyan
Write-Host "   Keep this window open while using Jenkins; press Ctrl+C to stop it." -ForegroundColor Yellow
Write-Host "`n[Starting Jenkins...]" -ForegroundColor Yellow

if (Get-NetTCPConnection -State Listen -LocalPort 9090 -ErrorAction SilentlyContinue) {
    throw "Port 9090 is already in use. Stop the process using it or change the Jenkins port."
}

$java = (Get-Command java -ErrorAction Stop).Source
& $java `
    "-DJENKINS_HOME=$homeDirectory" `
    -jar "$warFile" `
    --httpPort=9090 `
    --prefix=/jenkins

Write-Host "`n=== Jenkins stopped ===" -ForegroundColor Red
