# ========================================
# Descobrir IP para Configurar ESP32
# ========================================

Write-Host ""
Write-Host "╔════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║   Configuração ESP32 - Monitor Ellas   ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""

# Obter IP compatível com a rede do ESP32 (192.168.100.X)
$espNetwork = "192.168.100"
$serverIP = Get-NetIPAddress -AddressFamily IPv4 | Where-Object {
    $_.IPAddress -like "$espNetwork.*"
} | Select-Object -First 1

if ($serverIP) {
    Write-Host "✅ IP do Servidor encontrado:" -ForegroundColor Green
    Write-Host "   $($serverIP.IPAddress)" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "📝 Cole esta linha no código ESP32 (linha 27):" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "   const char* socketIoHost = `"$($serverIP.IPAddress)`";" -ForegroundColor White -BackgroundColor DarkBlue
    Write-Host ""
} else {
    Write-Host "⚠️  IP compatível não encontrado automaticamente." -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Todos os IPs disponíveis:" -ForegroundColor Cyan
    Get-NetIPAddress -AddressFamily IPv4 | Where-Object {
        $_.IPAddress -notlike "127.*" -and $_.IPAddress -notlike "169.254.*"
    } | ForEach-Object {
        Write-Host "   - $($_.IPAddress) ($($_.InterfaceAlias))" -ForegroundColor White
    }
    Write-Host ""
    Write-Host "📌 Escolha o IP da mesma rede do ESP32 (192.168.100.X)" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "ℹ️  Informações:" -ForegroundColor Cyan
Write-Host "   - ESP32 IP: 192.168.100.20" -ForegroundColor White
Write-Host "   - Porta: 9092" -ForegroundColor White
Write-Host "   - Protocolo: Socket.IO (sem SSL)" -ForegroundColor White
Write-Host ""

# Verificar se porta está aberta
Write-Host "🔍 Verificando se Socket.IO está rodando..." -ForegroundColor Cyan
$portOpen = Get-NetTCPConnection -LocalPort 9092 -State Listen -ErrorAction SilentlyContinue

if ($portOpen) {
    Write-Host "   ✅ Porta 9092 está aberta (Socket.IO rodando)" -ForegroundColor Green
} else {
    Write-Host "   ⚠️  Porta 9092 não está aberta" -ForegroundColor Yellow
    Write-Host "   Execute: mvn spring-boot:run" -ForegroundColor White
}

Write-Host ""
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Gray
Write-Host ""

