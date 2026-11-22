# Sistema de Monitoramento de Preços, Estoque e Compras

Projeto da disciplina de **DevOps** do curso de **Sistemas para Internet — UNIESP**, orientado pelo professor **Jonas Bernardino**.

## Sobre o projeto

Aplicação web para apoiar uma loja de produtos naturais no acompanhamento dos preços de seus fornecedores. O sistema receberá tabelas e orçamentos em PDF, transformará seus dados em registros estruturados e permitirá comparar preços de compra ao longo do tempo.

Além da aplicação funcional, o projeto contempla práticas de DevOps: versionamento com Git, colaboração por Pull Requests, testes automatizados, integração contínua, containers e logs de processamento.

**Status:** estrutura inicial do frontend criada. As funcionalidades de negócio e o backend ainda serão implementados.

## Funcionalidades previstas

### MVP — versão mínima obrigatória

- Cadastrar, consultar e editar produtos, incluindo unidade, preços e dados básicos de estoque.
- Cadastrar e consultar fornecedores.
- Enviar PDFs associados a um fornecedor e registrar cada tentativa de importação.
- Extrair referência, descrição, unidade/embalagem, quantidade e preço, quando disponíveis.
- Exibir os itens extraídos para conferência, correção e identificação de pendências.
- Associar os itens dos fornecedores aos produtos internos e reutilizar vínculos confirmados.
- Confirmar os dados válidos sem sobrescrever o histórico de preços.
- Comparar cada preço com o último preço válido do mesmo item, fornecedor e apresentação comercial.
- Classificar os resultados como **aumentou**, **diminuiu**, **sem alteração**, **novo** ou **pendente**.
- Exibir preços anterior e atual, diferença em reais e variação percentual.
- Consultar o histórico por produto, fornecedor e importação.

### Evoluções após o MVP

- Alertas de reposição com base no estoque atual e mínimo.
- Sugestões de revisão do preço de venda conforme uma política de margem.
- Gráficos de evolução de preços e indicadores.
- Suporte a novos layouts de PDF e melhorias no reconhecimento de produtos.

## Fluxo de uso

1. Cadastrar fornecedores e produtos internos.
2. Selecionar um fornecedor e enviar seu PDF.
3. Conferir os dados extraídos, corrigir inconsistências e associar os itens aos produtos.
4. Confirmar os itens válidos, preservando o histórico e identificando as pendências.
5. Importar uma tabela posterior do mesmo fornecedor.
6. Consultar as variações de preços e os registros históricos.

## Regras principais

- A comparação considera o mesmo fornecedor, item, unidade e embalagem.
- A referência é o último preço válido conhecido, mesmo que o item esteja ausente na importação imediatamente anterior.
- Itens sem preço anterior são classificados como novos, sem cálculo de percentual.
- Preços zero, ausentes ou inválidos e itens sem associação ficam pendentes e não entram automaticamente no histórico de preços válidos.
- A reimportação de um arquivo deve ser identificada para evitar duplicação silenciosa do histórico.
- Falhas parciais devem ser registradas; uma importação com pendências não pode ser apresentada como integralmente validada.
- A ausência de um item em um PDF posterior não significa que ele foi descontinuado.
- O preço de venda não será alterado automaticamente.
- Os resultados serão identificados por texto e ícones, além de cores.

A variação percentual será calculada somente quando o preço anterior for maior que zero:

```text
variação (%) = ((preço atual − preço anterior) / preço anterior) × 100
```

## Tecnologias e arquitetura previstas

| Camada | Tecnologia |
| --- | --- |
| Interface web | React com TypeScript e Vite |
| API REST | Java com Spring Boot |
| Banco de dados | PostgreSQL |
| Build | Maven no backend e npm no frontend |
| Testes e integração contínua | Testes automatizados e GitHub Actions |
| Containers | Docker e Docker Compose |
| Versionamento e colaboração | Git e GitHub |

O navegador acessará a interface web, que se comunicará com a API REST. O backend será responsável pela extração e validação dos PDFs, pelas regras de comparação e pela persistência no PostgreSQL.

O ambiente local será executado com Docker Compose.

## Estrutura planejada

```text
.
├── frontend/             # Interface web
├── backend/              # API Spring Boot e testes
├── docs/                 # Backlog e documentação complementar
├── .github/
│   └── workflows/        # Pipelines de build e testes
├── compose.yaml          # Serviços do ambiente local
├── .env.example          # Exemplo das variáveis de ambiente
├── .gitignore
└── README.md
```

## Execução e testes

As instruções para executar e verificar o frontend estão em [frontend/README.md](frontend/README.md). O backend e o ambiente com Docker Compose serão adicionados nas próximas etapas.

O ambiente deverá usar variáveis de ambiente para configurações de banco, portas e demais parâmetros. O arquivo `.env.example` documentará as variáveis necessárias, sem credenciais reais. Segredos não devem ser versionados.

Os testes automatizados deverão cobrir, no mínimo:

- Aumento, redução e igualdade de preços.
- Item novo sem preço anterior.
- Preço zero ou ausente.
- Associação pendente.

O pipeline do GitHub Actions executará build e testes em push e/ou Pull Request, sendo reprovado quando alguma dessas etapas falhar.

## Demonstração final prevista

- Iniciar a aplicação seguindo as instruções documentadas.
- Importar pelo menos dois PDFs compatíveis do mesmo fornecedor.
- Demonstrar aumento, redução, valor inalterado, item novo e pendência.
- Comprovar a preservação do histórico.
- Apresentar os testes passando no GitHub Actions e um Pull Request com revisão.
- Executar os serviços pelo Docker Compose e consultar os logs de importação.
