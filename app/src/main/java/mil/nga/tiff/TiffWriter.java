package mil.nga.tiff;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mil.nga.tiff.compression.CompressionEncoder;
import mil.nga.tiff.compression.DeflateCompression;
import mil.nga.tiff.compression.LZWCompression;
import mil.nga.tiff.compression.PackbitsCompression;
import mil.nga.tiff.compression.RawCompression;
import mil.nga.tiff.io.ByteWriter;
import mil.nga.tiff.io.IOUtils;
import mil.nga.tiff.util.TiffConstants;
import mil.nga.tiff.util.TiffException;

/**
 * TIFF Writer.
 * 
 * For a striped TIFF, the {@link FileDirectory#setStripOffsets(List)} and
 * {@link FileDirectory#setStripByteCounts(List)} methods are automatically set
 * or adjusted based upon attributes including:
 * {@link FileDirectory#getRowsPerStrip()},
 * {@link FileDirectory#getImageHeight()},
 * {@link FileDirectory#getPlanarConfiguration()}, and
 * {@link FileDirectory#getSamplesPerPixel()}.
 * 
 * The {@link Rasters#calculateRowsPerStrip(int)} and
 * {@link Rasters#calculateRowsPerStrip(int, int)} methods provide a mechanism
 * for determining a {@link FileDirectory#getRowsPerStrip()} setting.
 * 
 * @author osbornb
 */
public class TiffWriter {

	/**
	 * Write a TIFF to a file
	 * 
	 * @param file
	 *            file to create
	 * @param tiffImage
	 *            TIFF image
	 * @throws IOException
	 *             upon failure to write
	 */
	public static void writeTiff(File file, TIFFImage tiffImage)
			throws IOException {
		ByteWriter writer = new ByteWriter();
		writeTiff(file, writer, tiffImage);
		writer.close();
	}

	/**
	 * Write a TIFF to a file
	 * 
	 * @param file
	 *            file to create
	 * @param writer
	 *            byte writer
	 * @param tiffImage
	 *            TIFF Image
	 * @throws IOException
	 *             upon failure to write
	 */
	public static void writeTiff(File file, ByteWriter writer,
			TIFFImage tiffImage) throws IOException {
		byte[] bytes = writeTiffToBytes(writer, tiffImage);
		ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
		IOUtils.copyStream(inputStream, file);
	}

	/**
	 * Write a TIFF to bytes
	 * 
	 * @param tiffImage
	 *            TIFF image
	 * @return tiff bytes
	 * @throws IOException
	 *             upon failure to write
	 */
	public static byte[] writeTiffToBytes(TIFFImage tiffImage)
			throws IOException {
		ByteWriter writer = new ByteWriter();
		byte[] bytes = writeTiffToBytes(writer, tiffImage);
		writer.close();
		return bytes;
	}

	/**
	 * Write a TIFF to bytes
	 * 
	 * @param writer
	 *            byte writer
	 * @param tiffImage
	 *            TIFF image
	 * @return tiff bytes
	 * @throws IOException
	 *             upon failure to write
	 */
	public static byte[] writeTiffToBytes(ByteWriter writer, TIFFImage tiffImage)
			throws IOException {
		writeTiff(writer, tiffImage);
		byte[] bytes = writer.getBytes();
		return bytes;
	}

	/**
	 * Write a TIFF to a byte writer
	 * 
	 * @param writer
	 *            byte writer
	 * @param tiffImage
	 *            TIFF image
	 * @throws IOException
	 *             upon failure to write
	 */
	public static void writeTiff(ByteWriter writer, TIFFImage tiffImage)
			throws IOException {

		// Write the byte order (bytes 0-1)
		String byteOrder = writer.getByteOrder() == ByteOrder.BIG_ENDIAN ? TiffConstants.BYTE_ORDER_BIG_ENDIAN
				: TiffConstants.BYTE_ORDER_LITTLE_ENDIAN;
		writer.writeString(byteOrder);

		// Write the TIFF file identifier (bytes 2-3)
		writer.writeUnsignedShort(TiffConstants.FILE_IDENTIFIER);

		// Write the first IFD offset (bytes 4-7), set to start right away at
		// byte 8
		writer.writeUnsignedInt(TiffConstants.HEADER_BYTES);

		// Write the TIFF Image
		writeImageFileDirectories(writer, tiffImage);
	}

