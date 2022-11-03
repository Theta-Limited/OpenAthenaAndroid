package mil.nga.tiff.compression;

import java.nio.ByteOrder;

/**
 * Compression decoder interface
 * 
 * @author osbornb
 */
public interface CompressionDecoder {

	/**
	 * Decode the bytes
	 * 
	 * @param bytes
	 *            bytes to decode
	 * @param byteOrder
	 *            byte order
	 * @return decoded bytes
	 */
	public byte[] decode(byte[] bytes, ByteOrder byteOrder);

}
