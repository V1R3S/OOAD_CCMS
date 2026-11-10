param([string]$DataFile = '')
if ([string]::IsNullOrWhiteSpace($DataFile)) {
    $DataFile = if (Test-Path -LiteralPath (Join-Path $PSScriptRoot 'data/campus.bin')) { 'data/campus.bin' } else { 'data/arena.bin' }
}
$ErrorActionPreference = 'Stop'
& "$PSScriptRoot\build-app.ps1"
$jdkHome = $env:JAVA_HOME
if (!$jdkHome -or !(Test-Path (Join-Path $jdkHome 'bin\javac.exe'))) {
    $localJdk = Get-ChildItem -LiteralPath "$PSScriptRoot\.tools\java" -Directory -ErrorAction SilentlyContinue | Select-Object -First 1
    $jdkHome = if ($localJdk) { $localJdk.FullName } else { $null }
}
$java = if ($jdkHome) { Join-Path $jdkHome 'bin\java.exe' } else { 'java' }
Push-Location $PSScriptRoot
try { & $java -jar build\arena.jar --data $DataFile; if ($LASTEXITCODE -ne 0) { throw "Arena exited with code $LASTEXITCODE" } } finally { Pop-Location }
