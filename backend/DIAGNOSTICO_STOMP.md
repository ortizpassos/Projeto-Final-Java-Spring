# 🔍 DIAGNÓSTICO - ESP32 STOMP não recebe CONNECTED

## Status Atual

✅ **Correções já aplicadas:**
- Porta corrigida: 3000 (ESP32 e backend)
- Endpoint nativo: /ws-native
- Security liberando /ws-native/**
- Frame CONNECT com accept-version:1.1,1.2 e heart-beat:0,0
- Logs DEBUG ativados no backend

❌ **Problema:**
- ESP32 conecta via WebSocket
- ESP32 envia frame CONNECT
- Servidor NÃO responde com CONNECTED
- Loop infinito de timeout/reenvio

## Próximos Passos de Diagnóstico

### 1️⃣ Verificar Logs do Backend (URGENTE)

**Ao reiniciar o backend e conectar o ESP32, procure por:**

```
STOMP CONNECT received. sessionId=...
```

**Se NÃO aparecer:**
- O frame CONNECT não está chegando ao Spring
- Possíveis causas: firewall, endpoint errado, Security bloqueando

**Se aparecer mas não vier "STOMP CONNECTED sent":**
- O Spring recebeu mas não enviou resposta
- Possível incompatibilidade no frame CONNECT

### 2️⃣ Testar Endpoint Manualmente

Use uma ferramenta WebSocket para testar:

**PowerShell (wscat - se instalado):**
```powershell
wscat -c ws://192.168.100.4:3000/ws-native -s v12.stomp
```

Depois envie:
```
CONNECT
accept-version:1.1,1.2
heart-beat:0,0

^@
```

**Se CONNECTED chegar:**
- Endpoint funciona
- Problema está no frame do ESP32

**Se não chegar:**
- Endpoint ou Security com problema

### 3️⃣ Variações do Frame CONNECT

Se os logs mostrarem que o CONNECT chega mas não vem CONNECTED, teste estas variações no ESP32:

**Variação A - Com login (alguns brokers exigem):**
```cpp
void stompConnect() {
  String frame;
  frame += "CONNECT\n";
  frame += "accept-version:1.2\n";
  frame += "login:guest\n";
  frame += "passcode:guest\n";
  frame += "heart-beat:0,0\n";
  frame += "\n\0";
  ws.sendTXT(frame);
  lastConnectSentAt = millis();
  Serial.println("[STOMP] CONNECT enviado (com login)");
}
```

**Variação B - STOMP 1.0 simples:**
```cpp
void stompConnect() {
  String frame = "CONNECT\n\n\0";
  ws.sendTXT(frame);
  lastConnectSentAt = millis();
  Serial.println("[STOMP] CONNECT enviado (1.0 minimal)");
}
```

**Variação C - Com host novamente:**
```cpp
void stompConnect() {
  String frame;
  frame += "CONNECT\n";
  frame += "accept-version:1.1,1.2\n";
  frame += "host:192.168.100.4\n";
  frame += "heart-beat:0,0\n";
  frame += "\n\0";
  ws.sendTXT(frame);
  lastConnectSentAt = millis();
  Serial.println("[STOMP] CONNECT enviado (com host)");
}
```

### 4️⃣ Verificar Configuração do Backend

**Confirme que existe APENAS UMA classe WebSocketConfig:**

```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();

        registry.addEndpoint("/ws-native")
                .setAllowedOriginPatterns("*");
    }
}
```

**Não deve haver:**
- Outra classe chamada WebSocketConfig em outro arquivo
- SocketIOConfig com código dentro (deve estar vazio)

### 5️⃣ Firewall Windows

**Garantir que porta 3000 está aberta:**

```powershell
# Verificar se porta está em uso
netstat -an | findstr :3000

# Criar regra de firewall (como Administrador)
New-NetFirewallRule -DisplayName "Spring Boot STOMP" -Direction Inbound -LocalPort 3000 -Protocol TCP -Action Allow
```

## Ação Imediata para Você

**PASSO 1:** Reinicie o backend Spring Boot

**PASSO 2:** Observe o console ao iniciar. Deve aparecer:
```
Tomcat started on port(s): 3000 (http)
```

**PASSO 3:** Com o backend rodando, carregue o ESP32 e observe AMBOS os logs:

**Backend deve mostrar:**
```
STOMP CONNECT received. sessionId=xxx
```

**ESP32 deve mostrar:**
```
[WS] ✅ Connected to ws://192.168.100.4:3000/ws-native
[STOMP] CONNECT enviado
[STOMP] ✅ CONNECTED  <-- Aqui é o que está faltando!
```

**PASSO 4:** Me envie:
- [ ] 5-10 linhas dos logs do backend durante a conexão do ESP32
- [ ] Se aparece "STOMP CONNECT received"
- [ ] Se aparece "STOMP CONNECTED sent"
- [ ] Se aparece algum ERROR

## Soluções Rápidas

### Se o backend NÃO mostrar "STOMP CONNECT received":

1. **Desabilitar Security temporariamente:**

Em `SecurityConfig.java`:
```java
.authorizeHttpRequests(auth -> auth
    .anyRequest().permitAll()  // TEMPORÁRIO - permitir tudo
)
```

2. **Verificar se WebSocket está ativo:**

Adicione em `WebSocketEventListener`:
```java
@EventListener
public void handleWebSocketConnect(SessionConnectEvent event) {
    logger.info("==> WebSocket CONNECT attempt from {}", event.getMessage());
}
```

### Se o backend mostrar "STOMP CONNECT received" mas não "CONNECTED sent":

1. **Testar STOMP 1.0 puro (sem headers):**

No ESP32, use a **Variação B** do frame CONNECT acima.

2. **Adicionar handler de erro:**

```java
@MessageExceptionHandler
public void handleException(Exception e) {
    logger.error("STOMP ERROR: ", e);
}
```

## Checklist Final

Antes de continuar, confirme:
- [ ] Backend na porta 3000 (application.properties)
- [ ] ESP32 apontando para porta 3000
- [ ] IP correto (192.168.100.4 é o backend)
- [ ] Endpoint /ws-native registrado
- [ ] Security permitindo /ws-native/**
- [ ] Logs DEBUG ativados
- [ ] Apenas UMA WebSocketConfig
- [ ] Firewall liberado

## Próximo Passo

**Execute o PASSO 3 acima e me envie os logs.** Com isso, vou identificar exatamente onde está travando e aplicar a correção específica.

