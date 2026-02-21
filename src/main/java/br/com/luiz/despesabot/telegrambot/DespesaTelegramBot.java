package br.com.luiz.despesabot.telegrambot;
import br.com.luiz.despesabot.despesa.DespesaModel;
import br.com.luiz.despesabot.despesa.DespesaRepository;
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
                enviarMensagem(chatId, "Olá! Eu sou o seu Bot de Despesas. Use o comando '/nova [nome] - [descrição]' para adicionar uma despesa.");
            } else {
                enviarMensagem(chatId, "Comando não reconhecido.");
            }
        }
    }

    private void registrarDespesa(long chatId, String mensagem) {
        try {
            // Exemplo simples de parser da string: "/nova Almoço - Restaurante da esquina"
            String textoDespesa = mensagem.replace("/nova", "").trim();
            String[] partes = textoDespesa.split("-");
            
            String nome = partes.length > 0 ? partes[0].trim() : "Sem nome";
            String descricao = partes.length > 1 ? partes[1].trim() : "Sem descrição";

            DespesaModel despesa = new DespesaModel();
            despesa.setName(nome);
            despesa.setDescription(descricao);
            
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
