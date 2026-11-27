# Backend

API de cadastros do Sistema de Monitoramento de Preços, Estoque e Compras, com Java 21, Spring Boot, Maven e PostgreSQL.

## Executar com Docker

É necessário ter Docker com suporte a Compose instalado e em execução. Na raiz do repositório:

```powershell
Copy-Item .env.example .env
```

Edite o arquivo `.env` e defina `DB_PASSWORD`. Depois:

```powershell
docker compose up --build -d
docker compose logs -f backend
```

A API fica disponível em `http://localhost:8080/api`. O banco inicia vazio, e o Flyway cria as tabelas automaticamente. Os registros são preservados no volume `postgres_data` ao reiniciar os serviços.

Para consultar os produtos:

```powershell
Invoke-RestMethod http://localhost:8080/api/products
```

Para parar os serviços, mantendo os dados:

```powershell
docker compose down
```

O frontend consulta e salva os cadastros nesta API. Na execução local pelo Vite, as requisições em `/api` são encaminhadas para a porta `8080`. Os registros antigos do navegador não são importados automaticamente.

## Executar sem container para a API

Instale Java 21 e Maven 3.9. O PostgreSQL deve estar disponível. Para executar somente o banco com Docker, use `docker compose up -d db` na raiz.

No PowerShell, configure as mesmas credenciais usadas no banco e execute:

```powershell
$env:DB_URL = "jdbc:postgresql://localhost:5432/devops"
$env:DB_USER = "devops"
$env:DB_PASSWORD = "sua-senha-local"
cd backend
mvn spring-boot:run
```

O Spring Boot não lê o `.env` automaticamente. Esse arquivo é usado pelo Docker Compose; na execução direta, configure as variáveis no terminal.

## Configurações

| Variável | Uso |
| --- | --- |
| `DB_URL` | URL JDBC na execução direta; definida pelo Compose dentro do container |
| `DB_USER` | Usuário do banco |
| `DB_PASSWORD` | Senha do banco |
| `DB_NAME` | Nome do banco criado pelo Compose, padrão `devops` |
| `DB_PORT` | Porta local do PostgreSQL no Compose, padrão `5432` |
| `SERVER_PORT` | Porta local da API, padrão `8080` |
| `CORS_ORIGINS` | Origens autorizadas, separadas por vírgula; padrão `http://localhost:5173` |

Se o Vite iniciar em outra porta, inclua essa origem em `CORS_ORIGINS` e reinicie a API. A API desta etapa é destinada ao ambiente local, sem autenticação.

## Endpoints

| Método | Caminho | Operação |
| --- | --- | --- |
| GET | `/api/products` | Listar produtos |
| GET | `/api/products?q=chas` | Buscar por nome, código ou categoria |
| GET | `/api/products/{id}` | Consultar um produto |
| POST | `/api/products` | Cadastrar produto |
| PUT | `/api/products/{id}` | Atualizar todos os campos de um produto |
| GET | `/api/suppliers` | Listar fornecedores |
| GET | `/api/suppliers?q=raizes` | Buscar fornecedor por nome |
| GET | `/api/suppliers/{id}` | Consultar detalhes do fornecedor |
| POST | `/api/suppliers` | Cadastrar fornecedor |

As listas retornam arrays JSON. O identificador `id` é uma string UUID gerada pela API. POST retorna `201` com o cadastro e seu endereço no cabeçalho `Location`; PUT e GET retornam `200`. Não há exclusão nesta etapa.

### Produto

Exemplo de corpo para POST e PUT:

```json
{
  "code": "PRD-001",
  "name": "Hibisco",
  "category": "Chás",
  "unit": "kg",
  "purchasePrice": 33.00,
  "salePrice": 50.00,
  "stock": 1.5,
  "minimumStock": 0.5,
  "status": "Ativo"
}
```

Nome, unidade, preços, estoques e status são obrigatórios. Preços e estoques devem ser não negativos. Valores monetários aceitam até duas casas decimais; estoques aceitam até três. O status aceita `Ativo` ou `Inativo`.

O código é opcional, mas, quando preenchido, deve ser único. Nome aceita até 160 caracteres, código até 80, categoria até 100 e unidade até 30.

### Fornecedor

```json
{
  "name": "Raízes Naturais",
  "email": "contato@example.com",
  "phone": "(83) 3333-0100"
}
```

O nome é obrigatório e único, com até 160 caracteres. E-mail é opcional e deve ser válido quando preenchido, com até 255 caracteres. Telefone é opcional, com até 40 caracteres.

Buscas e comparações de duplicidade ignoram maiúsculas, acentos e espaços nas extremidades. As restrições de unicidade também são aplicadas no banco.

### Erros

- `400`: campos inválidos, JSON malformado ou UUID inválido.
- `404`: cadastro não encontrado.
- `409`: código de produto ou nome de fornecedor duplicado.

Os erros retornam `application/problem+json`, com uma mensagem em `detail`. Erros de validação incluem o objeto `errors`, relacionando campos e mensagens.

## Testes e build

Com Java 21 e Maven 3.9, na pasta `backend`:

```powershell
mvn verify
```

O comando compila, executa os testes de integração e gera `target/backend-0.0.1-SNAPSHOT.jar`.

Os testes usam H2 em modo PostgreSQL, exclusivo do perfil `test`, aplicando a mesma migração SQL. Cobrem cadastro, consulta, edição, busca, duplicidade, campos obrigatórios, valores negativos, precisão monetária, erros HTTP e CORS. Eles não substituem a verificação com PostgreSQL e Docker Compose.

## Estrutura

- `product`: entidade, requisição, repositório, serviço e controller de produtos.
- `supplier`: entidade, requisição, repositório, serviço e controller de fornecedores.
- `shared`: normalização e tratamento de erros.
- `config`: configuração de CORS.
- `src/main/resources/db/migration`: criação versionada das tabelas.
- `src/test`: testes automatizados da API.

A API de importação, conferência, associação e histórico está documentada em [docs/api-importacao.md](../docs/api-importacao.md). Os PDFs fictícios e o roteiro de demonstração ficam em [docs/exemplos](../docs/exemplos/README.md). A interface React de importação será adicionada separadamente.
