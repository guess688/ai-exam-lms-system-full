$ErrorActionPreference = 'Stop'

$ProjectRoot = (Get-Location).Path
$RequiredDirs = @('backend', 'frontend', 'deploy')

foreach ($Dir in $RequiredDirs) {
    if (-not (Test-Path -LiteralPath (Join-Path $ProjectRoot $Dir) -PathType Container)) {
        Write-Error "Please run this script from the project root. Missing directory: $Dir"
    }
}

$ImagesDir = Join-Path $ProjectRoot 'deploy/images'
$TarPath = Join-Path $ImagesDir 'ai-exam-lms-images-1.0.0.tar'
$Images = @(
    'ai-exam-lms-backend:1.0.0',
    'ai-exam-lms-frontend:1.0.0',
    'mysql:8.0',
    'redis:7-alpine',
    'minio/minio:RELEASE.2024-07-16T23-46-41Z',
    'minio/mc:RELEASE.2025-03-12T17-29-24Z'
)

New-Item -ItemType Directory -Force -Path $ImagesDir | Out-Null

foreach ($Image in $Images) {
    docker image inspect $Image *> $null
    if ($LASTEXITCODE -eq 0) {
        continue
    }

    if ($Image -like 'ai-exam-lms-*') {
        Write-Error "Image not found: $Image. Run deploy/build-images.ps1 first."
    }

    Write-Host "Image not found locally, pulling: $Image"
    docker pull $Image
    if ($LASTEXITCODE -ne 0) {
        exit $LASTEXITCODE
    }
}

Write-Host "Saving images to $TarPath"
docker save -o $TarPath @Images
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

$Tar = Get-Item -LiteralPath $TarPath
$SizeMb = [Math]::Round($Tar.Length / 1MB, 2)

Write-Host "Image archive: $($Tar.FullName)"
Write-Host "Archive size: $SizeMb MB"

