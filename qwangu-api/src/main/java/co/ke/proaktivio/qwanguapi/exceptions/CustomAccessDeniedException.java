package co.ke.proaktivio.qwanguapi.exceptions;

public class CustomAccessDeniedException extends RuntimeException {
	private static final long serialVersionUID = 2584979636114678077L;

	public CustomAccessDeniedException(String message) {
        super(message);
    }

}
