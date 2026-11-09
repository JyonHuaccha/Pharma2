param([int]$Puerto = 8080)

$ErrorActionPreference = 'Stop'
foreach ($nombre in @('DB_URL', 'DB_USERNAME', 'DB_PASSWORD', 'JWT_SECRET')) {
    if ([string]::IsNullOrWhiteSpace([Environment]::GetEnvironmentVariable($nombre))) {
        throw "Define la variable $nombre antes de iniciar producción."
    }
}
try { $clave = [Convert]::FromBase64String($env:JWT_SECRET) }
catch { throw 'JWT_SECRET debe contener una clave Base64 válida.' }
if ($clave.Length -lt 32) { throw 'La clave JWT debe tener al menos 256 bits.' }

$env:SPRING_PROFILES_ACTIVE = 'prod'
$env:SERVER_PORT = [string]$Puerto
if (-not $env:FRONTEND_ORIGINS) { $env:FRONTEND_ORIGINS = 'http://localhost:8081' }
if (-not $env:REDIS_HOST) { $env:REDIS_HOST = 'localhost' }

Push-Location (Split-Path $PSScriptRoot -Parent)
try {
    $jar = Join-Path (Get-Location) 'target\PharmaBackend-0.0.1-SNAPSHOT.jar'
    if (-not (Test-Path -LiteralPath $jar)) { throw 'Primero compila el proyecto con .\mvnw.cmd clean package.' }
    & java -jar $jar
    if ($LASTEXITCODE -ne 0) { throw 'El backend no pudo iniciar.' }
} finally {
    Pop-Location
}
