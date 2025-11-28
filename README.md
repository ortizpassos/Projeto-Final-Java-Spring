# Projeto Final - Costura Ágil

## Integrantes do Grupo
- Eduardo Ortiz dos Passos


## Descrição do Sistema
O sistema Costura Ágil é uma solução para monitoramento de produção em tempo real em ambientes industriais de costura. Ele permite o acompanhamento da produtividade de cada colaborador, operação e dispositivo, facilitando a gestão e a tomada de decisão. O público alvo são gestores de produção, operadores e equipes de TI de pequenas e médias confecções.

## Tecnologias Utilizadas
- **Back-end:** Java 17, Spring Boot, WebSocket (STOMP e RAW), MongoDB, Docker
- **Front-end:** Angular, TypeScript, LVGL (ESP32)
- **Dispositivo:** ESP32 (firmware customizado)
- **Mensageria:** WebSocket (produção e eventos em tempo real)

## Como rodar o Back-end
1. Acesse a pasta `backend`:
   ```powershell
   cd backend
   ```
2. Compile o projeto:
   ```powershell
   mvn clean package -DskipTests
   ```
3. Execute o serviço:
   ```powershell
   java -jar target/monitor-ellas-api-1.0.0.jar
   ```
4. O backend estará disponível em `http://localhost:3001`.

## Como rodar o Front-end
1. Acesse a pasta `frontend`:
   ```powershell
   cd frontend
   ```
2. Instale as dependências:
   ```powershell
   npm install
   ```
3. Rode o servidor de desenvolvimento:
   ```powershell
   npm start
   ```
4. Acesse o sistema em `http://localhost:4200`.

## Mensageria no Sistema
A mensageria é feita via WebSocket:
- **Produtor:** O dispositivo ESP32 envia eventos de produção (contagem de peças) para o backend via WebSocket.
- **Consumidor:** O backend processa e armazena os dados, e envia atualizações em tempo real para o frontend (dashboard) e para outros dispositivos conectados.
- **Fluxo:**
  1. ESP32 envia mensagem de produção → backend.
  2. Backend processa, salva e faz broadcast para o frontend (Angular) e outros clientes.


