# Monitor Ellas - Backend Spring Boot

Backend do sistema de monitoramento de produção convertido de Node.js/Express para Java Spring Boot.

## Tecnologias Utilizadas

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Data MongoDB** - Persistência de dados
- **Spring Security** - Autenticação e autorização
- **Spring WebSocket** - Comunicação em tempo real
- **JWT (jjwt)** - Tokens de autenticação
- **Lombok** - Redução de boilerplate
- **Maven** - Gerenciamento de dependências

## Estrutura do Projeto

```
src/main/java/com/monitorellas/
├── config/              # Configurações (Security, WebSocket, MongoDB)
├── model/               # Entidades do domínio
├── repository/          # Repositórios Spring Data
├── service/             # Lógica de negócio
├── controller/          # Controladores REST
├── websocket/           # Handlers WebSocket
├── security/            # Segurança e JWT
└── dto/                 # Data Transfer Objects
```

## Modelos de Dados

### Usuario
- Usuários do sistema com autenticação

### Funcionario
- Funcionários da produção

### Dispositivo
- Dispositivos ESP32 de monitoramento
- Status: online, offline, ocioso, em_producao

### Operacao
- Operações de produção com metas diárias

### Producao
- Registros de produção dos funcionários

## Endpoints REST

### Autenticação
- `POST /api/auth/cadastro` - Cadastrar novo usuário
- `POST /api/auth/login` - Login e obtenção de token JWT
- `GET /api/auth/perfil` - Obter perfil do usuário autenticado

### Dispositivos
- `POST /api/dispositivos` - Criar dispositivo
- `GET /api/dispositivos` - Listar dispositivos do usuário
- `GET /api/dispositivos/{id}` - Buscar dispositivo por ID
- `PATCH /api/dispositivos/{id}` - Atualizar dispositivo
- `DELETE /api/dispositivos/{id}` - Deletar dispositivo

### Funcionários
- `POST /api/funcionarios` - Criar funcionário
- `GET /api/funcionarios` - Listar funcionários
- `GET /api/funcionarios/{id}` - Buscar funcionário por ID
- `PATCH /api/funcionarios/{id}` - Atualizar funcionário
- `DELETE /api/funcionarios/{id}` - Deletar funcionário

### Operações
- `POST /api/operacoes` - Criar operação
- `GET /api/operacoes` - Listar operações do usuário
- `GET /api/operacoes/{id}` - Buscar operação por ID
- `PATCH /api/operacoes/{id}` - Atualizar operação
- `DELETE /api/operacoes/{id}` - Desativar operação (soft delete)

## WebSocket Events

### Conexão
- Endpoint: `/ws`
- Protocolo: STOMP over SockJS

### Eventos do Cliente para Servidor
- `/app/registerDevice` - Registrar dispositivo online
- `/app/loginFuncionario` - Login de funcionário no dispositivo
- `/app/selecionarOperacao` - Selecionar operação no dispositivo
- `/app/producao` - Enviar dados de produção

### Eventos do Servidor para Cliente
- `/topic/deviceStatusUpdate` - Atualização de status do dispositivo
- `/topic/deviceRegistered` - Confirmação de registro
- `/topic/loginSuccess` - Login bem-sucedido
- `/topic/loginFailed` - Falha no login
- `/topic/operacaoSelecionada` - Operação selecionada
- `/topic/productionUpdate` - Atualização de produção
- `/topic/producaoSuccess` - Confirmação de produção recebida

## Configuração

### Variáveis de Ambiente

```properties
# MongoDB
MONGO_URI=mongodb://localhost:27017/production-monitor

# JWT
JWT_SECRET=segredo_super_secreto

# Porta do servidor
PORT=3001
```

### application.properties

As configurações podem ser ajustadas em `src/main/resources/application.properties`:

- Porta do servidor
- URI do MongoDB
- Configurações de JWT
- CORS
- Logging

## Como Executar

### Pré-requisitos
- Java 17 ou superior
- Maven 3.6+
- MongoDB rodando localmente ou remotamente

### Compilar o projeto
```bash
mvn clean install
```

### Executar a aplicação
```bash
mvn spring-boot:run
```

Ou executar o JAR gerado:
```bash
java -jar target/monitor-ellas-api-1.0.0.jar
```

A API estará disponível em `http://localhost:3001`

## Diferenças em relação ao Node.js

### Autenticação
- **Node.js**: Middleware `autenticar()` personalizado
- **Spring**: `JwtAuthenticationFilter` integrado ao Spring Security

### WebSocket
- **Node.js**: Socket.IO com eventos customizados
- **Spring**: STOMP over SockJS com message mapping

### Validação
- **Node.js**: Validação manual ou bibliotecas como Joi
- **Spring**: Bean Validation (Jakarta Validation) com anotações

### Injeção de Dependências
- **Node.js**: `require()` e exports
- **Spring**: `@Autowired` e inversão de controle

## Segurança

- Senhas criptografadas com BCrypt
- Autenticação via JWT
- Endpoints protegidos por Spring Security
- CORS configurado para aceitar requisições do frontend

## Logging

O sistema utiliza SLF4J com Logback para logging:
- Nível DEBUG para pacote `com.monitorellas`
- Nível INFO para Spring Web
- Nível DEBUG para Spring Security

## Próximos Passos

- Implementar rotas de produção e relatórios
- Adicionar testes unitários e de integração
- Implementar paginação nas listagens
- Adicionar validações mais robustas
- Implementar cache com Redis
- Adicionar documentação Swagger/OpenAPI

## Licença

Este projeto é uma conversão do backend original Monitor-Ellas.

## Verificação de E-mail (Nova Funcionalidade)

Fluxo:
1. `POST /api/auth/cadastro` cria usuário não verificado e envia código de 6 dígitos por e-mail.
2. Frontend redireciona para tela de verificação solicitando `email` e `codigo`.
3. `POST /api/auth/verificar` com `{ email, codigo }` confirma o e-mail se código válido e não expirado.
4. Antes de verificar, tentativa de login retorna 403 com `error.needsVerification = true`.
5. `POST /api/auth/reenviar` gera novo código (invalida anterior) e envia por e-mail.

Propriedades:
- `verif.code.exp.minutes` (default 30)
- `verif.code.attempt.limit` (default 5)

RabbitMQ:
- Routing key `email.verificacao` publicada com payload `{ type: 'email.verificacao', usuarioEmail, codigo, expiresAt }`.

Campos adicionados em `Usuario`:
- `emailVerificado` (Boolean)
- `verifCodigoHash` (BCrypt do código ativo)
- `verifExpiresAt` (expiração do código)
- `verifTentativas` (contador de tentativas)

Erros possíveis:
- Código expirado / inválido / limite de tentativas.

Status HTTP:
- Login bloqueado por verificação pendente: 403.
