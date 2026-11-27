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
npm run lint
```

O build verifica o TypeScript e gera os arquivos de produção em `dist/`.

## Estrutura

- `src/main.tsx`: inicialização do React.
- `src/App.tsx`: página inicial.
- `src/index.css`: estilos globais.
- `src/App.css`: estilos da página inicial.
- `vite.config.ts`: configuração do Vite.

Esta primeira etapa contém somente a estrutura inicial e uma página de apresentação. Cadastros, importação de PDFs e integração com a API serão implementados nas próximas etapas.
