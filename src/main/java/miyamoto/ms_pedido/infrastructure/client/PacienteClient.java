package miyamoto.ms_pedido.infrastructure.client;

import miyamoto.ms_pedido.business.dto.response.PacienteResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "ms-paciente", url = "http://localhost:8081")

public interface PacienteClient {
    // Método para validar se o paciente existe antes de gerar o pedido
    @GetMapping("/api/pacientes/{cpf}")
    PacienteResponseDTO buscarPorCpf(@PathVariable("cpf") String cpf,
                                    @RequestHeader("Authorization") String token);
}
