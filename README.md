# Task Manager API

API REST de tarefas pessoais em **Java 21 + Spring Boot**, com PostgreSQL,
autenticação JWT e senhas protegidas por BCrypt. É o backend do projeto separado
`task-manager-desktop` (JavaFX).

## O que está implementado

- Cadastro e login.
- Criar, listar, editar, concluir, reabrir e excluir tarefas.
- Isolamento por usuário: o dono é obtido do token.
- Validação de entradas e respostas de erro em Problem Details.
- Testes de autenticação, autorização, ciclo de tarefas e expiração do JWT.

## Executar localmente

Pré-requisitos: **JDK 21**, PostgreSQL e uma base chamada `task_manager_db`.
O Maven Wrapper está incluído; não é obrigatório instalar Maven.

1. Crie a base pelo pgAdmin ou pelo comando SQL `CREATE DATABASE task_manager_db;`.
2. Copie `application-local.example.properties` para `application-local.properties`.
3. Preencha usuário/senha do PostgreSQL e uma chave JWT aleatória de pelo menos
   32 bytes. O arquivo local é ignorado pelo Git.
4. Execute na raiz do projeto:

```powershell
.\mvnw.cmd spring-boot:run
```

No Linux/macOS: `./mvnw spring-boot:run`.
No IntelliJ: importe o `pom.xml`, selecione JDK 21 e execute
`TaskManagerApiApplication`, com a raiz do projeto como diretório de trabalho.

A API atende em `http://localhost:8080`. Ela não possui uma página inicial;
acesse os endpoints pelo aplicativo ou pelo Postman.

### Configuração por ambiente

Também é possível fornecer `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` e
`JWT_SECRET`. O arquivo privado é opcional, mas a senha e o segredo são obrigatórios.
Não distribua `application-local.properties`. O Spring não carrega arquivos `.env`
automaticamente.

| Propriedade | Padrão |
|---|---|
| DB_URL | jdbc:postgresql://localhost:5432/task_manager_db |
| DB_USERNAME | postgres |
| DB_PASSWORD | obrigatório |
| JWT_SECRET | obrigatório; mínimo 32 bytes |
| DDL_AUTO | update, para desenvolvimento |
| app.cors.allowed-origins | http://localhost:3000,http://localhost:5173 |

O JavaFX não depende de CORS; essa configuração atende eventuais clientes de navegador.

## Usar a API

```http
POST /api/users
Content-Type: application/json

{"email":"voce@example.com","password":"UmaSenhaDeTeste123!"}
```

Faça login em `POST /api/auth/login` com o mesmo JSON.
A resposta é o **token como texto**, não um objeto JSON.
Envie `Authorization: Bearer <token>` nas requisições de tarefas.

| Método | Rota | Resultado |
|---|---|---|
| POST | /api/users | 201, id e email |
| POST | /api/auth/login | 200, token JWT |
| GET | /api/tasks | 200, tarefas do usuário autenticado |
| POST | /api/tasks | 201, tarefa criada |
| PUT | /api/tasks/{taskId} | 200, tarefa editada |
| PATCH | /api/tasks/{taskId}/status | 200, status atualizado |
| DELETE | /api/tasks/{taskId} | 204, sem corpo |

As rotas antigas `POST /api/tasks/{userId}` e
`GET /api/tasks/user/{userId}` continuam funcionando somente para o próprio usuário.

Exemplo de tarefa:
```json
{"title":"Estudar Java","description":"Revisar a integração com JavaFX","status":"PENDING"}
```

Status: `PENDING`, `IN_PROGRESS`, `COMPLETED`.
Para alterar somente o status, envie `{"status":"COMPLETED"}` à rota PATCH.
Título: até 255 caracteres; descrição: até 2000.
Cadastro: senha com pelo menos 8 caracteres e no máximo 72 bytes UTF-8
(limite do BCrypt). O limite em bytes também vale para o login.

Erros: **400** entrada inválida, **401** autenticação inválida/ausente,
**403** acesso negado, **404** tarefa inexistente ou de outro usuário,
**409** cadastro em conflito.
PUT substitui título/descrição; quando status é omitido, mantém o atual.

## Testar

```powershell
.\mvnw.cmd test
```

Os testes usam **H2 em memória**, sem importar as credenciais locais e sem tocar
no banco de desenvolvimento. A pipeline também executa os testes com PostgreSQL.
Há uma coleção pronta em `docs/postman/task-manager-api.postman_collection.json`:
importe e execute em ordem. Ela preenche token/IDs automaticamente, remove a
tarefa criada e mantém a conta de teste.

Para testar com PostgreSQL, forneça `TEST_DB_URL`, `TEST_DB_USERNAME`,
`TEST_DB_PASSWORD` e `TEST_DB_DRIVER=org.postgresql.Driver`.
**Use uma base descartável:** os testes usam `create-drop`.

## Organização

```text
src/main/java/com/portfolio/taskmanagerapi/
  config/       Configuração de segurança e CORS
  controller/   Rotas HTTP
  dto/          Contratos de entrada e saída
  domain/       Entidades persistidas
  repository/   Consultas ao banco
  security/     Validação e geração de JWT
  service/      Regras de negócio
  exception/    Respostas padronizadas de erro
```

Veja [arquitetura](docs/ARCHITECTURE.md), [roteiro de validação](docs/TESTING.md)
e [limites de segurança](docs/SECURITY.md).

## Escopo e próximos passos

Esta é uma versão funcional para desenvolvimento e portfólio.
Antes de expor à internet: HTTPS, limitação de tentativas de login, migrações
versionadas do banco, gestão de segredos e observabilidade.
Ainda não há recuperação de senha, refresh token, paginação nem instalador desktop.
