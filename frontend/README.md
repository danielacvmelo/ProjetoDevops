# Frontend

Interface do Sistema de Monitoramento de Preços, Estoque e Compras, desenvolvida com React, TypeScript e Vite.

## Requisitos

- Node.js 24 LTS com npm.

## Executar localmente

A partir da raiz do repositório:

```sh
cd frontend
npm ci
npm run dev
```

Acesse o endereço informado pelo Vite no terminal (normalmente http://localhost:5173).

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
- `src/hooks/`: armazenamento local dos registros.
- `tests/`: testes das regras de cadastro.
- `src/index.css`: estilos globais.
- `src/App.css`: estilos das telas de cadastros.
- `vite.config.ts`: configuração do Vite.

## Cadastros disponíveis

- Produtos: cadastro, busca por nome, código ou categoria e edição.
- Fornecedores: cadastro, busca por nome e consulta dos dados de contato.
- Validação de campos obrigatórios, preços e estoques não negativos, códigos de produtos e nomes de fornecedores duplicados.
- Testes das regras de validação e normalização da busca com o executor nativo do Node.js.

Os registros começam vazios e ficam salvos no armazenamento local deste navegador. Recarregar a página mantém os dados; limpar os dados do site remove os cadastros. Eles não são compartilhados com outros computadores ou navegadores.

A integração com a API e o PostgreSQL ainda será implementada. Esta etapa não conclui a persistência exigida para a entrega final. Importação de PDFs, comparação e histórico também serão adicionados nas próximas etapas.
