package miyamoto.ms_pedido.infrastructure.exceptions;

public class ResourceNotFoundException extends RuntimeException{

    //Erro de recurso não encontrado
    public ResourceNotFoundException(String mensagem){
        super(mensagem);
    }

    public ResourceNotFoundException(String mensagem, Throwable throwable){
        super(mensagem,throwable);

    }
}
