param(
    [switch]$CachedOnly,
    [ValidateSet('analysis','design','final-report')]
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
    New-Item -ItemType Directory -Force reports | Out-Null
    $reports = [ordered]@{
        'analysis' = '01-analysis.pdf'
        'design' = '02-design.pdf'
        'final-report' = '03-final-report.pdf'
    }
    foreach ($name in $reports.Keys) {
        if ($Report -and $name -notin $Report) { continue }
        $buildDir = Join-Path $projectRoot "build\latex\$name"
        New-Item -ItemType Directory -Force $buildDir | Out-Null
        $flags = @('--keep-logs','--outdir',$buildDir)
        if ($CachedOnly) { $flags += '--only-cached' }
        & $compiler @flags "reports\source\$name.tex"
        if ($LASTEXITCODE -ne 0) { throw "LaTeX compilation failed: $name" }
        Copy-Item -LiteralPath "$buildDir\$name.pdf" -Destination (Join-Path 'reports' $reports[$name]) -Force
    }
} finally { Pop-Location }
