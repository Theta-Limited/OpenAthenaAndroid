package mil.nga.tiff.compression;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import mil.nga.tiff.io.IOUtils;
import mil.nga.tiff.util.TiffException;

/**
 * Deflate Compression
 * 
 * @author osbornb
 */
public class DeflateCompression implements CompressionDecoder,
		CompressionEncoder {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public byte[] decode(byte[] bytes, ByteOrder byteOrder) {
		try {
			Inflater inflater = new Inflater();
			inflater.setInput(bytes);  
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream(bytes.length);
			byte[] buffer = new byte[IOUtils.COPY_BUFFER_SIZE];
			while (!inflater.finished()) {
				int count = inflater.inflate(buffer);
				outputStream.write(buffer, 0, count);
			}
			outputStream.close();
			byte[] output = outputStream.toByteArray();

			return output;
		} catch (IOException e) {
			throw new TiffException("Failed close decoded byte stream", e);
		} catch (DataFormatException e) {
			throw new TiffException("Data format error while decoding stream", e);
		}
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
		try {
			Deflater deflater = new Deflater();
			deflater.setInput(bytes);
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream(bytes.length);
			deflater.finish();
			byte[] buffer = new byte[IOUtils.COPY_BUFFER_SIZE];
			while (!deflater.finished()) {
				int count = deflater.deflate(buffer); // returns the generated code... index
				outputStream.write(buffer, 0, count);
			}

			outputStream.close();
			byte[] output = outputStream.toByteArray();
			return output;
		} catch (IOException e) {
			throw new TiffException("Failed close encoded stream", e);
		}
	}
}
