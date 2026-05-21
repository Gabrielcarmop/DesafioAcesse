# Leads Pro — Sistema de Importação de Leads em Lote

Sistema fullstack para importação, processamento e consulta de leads via arquivos CSV, com processamento assíncrono multi-thread, mensageria via Kafka e notificação de progresso em tempo real.

---

## Tecnologias

**Backend**
- Java 17
- Spring Boot 3.4.6
- Spring Kafka
- Spring Data JPA + Hibernate
- Flyway (migrations versionadas)
- PostgreSQL 15
- OpenCSV
- MapStruct + Lombok
- Springdoc OpenAPI (Swagger)

**Frontend**
- React 18 + Vite
- Axios
- Recharts
- Lucide React

**Infraestrutura**
- Docker + Docker Compose
- Apache Kafka + Zookeeper (Confluent 7.5.0)
- Kafka UI
- PgAdmin

---

## Como executar

### Pre-requisitos

- Docker e Docker Compose instalados
- Node.js 18+ (para o frontend)

### 1. Backend e infraestrutura

```bash
cd desafio_final
docker compose up --build
```

Na primeira execução o Maven baixa as dependencias e compila o projeto (~3-4 minutos).
Nas execucoes seguintes:

```bash
docker compose up
```

Aguarde todos os servicos subirem. O backend esta pronto quando aparecer no log:

```
leads-backend | Started DesafioApplication
```

### 2. Frontend

```bash
cd leads-frontend
npm install
npm run dev
```

### URLs disponiveis

| Servico     | URL                          | Descricao                        |
|-------------|------------------------------|----------------------------------|
| Frontend    | http://localhost:5173        | Interface React                  |
| Backend API | http://localhost:8080        | API REST Spring Boot             |
| Swagger     | http://localhost:8080/swagger-ui.html | Documentacao interativa |
| Kafka UI    | http://localhost:8081        | Monitoramento de topicos Kafka   |
| PgAdmin     | http://localhost:5050        | Interface do banco de dados      |

**Credenciais PgAdmin**
- Email: admin@leads.com
- Senha: admin

**Credenciais PostgreSQL**
- Host: localhost
- Porta: 5433
- Banco: leads_db
- Usuario: admin
- Senha: admin

### Comandos uteis

```bash
# Ver logs de um servico
docker compose logs -f backend
docker compose logs -f kafka

# Parar tudo
docker compose down

# Parar e apagar volumes (reseta o banco)
docker compose down -v
```

---

## Estrutura do projeto

```
desafio_final/          Backend Spring Boot
  src/
    main/
      java/com/example/desafio/
        config/         Configuracoes (Async, CORS, Kafka)
        core/           Excecoes e handlers globais
        dashboard/      Endpoint de metricas gerais
        infra/
          csv/          Parser e validador de CSV
          kafka/        Producer, consumer e eventos
          sse/          Servico de Server-Sent Events
        lead/           Entidade, repositorio, servico e controller de leads
        lote/           Entidade, repositorio, servico e controller de lotes
          processamento/ Entidade e servico de chunks (lote_processamento)
      resources/
        db/migration/   Migrations Flyway (V1 a V4)
        application.properties
  docker-compose.yml
  Dockerfile

leads-frontend/         Frontend React
  src/
    components/         Sidebar, StatCard
    pages/              Dashboard, Upload, Lotes, Leads
    services/           Cliente HTTP (api.js)
```

---

## Formato do CSV

O arquivo deve estar em UTF-8 com as seguintes colunas na primeira linha:

```
nome,email,telefone,origem,data_cadastro
```

Exemplo:

```csv
nome,email,telefone,origem,data_cadastro
Joao Silva,joao@email.com,(62)99999-0001,parceiro-a,2024-01-15
Maria Souza,maria@email.com,(11)98888-0002,parceiro-b,2024-02-20
```

- Tamanho maximo: 50 MB
- Ate 100.000 linhas por arquivo
- Duplicatas (mesmo email + origem) sao ignoradas automaticamente

---

## Decisoes tecnicas

### Processamento multi-thread com chunks

O CSV e particionado em chunks de N linhas (configuravel via `app.processamento.chunk-size`, padrao 1000). Cada chunk e processado por uma thread do pool `csvTaskExecutor` (4 threads core, 10 maximas). Isso permite processar arquivos de 100k linhas de forma paralela sem travar o servidor.

O endpoint `POST /api/lotes` retorna `202 Accepted` imediatamente — o processamento ocorre em background via `@Async`.

### Deduplicacao sem locks

