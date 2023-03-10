package co.ke.proaktivio.qwanguapi.exceptions;

public class CustomNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 7113257984797529857L;

	public CustomNotFoundException(String message) {
        super(message);
    }
}
