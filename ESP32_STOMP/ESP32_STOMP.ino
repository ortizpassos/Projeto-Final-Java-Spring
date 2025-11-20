// =======================================================
// ESP32 ↔ Spring Boot via WebSocket
// Requer bibliotecas: arduinoWebSockets (WebSocketsClient), ArduinoJson, WiFi, Preferences
// =======================================================

#include <WiFi.h>
#include <WebSocketsClient.h>
#include <ArduinoJson.h>
#include <Preferences.h>

// ---- Pino do botão ----
#define BUTTON_PIN 0 // GPIO0

// ---- Config WiFi ----
const char* ssid = "Use o seu 4G";
const char* password = "d1985A2025.";

// ---- Config servidor ----
const char* host = "https://monitor-ellas-backend.onrender.com";  // AJUSTE PARA O IP DO BACKEND
const uint16_t port = 443;           // Porta do Spring Boot (CORRIGIDO)
const char* wsPath = "/ws-raw";      // Endpoint WebSocket puro

// ---- Dispositivo ----
const char* deviceToken = "461545616614165";

WebSocketsClient ws;
Preferences prefs;

// ---- Estado de negócio ----
String funcionarioSenha = "";
String operacaoId = "";
String operacaoNome = "";
int metaDiaria = 0;
int quantidade = 0;

// ---- Controle de conexão ----
static bool wsConnected = false;

// ---- Declarações ----
void onWebSocketEvent(WStype_t type, uint8_t * payload, size_t length);
void processJsonMessage(const String& msg);
void solicitarSenhaFuncionario();
void registerDevice();
void loginFuncionario();
void sendProductionData();

// ---- WebSocket Events ----
void onWebSocketEvent(WStype_t type, uint8_t * payload, size_t length) {
  switch (type) {
    case WStype_DISCONNECTED:
      Serial.println("[WS] ❌ Disconnected");
      wsConnected = false;
      break;
    case WStype_CONNECTED:
      Serial.printf("[WS] ✅ Connected to ws://%s:%u%s\n", host, port, wsPath);
      wsConnected = true;
      registerDevice();
      break;
    case WStype_TEXT: {
      String msg = String((char*)payload, length);
      processJsonMessage(msg);
      break; }
    default:
      break;
  }
}

