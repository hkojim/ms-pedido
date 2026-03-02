package miyamoto.ms_pedido.infrastructure.security;


import miyamoto.ms_pedido.business.dto.response.PacienteResponseDTO;
import miyamoto.ms_pedido.infrastructure.client.PacienteClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl {

    @Autowired
    private PacienteClient client;

    public UserDetails carregaDadosPaciente(String cpf, String token) {
        PacienteResponseDTO paciente = client.buscarPorCpf(cpf, token);
        return User
                .withUsername(paciente.cpf())
                .password("")
                .authorities("USER")
                .build();
    }
}
