package mil.nga.tiff.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Write a byte array
 * 
 * @author osbornb
 */
public class ByteWriter {

	/**
	 * Output stream to write bytes to
	 */
	private final ByteArrayOutputStream os = new ByteArrayOutputStream();

	/**
	 * Byte order
	 */
	private ByteOrder byteOrder = null;

	/**
	 * Constructor
	 */
	public ByteWriter() {
		this(ByteOrder.nativeOrder());
	}

	/**
	 * Constructor
	 * 
	 * @param byteOrder
	 *            byte order
	 */
	public ByteWriter(ByteOrder byteOrder) {
		this.byteOrder = byteOrder;
	}

	/**
	 * Close the byte writer
	 */
	public void close() {
		try {
			os.close();
		} catch (IOException e) {
		}
	}

	/**
	 * Get the byte array output stream
	 * 
	 * @return byte array output stream
	 */
	public ByteArrayOutputStream getOutputStream() {
		return os;
	}

	/**
	 * Get the byte order
	 * 
	 * @return byte order
	 */
	public ByteOrder getByteOrder() {
		return byteOrder;
	}

	/**
	 * Set the byte order
	 * 
	 * @param byteOrder
	 *            byte order
	 */
	public void setByteOrder(ByteOrder byteOrder) {
		this.byteOrder = byteOrder;
	}

	/**
	 * Get the written bytes
	 * 
	 * @return written bytes
	 */
	public byte[] getBytes() {
		return os.toByteArray();
	}

	/**
	 * Get the current size in bytes written
	 * 
	 * @return bytes written
	 */
	public int size() {
		return os.size();
	}

	/**
	 * Write a String
	 * 
	 * @param value
	 *            string value
	 * @return bytes written
	 * @throws IOException
	 *             upon failure to write
	 */
	public int writeString(String value) throws IOException {
		byte[] valueBytes = value.getBytes();
		os.write(valueBytes);
		return valueBytes.length;
	}

	/**
	 * Write a byte
	 * 
	 * @param value
	 *            byte
	 */
	public void writeByte(byte value) {
		os.write(value);
	}

	/**
	 * Write an unsigned byte
	 * 
	 * @param value
	 *            unsigned byte as a short
	 */
	public void writeUnsignedByte(short value) {
		os.write((byte) (value & 0xff));
	}

	/**
	 * Write the bytes
	 * 
	 * @param value
	 *            bytes
	 * @throws IOException
	 *             upon failure to write
	 */
	public void writeBytes(byte[] value) throws IOException {
		os.write(value);
	}

	/**
	 * Write a short
	 * 
	 * @param value
	 *            short
	 * @throws IOException
	 *             upon failure to write
	 */
	public void writeShort(short value) throws IOException {
		byte[] valueBytes = new byte[2];
		ByteBuffer byteBuffer = ByteBuffer.allocate(2).order(byteOrder)
				.putShort(value);
		byteBuffer.flip();
		byteBuffer.get(valueBytes);
		os.write(valueBytes);
	}

	/**
	 * Write an unsigned short
	 * 
	 * @param value
	 *            unsigned short as an int
	 * @throws IOException
	 *             upon failure to write
	 */
	public void writeUnsignedShort(int value) throws IOException {
		byte[] valueBytes = new byte[2];
		ByteBuffer byteBuffer = ByteBuffer.allocate(2).order(byteOrder)
				.putShort((short) (value & 0xffff));
		byteBuffer.flip();
		byteBuffer.get(valueBytes);
		os.write(valueBytes);
	}

	/**
	 * Write an integer
	 * 
	 * @param value
	 *            int
	 * @throws IOException
	 *             upon failure to write
	 */
	public void writeInt(int value) throws IOException {
		byte[] valueBytes = new byte[4];
		ByteBuffer byteBuffer = ByteBuffer.allocate(4).order(byteOrder)
				.putInt(value);
		byteBuffer.flip();
		byteBuffer.get(valueBytes);
		os.write(valueBytes);
	}

	/**
	 * Write an unsigned int
	 * 
	 * @param value
	 *            unsigned int as long
	 * @throws IOException
	 *             upon failure to write
	 */
	public void writeUnsignedInt(long value) throws IOException {
		byte[] valueBytes = new byte[4];
		ByteBuffer byteBuffer = ByteBuffer.allocate(4).order(byteOrder)
				.putInt((int) (value & 0xffffffffL));
		byteBuffer.flip();
		byteBuffer.get(valueBytes);
		os.write(valueBytes);
	}

	/**
	 * Write a float
	 * 
	 * @param value
	 *            float
	 * @throws IOException
	 *             upon failure to write
	 */
	public void writeFloat(float value) throws IOException {
		byte[] valueBytes = new byte[4];
		ByteBuffer byteBuffer = ByteBuffer.allocate(4).order(byteOrder)
				.putFloat(value);
		byteBuffer.flip();
		byteBuffer.get(valueBytes);
		os.write(valueBytes);
	}

	/**
	 * Write a double
	 * 
	 * @param value
	 *            double
	 * @throws IOException
	 *             upon failure to write
	 */
	public void writeDouble(double value) throws IOException {
		byte[] valueBytes = new byte[8];
		ByteBuffer byteBuffer = ByteBuffer.allocate(8).order(byteOrder)
				.putDouble(value);
		byteBuffer.flip();
		byteBuffer.get(valueBytes);
		os.write(valueBytes);
	}

}
