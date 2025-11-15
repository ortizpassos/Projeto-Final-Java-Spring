# Guia de Conexão ESP32 com Backend Spring Boot

## ✅ Configurações Realizadas no Backend

1. **Dependência Socket.IO** adicionada ao `pom.xml`
2. **SocketIOConfig** criado para configurar servidor Socket.IO na porta **9092**
3. **SocketIOService** criado para processar eventos do ESP32:
   - `registerDevice` - Registro do dispositivo
   - `loginFuncionario` - Login do funcionário
   - `selecionarOperacao` - Seleção da operação
   - `producao` - Envio de dados de produção

## 🔧 Alterações Necessárias no Código do ESP32

### 1. Alterar Configuração de Conexão

**ANTES:**
```cpp
const char* socketIoHost = "172.26.128.1";
const int socketIoPort = 443;

// No setup():
socketIO.beginSSL(socketIoHost, socketIoPort);
```

**DEPOIS:**
```cpp
const char* socketIoHost = "172.26.128.1";  // IP do servidor Spring
const int socketIoPort = 9092;               // Porta do Socket.IO

// No setup():
socketIO.begin(socketIoHost, socketIoPort);  // SEM SSL
```

### 2. IP do Servidor

- Verifique o IP do computador onde o Spring Boot está rodando
- No Windows PowerShell: `ipconfig`
- Procure pelo IPv4 da interface de rede que o ESP32 usa (mesma rede WiFi)
- Atualize a variável `socketIoHost` com o IP correto

Exemplo:
```cpp
const char* socketIoHost = "192.168.1.100";  // Seu IP local
const int socketIoPort = 9092;
```

## 📝 Resumo das Mudanças no ESP32

```cpp
// Linha ~17-18 - Configuração do servidor
const char* socketIoHost = "SEU_IP_AQUI";  // Ex: 192.168.1.100
const int socketIoPort = 9092;              // Porta do Socket.IO Server

// Linha ~68 - Setup (remover SSL)
Serial.println("Conectando ao servidor Socket.IO...");
socketIO.begin(socketIoHost, socketIoPort);  // Mudar de beginSSL para begin
socketIO.onEvent(socketIOEvent);
```

## 🚀 Passos para Testar

1. **Compilar o Backend:**
   ```bash
   cd C:\Devs2Blu\Aulas_Ranyer\Projeto-Final-Java-Spring\monitor-ellas-spring
   mvn clean install -DskipTests
   ```

2. **Iniciar o Backend:**
   ```bash
   mvn spring-boot:run
   ```
   
   Ou executar a classe `MonitorEllasApplication` na IDE

3. **Verificar logs:**
   - Porta 3001: API REST principal
   - Porta 9092: Socket.IO Server (para ESP32)

4. **Atualizar código ESP32** com as mudanças acima

5. **Carregar código no ESP32** via Arduino IDE

6. **Monitorar Serial do ESP32:**
   - Conexão WiFi
   - Conexão Socket.IO
   - Registro do dispositivo
   - Login do funcionário
   - Produção

## ⚠️ Troubleshooting

### ESP32 não conecta:
- ✅ Verificar se o IP está correto
- ✅ Certificar que ESP32 e servidor estão na mesma rede
- ✅ Verificar firewall do Windows (porta 9092 deve estar aberta)
- ✅ Confirmar que o backend está rodando

### Dispositivo não registrado:
- Verificar se o `deviceToken` existe no banco de dados MongoDB
- Criar dispositivo via API REST se necessário

### Funcionário não loga:
- Verificar se o `codigo` do funcionário existe no banco
- Criar funcionário via API REST se necessário

## 🔐 Criar Firewall Rule (Windows)

Se o ESP32 não conseguir conectar, abra a porta no firewall:

```powershell
New-NetFirewallRule -DisplayName "Socket.IO Server" -Direction Inbound -LocalPort 9092 -Protocol TCP -Action Allow
```

## 📊 Endpoints do Backend

### REST API (Porta 3001):
- POST /api/auth/login
- GET /api/dispositivos
- POST /api/dispositivos
- GET /api/funcionarios
- POST /api/funcionarios
- GET /api/operacoes
- POST /api/operacoes

### Socket.IO (Porta 9092):
- Eventos ESP32 → Backend:
  - registerDevice
  - loginFuncionario
  - selecionarOperacao
  - producao

- Eventos Backend → ESP32:
  - deviceRegistered
  - loginSuccess / loginFailed
  - operacaoSelecionada
  - producaoSuccess / producaoFailed
  - deviceStatusUpdate

