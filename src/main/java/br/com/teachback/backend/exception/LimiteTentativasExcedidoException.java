package br.com.teachback.backend.exception;

public class LimiteTentativasExcedidoException extends RuntimeException {
    public LimiteTentativasExcedidoException(String message) {
        super(message);
    }
}
