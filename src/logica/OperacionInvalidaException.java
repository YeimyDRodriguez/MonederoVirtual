package logica;
/**
 * Es el error de negocio del proyecto: “saldo insuficiente”, “no existe el cliente”, “el monto debe ser mayor que 0”.
 */
public class OperacionInvalidaException extends RuntimeException {
    public OperacionInvalidaException(String message) {
        super(message);
    }
}
