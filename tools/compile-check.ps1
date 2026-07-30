# UndefinedWorld javac 컴파일 검증 스크립트 (이 환경에는 mvn 바이너리가 없다)
#
# 사용법:
#   powershell -NoProfile -ExecutionPolicy Bypass -File tools/compile-check.ps1 <target> [tag]
#     target : core | item | mob | enchant | all
#     tag    : 병렬 실행 시 출력 디렉토리 격리용 태그 (기본 default)
#
# 주의:
#   - classpath argfile은 따옴표 내 백슬래시를 이스케이프로 해석하므로 '/'로 변환한다
#   - 출력 경로에 비ASCII 문자가 있으면 argfile 인코딩이 깨지므로 프로젝트 내부 target을 사용한다
param(
    [string]$Target = "all",
    [string]$Tag = "default"
)

$ErrorActionPreference = "Stop"
$javac = "D:\jdk\25\bin\javac.exe"
$proj = "D:\Project\UndefinedWorld"
$repo = "D:\Maven\repository"
$out = Join-Path $proj "target\javac-check\$Tag"

New-Item -ItemType Directory -Force $out | Out-Null

# 로컬 저장소 전체 jar를 classpath로 사용 (타입 체크 목적이라 과잉 포함은 무해)
#
# 단 **이 프로젝트 자신의 설치본(org/red/minecraft/uw)은 반드시 제외한다.**
# 포함하면 javac 가 소스에서 못 찾은 심볼을 낡은 jar 에서 찾아 해결해버려
# 클래스 삭제·이름 변경·시그니처 변경·패키지 이동이 전부 에러 없이 통과한다(= 검증이 무의미해진다).
# dellarte(org/red/minecraft/dellarte)는 진짜 외부 의존성이므로 남긴다.
$jars = Get-ChildItem $repo -Recurse -Filter "*.jar" |
        Where-Object {
            $_.Name -notmatch "sources|javadoc" -and
            $_.FullName -notmatch "repository\\org\\red\\minecraft\\uw\\"
        } |
        Select-Object -ExpandProperty FullName
$baseCp = ($jars -join ";")

$coreOut = Join-Path $out "core"

function Compile-Module([string]$name, [string[]]$extraCp) {
    $src = Join-Path $proj "$name\src\main\java"
    if (-not (Test-Path $src)) { Write-Host "SKIP: $name (no source)"; return }

    $dest = Join-Path $out $name
    New-Item -ItemType Directory -Force $dest | Out-Null

    $files = Get-ChildItem $src -Recurse -Filter "*.java" | Select-Object -ExpandProperty FullName
    if ($files.Count -eq 0) { Write-Host "SKIP: $name (no files)"; return }

    $listFile = Join-Path $out "$name-sources.txt"
    $files | Out-File -FilePath $listFile -Encoding ascii

    $cp = $baseCp
    if ($extraCp) { $cp = ($extraCp -join ";") + ";" + $cp }

    $cpArgFile = Join-Path $out "$name-cp.txt"
    Set-Content -Path $cpArgFile -Value ('-cp "' + ($cp -replace '\\', '/') + '"') -Encoding ascii

    Write-Host "=== Compiling $name ($($files.Count) files) ==="
    cmd /c "`"$javac`" -encoding UTF-8 --release 25 -nowarn -d `"$dest`" `"@$cpArgFile`" `"@$listFile`" 2>&1"
    if ($LASTEXITCODE -ne 0) { Write-Host "FAILED: $name"; exit 1 }
    Write-Host "OK: $name"
}

switch ($Target) {
    "core"    { Compile-Module "core" @() }
    "item"    { Compile-Module "core" @(); Compile-Module "item" @($coreOut) }
    "mob"     { Compile-Module "core" @(); Compile-Module "mob" @($coreOut) }
    "enchant" { Compile-Module "core" @(); Compile-Module "enchant" @($coreOut) }
    "all"     {
        Compile-Module "core" @()
        Compile-Module "item" @($coreOut)
        Compile-Module "mob" @($coreOut)
        Compile-Module "enchant" @($coreOut)
    }
    default   { Write-Host "Unknown target: $Target"; exit 1 }
}

Write-Host "BUILD SUCCESS"
