package miyamoto.ms_pedido.infrastructure.repository;

import miyamoto.ms_pedido.infrastructure.entity.PedidoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<PedidoEntity, Long> {
    List<PedidoEntity> findByPacienteCpf(String pacienteCpf);
}
