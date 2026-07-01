$ErrorActionPreference = 'Stop'

$ProjectRoot = (Get-Location).Path
$RequiredDirs = @('backend', 'frontend', 'deploy')

foreach ($Dir in $RequiredDirs) {
    if (-not (Test-Path -LiteralPath (Join-Path $ProjectRoot $Dir) -PathType Container)) {
        Write-Error "Please run this script from the project root. Missing directory: $Dir"
    }
}

Write-Host "Building backend image: ai-exam-lms-backend:1.0.0"
docker build -t ai-exam-lms-backend:1.0.0 -f backend/Dockerfile backend
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

Write-Host "Building frontend image: ai-exam-lms-frontend:1.0.0"
docker build -t ai-exam-lms-frontend:1.0.0 -f frontend/Dockerfile frontend
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

Write-Host "Built images:"
docker images | findstr "ai-exam-lms"
