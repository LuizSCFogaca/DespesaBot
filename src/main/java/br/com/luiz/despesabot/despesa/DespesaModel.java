package br.com.luiz.despesabot.despesa;

import java.time.LocalDateTime;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;


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
    
}
