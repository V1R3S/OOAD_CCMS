param([switch]$Test, [switch]$UiTest)
$ErrorActionPreference = 'Stop'
$jdkHome = $env:JAVA_HOME
if (!$jdkHome -or !(Test-Path (Join-Path $jdkHome 'bin\javac.exe'))) {
    $localJdk = Get-ChildItem -LiteralPath "$PSScriptRoot\.tools\java" -Directory -ErrorAction SilentlyContinue | Select-Object -First 1
    $jdkHome = if ($localJdk) { $localJdk.FullName } else { $null }
}
$javac = if ($jdkHome) { Join-Path $jdkHome 'bin\javac.exe' } else { 'javac' }
$java = if ($jdkHome) { Join-Path $jdkHome 'bin\java.exe' } else { 'java' }
$jar = if ($jdkHome) { Join-Path $jdkHome 'bin\jar.exe' } else { 'jar' }
Push-Location $PSScriptRoot
try {
    # Only remove compiled output in these two fixed workspace directories.
    foreach ($relative in @('build\app-classes', 'build\check-classes')) {
        $classes = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot $relative))
        $buildRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot 'build')) + [IO.Path]::DirectorySeparatorChar
        if (!$classes.StartsWith($buildRoot, [StringComparison]::OrdinalIgnoreCase)) { throw 'Invalid build directory.' }
        if (Test-Path -LiteralPath $classes) { Remove-Item -LiteralPath $classes -Recurse -Force }
        New-Item -ItemType Directory -Path $classes -Force | Out-Null
    }
    $sources = @(Get-ChildItem src\main\java -Filter *.java -Recurse | ForEach-Object FullName)
    & $javac -encoding UTF-8 --release 17 -d build\app-classes @sources
    if ($LASTEXITCODE -ne 0) { throw 'Java compilation failed.' }
    & $jar "-J-Djava.io.tmpdir=$PSScriptRoot\build" --create --file build\campus.jar --main-class edu.aau.ccms.ui.CampusApp -C build\app-classes . -C $PSScriptRoot aau.png
    if ($LASTEXITCODE -ne 0) { throw 'JAR packaging failed.' }
    if ($Test -or $UiTest) {
        $tests = @(Get-ChildItem src\test\java -Filter *.java -Recurse | Where-Object Name -ne 'AcceptanceMavenTest.java' | ForEach-Object FullName)
        & $javac -encoding UTF-8 --release 17 -cp build\app-classes -d build\check-classes @tests
        if ($LASTEXITCODE -ne 0) { throw 'Test compilation failed.' }
        if ($Test) {
            & $java '-Djava.awt.headless=true' -cp 'build\app-classes;build\check-classes' edu.aau.ccms.CampusServiceTest
            if ($LASTEXITCODE -ne 0) { throw 'Service tests failed.' }
        }
        if ($UiTest) {
            & $java -cp 'build\campus.jar;build\check-classes' edu.aau.ccms.CampusUiSmokeTest
            if ($LASTEXITCODE -ne 0) { throw 'Swing UI tests failed.' }
        }
    }
    Write-Host 'Built build\campus.jar'
} finally { Pop-Location }
