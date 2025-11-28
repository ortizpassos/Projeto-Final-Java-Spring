# Frontend - Costura Ágil

Este frontend é a interface web do sistema Costura Ágil, voltada para o acompanhamento em tempo real da produção industrial de costura.

## Objetivo
Permite que gestores e operadores visualizem o status dos dispositivos, operações, metas e produção diária, com atualização instantânea via WebSocket.

## Tecnologias Utilizadas
- Angular (TypeScript)
- LVGL (para display embarcado, integração indireta)
- WebSocket (STOMP e RAW, integração com backend)
- Docker (opcional para deploy)

## Estrutura de Pastas
- `src/app/` 		- Componentes principais (dashboard, cadastro, login, dispositivos, etc.)
- `src/assets/` 	- Imagens e recursos estáticos
- `src/styles.css` 	- Estilos globais

## Como funciona a integração
- O frontend se conecta ao backend Spring Boot via WebSocket para receber atualizações de produção em tempo real.
- Quando um dispositivo (ESP32) envia um evento de produção, o backend processa e faz broadcast para o frontend, que atualiza a dashboard automaticamente.
- O frontend também consome APIs REST para cadastro, login e configuração de dispositivos/usuários.

## Mensageria
- **Produtor:**     ESP32 envia eventos de produção para o backend.
- **Consumidor:**   Backend envia atualizações para o frontend via WebSocket (STOMP).
- **Fluxo:**
	1. Dispositivo incrementa produção → backend.
	2. Backend processa e envia atualização para o frontend.
	3. Frontend exibe dados atualizados na dashboard.

---


## Servidor de desenvolvimento

Para iniciar o servidor de desenvolvimento local, execute:

```bash
ng serve
```

Depois, acesse `http://localhost:4200/` no navegador. O aplicativo recarrega automaticamente ao salvar alterações nos arquivos fonte.

## Gerando componentes e recursos

O Angular CLI permite criar componentes, diretivas e outros recursos facilmente. Para gerar um novo componente:

```bash
ng generate component nome-do-componente
```

Para ver todos os esquemas disponíveis:

```bash
ng generate --help
```

## Build (compilação)

Para compilar o projeto para produção:

```bash
ng build
```

Os arquivos finais ficarão na pasta `dist/`. O build de produção já aplica otimizações de desempenho.

## Testes unitários

Para rodar os testes unitários (Karma):

```bash
ng test
```

## Testes end-to-end (e2e)

Para testes de ponta a ponta:

```bash
ng e2e
```

O Angular CLI não inclui framework e2e por padrão, escolha o que preferir.

## Mais informações

Para detalhes sobre o Angular CLI e comandos, consulte a [documentação oficial](https://angular.dev/tools/cli).