	/**
	 * Write the image file directories
	 * 
	 * @param writer
	 *            byte writer
	 * @param tiffImage
	 *            tiff image
	 * @throws IOException
	 */
	private static void writeImageFileDirectories(ByteWriter writer,
			TIFFImage tiffImage) throws IOException {

		// Write each file directory
		for (int i = 0; i < tiffImage.getFileDirectories().size(); i++) {
			FileDirectory fileDirectory = tiffImage.getFileDirectories().get(i);

			// Populate strip entries with placeholder values so the sizes come
			// out correctly
			populateRasterEntries(fileDirectory);

			// Track of the starting byte of this directory
			int startOfDirectory = writer.size();
			long afterDirectory = startOfDirectory + fileDirectory.size();
			long afterValues = startOfDirectory
					+ fileDirectory.sizeWithValues();

			// Write the number of directory entries
			writer.writeUnsignedShort(fileDirectory.numEntries());

			List<FileDirectoryEntry> entryValues = new ArrayList<>();

			// Byte to write the next values
			long nextByte = afterDirectory;

			List<Long> valueBytesCheck = new ArrayList<>();

			// Write the raster bytes to temporary storage
			if (fileDirectory.isTiled()) {
				throw new TiffException("Tiled images are not supported");
			}

			// Create the raster bytes, written to the stream later
			byte[] rastersBytes = writeRasters(writer.getByteOrder(),
					fileDirectory, afterValues);

			// Write each entry
			for (FileDirectoryEntry entry : fileDirectory.getEntries()) {
				writer.writeUnsignedShort(entry.getFieldTag().getId());
				writer.writeUnsignedShort(entry.getFieldType().getValue());
				writer.writeUnsignedInt(entry.getTypeCount());
				long valueBytes = entry.getFieldType().getBytes()
						* entry.getTypeCount();
				if (valueBytes > 4) {
					// Write the value offset
					entryValues.add(entry);
					writer.writeUnsignedInt(nextByte);
					valueBytesCheck.add(nextByte);
					nextByte += entry.sizeOfValues();
				} else {
					// Write the value in the inline 4 byte space, left aligned
					int bytesWritten = writeValues(writer, entry);
					if (bytesWritten != valueBytes) {
						throw new TiffException(
								"Unexpected bytes written. Expected: "
										+ valueBytes + ", Actual: "
										+ bytesWritten);
					}
					writeFillerBytes(writer, 4 - valueBytes);
				}
			}

			if (i + 1 == tiffImage.getFileDirectories().size()) {
				// Write 0's since there are not more file directories
				writeFillerBytes(writer, 4);
			} else {
				// Write the start address of the next file directory
				long nextFileDirectory = afterValues + rastersBytes.length;
				writer.writeUnsignedInt(nextFileDirectory);
			}

			// Write the external entry values
			for (int entryIndex = 0; entryIndex < entryValues.size(); entryIndex++) {
				FileDirectoryEntry entry = entryValues.get(entryIndex);
				long entryValuesByte = valueBytesCheck.get(entryIndex);
				if (entryValuesByte != writer.size()) {
					throw new TiffException(
							"Entry values byte does not match the write location. Entry Values Byte: "
									+ entryValuesByte + ", Current Byte: "
									+ writer.size());
				}
				int bytesWritten = writeValues(writer, entry);
				long valueBytes = entry.getFieldType().getBytes()
						* entry.getTypeCount();
				if (bytesWritten != valueBytes) {
					throw new TiffException(
							"Unexpected bytes written. Expected: " + valueBytes
									+ ", Actual: " + bytesWritten);
				}
			}

			// Write the image bytes
			writer.writeBytes(rastersBytes);
		}

	}

	/**
	 * Populate the raster entry values with placeholder values for correct size
	 * calculations
	 * 
	 * @param fileDirectory
	 *            file directory
	 */
	private static void populateRasterEntries(FileDirectory fileDirectory) {

		Rasters rasters = fileDirectory.getWriteRasters();
		if (rasters == null) {
			throw new TiffException(
					"File Directory Writer Rasters is required to create a TIFF");
		}

		// Populate the raster entries
		if (!fileDirectory.isTiled()) {
			populateStripEntries(fileDirectory);
		} else {
			throw new TiffException("Tiled images are not supported");
		}

	}