A deduplicacao e feita via `unique constraint` no banco (`email + origem`). Quando uma linha duplicada chega, o banco lanca `DataIntegrityViolationException`, que e capturada e contabilizada como duplicata — sem necessidade de locks ou consultas pre-inserção. E a abordagem mais eficiente para alto volume.

### Tres entidades do dominio

- **Lote**: cabecalho do lote, status geral e totais agregados ao final
- **Lead**: dado individual importado, vinculado ao lote de origem
- **LoteProcessamento**: registro granular de cada chunk processado (total, sucesso, duplicatas, erros, tempo em ms). Permite auditoria detalhada de qual parte do arquivo falhou

### Mensageria Kafka

Tres topicos orquestram o ciclo de vida do processamento:

- `lote.iniciado` — publicado apos o upload, antes de comecar o processamento
- `lote.chunk.concluido` — publicado apos cada chunk com estatisticas parciais
- `lote.finalizado` — publicado ao fim do lote, carrega totais definitivos

O consumer atualiza o banco a cada chunk recebido, garantindo que o endpoint de status reflita o progresso real em tempo real.

### Progresso em tempo real

O endpoint `GET /api/lotes/{id}/status` retorna o progresso atual do lote incluindo `progressoPercentual`. O frontend consulta esse endpoint a cada 1,5 segundos (polling) enquanto o status for `PENDING` ou `PROCESSING`. A cada chunk processado, o backend persiste os totais parciais no banco antes de publicar o evento Kafka, garantindo que o progresso seja visivel imediatamente.

### Migrations versionadas com Flyway

```
V1__initial_schema.sql       Schema inicial
V2__create_table_lotes.sql   Tabela de lotes
V3__create_table_leads.sql   Tabela de leads com unique constraint
V4__create_table_lote_processamento.sql  Tabela de chunks
```

---

## Diagrama de arquitetura

```
                        +------------------+
                        |   React Frontend  |
                        |   (porta 5173)    |
                        +--------+---------+
                                 |
                          HTTP / REST
                                 |
                        +--------v---------+
                        |  Spring Boot API  |
                        |   (porta 8080)    |
                        +--+----------+----+
                           |          |
              +------------+          +------------+
              |                                    |
     +--------v--------+                 +---------v-------+
     |   PostgreSQL     |                |      Kafka       |
     |   (porta 5433)   |                |   (porta 9092)   |
     |                  |                |                  |
     |  leads           |                | lote.iniciado    |
     |  lotes           |                | lote.chunk.*     |
     |  lote_processam. |                | lote.finalizado  |
     +------------------+                +-----------------+
                                                  |
                                         +--------v--------+
                                         | Kafka Consumer  |
                                         | (mesmo servico) |
                                         | le banco e      |
                                         | atualiza status |
                                         +-----------------+

Fluxo de processamento:

  1. Upload CSV  -->  POST /api/lotes  -->  202 Accepted
  2. CsvProcessorService (thread async)
       |-- le CSV em chunks
       |-- para cada chunk:
             |-- valida e persiste leads (LeadBatchService)
             |-- atualiza Lote no banco (progresso parcial)
             |-- registra LoteProcessamento
             |-- publica lote.chunk.concluido no Kafka
       |-- ao fim: publica lote.finalizado no Kafka
  3. KafkaConsumer recebe eventos e notifica via SSE
  4. Frontend consulta GET /api/lotes/{id}/status a cada 1.5s
```

---

## Endpoints principais

| Metodo | Endpoint                    | Descricao                              |
|--------|-----------------------------|----------------------------------------|
| POST   | /api/lotes                  | Upload de CSV (multipart/form-data)    |
| GET    | /api/lotes                  | Listagem paginada de lotes             |
| GET    | /api/lotes/{id}/status      | Status e progresso do lote             |
| GET    | /api/lotes/{id}/chunks      | Chunks processados (lote_processamento)|
| GET    | /api/leads                  | Listagem paginada com filtros          |
| GET    | /api/dashboard              | Metricas gerais do sistema             |

Documentacao completa disponivel no Swagger: `http://localhost:8080/swagger-ui.html`

---

## Variaveis de ambiente

| Variavel                          | Padrao                                    | Descricao               |
|-----------------------------------|-------------------------------------------|-------------------------|
| DB_URL                            | jdbc:postgresql://localhost:5433/leads_db | URL do banco            |
| DB_USERNAME                       | admin                                     | Usuario do banco        |
| DB_PASSWORD                       | admin                                     | Senha do banco          |
| KAFKA_BOOTSTRAP_SERVERS           | localhost:9092                            | Endereco do Kafka       |
| APP_UPLOAD_DIRETORIO_TEMPORARIO   | /tmp/leads-uploads                        | Diretorio de uploads    |
