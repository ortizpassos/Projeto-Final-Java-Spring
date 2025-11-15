# Guia Rápido - Frontend Atualizado

## O que mudou?

O frontend foi atualizado para funcionar com o **backend Spring Boot** ao invés do backend Node.js original. A principal mudança foi a substituição do **Socket.IO** por **STOMP/SockJS** para comunicação em tempo real.

## Pré-requisitos

- Node.js 18+ instalado
- Backend Spring Boot rodando na porta 3001
- MongoDB configurado e acessível

## Instalação Rápida

```bash
# 1. Navegar para o diretório do frontend
cd frontend

# 2. As dependências já foram instaladas, mas se necessário:
npm install

# 3. Iniciar o servidor de desenvolvimento
npm start
```

O frontend estará disponível em `http://localhost:4200`

## Verificação

Após iniciar o frontend, verifique:

### 1. Console do Navegador
Você deve ver a mensagem:
```
STOMP Debug: Web Socket Opened...
WebSocket conectado via STOMP
```

### 2. Network Tab
Deve haver uma conexão WebSocket ativa para:
- `ws://localhost:3001/ws` (desenvolvimento local)

### 3. Funcionalidades
- Login deve funcionar normalmente
- Dashboard deve mostrar dispositivos
- Atualizações em tempo real devem funcionar

## Estrutura de Arquivos Modificados

```
frontend/
├── src/app/services/
│   ├── websocket.service.ts (NOVO - substitui socket.service.ts)
│   ├── auth.service.ts (sem mudanças)
│   ├── dispositivos.ts (sem mudanças)
│   └── ...
├── src/app/dashboard/
│   └── dashboard.ts (atualizado para usar WebSocketService)
├── src/app/display/
│   └── display.ts (atualizado para usar WebSocketService)
├── src/app/dispositivos/
│   └── dispositivos-list.ts (atualizado para usar WebSocketService)
├── src/app/producao/
│   └── producao.ts (atualizado para usar WebSocketService)
├── package.json (dependências atualizadas)
├── MIGRATION_NOTES.md (documentação detalhada)
└── QUICK_START.md (este arquivo)
```

## Configuração de Ambiente

### Desenvolvimento Local

O frontend está configurado para conectar automaticamente ao backend local:
- API REST: `http://localhost:3001/api`
- WebSocket: `ws://localhost:3001/ws`

### Produção

Para produção, atualize a URL do backend em:
- `src/app/services/websocket.service.ts`
- Outros serviços que fazem chamadas HTTP

## Comandos Úteis

```bash
# Iniciar servidor de desenvolvimento
npm start

# Build para produção
npm run build

# Executar testes
npm test

# Verificar dependências
npm list @stomp/stompjs sockjs-client
```

## Troubleshooting

### Erro: "WebSocket não está conectado"

**Causa:** Backend Spring Boot não está rodando ou não está acessível.

**Solução:**
1. Verifique se o backend está rodando: `curl http://localhost:3001/api/auth/login`
2. Verifique se o endpoint WebSocket está acessível
3. Verifique o console do backend para erros

### Erro: "CORS policy"

**Causa:** Configuração CORS do backend não permite requisições do frontend.

**Solução:**
1. Verifique `SecurityConfig.java` no backend
2. Certifique-se de que o CORS está configurado para aceitar `http://localhost:4200`

### Dispositivos não atualizam em tempo real

**Causa:** WebSocket não está conectado ou eventos não estão sendo recebidos.

**Solução:**
1. Abra o console do navegador e verifique se há mensagens de erro
2. Verifique a aba Network → WS para ver se há conexão WebSocket ativa
3. Verifique se o backend está emitindo eventos corretamente

### Erro ao fazer login

**Causa:** Backend não está respondendo ou credenciais inválidas.

**Solução:**
1. Verifique se o backend está rodando
2. Verifique se o MongoDB está acessível
3. Verifique as credenciais no banco de dados

## Diferenças Importantes

### Socket.IO vs STOMP

| Aspecto | Socket.IO (Antigo) | STOMP (Novo) |
|---------|-------------------|--------------|
| Protocolo | Proprietário | Padrão STOMP |
| Transporte | WebSocket + polling | WebSocket + SockJS fallback |
| Eventos | `socket.on('evento')` | `subscribe('/topic/evento')` |
| Emissão | `socket.emit('evento')` | `publish('/app/evento')` |

### Endpoints WebSocket

**Antigo (Socket.IO):**
- Conexão direta ao servidor
- Eventos customizados

**Novo (STOMP):**
- Endpoint: `/ws`
- Tópicos de subscrição: `/topic/*`
- Destinos de envio: `/app/*`

## Suporte

Para problemas ou dúvidas:

1. Verifique `MIGRATION_NOTES.md` para detalhes técnicos
2. Verifique os logs do console do navegador
3. Verifique os logs do backend Spring Boot
4. Consulte a documentação do Spring WebSocket

## Próximos Passos

Após verificar que tudo está funcionando:

1. Teste todas as funcionalidades principais
2. Verifique se as atualizações em tempo real estão funcionando
3. Teste com múltiplos dispositivos/usuários
4. Prepare para deploy em produção

## Deploy em Produção

Para fazer deploy do frontend:

```bash
# Build de produção
npm run build

# Os arquivos estarão em dist/
# Faça deploy para seu servidor web (Nginx, Apache, etc.)
```

Lembre-se de atualizar as URLs do backend para o ambiente de produção!
