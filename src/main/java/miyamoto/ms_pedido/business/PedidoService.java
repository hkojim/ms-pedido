package miyamoto.ms_pedido.business;

import lombok.RequiredArgsConstructor;
import miyamoto.ms_pedido.business.converter.PedidoMapper;
import miyamoto.ms_pedido.business.dto.request.PedidoRequestDTO;
import miyamoto.ms_pedido.business.dto.response.PacienteResponseDTO;
import miyamoto.ms_pedido.business.dto.response.PedidoResponseDTO;
import miyamoto.ms_pedido.infrastructure.client.PacienteClient;
import miyamoto.ms_pedido.infrastructure.entity.PedidoEntity;
import miyamoto.ms_pedido.infrastructure.enums.StatusPedido;
import miyamoto.ms_pedido.infrastructure.exceptions.BusinessException;
import miyamoto.ms_pedido.infrastructure.exceptions.ResourceNotFoundException;
import miyamoto.ms_pedido.infrastructure.repository.PedidoRepository;
import miyamoto.ms_pedido.infrastructure.security.JwtUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PedidoService {
    private final PedidoRepository repository;
    private final PedidoMapper mapper;
    private final PacienteClient pacienteClient;
    private final JwtUtil jwtUtil;

    //Cria um pedido validando o CPF do paciente no microsserviço externo.
    public PedidoResponseDTO criarPedido(PedidoRequestDTO dto, String token) {
        // Validação Lógica: O paciente existe?
        PacienteResponseDTO paciente;
        try {
            paciente = pacienteClient.buscarPorCpf(dto.pacienteCpf(), token);
        } catch (Exception e) {
            throw new ResourceNotFoundException("Paciente com CPF " + dto.pacienteCpf() + " não encontrado.");
        }
        String cpf = jwtUtil.extrairCpfToken(token.substring(7));

        PedidoEntity entity = new PedidoEntity();
        entity.setPacienteId(paciente.id());
        entity.setPacienteCpf(cpf);
        entity.setDataSolicitacao(LocalDateTime.now());
        entity.setStatus(StatusPedido.PENDENTE);
        entity.setObservacoes(dto.observacoes());

        PedidoEntity salvo = repository.save(entity);
        return montarResposta(salvo, paciente);
    }

    public PedidoResponseDTO buscarPorId(Long id, String token) {
        PedidoEntity pedido = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido não localizado."));

        // Busca dados do paciente para enriquecer a resposta
        PacienteResponseDTO paciente = pacienteClient.buscarPorCpf(pedido.getPacienteCpf(), token);

        return montarResposta(pedido, paciente);
    }

     //Atualiza o status do pedido.
     //RN: Se o pedido estiver CONCLUIDO, talvez não deva permitir retroceder (depende do lab).
    public PedidoResponseDTO atualizarStatus(Long id, StatusPedido novoStatus, String token) {
        PedidoEntity pedido = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado para o ID: " + id));

        // Atualiza apenas o status, mantendo os dados anteriores (comportamento de PATCH)
        pedido.setStatus(novoStatus);
        PedidoEntity atualizado = repository.save(pedido);

        // Busca dados do paciente para compor a resposta completa
        PacienteResponseDTO paciente = pacienteClient.buscarPorCpf(pedido.getPacienteCpf(), token);
        return montarResposta(atualizado, paciente);
    }

     //Lista todos os pedidos vinculados a um CPF.
     //Utiliza o Feign para validar o CPF antes de buscar no banco local.
    public List<PedidoResponseDTO> listarPorCpf(String cpf, String token) {
        // Valida se o paciente existe
        PacienteResponseDTO paciente;
        try {
            paciente = pacienteClient.buscarPorCpf(cpf, token);
        } catch (Exception e) {
            throw new ResourceNotFoundException("Paciente com CPF " + cpf + " não encontrado.");
        }

        // Busca no banco de pedidos todos os registros com este CPF
        List<PedidoEntity> pedidos = repository.findAllByPacienteCpf(cpf);

        // Converte a lista de entidades para DTOs compostos
        return pedidos.stream()
                .map(p -> montarResposta(p, paciente))
                .toList();
    }

     //Deleta um pedido.
     //RN: Um pedido só pode ser deletado se ainda estiver com status PENDENTE.
    public void deletar(Long id) {
        PedidoEntity pedido = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado para exclusão."));

        if (pedido.getStatus() != StatusPedido.PENDENTE) {
            throw new BusinessException("Não é possível excluir um pedido que já está em análise ou concluído.");
        }

        repository.delete(pedido);
    }

    //Método auxiliar para montar o DTO composto (Pedido + Paciente)
    private PedidoResponseDTO montarResposta(PedidoEntity pedido, PacienteResponseDTO paciente) {
        return new PedidoResponseDTO(
                pedido.getId(),
                pedido.getPacienteCpf(),
                pedido.getDataSolicitacao(),
                pedido.getStatus(),
                pedido.getObservacoes(),
                paciente
        );
    }
}
