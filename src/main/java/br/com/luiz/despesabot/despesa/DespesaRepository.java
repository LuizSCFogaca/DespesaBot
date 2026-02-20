package br.com.luiz.despesabot.despesa;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DespesaRepository extends JpaRepository<DespesaModel, UUID>{
    
}
