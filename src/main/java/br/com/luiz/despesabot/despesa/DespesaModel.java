package br.com.luiz.despesabot.despesa;

import java.time.LocalDateTime;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import br.com.luiz.despesabot.user.UserModel;

@Data
@Entity(name="tb_despesas")
public class DespesaModel {

    @Id
    @GeneratedValue(generator = "UUID")
    private UUID idDespesas;

    private String name;
    private String valor;

    @CreationTimestamp
    private LocalDateTime dateTime;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserModel user;
    
}
