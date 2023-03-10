package co.ke.proaktivio.qwanguapi.exceptions;

public class CustomAlreadyExistsException extends RuntimeException {

    private static final long serialVersionUID = -5579267121360648161L;

	public CustomAlreadyExistsException(String message) {
        super(message);
    }
}
