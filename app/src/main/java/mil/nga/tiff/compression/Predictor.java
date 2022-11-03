package mil.nga.tiff.compression;

import java.io.IOException;
import java.util.List;

import mil.nga.tiff.io.ByteReader;
import mil.nga.tiff.io.ByteWriter;
import mil.nga.tiff.util.TiffConstants;
import mil.nga.tiff.util.TiffException;

/**
 * Differencing Predictor decoder
 * 
 * @author osbornb
 * @since 3.0.0
 */
public class Predictor {

	/**
	 * Decode the predictor encoded bytes
	 * 
	 * @param bytes
	 *            bytes to decode
	 * @param predictor
	 *            predictor value
	 * @param width
	 *            tile width
	 * @param height
	 *            tile height
	 * @param bitsPerSample
	 *            bits per samples
	 * @param planarConfiguration
	 *            planar configuration
	 * @return decoded or original bytes
	 */
	public static byte[] decode(byte[] bytes, int predictor, int width,
			int height, List<Integer> bitsPerSample, int planarConfiguration) {

		if (predictor != TiffConstants.PREDICTOR_NO) {

			int numBitsPerSample = bitsPerSample.get(0);
			if (numBitsPerSample % 8 != 0) {
				throw new TiffException(
						"When decoding with predictor, only multiple of 8 bits are supported");
			}

			for (int i = 1; i < bitsPerSample.size(); i++) {
				if (bitsPerSample.get(i) != numBitsPerSample) {
					throw new TiffException(
							"When decoding with predictor, all samples must have the same size");
				}
			}

			int bytesPerSample = numBitsPerSample / 8;
			int samples = planarConfiguration == 2 ? 1 : bitsPerSample.size();

			ByteReader reader = new ByteReader(bytes);
			ByteWriter writer = new ByteWriter();
			try {

				for (int row = 0; row < height; row++) {
					// Last strip will be truncated if height % stripHeight != 0
					if (row * samples * width
							* bytesPerSample >= bytes.length) {
						break;
					}
					switch (predictor) {
					case TiffConstants.PREDICTOR_HORIZONTAL:
						decodeHorizontal(reader, writer, width, bytesPerSample,
								samples);
						break;
					case TiffConstants.PREDICTOR_FLOATINGPOINT:
						decodeFloatingPoint(reader, writer, width,
								bytesPerSample, samples);
						break;
					default:
						throw new TiffException(
								"Unsupported predictor: " + predictor);
					}
				}

				bytes = writer.getBytes();

			} finally {
				writer.close();
			}

		}

		return bytes;
	}

	/**
	 * Decode a horizontal encoded predictor row
	 * 
	 * @param reader
	 *            byte reader
	 * @param writer
	 *            byte writer
	 * @param width
	 *            tile width
	 * @param bytesPerSample
	 *            bytes per sample
	 * @param samples
	 *            number of samples
	 */
	private static void decodeHorizontal(ByteReader reader, ByteWriter writer,
			int width, int bytesPerSample, int samples) {

		int[] previous = new int[samples];

		for (int pixel = 0; pixel < width; pixel++) {

			for (int sample = 0; sample < samples; sample++) {
				int value = readValue(reader, bytesPerSample)
						+ previous[sample];
				writeValue(writer, bytesPerSample, value);
				previous[sample] = value;
			}

		}

	}

	/**
	 * Decode a floating point encoded predictor row
	 * 
	 * @param reader
	 *            byte reader
	 * @param writer
	 *            byte writer
	 * @param width
	 *            tile width
	 * @param bytesPerSample
	 *            bytes per sample
	 * @param samples
	 *            number of samples
	 */
	private static void decodeFloatingPoint(ByteReader reader,
			ByteWriter writer, int width, int bytesPerSample, int samples) {

		int samplesWidth = width * samples;

		byte[] bytes = new byte[samplesWidth * bytesPerSample];

		byte[] previous = new byte[samples];

		for (int sampleByte = 0; sampleByte < width
				* bytesPerSample; sampleByte++) {

			for (int sample = 0; sample < samples; sample++) {
				byte value = (byte) (reader.readByte() + previous[sample]);
				bytes[sampleByte * samples + sample] = value;
				previous[sample] = value;
			}

		}

		for (int widthSample = 0; widthSample < samplesWidth; widthSample++) {
			for (int sampleByte = 0; sampleByte < bytesPerSample; sampleByte++) {
				int index = ((bytesPerSample - sampleByte - 1) * samplesWidth)
						+ widthSample;
				writer.writeByte(bytes[index]);
			}
		}

	}

	/**
	 * Read a sample value
	 * 
	 * @param reader
	 *            byte reader
	 * @param bytesPerSample
	 *            bytes per sample
	 * @return sample value
	 */
	private static int readValue(ByteReader reader, int bytesPerSample) {

		int value;

		switch (bytesPerSample) {
		case 1:
			value = reader.readByte();
			break;
		case 2:
			value = reader.readShort();
			break;
		case 4:
			value = reader.readInt();
			break;
		default:
			throw new TiffException("Predictor not supported with "
					+ bytesPerSample + " bytes per sample");
		}

		return value;
	}

	/**
	 * Write a sample value
	 * 
	 * @param writer
	 *            byte writer
	 * @param bytesPerSample
	 *            bytes per sample
	 * @param value
	 *            sample value
	 */
	private static void writeValue(ByteWriter writer, int bytesPerSample,
			int value) {

		try {

			switch (bytesPerSample) {
			case 1:
				writer.writeByte((byte) value);
				break;
			case 2:
				writer.writeShort((short) value);
				break;
			case 4:
				writer.writeInt(value);
				break;
			default:
				throw new TiffException("Predictor not supported with "
						+ bytesPerSample + " bytes per sample");
			}

		} catch (IOException e) {
			throw new TiffException("Failed to write value: " + value
					+ ", bytes: " + bytesPerSample, e);
		}

	}

}
