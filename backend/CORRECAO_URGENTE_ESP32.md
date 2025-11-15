# ⚠️ CORREÇÃO URGENTE - ESP32 NÃO CONECTA

## 🔴 Problema Identificado

Seu ESP32 ainda está usando `beginSSL` (conexão SSL na porta 443), mas o backend Spring Boot está rodando Socket.IO **sem SSL na porta 9092**.

```
Wi-Fi conectado! IP: 192.168.100.20
Conectando ao servidor Socket.IO (SSL)...  ← ERRO AQUI!
[Socket.IO] ❌ Desconectado do servidor!
```

## ✅ Solução - 3 Mudanças Obrigatórias

### 1️⃣ Descobrir o IP do Servidor Spring Boot

No computador rodando o Spring Boot, execute no PowerShell:

```powershell
ipconfig
```

Procure algo como:
```
Adaptador de Rede sem Fio Wi-Fi:
   IPv4 Address. . . . . . . . . . . : 192.168.100.XXX
```

**OU** execute o script helper:
```powershell
cd C:\Devs2Blu\Aulas_Ranyer\Projeto-Final-Java-Spring\monitor-ellas-spring
.\configurar-esp32.ps1
```

### 2️⃣ Alterar IP e Porta no ESP32

**Linha ~17-18** do código ESP32:

```cpp
// ANTES (ERRADO):
const char* socketIoHost = "172.26.128.1";
const int socketIoPort = 443;

// DEPOIS (CORRETO):
const char* socketIoHost = "192.168.100.XXX";  // IP do servidor Spring (ver passo 1)
const int socketIoPort = 9092;                  // Porta do Socket.IO Server
```

**⚠️ IMPORTANTE:** Use o IP do computador onde o Spring Boot está rodando (mesma rede do ESP32)

### 3️⃣ Remover SSL - Linha ~85

**ANTES (ERRADO):**
```cpp
Serial.println("Conectando ao servidor Socket.IO (SSL)...");
socketIO.beginSSL(socketIoHost, socketIoPort);  // ❌ REMOVER SSL
```

**DEPOIS (CORRETO):**
```cpp
Serial.println("Conectando ao servidor Socket.IO...");
socketIO.begin(socketIoHost, socketIoPort);  // ✅ SEM SSL
```

## 📋 Checklist Completo

- [ ] Backend Spring Boot está rodando? (`mvn spring-boot:run`)
- [ ] Ver log: `Socket.IO Server iniciado na porta 9092`
- [ ] Descobri o IP do servidor? (ex: 192.168.100.5)
- [ ] Alterei `socketIoHost` no ESP32?
- [ ] Alterei `socketIoPort` para 9092?
- [ ] Troquei `beginSSL` por `begin`?
- [ ] Carreguei o código no ESP32?
- [ ] ESP32 e servidor na mesma rede WiFi?

## 🔥 Código Corrigido Completo

Use o arquivo: **ESP32_CODIGO_CORRIGIDO.ino**

Ou faça as mudanças manualmente:

```cpp
// ========================
// CONFIGURAÇÕES DO SERVIDOR (Linha ~17)
// ========================
const char* socketIoHost = "192.168.100.5";  // ⚠️ ALTERAR PARA SEU IP!
const int socketIoPort = 9092;                // Porta do Socket.IO

// ========================
// SETUP (Linha ~85)
// ========================
void setup() {
  // ...existing code...
  
  // Conectar ao servidor Socket.IO SEM SSL
  Serial.printf("Conectando ao servidor Socket.IO em %s:%d...\n", 
                socketIoHost, socketIoPort);
  socketIO.begin(socketIoHost, socketIoPort);  // ⚠️ SEM SSL!
  socketIO.onEvent(socketIOEvent);
}
```

## 🧪 Teste de Conectividade

Antes de carregar no ESP32, teste se o servidor está acessível:

### No PowerShell do servidor:
```powershell
# Ver IP do servidor
ipconfig

# Testar se porta 9092 está aberta
netstat -an | findstr 9092
```

Deve aparecer:
```
TCP    0.0.0.0:9092           0.0.0.0:0              LISTENING
```

### Liberar Firewall (se necessário):
```powershell
# Execute como Administrador
New-NetFirewallRule -DisplayName "Socket.IO Server" -Direction Inbound -LocalPort 9092 -Protocol TCP -Action Allow
```

## 📊 Resultado Esperado

Após as correções, você deve ver no Serial Monitor:

```
Wi-Fi conectado! IP: 192.168.100.20
Conectando ao servidor Socket.IO em 192.168.100.5:9092...
[Socket.IO] ✅ Conectado ao servidor!
[LOG] Enviando evento registerDevice para backend (deviceToken: 461545616614166)
[deviceRegistered] Dispositivo registrado com sucesso
Digite a senha do funcionário:
```

## ❌ Se Ainda Não Conectar

1. **Verificar IP:** Ping do ESP32 para o servidor
   ```powershell
   ping 192.168.100.5
   ```

2. **Verificar Backend:** Logs do Spring Boot devem mostrar:
   ```
   Socket.IO Server iniciado na porta 9092
   ```

3. **Verificar Rede:** ESP32 e servidor na mesma rede WiFi/LAN?

4. **Desativar Firewall temporariamente** para testar:
   ```powershell
   # Desativar (CUIDADO - só para teste!)
   Set-NetFirewallProfile -Profile Domain,Public,Private -Enabled False
   
   # Reativar depois
   Set-NetFirewallProfile -Profile Domain,Public,Private -Enabled True
   ```

## 📞 Suporte

Se continuar com erro, forneça:
- [ ] IP do ESP32 (ex: 192.168.100.20)
- [ ] IP do servidor Spring Boot
- [ ] Logs do Spring Boot ao iniciar
- [ ] Serial Monitor completo do ESP32
- [ ] Resultado de `ping` entre os dispositivos

