package br.com.luiz.despesabot.despesa;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;


public interface DespesaRepository extends JpaRepository<DespesaModel, UUID>{
    List<DespesaModel> findByDateTimeBetween(LocalDateTime start, LocalDateTime end); 
}
