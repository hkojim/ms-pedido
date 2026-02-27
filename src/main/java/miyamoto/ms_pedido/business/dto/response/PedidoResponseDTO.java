package miyamoto.ms_pedido.business.dto.response;

import miyamoto.ms_pedido.infrastructure.enums.StatusPedido;

import java.time.LocalDateTime;

public record PedidoResponseDTO(
        Long id,
        String pacienteCpf,
        LocalDateTime dataSolicitacao,
        StatusPedido status,
        String observacoes,
        Object paciente // Pode ser o DTO vindo do outro microsserviço
) {
}
