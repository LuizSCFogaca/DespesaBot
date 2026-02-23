package br.com.luiz.despesabot.telegrambot;

import br.com.luiz.despesabot.despesa.DespesaModel;
import br.com.luiz.despesabot.despesa.DespesaRepository;
import br.com.luiz.despesabot.user.UserModel;
import br.com.luiz.despesabot.user.UserRepository;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class DespesaTelegramBot extends TelegramLongPollingBot {

    @Value("${telegram.bot.username}")
    private String botUsername;

    @Autowired
    private DespesaRepository despesaRepository;
    
    @Autowired
    private UserRepository userRepository;

    public DespesaTelegramBot(@Value("${telegram.bot.token}") String botToken) {
        super(botToken);
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String mensagem = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            
            Long telegramUserId = update.getMessage().getFrom().getId();

            // 1. Busca o usuário ou cria um NOVO no estado AGUARDANDO_NOME
            UserModel user = userRepository.findByTelegramUserId(telegramUserId)
                .orElseGet(() -> {
                    UserModel novoUsuario = new UserModel();
                    novoUsuario.setTelegramUserId(telegramUserId);
                    novoUsuario.setEstado("AGUARDANDO_NOME"); // Define o estado inicial
                    return userRepository.save(novoUsuario);
                });

            // 2. Lógica de Cadastro (Intercepta a mensagem antes de verificar comandos)
            if ("AGUARDANDO_NOME".equals(user.getEstado())) {
                if (mensagem.equals("/start")) {
                    enviarMensagem(chatId, "Olá! Bem-vindo ao seu Bot de Despesas. 🤖\n\nPara começarmos, como você gostaria de ser chamado?");
                } else {
                    // Se o estado é AGUARDANDO_NOME e não é /start, assumimos que a mensagem é o nome
                    user.setUsername(mensagem.trim());
                    user.setEstado("ATIVO"); // Atualiza o estado para liberar os comandos
                    userRepository.save(user);
                    
                    enviarMensagem(chatId, "Prazer em te conhecer, " + user.getUsername() + "! 🎉\nSeu cadastro foi concluído.\n\nComandos disponíveis:\n'/nova [nome] [valor]' para adicionar uma despesa.\n'/despesas [mês] [ano]' para receber um relatório.");
                }
                return; // Interrompe a execução aqui para não cair nos ifs de baixo
            }

            // 3. Execução normal de comandos (Só chega aqui se o estado for ATIVO)
            if (mensagem.startsWith("/nova")) {
                registrarDespesa(chatId, mensagem, user);
            } else if (mensagem.equals("/start")) {
                enviarMensagem(chatId, "Olá novamente, " + user.getUsername() + "! 👋 \n\nComandos disponíveis:\n'/nova [nome] [valor]' para adicionar uma despesa.\n'/despesas [mês] [ano]' para receber um relatório.");
            } else if(mensagem.startsWith("/despesas")){
                enviarDespesas(chatId, mensagem, user);
            } else {
                enviarMensagem(chatId, "Comando não reconhecido, " + user.getUsername() + ".");
            }
        }
    }

    private void enviarDespesas(long chatId, String mensagem, UserModel user) {
        try {
            String textoDespesa = mensagem.replace("/despesas", "").trim();
            String[] partes = textoDespesa.split(" ");

            if (partes.length < 2) {
                throw new IllegalArgumentException("Parâmetros insuficientes. Colocar mês e ano");
            }

            int mes = Integer.parseInt(partes[0].trim());
            int ano = Integer.parseInt(partes[1].trim());

            YearMonth yearMonth = YearMonth.of(ano, mes);
            LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
            LocalDateTime end = yearMonth.atEndOfMonth().atTime(23, 59, 59);

            List<DespesaModel> despesas = despesaRepository.findByUserAndDateTimeBetween(user, start, end);

            if (despesas.isEmpty()) {
                enviarMensagem(chatId, "Nenhuma despesa encontrada para você em " + String.format("%02d", mes) + "/" + ano + ".");
                return;
            }

            StringBuilder sb = new StringBuilder();
            sb.append("📋 *Seu Relatório de Despesas - ").append(String.format("%02d", mes)).append("/").append(ano).append("*\n");
            sb.append("---------------------------------------\n\n");

            DateTimeFormatter formatadorData = DateTimeFormatter.ofPattern("dd/MM HH:mm");

            for (DespesaModel d : despesas) {
                String dataFormatada = d.getDateTime().format(formatadorData);
                sb.append("🔹 *").append(d.getName()).append("*\n");
                sb.append("   Valor: R$").append(d.getValor()).append("\n");
                sb.append("   Data: ").append(dataFormatada).append("\n\n");
            }
            enviarMensagem(chatId, sb.toString());
            
        } catch (Exception e) {
            enviarMensagem(chatId, "❌ Erro ao enviar relatório de despesas.\nFormato Esperado: /despesas Mês(01, 02..., 12) Ano(2025)");
        }
    }

    private void registrarDespesa(long chatId, String mensagem, UserModel user) {
        try {
            String textoDespesa = mensagem.replace("/nova", "").trim();
            String[] partes = textoDespesa.split(" ");
            
            String nome = partes.length > 0 ? partes[0].trim() : "Sem nome";
            String descricao = partes.length > 1 ? partes[1].trim() : "Sem descrição";

            DespesaModel despesa = new DespesaModel();
            despesa.setName(nome);
            despesa.setValor(descricao);
            
            despesa.setUser(user);
            despesaRepository.save(despesa);

            enviarMensagem(chatId, "✅ Despesa '" + nome + "' salva com sucesso para " + user.getUsername() + "!");

        } catch (Exception e) {
            enviarMensagem(chatId, "❌ Erro ao salvar despesa. Formato esperado: /nova Nome - Descrição");
        }
    }

    private void enviarMensagem(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}