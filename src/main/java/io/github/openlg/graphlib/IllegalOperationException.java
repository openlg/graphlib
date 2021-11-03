package io.github.openlg.graphlib;

/**
 * @author lg
 * Create by lg on 4/27/20 9:43 PM
 */
public class IllegalOperationException extends RuntimeException {

	public IllegalOperationException(){
		super();
	}

	public IllegalOperationException(String message) {
		super(message);
	}

	public IllegalOperationException(String message, Throwable cause) {
		super(message, cause);
	}

	public IllegalOperationException(Throwable cause) {
		super(cause);
	}

	public IllegalOperationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
