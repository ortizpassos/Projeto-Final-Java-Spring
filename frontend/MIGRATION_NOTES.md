# Notas de Migração - Frontend Angular

## Mudanças Realizadas

### 1. WebSocket - Socket.IO para STOMP/SockJS

O frontend foi atualizado para usar **STOMP over SockJS** ao invés de **Socket.IO**, para compatibilidade com o backend Spring Boot.

#### Dependências Atualizadas

**Removidas:**
- `socket.io-client`
- `@types/socket.io-client`

**Adicionadas:**
- `@stomp/stompjs`
- `sockjs-client`

#### Novo Serviço WebSocket

Foi criado o arquivo `src/app/services/websocket.service.ts` que substitui o antigo `socket.service.ts`.

**Principais diferenças:**

| Socket.IO | STOMP/SockJS |
|-----------|--------------|
| `socket.on('evento')` | `client.subscribe('/topic/evento')` |
| `socket.emit('evento', data)` | `client.publish({ destination: '/app/evento', body: JSON.stringify(data) })` |
| Conexão automática | Conexão via `Client.activate()` |

#### Eventos Mapeados

| Evento Original | Novo Tópico/Destino |
|----------------|---------------------|
| `deviceStatusUpdate` | `/topic/deviceStatusUpdate` |
| `productionUpdate` | `/topic/productionUpdate` |
| `deviceRegistered` | `/topic/deviceRegistered` |
| `loginSuccess` | `/topic/loginSuccess` |
| `loginFailed` | `/topic/loginFailed` |
| `operacaoSelecionada` | `/topic/operacaoSelecionada` |
| `producaoSuccess` | `/topic/producaoSuccess` |

### 2. Componentes Atualizados

Os seguintes componentes foram atualizados para usar o novo `WebSocketService`:

- **Dashboard** (`src/app/dashboard/dashboard.ts`)
  - Atualização de status de dispositivos em tempo real
  - Atualização de produção em tempo real

- **Display** (`src/app/display/display.ts`)
  - Monitoramento de produção
  - Atualização de status dos dispositivos

- **Dispositivos List** (`src/app/dispositivos/dispositivos-list/dispositivos-list.ts`)
  - Atualização de status dos dispositivos

- **Produção** (`src/app/producao/producao.ts`)
  - Monitoramento de produção em tempo real
  - Atualização de status

### 3. Compatibilidade com Backend Spring Boot

O novo serviço WebSocket está configurado para conectar ao endpoint correto do Spring Boot:

```typescript
const socketUrl = window.location.hostname === 'localhost'
  ? 'http://localhost:3001/ws'
  : 'https://monitor-ellas-backend.onrender.com/ws';
```

### 4. Recursos do Novo Serviço

O `WebSocketService` oferece:

- **Reconexão automática** com delay de 5 segundos
- **Heartbeat** para manter a conexão ativa
- **Observables** para todos os eventos
- **Métodos de envio** para comunicação com o servidor
- **Status de conexão** observável

## Como Usar

### Injetar o Serviço

```typescript
import { WebSocketService } from '../services/websocket.service';

constructor(private websocketService: WebSocketService) {}
```

### Escutar Eventos

```typescript
this.websocketService.onDeviceStatusUpdate().subscribe(data => {
  console.log('Dispositivo atualizado:', data);
});
```

### Enviar Mensagens

```typescript
this.websocketService.registerDevice({
  deviceToken: 'ABC123'
});
```

### Verificar Conexão

```typescript
this.websocketService.isConnected().subscribe(connected => {
  console.log('Conectado:', connected);
});
```

## Executar o Frontend

```bash
# Instalar dependências (já feito)
npm install

# Executar em modo desenvolvimento
npm start

# Build para produção
npm run build
```

## Testes

Após iniciar o frontend, verifique:

1. **Console do navegador** - Deve mostrar "WebSocket conectado via STOMP"
2. **Network tab** - Deve mostrar conexão WebSocket ativa
3. **Atualizações em tempo real** - Dispositivos devem atualizar automaticamente

## Troubleshooting

### WebSocket não conecta

- Verifique se o backend Spring Boot está rodando na porta 3001
- Verifique se o endpoint `/ws` está acessível
- Verifique o console para mensagens de erro STOMP

### Eventos não são recebidos

- Verifique se os tópicos estão corretos (`/topic/...`)
- Verifique se o backend está emitindo para os tópicos corretos
- Verifique se a subscrição foi feita após a conexão

### Mensagens não são enviadas

- Verifique se a conexão está ativa antes de enviar
- Verifique se o destino está correto (`/app/...`)
- Verifique se os dados estão sendo serializados corretamente

## Próximos Passos

- Adicionar tratamento de erro mais robusto
- Implementar retry logic personalizado
- Adicionar testes unitários para o WebSocketService
- Implementar indicador visual de status de conexão
