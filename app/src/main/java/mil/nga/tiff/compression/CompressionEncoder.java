package mil.nga.tiff.compression;

import java.nio.ByteOrder;

/**
 * Compression encoder interface. Encode either on a per row or block basis
 * 
 * @author osbornb
 */
public interface CompressionEncoder {

	/**
	 * True to encode on a per row basis, false to encode on a per block / strip
	 * basis
	 * 
	 * @return true for row encoding
	 */
	public boolean rowEncoding();

	/**
	 * Encode the bytes
	 * 
	 * @param bytes
	 *            bytes to encode
	 * @param byteOrder
	 *            byte order
	 * @return encoded block of bytes
	 */
	public byte[] encode(byte[] bytes, ByteOrder byteOrder);

}