	/**
	 * Populate the strip entries with placeholder values
	 * 
	 * @param fileDirectory
	 *            file directory
	 */
	private static void populateStripEntries(FileDirectory fileDirectory) {

		int rowsPerStrip = fileDirectory.getRowsPerStrip().intValue();
		int imageHeight = fileDirectory.getImageHeight().intValue();
		int stripsPerSample = (imageHeight + rowsPerStrip - 1) / rowsPerStrip;
		int strips = stripsPerSample;
		if (fileDirectory.getPlanarConfiguration() == TiffConstants.PLANAR_CONFIGURATION_PLANAR) {
			strips *= fileDirectory.getSamplesPerPixel();
		}

		fileDirectory.setStripOffsetsAsLongs(new ArrayList<>(Collections
				.nCopies(strips, 0l)));
		fileDirectory.setStripByteCounts(new ArrayList<>(Collections.nCopies(
				strips, 0)));
	}

	/**
	 * Write the rasters as bytes
	 * 
	 * @param byteOrder
	 *            byte order
	 * @param fileDirectory
	 *            file directory
	 * @param offset
	 *            byte offset
	 * @return rasters bytes
	 * @throws IOException
	 */
	private static byte[] writeRasters(ByteOrder byteOrder,
			FileDirectory fileDirectory, long offset) throws IOException {

		Rasters rasters = fileDirectory.getWriteRasters();
		if (rasters == null) {
			throw new TiffException(
					"File Directory Writer Rasters is required to create a TIFF");
		}

		// Get the compression encoder
		CompressionEncoder encoder = getEncoder(fileDirectory);

		// Byte writer to write the raster
		ByteWriter writer = new ByteWriter(byteOrder);

		// Write the rasters
		if (!fileDirectory.isTiled()) {
			writeStripRasters(writer, fileDirectory, offset, encoder);
		} else {
			throw new TiffException("Tiled images are not supported");
		}

		// Return the rasters bytes
		byte[] bytes = writer.getBytes();
		writer.close();

		return bytes;
	}

	/**
	 * Write the rasters as bytes
	 * 
	 * @param writer
	 *            byte writer
	 * @param fileDirectory
	 *            file directory
	 * @param offset
	 *            byte offset
	 * @param encoder
	 *            compression encoder
	 * @throws IOException
	 */
	private static void writeStripRasters(ByteWriter writer,
			FileDirectory fileDirectory, long offset, CompressionEncoder encoder)
			throws IOException {

		Rasters rasters = fileDirectory.getWriteRasters();

		// Get the row and strip counts
		int rowsPerStrip = fileDirectory.getRowsPerStrip().intValue();
		int maxY = fileDirectory.getImageHeight().intValue();
		int stripsPerSample = (maxY + rowsPerStrip - 1) / rowsPerStrip;
		int strips = stripsPerSample;
		if (fileDirectory.getPlanarConfiguration() == TiffConstants.PLANAR_CONFIGURATION_PLANAR) {
			strips *= fileDirectory.getSamplesPerPixel();
		}

		// Build the strip offsets and byte counts
		List<Long> stripOffsets = new ArrayList<>();
		List<Integer> stripByteCounts = new ArrayList<>();

		// Write each strip
		for (int strip = 0; strip < strips; strip++) {

			int startingY;
			Integer sample = null;
			if (fileDirectory.getPlanarConfiguration() == TiffConstants.PLANAR_CONFIGURATION_PLANAR) {
				sample = strip / stripsPerSample;
				startingY = (strip % stripsPerSample) * rowsPerStrip;
			} else {
				startingY = strip * rowsPerStrip;
			}

			// Write the strip of bytes
			ByteWriter stripWriter = new ByteWriter(writer.getByteOrder());

			int endingY = Math.min(startingY + rowsPerStrip, maxY);
			for (int y = startingY; y < endingY; y++) {
				// Get the row bytes and encode if needed
				byte[] rowBytes = null;
				if (sample != null) {
					rowBytes = rasters.getSampleRow(y, sample,
							writer.getByteOrder());
				} else {
					rowBytes = rasters.getPixelRow(y, writer.getByteOrder());
				}

				if (encoder.rowEncoding()) {
					rowBytes = encoder.encode(rowBytes, writer.getByteOrder());
				}

				// Write the row
				stripWriter.writeBytes(rowBytes);
			}

			// Get the strip bytes and encode if needed
			byte[] stripBytes = stripWriter.getBytes();
			stripWriter.close();
			if (!encoder.rowEncoding()) {
				stripBytes = encoder.encode(stripBytes, writer.getByteOrder());
			}

			// Write the strip bytes
			writer.writeBytes(stripBytes);

			// Add the strip byte count
			int bytesWritten = stripBytes.length;
			stripByteCounts.add(bytesWritten);

			// Add the strip offset
			stripOffsets.add(offset);
			offset += bytesWritten;

		}

		// Set the strip offsets and byte counts
		fileDirectory.setStripOffsetsAsLongs(stripOffsets);
		fileDirectory.setStripByteCounts(stripByteCounts);

	}

