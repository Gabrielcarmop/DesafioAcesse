# Leads Pro — Frontend

Interface React para o sistema de importação de leads em lote.

## Stack

- **React 18** + Vite
- **Recharts** — gráficos
- **Lucide React** — ícones
- **Axios** — cliente HTTP

## Pré-requisitos

- Node.js 18+
- Backend rodando em `http://localhost:8080`

## Instalação e execução

```bash
npm install
npm run dev
```

Acesse: http://localhost:5173

## Build de produção

```bash
npm run build
npm run preview
```

## Páginas

| Rota (interna) | Descrição |
|---|---|
| dashboard | Visão geral: estatísticas, gráfico de lotes e lista recente |
| upload | Drag-and-drop de CSV com barra de progresso em tempo real |
| lotes | Listagem paginada de lotes com modal de status detalhado |
| leads | Tabela paginada de leads com busca por nome/email e origem |

## Configuração de proxy

O arquivo `vite.config.js` já está configurado para fazer proxy de `/api/*`
para `http://localhost:8080`, sem necessidade de lidar com CORS em dev.

Para apontar para outro host, edite o `target` em `vite.config.js`.
