BotDespesa1 🤖💰
O BotDespesa1 é uma API desenvolvida com Spring Boot integrada a um bot do Telegram para auxiliar no controle financeiro pessoal. Com ele, você pode registrar despesas e gerar relatórios mensais diretamente pelo chat.

✨ Funcionalidades
Cadastro Interativo: Na primeira interação, o bot solicita como você deseja ser chamado e armazena sua preferência.

Registro de Despesas: Comando /nova [nome] [valor] para salvar gastos rapidamente.

Relatórios Mensais: Comando /despesas [mês] [ano] que gera um resumo detalhado dos gastos de um período específico.

Banco de Dados H2: Utiliza banco de dados em memória para execução rápida em ambiente de desenvolvimento.

🚀 Tecnologias Utilizadas
Java 25

Spring Boot 3.5.7

Spring Data JPA

Telegram Bots Spring Boot Starter

Lombok

H2 Database

🛠️ Pré-requisitos
JDK 25 ou superior.

Maven (ou utilizar o Maven Wrapper incluído no projeto).

Uma conta no Telegram.

⚙️ Configuração Passo a Passo
1. Obter Token do Telegram
No Telegram, procure pelo @BotFather.

Envie o comando /newbot e siga as instruções para criar seu bot.

Guarde o Token de acesso e o Username gerados.

2. Configurar Arquivo de Segredos (Obrigatório)
Por questões de segurança, as credenciais do bot não são enviadas ao repositório. Você deve criar este arquivo manualmente:

Navegue até a pasta src/main/resources/.

Crie um arquivo chamado application-secret.properties.

Adicione o seguinte conteúdo, substituindo pelos seus dados:

Properties
telegram.bot.username=SEU_BOT_USERNAME
telegram.bot.token=SEU_TOKEN_AQUI
3. Executar o Projeto
Via Terminal (Linux/macOS):

Bash
./mvnw spring-boot:run
Via Terminal (Windows):

Bash
mvnw.cmd spring-boot:run
📖 Como Usar o Bot
Inicie uma conversa com seu bot no Telegram enviando /start.

O bot perguntará seu nome. Responda apenas com o nome desejado.

Após o cadastro, use:

/nova Almoço 35.50 para registrar uma despesa.

/despesas 02 2026 para listar os gastos de fevereiro de 2026.

🗄️ Acesso ao Banco de Dados (H2 Console)
Com a aplicação rodando, você pode visualizar os dados em:

URL: http://localhost:8080/h2-console

JDBC URL: jdbc:h2:mem:despesabase

User: admin

Password: admin