	/**
	 * Get the compression encoder
	 * 
	 * @param fileDirectory
	 *            file directory
	 * @return encoder
	 */
	@SuppressWarnings("deprecation")
	private static CompressionEncoder getEncoder(FileDirectory fileDirectory) {

		CompressionEncoder encoder = null;

		// Determine the encoder based upon the compression
		Integer compression = fileDirectory.getCompression();
		if (compression == null) {
			compression = TiffConstants.COMPRESSION_NO;
		}

		switch (compression) {
		case TiffConstants.COMPRESSION_NO:
			encoder = new RawCompression();
			break;
		case TiffConstants.COMPRESSION_CCITT_HUFFMAN:
			throw new TiffException("CCITT Huffman compression not supported: "
					+ compression);
		case TiffConstants.COMPRESSION_T4:
			throw new TiffException("T4-encoding compression not supported: "
					+ compression);
		case TiffConstants.COMPRESSION_T6:
			throw new TiffException("T6-encoding compression not supported: "
					+ compression);
		case TiffConstants.COMPRESSION_LZW:
			encoder = new LZWCompression();
			break;
		case TiffConstants.COMPRESSION_JPEG_OLD:
		case TiffConstants.COMPRESSION_JPEG_NEW:
			throw new TiffException("JPEG compression not supported: "
					+ compression);
		case TiffConstants.COMPRESSION_DEFLATE:
		case TiffConstants.COMPRESSION_PKZIP_DEFLATE:
			encoder = new DeflateCompression();
			break;
		case TiffConstants.COMPRESSION_PACKBITS:
			encoder = new PackbitsCompression();
			break;
		default:
			throw new TiffException("Unknown compression method identifier: "
					+ compression);
		}

		return encoder;
	}

	/**
	 * Write filler 0 bytes
	 * 
	 * @param writer
	 *            byte writer
	 * @param count
	 *            number of 0 bytes to write
	 */
	private static void writeFillerBytes(ByteWriter writer, long count) {
		for (long i = 0; i < count; i++) {
			writer.writeUnsignedByte((short) 0);
		}
	}

	/**
	 * Write file directory entry values
	 * 
	 * @param writer
	 *            byte writer
	 * @param entry
	 *            file directory entry
	 * @return bytes written
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	private static int writeValues(ByteWriter writer, FileDirectoryEntry entry)
			throws IOException {

		List<Object> valuesList = null;
		if (entry.getTypeCount() == 1
				&& !entry.getFieldTag().isArray()
				&& !(entry.getFieldType() == FieldType.RATIONAL || entry
						.getFieldType() == FieldType.SRATIONAL)) {
			valuesList = new ArrayList<>();
			valuesList.add(entry.getValues());
		} else {
			valuesList = (List<Object>) entry.getValues();
		}

		int bytesWritten = 0;

		for (Object value : valuesList) {

			switch (entry.getFieldType()) {
			case ASCII:
				bytesWritten += writer.writeString((String) value);
				if (bytesWritten < entry.getTypeCount()) {
					long fillerBytes = entry.getTypeCount() - bytesWritten;
					writeFillerBytes(writer, fillerBytes);
					bytesWritten += fillerBytes;
				}
				break;
			case BYTE:
			case UNDEFINED:
				writer.writeUnsignedByte((short) value);
				bytesWritten += 1;
				break;
			case SBYTE:
				writer.writeByte((byte) value);
				bytesWritten += 1;
				break;
			case SHORT:
				writer.writeUnsignedShort((int) value);
				bytesWritten += 2;
				break;
			case SSHORT:
				writer.writeShort((short) value);
				bytesWritten += 2;
				break;
			case LONG:
				writer.writeUnsignedInt((long) value);
				bytesWritten += 4;
				break;
			case SLONG:
				writer.writeInt((int) value);
				bytesWritten += 4;
				break;
			case RATIONAL:
				writer.writeUnsignedInt((long) value);
				bytesWritten += 4;
				break;
			case SRATIONAL:
				writer.writeInt((int) value);
				bytesWritten += 4;
				break;
			case FLOAT:
				writer.writeFloat((float) value);
				bytesWritten += 4;
				break;
			case DOUBLE:
				writer.writeDouble((double) value);
				bytesWritten += 8;
				break;
			default:
				throw new TiffException("Invalid field type: "
						+ entry.getFieldType());
			}

		}

		return bytesWritten;
	}

}