// ---- Processamento de mensagens JSON ----
void processJsonMessage(const String& msg) {
  Serial.printf("[WS] 📩 RX: %s\n", msg.c_str());
  DynamicJsonDocument doc(4096);
  DeserializationError err = deserializeJson(doc, msg);
  if (err) {
    Serial.printf("[JSON] ❌ Erro: %s\n", err.c_str());
    return;
  }
  String type = doc["type"] | "";
  if (type == "deviceRegistered") {
    bool success = doc["success"];
    String message = doc["message"];
    Serial.printf("[deviceRegistered] %s\n", message.c_str());
    if (success) {
      solicitarSenhaFuncionario();
      // Após digitar a senha no Serial, fazemos o login
      loginFuncionario();
    }
  } else if (type == "loginSuccess") {
    String nome = doc["data"]["funcionario"]["nome"];
    Serial.printf("[loginSuccess] 👤 %s logado!\n", nome.c_str());
    // Após login, carregar dados salvos da operação (se houver)
    String savedOperacaoId = "";
    String savedOperacaoNome = "";
    int savedMetaDiaria = 0;
    int savedQuantidade = 0;
    prefs.begin("prod", true);
    savedOperacaoId = prefs.getString("operacaoId", "");
    savedOperacaoNome = prefs.getString("operacaoNome", "");
    savedMetaDiaria = prefs.getInt("metaDiaria", 0);
    savedQuantidade = prefs.getInt("quantidade", 0);
    prefs.end();

    bool escolherNovaOperacao = false;
    if (savedOperacaoId.length() > 0) {
      Serial.printf("Operação salva: %s (meta: %d, produção: %d). Continuar? (S/N)\n", savedOperacaoNome.c_str(), savedMetaDiaria, savedQuantidade);
      // Ler resposta do usuário
      while (Serial.available() > 0) { Serial.read(); }
      while (Serial.available() == 0) { delay(100); }
      String ans = Serial.readStringUntil('\n');
      ans.trim();
      ans.toLowerCase();
      if (ans == "s" || ans == "y" || ans == "sim" || ans == "yes") {
        // Retomar operação anterior
        operacaoId = savedOperacaoId;
        operacaoNome = savedOperacaoNome;
        metaDiaria = savedMetaDiaria;
        quantidade = savedQuantidade;
        Serial.printf("Retomando operação: %s (meta: %d, produção: %d)\n", operacaoNome.c_str(), metaDiaria, quantidade);
        DynamicJsonDocument opdoc(256);
        opdoc["type"] = "selecionarOperacao";
        opdoc["deviceToken"] = deviceToken;
        opdoc["operacaoId"] = operacaoId;
        String opjson; serializeJson(opdoc, opjson);
        ws.sendTXT(opjson);
      } else {
        escolherNovaOperacao = true;
      }
    } else {
      escolherNovaOperacao = true;
    }

    if (escolherNovaOperacao && doc["data"].containsKey("operacoes") && doc["data"]["operacoes"].is<JsonArray>()) {
      JsonArray ops = doc["data"]["operacoes"].as<JsonArray>();
      Serial.println("Operações disponíveis:");
      for (size_t i = 0; i < ops.size(); i++) {
        Serial.printf("[%d] %s (meta: %d)\n", (int)(i+1), ops[i]["nome"].as<const char*>(), ops[i]["metaDiaria"].as<int>());
      }
      Serial.println("Digite o número da operação desejada:");
      while (Serial.available() == 0) { delay(100); }
      int idx = Serial.parseInt();
      if (idx < 1 || idx > (int)ops.size()) idx = 1;
      operacaoId = ops[idx-1]["_id"].as<String>();
      operacaoNome = ops[idx-1]["nome"].as<String>();
      metaDiaria = ops[idx-1]["metaDiaria"].as<int>();
      Serial.printf("Operação selecionada: %s (meta: %d)\n", operacaoNome.c_str(), metaDiaria);
      // Enviar seleção de operação
      DynamicJsonDocument opdoc(256);
      opdoc["type"] = "selecionarOperacao";
      opdoc["deviceToken"] = deviceToken;
      opdoc["operacaoId"] = operacaoId;
      String opjson; serializeJson(opdoc, opjson);
      ws.sendTXT(opjson);
      // Persistir
      prefs.begin("prod", false);
      prefs.putString("operacaoId", operacaoId);
      prefs.putString("operacaoNome", operacaoNome);
      prefs.putInt("metaDiaria", metaDiaria);
      prefs.end();
    }
  } else if (type == "operacaoSelecionada") {
    operacaoId = doc["data"]["operacao"]["_id"].as<String>();
    operacaoNome = doc["data"]["operacao"]["nome"].as<String>();
    metaDiaria = doc["data"]["operacao"]["metaDiaria"].as<int>();
    if (doc["data"].containsKey("producaoAtual") && !doc["data"]["producaoAtual"].isNull()) {
      quantidade = doc["data"]["producaoAtual"].as<int>();
    } else {
      quantidade = 0;
    }
    Serial.printf("✅ Operação carregada: %s (meta: %d, produção: %d)\n", operacaoNome.c_str(), metaDiaria, quantidade);
    prefs.begin("prod", false);
    prefs.putString("operacaoId", operacaoId);
    prefs.putString("operacaoNome", operacaoNome);
    prefs.putInt("metaDiaria", metaDiaria);
    prefs.putInt("quantidade", quantidade);
    prefs.end();
  } else if (type == "producaoSuccess") {
    Serial.println("[producaoSuccess] Produção registrada!");
  } else if (type == "error") {
    String message = doc["message"];
    Serial.printf("[Erro] ❌ %s\n", message.c_str());
    // Reinicia o fluxo de login do funcionário
    solicitarSenhaFuncionario();
    loginFuncionario();
  }
}

