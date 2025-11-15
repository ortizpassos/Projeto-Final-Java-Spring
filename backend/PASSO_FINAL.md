# 🎯 ÚLTIMA ETAPA - Descobrir IP do Servidor

## ⚠️ Problema Atual

Seu código ESP32 está **quase correto**, mas o IP está errado:

```cpp
const char* socketIoHost = "172.26.128.1";  // ❌ ERRADO
```

O ESP32 está em `192.168.100.20`, então o servidor **precisa estar na faixa 192.168.100.X** para se comunicarem.

---

## ✅ Solução em 3 Passos

### 1️⃣ Descobrir IP do Servidor Spring Boot

**No computador rodando o Spring Boot**, abra PowerShell e execute:

```powershell
ipconfig
```

**Procure por algo assim:**

```
Adaptador de Rede sem Fio Wi-Fi:
   
   Endereço IPv4. . . . . . . .  : 192.168.100.5
```

**OU use o script helper:**

```powershell
cd C:\Devs2Blu\Aulas_Ranyer\Projeto-Final-Java-Spring\monitor-ellas-spring
.\configurar-esp32.ps1
```

### 2️⃣ Atualizar IP no Código ESP32

Abra o arquivo **ESP32_FINAL.ino** e mude a linha 27:

```cpp
// ❌ ANTES:
const char* socketIoHost = "192.168.100.1";

// ✅ DEPOIS (use o IP que descobriu no passo 1):
const char* socketIoHost = "192.168.100.5";  // EXEMPLO - use seu IP real!
```

### 3️⃣ Carregar e Testar

1. Carregue o código no ESP32
2. Abra Serial Monitor (115200 baud)
3. Aguarde a conexão

---

## 📊 Resultado Esperado

```
Wi-Fi conectado! IP: 192.168.100.20
Conectando ao servidor Socket.IO em 192.168.100.5:9092...
[Socket.IO] ✅ Conectado ao servidor!
[LOG] Enviando evento registerDevice para backend (deviceToken: 461545616614166)
[deviceRegistered] Dispositivo registrado com sucesso
[LOG] Backend confirmou registro do dispositivo: OK
Digite a senha do funcionário:
```

---

## 🔍 Checklist Rápido

- [ ] Backend Spring Boot está rodando?
- [ ] Viu a mensagem: `Socket.IO Server iniciado na porta 9092`?
- [ ] Executou `ipconfig` e anotou o IPv4 (192.168.100.X)?
- [ ] Alterou linha 27 do ESP32_FINAL.ino com o IP correto?
- [ ] ESP32 e servidor estão na **mesma rede WiFi**?
- [ ] Carregou o código no ESP32?

---

## 🆘 Se Ainda Não Funcionar

### Teste 1: Verificar se porta está aberta

No servidor Spring Boot:

```powershell
netstat -an | findstr 9092
```

Deve aparecer:
```
TCP    0.0.0.0:9092           0.0.0.0:0              LISTENING
```

### Teste 2: Liberar Firewall

Execute como **Administrador**:

```powershell
New-NetFirewallRule -DisplayName "Socket.IO Server" -Direction Inbound -LocalPort 9092 -Protocol TCP -Action Allow
```

### Teste 3: Ping

Do computador servidor para o ESP32:

```powershell
ping 192.168.100.20
```

Deve responder com sucesso.

---

## 🎉 Pronto!

Após seguir esses 3 passos, o ESP32 deve conectar corretamente ao Spring Boot e você poderá:

1. ✅ Registrar o dispositivo
2. ✅ Fazer login do funcionário
3. ✅ Selecionar operação
4. ✅ Enviar produção ao pressionar o botão

**Arquivo final:** `ESP32_FINAL.ino` (só falta ajustar o IP na linha 27!)

