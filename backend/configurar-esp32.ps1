# Script PowerShell para Configurar Conexão ESP32
# Execute este script para obter informações necessárias

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Configuração ESP32 - Monitor Ellas   " -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 1. Obter IP do computador
Write-Host "1. Endereços IP deste computador:" -ForegroundColor Yellow
Get-NetIPAddress -AddressFamily IPv4 | Where-Object {$_.IPAddress -notlike "127.*"} | ForEach-Object {
    Write-Host "   - $($_.IPAddress) ($($_.InterfaceAlias))" -ForegroundColor Green
}
Write-Host ""

# 2. IP recomendado
$recommendedIP = (Get-NetIPAddress -AddressFamily IPv4 | Where-Object {
    $_.IPAddress -notlike "127.*" -and
    $_.IPAddress -notlike "169.254.*" -and
    $_.InterfaceAlias -notlike "*Loopback*"
} | Select-Object -First 1).IPAddress

if ($recommendedIP) {
    Write-Host "2. IP recomendado para o ESP32:" -ForegroundColor Yellow
    Write-Host "   $recommendedIP" -ForegroundColor Green
    Write-Host ""
    Write-Host "   Cole no código ESP32:" -ForegroundColor Yellow
    Write-Host "   const char* socketIoHost = `"$recommendedIP`";" -ForegroundColor White
    Write-Host ""
}

# 3. Verificar se a porta 9092 está aberta
Write-Host "3. Verificando firewall (porta 9092):" -ForegroundColor Yellow
$firewallRule = Get-NetFirewallRule -DisplayName "Socket.IO Server" -ErrorAction SilentlyContinue

if ($firewallRule) {
    Write-Host "   ✓ Regra de firewall já existe" -ForegroundColor Green
} else {
    Write-Host "   ✗ Regra de firewall não encontrada" -ForegroundColor Red
    Write-Host ""
    $createRule = Read-Host "   Deseja criar a regra de firewall? (S/N)"

    if ($createRule -eq "S" -or $createRule -eq "s") {
        try {
            New-NetFirewallRule -DisplayName "Socket.IO Server" `
                               -Direction Inbound `
                               -LocalPort 9092 `
                               -Protocol TCP `
                               -Action Allow `
                               -ErrorAction Stop
            Write-Host "   ✓ Regra de firewall criada com sucesso!" -ForegroundColor Green
        } catch {
            Write-Host "   ✗ Erro ao criar regra (execute como Administrador)" -ForegroundColor Red
        }
    }
}
Write-Host ""

# 4. Resumo
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  RESUMO DA CONFIGURAÇÃO" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Backend Spring Boot:" -ForegroundColor Yellow
Write-Host "  - API REST: http://localhost:3001" -ForegroundColor White
Write-Host "  - Socket.IO: $recommendedIP:9092" -ForegroundColor White
Write-Host ""
Write-Host "Configuração ESP32:" -ForegroundColor Yellow
Write-Host "  - Host: $recommendedIP" -ForegroundColor White
Write-Host "  - Porta: 9092" -ForegroundColor White
Write-Host "  - SSL: Não (usar socketIO.begin, não beginSSL)" -ForegroundColor White
Write-Host ""
Write-Host "Próximos passos:" -ForegroundColor Yellow
Write-Host "  1. Compile o backend: mvn clean install" -ForegroundColor White
Write-Host "  2. Execute: mvn spring-boot:run" -ForegroundColor White
Write-Host "  3. Atualize o IP no código ESP32" -ForegroundColor White
Write-Host "  4. Carregue o código no ESP32" -ForegroundColor White
Write-Host ""
Write-Host "Arquivo com código corrigido:" -ForegroundColor Yellow
Write-Host "  ESP32_CODIGO_CORRIGIDO.ino" -ForegroundColor White
Write-Host ""

