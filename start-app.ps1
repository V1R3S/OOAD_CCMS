param([string]$DataFile = 'data/campus.bin')
$ErrorActionPreference = 'Stop'
& "$PSScriptRoot\build-app.ps1"
$jdkHome = $env:JAVA_HOME
if (!$jdkHome -or !(Test-Path (Join-Path $jdkHome 'bin\javac.exe'))) {
    $localJdk = Get-ChildItem -LiteralPath "$PSScriptRoot\.tools\java" -Directory -ErrorAction SilentlyContinue | Select-Object -First 1
    $jdkHome = if ($localJdk) { $localJdk.FullName } else { $null }
}
$java = if ($jdkHome) { Join-Path $jdkHome 'bin\java.exe' } else { 'java' }
Push-Location $PSScriptRoot
try { & $java -jar build\campus.jar --data $DataFile; if ($LASTEXITCODE -ne 0) { throw "Campus exited with code $LASTEXITCODE" } } finally { Pop-Location }
