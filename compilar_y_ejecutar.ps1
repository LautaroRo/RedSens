# Compila RedSens y abre el menú. Uso:
#   .\compilar_y_ejecutar.ps1                 -> menú interactivo
#   .\compilar_y_ejecutar.ps1 --experimentos  -> corre la batería de experimentos y la guarda en resultados/
# Necesita un JDK 17 o superior. Si javac no está en el PATH, usa el que trae IntelliJ.
$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

$javac = (Get-Command javac -ErrorAction SilentlyContinue).Source
$java = (Get-Command java -ErrorAction SilentlyContinue).Source
if (-not $javac) {
    $jbr = Get-ChildItem "C:\Program Files\JetBrains\*\jbr\bin\javac.exe" -ErrorAction SilentlyContinue | Select-Object -First 1
    if (-not $jbr) { throw "No se encontró un JDK. Instalen JDK 17+ o abran el proyecto con IntelliJ." }
    $javac = $jbr.FullName
    $java = Join-Path $jbr.DirectoryName "java.exe"
}

if (Test-Path out) { Remove-Item -Recurse -Force out }
$fuentes = Get-ChildItem -Recurse src -Filter *.java | ForEach-Object FullName
& $javac --release 17 -encoding UTF-8 -d out $fuentes
if ($LASTEXITCODE -ne 0) { throw "Error de compilación" }

[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
& $java "-Dstdout.encoding=UTF-8" "-Dstdin.encoding=UTF-8" -cp out redsens.Main @args
