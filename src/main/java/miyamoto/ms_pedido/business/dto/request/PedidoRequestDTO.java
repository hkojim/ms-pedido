package miyamoto.ms_pedido.business.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PedidoRequestDTO(
        @NotNull @Size(min = 11, max = 11) String pacienteCpf,
        String observacoes
) {
}
