package miyamoto.ms_pedido.infrastructure.exceptions;

public class BusinessException extends RuntimeException{

    //Erro de regra de negócio (ex: CPF não encontrado)
    public BusinessException(String mensagem){
        super(mensagem);
    }

    public BusinessException(String mensagem, Throwable throwable){
        super(mensagem,throwable);

    }
}
