package miyamoto.ms_pedido.business.dto.response;

import java.time.LocalDate;

public record PacienteResponseDTO(Long id,
                                  String nome,
                                  String cpf,
                                  String senha,
                                  LocalDate dataNascimento,
                                  String convenio) {
}
