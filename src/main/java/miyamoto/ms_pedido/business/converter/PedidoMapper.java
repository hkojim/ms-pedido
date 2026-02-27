package miyamoto.ms_pedido.business.converter;

import miyamoto.ms_pedido.business.dto.request.PedidoRequestDTO;
import miyamoto.ms_pedido.business.dto.response.PedidoResponseDTO;
import miyamoto.ms_pedido.infrastructure.entity.PedidoEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface PedidoMapper {
    @Mapping(target = "status", constant = "PENDENTE")
    @Mapping(target = "dataSolicitacao", expression = "java(java.time.LocalDateTime.now())")
    PedidoEntity toEntity(PedidoRequestDTO dto);

    PedidoResponseDTO toResponse(PedidoEntity entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(PedidoRequestDTO dto, @MappingTarget PedidoEntity entity);
}
