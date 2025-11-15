# Guia de Migração - Node.js para Java Spring Boot

Este documento descreve as principais mudanças e equivalências entre o backend original em Node.js/Express e a nova versão em Java Spring Boot.

## Arquitetura

O backend em Spring Boot segue uma arquitetura em camadas mais estruturada, separando claramente as responsabilidades em diferentes pacotes. A estrutura foi organizada seguindo as melhores práticas do ecossistema Spring, com separação entre modelos, repositórios, serviços e controladores.

## Modelos de Dados

Os modelos Mongoose foram convertidos para entidades JPA com anotações Spring Data MongoDB. As principais diferenças incluem o uso de anotações como `@Document` para definir coleções, `@Id` para identificadores, e `@DBRef` para referências entre documentos. Os timestamps automáticos são gerenciados através de `@CreatedDate` e `@LastModifiedDate` com a anotação `@EnableMongoAuditing` na classe principal.

O hook `pre('save')` do Mongoose para hash de senha foi substituído por lógica no serviço de autenticação, utilizando o `BCryptPasswordEncoder` do Spring Security. O método `validarSenha()` foi movido para o `AuthService`, usando `passwordEncoder.matches()`.

## Repositórios

Os métodos Mongoose como `find()`, `findOne()`, `findById()` foram substituídos por interfaces que estendem `MongoRepository`. O Spring Data MongoDB gera automaticamente as implementações dos métodos baseados em convenções de nomenclatura, como `findByEmail()`, `findByCodigo()`, e `findByUsuarioAndAtivoOrderByNomeAsc()`.

## Autenticação e Segurança

O middleware `autenticar()` do Express foi substituído pelo `JwtAuthenticationFilter` integrado ao Spring Security. A configuração de segurança é centralizada na classe `SecurityConfig`, que define quais endpoints são públicos e quais requerem autenticação.

O token JWT é gerado usando a biblioteca `jjwt` versão 0.12.3, com suporte a chaves HMAC-SHA. A validação do token é feita automaticamente pelo filtro de autenticação antes de cada requisição protegida.

## Controladores REST

Os roteadores Express foram convertidos para `@RestController` com mapeamentos usando anotações como `@GetMapping`, `@PostMapping`, `@PatchMapping` e `@DeleteMapping`. O objeto `Authentication` do Spring Security fornece acesso ao usuário autenticado através do método `getPrincipal()`.

As respostas HTTP são construídas usando `ResponseEntity` para maior controle sobre status codes e headers. O tratamento de erros foi implementado com blocos try-catch que retornam respostas apropriadas com mensagens de erro.

## WebSocket

O Socket.IO foi substituído por STOMP over SockJS, que é o padrão do Spring WebSocket. Os eventos Socket.IO foram mapeados para métodos anotados com `@MessageMapping`, e as emissões são feitas através do `SimpMessagingTemplate`.

### Mapeamento de Eventos

| Node.js (Socket.IO) | Spring Boot (STOMP) |
|---------------------|---------------------|
| `socket.on('registerDevice')` | `@MessageMapping("/registerDevice")` |
| `socket.on('loginFuncionario')` | `@MessageMapping("/loginFuncionario")` |
| `socket.on('selecionarOperacao')` | `@MessageMapping("/selecionarOperacao")` |
| `socket.on('producao')` | `@MessageMapping("/producao")` |
| `io.emit('deviceStatusUpdate')` | `messagingTemplate.convertAndSend("/topic/deviceStatusUpdate")` |

### Configuração do Cliente

No frontend, será necessário usar uma biblioteca STOMP como `@stomp/stompjs` ou `sockjs-client` com `stompjs`. A conexão é estabelecida através do endpoint `/ws` com fallback SockJS.

Exemplo de conexão no cliente:
```javascript
const socket = new SockJS('http://localhost:3001/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function(frame) {
    // Subscrever aos tópicos
    stompClient.subscribe('/topic/deviceStatusUpdate', function(message) {
        const data = JSON.parse(message.body);
        // Processar atualização
    });
    
    // Enviar mensagem
    stompClient.send('/app/registerDevice', {}, JSON.stringify({
        deviceToken: 'ABC123'
    }));
});
```

## Validação

A validação de dados no Spring Boot utiliza Bean Validation (Jakarta Validation) com anotações como `@NotBlank`, `@Email`, e `@Valid` nos controladores. Isso substitui validações manuais ou bibliotecas como Joi do Node.js.

## Injeção de Dependências

O sistema de módulos do Node.js (`require`/`module.exports`) foi substituído pela injeção de dependências do Spring usando `@Autowired`. Todas as dependências são gerenciadas pelo container Spring, proporcionando melhor testabilidade e desacoplamento.

## Tratamento de Datas

As datas JavaScript foram convertidas para `LocalDateTime` do Java 8+. O Spring Boot serializa automaticamente essas datas para ISO-8601 no formato JSON. A configuração de timezone está definida em `application.properties` como `America/Sao_Paulo`.

## Variáveis de Ambiente

As variáveis de ambiente continuam sendo usadas da mesma forma, mas são injetadas através de `@Value` ou lidas diretamente do `application.properties`. O arquivo `.env` do Node.js pode ser substituído por profiles do Spring (`application-dev.properties`, `application-prod.properties`).

## CORS

A configuração CORS do Express foi substituída por `CorsConfigurationSource` no Spring Security, permitindo controle granular sobre origens, métodos e headers permitidos.

## Logging

O `console.log()` do Node.js foi substituído por SLF4J com Logback, usando diferentes níveis de log (DEBUG, INFO, WARN, ERROR). Os logs são configuráveis através do `application.properties`.

## Testes

Para testes, o Spring Boot oferece `@SpringBootTest` para testes de integração e `@WebMvcTest` para testes de controladores. O MockMvc permite testar endpoints REST sem iniciar o servidor completo.

## Deployment

O backend Spring Boot pode ser empacotado como um JAR executável usando Maven. Para deployment em produção, recomenda-se usar containers Docker ou plataformas como Heroku, AWS Elastic Beanstalk, ou Google Cloud Run.

### Dockerfile Exemplo

```dockerfile
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/monitor-ellas-api-1.0.0.jar app.jar
EXPOSE 3001
ENTRYPOINT ["java", "-jar", "app.jar"]
```

## Próximos Passos para Migração Completa

Para completar a migração do sistema, será necessário implementar as rotas de produção e relatórios que ainda não foram convertidas. Também é recomendado adicionar testes unitários e de integração para garantir a compatibilidade com o sistema original.

O frontend Angular precisará ser atualizado para usar STOMP/SockJS ao invés de Socket.IO. As chamadas HTTP REST podem permanecer as mesmas, desde que os formatos de requisição e resposta sejam compatíveis.

Por fim, é importante configurar um ambiente de staging para testar a integração completa entre frontend e backend antes de migrar para produção.
