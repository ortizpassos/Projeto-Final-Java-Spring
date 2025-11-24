 ========================================
# Verificação Completa - ESP32 + Spring Boot
# ========================================

Write-Host ""
Write-Host "╔════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║  Diagnóstico Completo - Monitor Ellas  ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""

$allOk = $true

# ===== 1. Verificar IPs =====
Write-Host "1️⃣  Verificando Rede..." -ForegroundColor Yellow
Write-Host ""

$espIP = "192.168.100.20"
$espNetwork = "192.168.100"

$serverIP = Get-NetIPAddress -AddressFamily IPv4 | Where-Object {
    $_.IPAddress -like "$espNetwork.*"
} | Select-Object -First 1

if ($serverIP) {
    Write-Host "   ✅ Servidor IP: $($serverIP.IPAddress)" -ForegroundColor Green
    Write-Host "   ✅ ESP32 IP: $espIP" -ForegroundColor Green
    Write-Host "   ✅ Mesma rede: Sim" -ForegroundColor Green
} else {
    Write-Host "   ❌ Servidor não está na rede do ESP32 ($espNetwork.X)" -ForegroundColor Red
    $allOk = $false
    Write-Host ""
    Write-Host "   IPs disponíveis:" -ForegroundColor Yellow
    Get-NetIPAddress -AddressFamily IPv4 | Where-Object {
        $_.IPAddress -notlike "127.*"
    } | ForEach-Object {
        Write-Host "      - $($_.IPAddress)" -ForegroundColor White
    }
}

Write-Host ""

# ===== 2. Verificar Porta 9092 =====
Write-Host "2️⃣  Verificando Socket.IO Server (porta 9092)..." -ForegroundColor Yellow
Write-Host ""

$portOpen = Get-NetTCPConnection -LocalPort 9092 -State Listen -ErrorAction SilentlyContinue

if ($portOpen) {
    Write-Host "   ✅ Porta 9092 está aberta (Socket.IO rodando)" -ForegroundColor Green
} else {
    Write-Host "   ❌ Porta 9092 não está aberta" -ForegroundColor Red
    Write-Host "   💡 Execute: mvn spring-boot:run" -ForegroundColor Yellow
    $allOk = $false
}

Write-Host ""

# ===== 3. Verificar Firewall =====
Write-Host "3️⃣  Verificando Firewall..." -ForegroundColor Yellow
Write-Host ""

$firewallRule = Get-NetFirewallRule -DisplayName "Socket.IO Server" -ErrorAction SilentlyContinue

if ($firewallRule) {
    Write-Host "   ✅ Regra de firewall existe" -ForegroundColor Green
} else {
    Write-Host "   ⚠️  Regra de firewall não encontrada" -ForegroundColor Yellow
    Write-Host "   💡 Criar regra? (Execute como Administrador):" -ForegroundColor Cyan
    Write-Host "      New-NetFirewallRule -DisplayName 'Socket.IO Server' -Direction Inbound -LocalPort 9092 -Protocol TCP -Action Allow" -ForegroundColor White
}

Write-Host ""

# ===== 4. Verificar MongoDB =====
Write-Host "4️⃣  Verificando MongoDB (porta 27017)..." -ForegroundColor Yellow
Write-Host ""

$mongoPort = Get-NetTCPConnection -LocalPort 27017 -State Listen -ErrorAction SilentlyContinue

if ($mongoPort) {
    Write-Host "   ✅ MongoDB está rodando" -ForegroundColor Green
} else {
    Write-Host "   ⚠️  MongoDB não detectado na porta 27017" -ForegroundColor Yellow
    Write-Host "   💡 Inicie o MongoDB se necessário" -ForegroundColor Cyan
}

Write-Host ""
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Gray
Write-Host ""

# ===== RESUMO =====
if ($allOk -and $portOpen) {
    Write-Host "✅ TUDO PRONTO! Sistema pode ser testado." -ForegroundColor Green -BackgroundColor DarkGreen
    Write-Host ""
    Write-Host "📝 Configure o ESP32 com:" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "   const char* socketIoHost = `"$($serverIP.IPAddress)`";" -ForegroundColor White -BackgroundColor DarkBlue
    Write-Host "   const int socketIoPort = 9092;" -ForegroundColor White -BackgroundColor DarkBlue
    Write-Host ""
} else {
    Write-Host "⚠️  ATENÇÃO: Alguns problemas precisam ser corrigidos." -ForegroundColor Yellow -BackgroundColor DarkYellow
    Write-Host ""
    Write-Host "Próximos passos:" -ForegroundColor Cyan
    if (-not $portOpen) {
        Write-Host "   1. Iniciar backend: mvn spring-boot:run" -ForegroundColor White
    }
    if (-not $serverIP) {
        Write-Host "   2. Conectar servidor à mesma rede do ESP32" -ForegroundColor White
    }
    Write-Host ""
}

Write-Host "📚 Documentação:" -ForegroundColor Cyan
Write-Host "   - ESP32_FINAL.ino - Código completo" -ForegroundColor White
Write-Host "   - PASSO_FINAL.md - Guia passo a passo" -ForegroundColor White
Write-Host "   - GUIA_CONEXAO_ESP32.md - Documentação completa" -ForegroundColor White
Write-Host ""

