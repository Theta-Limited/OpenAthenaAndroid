package mil.nga.tiff.compression;

import java.nio.ByteOrder;

/**
 * Raw / no compression
 * 
 * @author osbornb
 */
public class RawCompression implements CompressionDecoder, CompressionEncoder {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public byte[] decode(byte[] bytes, ByteOrder byteOrder) {
		return bytes;
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
		return bytes;
	}

}
