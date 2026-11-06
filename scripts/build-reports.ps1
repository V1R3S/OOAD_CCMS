param(
    [switch]$CachedOnly,
    [ValidateSet('system-analysis','system-design','implementation-report','testing-report','interface-mockups')]
    [string[]]$Report
)
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path $PSScriptRoot -Parent
$compiler = Join-Path $projectRoot '.tools\tectonic\tectonic.exe'
if (!(Test-Path -LiteralPath $compiler)) { $compiler = 'tectonic' }
$cache = Join-Path $projectRoot '.tools\tex-cache'
if (Test-Path -LiteralPath $cache) { $env:TECTONIC_CACHE_DIR = $cache }
Push-Location $projectRoot
try {
    New-Item -ItemType Directory -Force output\pdf | Out-Null
    $reports = [ordered]@{
        'system-analysis' = '01-system-analysis.pdf'
        'system-design' = '02-system-design.pdf'
        'implementation-report' = '03-implementation-report.pdf'
        'testing-report' = '04-testing-report.pdf'
        'interface-mockups' = '05-interface-mockups.pdf'
    }
    foreach ($name in $reports.Keys) {
        if ($Report -and $name -notin $Report) { continue }
        $buildDir = Join-Path $projectRoot "build\latex\$name"
        New-Item -ItemType Directory -Force $buildDir | Out-Null
        $flags = @('--keep-logs','--outdir',$buildDir)
        if ($CachedOnly) { $flags += '--only-cached' }
        & $compiler @flags "docs\$name\main.tex"
        if ($LASTEXITCODE -ne 0) { throw "LaTeX compilation failed: $name" }
        Copy-Item -LiteralPath "$buildDir\main.pdf" -Destination (Join-Path 'output\pdf' $reports[$name]) -Force
    }
} finally { Pop-Location }
