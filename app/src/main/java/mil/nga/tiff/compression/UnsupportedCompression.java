package mil.nga.tiff.compression;

import java.nio.ByteOrder;

import mil.nga.tiff.util.TiffException;

/**
 * Unsupported compression
 * 
 * @author michaelknigge
 */
public class UnsupportedCompression implements CompressionDecoder, CompressionEncoder {

	private final String message;

	/**
	 * Constructor
	 * 
	 * @param message
	 *            message of the TiffException
	 * @since 2.0.1
	 */
	public UnsupportedCompression(final String message) {
		this.message = message;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public byte[] decode(byte[] bytes, ByteOrder byteOrder) {
		throw new TiffException(this.message);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean rowEncoding() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public byte[] encode(byte[] bytes, ByteOrder byteOrder) {
		throw new TiffException(this.message);
	}
}
