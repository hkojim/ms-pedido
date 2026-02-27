package miyamoto.ms_pedido.business.dto.request;

import org.antlr.v4.runtime.misc.NotNull;

public record PedidoRequestDTO(
        @NotNull String pacienteCpf,
        String observacoes
) {
}