void solicitarSenhaFuncionario() {
  Serial.println("Digite a senha do funcionário:");
  // Limpa o buffer Serial para evitar leitura de entradas antigas
  while (Serial.available() > 0) { Serial.read(); }
  while (Serial.available() == 0) { delay(100); }
  funcionarioSenha = Serial.readStringUntil('\n');
  funcionarioSenha.trim();
}

void registerDevice() {
  DynamicJsonDocument doc(256);
  doc["type"] = "registerDevice";
  doc["deviceToken"] = deviceToken;
  String json; serializeJson(doc, json);
  ws.sendTXT(json);
  Serial.printf("➡️ Registrando dispositivo: %s\n", deviceToken);
}

void loginFuncionario() {
  DynamicJsonDocument doc(256);
  doc["type"] = "loginFuncionario";
  doc["deviceToken"] = deviceToken;
  doc["codigo"] = funcionarioSenha;
  String json; serializeJson(doc, json);
  ws.sendTXT(json);
  Serial.printf("➡️ Login do funcionário (código: %s)\n", funcionarioSenha.c_str());
}

void sendProductionData() {
  if (operacaoId == "") {
    Serial.println("⚠️ Nenhuma operação selecionada.");
    return;
  }
  int tempoProducao = random(100, 500);
  quantidade++;
  prefs.begin("prod", false);
  prefs.putInt("quantidade", quantidade);
  prefs.end();
  DynamicJsonDocument doc(256);
  doc["type"] = "producao";
  doc["deviceToken"] = deviceToken;
  doc["quantidade"] = quantidade;
  doc["tempoProducao"] = tempoProducao;
  String json; serializeJson(doc, json);
  ws.sendTXT(json);
  Serial.printf("📤 Produção enviada: %d peças em %d ms (%s)\n", quantidade, tempoProducao, operacaoNome.c_str());
}

// ---- Setup / Loop ----
void setup() {
  Serial.begin(115200);
  Serial.println();
  Serial.println("===== Sistema de Produção - ESP32 (WebSocket Puro) =====");
  pinMode(BUTTON_PIN, INPUT_PULLUP);
  Serial.printf("Conectando a rede Wi-Fi '%s'...\n", ssid);
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println();
  Serial.printf("Wi-Fi conectado! IP: %s\n", WiFi.localIP().toString().c_str());
  // Ao iniciar, não carrega dados de login/operacao nem faz login automático
  funcionarioSenha = "";
  operacaoId = "";
  operacaoNome = "";
  metaDiaria = 0;
  quantidade = 0;
  // Garantir que senha antiga não permaneça salva
  prefs.begin("prod", false);
  prefs.remove("senha");
  prefs.end();
  Serial.println("Conectando ao servidor WebSocket (puro)...");
  ws.begin(host, port, wsPath);
  ws.onEvent(onWebSocketEvent);
  ws.setReconnectInterval(3000);
  ws.enableHeartbeat(15000, 3000, 2);
}

void loop() {
  ws.loop();
  static bool lastButtonState = HIGH;
  bool buttonState = digitalRead(BUTTON_PIN);
  if (lastButtonState == HIGH && buttonState == LOW) {
    if (wsConnected && operacaoId.length() > 0) {
      sendProductionData();
    } else if (!wsConnected) {
      Serial.println("⚠️ WebSocket não conectado.");
    } else {
      Serial.println("⚠️ Faça login e selecione a operação antes de iniciar produção.");
    }
    delay(100);
  }
  lastButtonState = buttonState;
  // Não mostrar dados antigos no display se não estiver logado
  if (operacaoId == "") {
    // Aqui você pode adicionar código para limpar o display, se houver display físico
    // Exemplo: display.clear();
  }
}
