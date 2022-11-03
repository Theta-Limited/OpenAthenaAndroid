package mil.nga.tiff.util;

/**
 * TIFF exception
 * 
 * @author osbornb
 */
public class TiffException extends RuntimeException {

	/**
	 * Serial version id
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 */
	public TiffException() {
		super();
	}

	/**
	 * Constructor
	 * 
	 * @param message
	 *            error message
	 */
	public TiffException(String message) {
		super(message);
	}

	/**
	 * Constructor
	 * 
	 * @param message
	 *            error message
	 * @param throwable
	 *            cause
	 */
	public TiffException(String message, Throwable throwable) {
		super(message, throwable);
	}

	/**
	 * Constructor
	 * 
	 * @param throwable
	 *            cause
	 */
	public TiffException(Throwable throwable) {
		super(throwable);
	}

}
