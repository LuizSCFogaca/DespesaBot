package br.com.luiz.despesabot.despesa;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/despesas")
public class DespesaController {
    
    @Autowired
    private DespesaRepository despesaRepository;

    @PostMapping("/")
    public DespesaModel create(@RequestBody DespesaModel despesaModel){
        var despesaCreated = this.despesaRepository.save(despesaModel);
        return despesaCreated;
    }
}
