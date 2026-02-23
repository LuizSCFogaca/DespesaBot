package br.com.luiz.despesabot.telegrambot;
import br.com.luiz.despesabot.despesa.DespesaModel;
import br.com.luiz.despesabot.despesa.DespesaRepository;

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

    // Injetamos o repositório que você já havia criado para salvar no H2
    @Autowired
    private DespesaRepository despesaRepository;

    public DespesaTelegramBot(@Value("${telegram.bot.token}") String botToken) {
        super(botToken);
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    // Este método é chamado toda vez que alguém manda mensagem pro bot
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String mensagem = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            if (mensagem.startsWith("/nova")) {
                registrarDespesa(chatId, mensagem);
            } else if (mensagem.equals("/start")) {
                enviarMensagem(chatId, "Olá! Eu sou o seu Bot de Despesas. \n \n Comandos:\n '/nova [nome] [valor]' para adicionar uma despesa. \n '/despesas [mês] [ano]' para receber um relatório de suas despesas");
            } else if(mensagem.startsWith("/despesa")){
                enviarDespesas(chatId, mensagem);
            }else{
                enviarMensagem(chatId, "Comando não reconhecido.");
            }
        }
    }

    private void enviarDespesas(long chatId, String mensagem) {
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

            List<DespesaModel> despesas = despesaRepository.findByDateTimeBetween(start, end);

            if (despesas.isEmpty()) {
                enviarMensagem(chatId, "Nenhuma despesa encontrada para " + String.format("%02d", mes) + "/" + ano + ".");
                return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("📋 *Relatório de Despesas - ").append(String.format("%02d", mes)).append("/").append(ano).append("*\n");
        sb.append("---------------------------------------\n\n");

        DateTimeFormatter formatadorData = DateTimeFormatter.ofPattern("dd/MM HH:mm");

        for (DespesaModel d : despesas) {
            String dataFormatada = d.getDateTime().format(formatadorData);
            sb.append("🔹 *").append(d.getName()).append("*\n");
            sb.append("   Descrição: ").append(d.getValor()).append("\n");
            sb.append("   Data: ").append(dataFormatada).append("\n\n");
        }
        enviarMensagem(chatId, sb.toString());
    } catch (Exception e) {
        enviarMensagem(chatId, "❌ Erro ao enviar relatório de despesas.\nFormato Esperado: /despesas Mês(01, 02..., 12) Ano(2025)");
    }
}

    private void registrarDespesa(long chatId, String mensagem) {
        try {
            // Exemplo simples de parser da string: "/nova Almoço - Restaurante da esquina"
            String textoDespesa = mensagem.replace("/nova", "").trim();
            String[] partes = textoDespesa.split(" ");
            
            String nome = partes.length > 0 ? partes[0].trim() : "Sem nome";
            String descricao = partes.length > 1 ? partes[1].trim() : "Sem descrição";

            DespesaModel despesa = new DespesaModel();
            despesa.setName(nome);
            despesa.setValor(descricao);
            
            // Salva no banco de dados H2
            despesaRepository.save(despesa);

            enviarMensagem(chatId, "✅ Despesa '" + nome + "' salva com sucesso no banco de dados!");

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
