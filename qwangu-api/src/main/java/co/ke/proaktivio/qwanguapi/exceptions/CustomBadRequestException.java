package co.ke.proaktivio.qwanguapi.exceptions;

public class CustomBadRequestException extends RuntimeException {
	private static final long serialVersionUID = -2130391159334977897L;

	public CustomBadRequestException(String message) {
        super(message);
    }
}
