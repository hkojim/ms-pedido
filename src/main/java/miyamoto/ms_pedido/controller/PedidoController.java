package miyamoto.ms_pedido.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import miyamoto.ms_pedido.business.PedidoService;
import miyamoto.ms_pedido.business.dto.request.PedidoRequestDTO;
import miyamoto.ms_pedido.business.dto.response.PedidoResponseDTO;
import miyamoto.ms_pedido.infrastructure.enums.StatusPedido;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
@Tag(name = "Pedidos", description = "Gerenciamento de ordens de serviço do laboratório")
public class PedidoController {
    private final PedidoService service;

     //Cria um novo pedido para um paciente validado pelo CPF.
     //O token de autorização é capturado para validar o paciente no outro microsserviço.
    @PostMapping
    @Operation(summary = "Cria um novo pedido", description = "O CPF deve existir na base de Pacientes")
    public ResponseEntity<PedidoResponseDTO> create(
            @RequestBody @Valid PedidoRequestDTO dto,
            @RequestHeader("Authorization") String token) {

        // O Service valida o CPF via OpenFeign e cria o registro
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criarPedido(dto, token));
    }

    //Busca um pedido pelo seu ID único, trazendo os dados do Paciente via composição.
    @GetMapping("/{id}")
    @Operation(summary = "Busca pedido por ID", description = "Retorna o pedido e os dados do paciente associado")
    public ResponseEntity<PedidoResponseDTO> getById(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {

        return ResponseEntity.ok(service.buscarPorId(id, token));
    }

     //Atualiza o status de um pedido (PENDENTE, EM_ANALISE, CONCLUIDO).
    @PatchMapping("/{id}/status")
    @Operation(summary = "Atualiza status do pedido", description = "Permite mudar o fluxo do exame")
    public ResponseEntity<PedidoResponseDTO> updateStatus(
            @PathVariable Long id,
            @RequestParam StatusPedido status,
            @RequestHeader("Authorization") String token) {

        return ResponseEntity.ok(service.atualizarStatus(id, status, token));
    }

     //Busca todos os pedidos associados a um CPF específico.
    @GetMapping("/paciente/{cpf}")
    @Operation(summary = "Lista pedidos por CPF", description = "Retorna histórico de pedidos de um paciente")
    public ResponseEntity<List<PedidoResponseDTO>> getByPacienteCpf(
            @PathVariable String cpf,
            @RequestHeader("Authorization") String token) {

        return ResponseEntity.ok(service.listarPorCpf(cpf, token));
    }

     //Exclui um pedido (Apenas se o status for PENDENTE - Regra de negócio sugerida).
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove um pedido", description = "Apenas pedidos pendentes podem ser removidos")
    public void delete(@PathVariable Long id) {
        service.deletar(id);
    }
}
