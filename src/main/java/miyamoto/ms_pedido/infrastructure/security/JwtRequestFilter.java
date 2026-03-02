package miyamoto.ms_pedido.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import miyamoto.ms_pedido.infrastructure.exceptions.ErrorResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
@Slf4j
// Define a classe JwtRequestFilter, que estende OncePerRequestFilter
public class JwtRequestFilter extends OncePerRequestFilter {

    // Define propriedades para armazenar instâncias de JwtUtil e UserDetailsService
    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;
    private final ObjectMapper objectMapper;

    // Construtor que inicializa as propriedades com instâncias fornecidas
    public JwtRequestFilter(JwtUtil jwtUtil, UserDetailsServiceImpl userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());;
    }

    // Metodo chamado uma vez por requisição para processar o filtro
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        // Obtém o valor do header "Authorization" da requisição
        final String authHeader = request.getHeader("Authorization");

        // Verifica se o cabeçalho existe e começa com "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        try {
            // Extrai o token JWT do cabeçalho
            final String token = authHeader.substring(7);
            // Extrai o nome de usuário do token JWT
            final String cpf = jwtUtil.extrairCpfToken(token);

            // Se o nome de usuário não for nulo e o usuário não estiver autenticado ainda
            if (cpf != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // Carrega os detalhes do usuário a partir do nome de usuário
                UserDetails userDetails = userDetailsService.carregaDadosPaciente(cpf, authHeader);
                // Valida o token JWT
                if (jwtUtil.validateToken(token, cpf)) {
                    // Cria um objeto de autenticação com as informações do usuário
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    // Define a autenticação no contexto de segurança
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
            // Continua a cadeia de filtros, permitindo que a requisição prossiga
            chain.doFilter(request, response);

        } catch (ExpiredJwtException e) {
            log.error("Token JWT expirado: {}", e.getMessage());
            handleError(response, "Token expirado", e.getMessage(), request.getRequestURI(), HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            //    e.printStackTrace(); //Exibe o tipo de exceção, mensagem e a linha do código onde ocorreu o erro
            log.error("Erro na autenticação JWT", e);
            handleError(response, "Erro de autenticação", "Credenciais inválidas", request.getRequestURI(), HttpStatus.FORBIDDEN);
        }
    }

    private void handleError(HttpServletResponse response, String msg, String detail, String path, HttpStatus status) throws IOException {
        ErrorResponseDTO error = ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .message(msg)
                .error(detail)
                .status(status.value())
                .path(path)
                .build();

        response.setStatus(status.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(error));
    }
}
