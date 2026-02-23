package br.com.luiz.despesabot.despesa;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import br.com.luiz.despesabot.user.UserModel;

public interface DespesaRepository extends JpaRepository<DespesaModel, UUID>{
    List<DespesaModel> findByUserAndDateTimeBetween(UserModel user, LocalDateTime start, LocalDateTime end); 
}
