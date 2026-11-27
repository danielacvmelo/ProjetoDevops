# Frontend

Interface do Sistema de Monitoramento de Preços, Estoque e Compras, desenvolvida com React, TypeScript e Vite.

## Requisitos

- Node.js 24 LTS com npm.

## Executar localmente

Inicie a API e o PostgreSQL seguindo [backend/README.md](../backend/README.md). O frontend precisa da API para consultar e salvar registros.

A partir da raiz do repositório:

```sh
cd frontend
npm ci
npm run dev
```

Acesse o endereço informado pelo Vite no terminal (normalmente http://localhost:5173).

O Vite encaminha `/api` para `http://localhost:8080`. Não é necessário configurar variáveis para usar essas portas.

Para usar outro endereço, copie `frontend/.env.example` para `frontend/.env.local` e ajuste `API_PROXY_TARGET`. Reinicie o Vite depois de alterar as variáveis. `VITE_API_URL` define o endereço público da API no frontend; o padrão é `/api`.

Em uma publicação, configure o servidor para encaminhar `/api` ao backend ou defina `VITE_API_URL` com a URL completa antes do build. Para uma API em outra origem, ajuste também `CORS_ORIGINS` no backend. Variáveis `VITE_` ficam visíveis no navegador e não devem conter segredos.

## Verificar o projeto

```sh
npm run build
npm test
npm run lint
```

O build verifica o TypeScript e gera os arquivos de produção em `dist/`.

## Estrutura

- `src/main.tsx`: inicialização do React.
- `src/App.tsx`: estrutura da aplicação e navegação dos cadastros.
- `src/pages/`: telas de produtos e fornecedores.
- `src/components/`: campo reutilizável dos formulários.
- `src/domain/`: tipos e validações dos cadastros.
- `src/hooks/`: consulta e gravação dos registros pela API.
- `src/services/`: cliente HTTP e tratamento das respostas do backend.
- `tests/`: testes das regras de cadastro.
- `src/index.css`: estilos globais.
- `src/App.css`: estilos das telas de cadastros.
- `vite.config.ts`: configuração do Vite.

## Cadastros disponíveis

- Produtos: cadastro, busca por nome, código ou categoria e edição.
- Fornecedores: cadastro, busca por nome e consulta dos dados de contato.
- Validação de campos obrigatórios, preços e estoques não negativos, códigos de produtos e nomes de fornecedores duplicados.
- Carregamento dos dados, confirmação de gravação e mensagens de erro com opção de tentar novamente.
- Testes das regras de validação, normalização da busca e tratamento das respostas HTTP com o executor nativo do Node.js.

Os cadastros são consultados e salvos pela API. Os identificadores são gerados pelo backend. Na execução normal, o backend persiste os registros no PostgreSQL.

Os dados antigos do armazenamento local não são lidos, apagados nem migrados automaticamente. Caso tenha cadastrado dados antes desta integração, cadastre-os novamente com a API em execução.

Se o backend estiver indisponível, o sistema exibirá um erro e não usará armazenamento local como alternativa. Se uma gravação falhar, o formulário permanece preenchido para nova tentativa.

Importação de PDFs, comparação e histórico serão adicionados nas próximas etapas.
