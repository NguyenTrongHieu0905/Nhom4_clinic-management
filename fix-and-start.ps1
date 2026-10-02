$ErrorActionPreference = 'Stop'
$projectDir = $PSScriptRoot
$javaPath = 'C:/Program Files/Java/jdk-21/bin/java.exe'
$runtimeJar = Join-Path $projectDir 'dist/clinic-desktop-v2.jar'
if (-not (Test-Path -LiteralPath $runtimeJar)) { throw 'Khong tim thay dist/clinic-desktop-v2.jar' }
try {
    $listeners = Get-NetTCPConnection -LocalPort 8888 -State Listen -ErrorAction SilentlyContinue
    foreach ($serverPid in @($listeners.OwningProcess | Select-Object -Unique)) {
        if (-not $serverPid) { continue }
        $owner = Get-CimInstance Win32_Process -Filter "ProcessId=$serverPid"
        if ($owner.Name -ne 'java.exe' -or $owner.CommandLine -notmatch 'vn\.edu\.clinic\.server\.ClinicServer(?:\s|$)') {
            throw "Cong 8888 do chuong trinh khac su dung (PID $serverPid); khong tu dong dung."
        }
        Write-Host "Dung server phong kham cu (PID $serverPid)..."
        Stop-Process -Id $serverPid -ErrorAction Stop
        (Get-Process -Id $serverPid -ErrorAction SilentlyContinue) | Wait-Process -Timeout 10
    }
    $logsDir = Join-Path $projectDir 'logs'
    New-Item -ItemType Directory -Force $logsDir | Out-Null
    $tag = Get-Date -Format 'yyyyMMdd-HHmmss-fff'
    $stdout = Join-Path $logsDir "server-$tag.log"
    $stderr = Join-Path $logsDir "server-$tag.err.log"
    $server = Start-Process -FilePath $javaPath -ArgumentList @('-cp', 'dist/clinic-desktop-v2.jar;lib/*', 'vn.edu.clinic.server.ClinicServer') -WorkingDirectory $projectDir -WindowStyle Hidden -PassThru -RedirectStandardOutput $stdout -RedirectStandardError $stderr
    $ready = $false
    for ($attempt = 0; $attempt -lt 45; $attempt++) {
        if ($server.HasExited) { throw "Server khong khoi dong duoc. Xem $stderr" }
        if ((Get-Content -LiteralPath $stdout -Raw -ErrorAction SilentlyContinue) -match 'dang chay tai cong 8888') { $ready = $true; break }
        Start-Sleep -Seconds 1
    }
    if (-not $ready) {
        Stop-Process -Id $server.Id
        throw 'Server khoi dong qua lau; da dung server vua tao.'
    }
    Write-Host "Server moi san sang. PID: $($server.Id)"
    # The user requested an interactive application window.
    Start-Process -FilePath $javaPath -ArgumentList @('-cp', 'dist/clinic-desktop-v2.jar;lib/*', 'vn.edu.clinic.client.ClinicDesktop') -WorkingDirectory $projectDir | Out-Null
    Write-Host 'Da mo giao dien. Dang nhap manager / 123.'
} catch {
    Write-Host $_.Exception.Message -ForegroundColor Red
    exit 1
}
