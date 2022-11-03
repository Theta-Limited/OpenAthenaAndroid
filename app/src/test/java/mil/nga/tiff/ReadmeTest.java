package mil.nga.tiff;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import mil.nga.tiff.util.TiffConstants;

/**
 * README example tests
 * 
 * @author osbornb
 */
public class ReadmeTest {

	/**
	 * Test read
	 * 
	 * @throws IOException
	 *             upon error
	 */
	@Test
	public void testWriteAndRead() throws IOException {
		testRead(testWrite());
	}

	/**
	 * Test read
	 * 
	 * @param input
	 *            input bytes
	 * @throws IOException
	 *             upon error
	 */
	public void testRead(byte[] input) throws IOException {

		// File input = ...
		// InputStream input = ...
		// byte[] input = ...
		// ByteReader input = ...

		TIFFImage tiffImage = TiffReader.readTiff(input);
		List<FileDirectory> directories = tiffImage.getFileDirectories();
		FileDirectory directory = directories.get(0);
		Rasters rasters = directory.readRasters();

	}

	/**
	 * Test write
	 * 
	 * @return bytes
	 * @throws IOException
	 *             upon error
	 */
	public byte[] testWrite() throws IOException {

		int width = 256;
		int height = 256;
		int samplesPerPixel = 1;
		FieldType fieldType = FieldType.FLOAT;
		int bitsPerSample = fieldType.getBits();

		Rasters rasters = new Rasters(width, height, samplesPerPixel,
				fieldType);

		int rowsPerStrip = rasters.calculateRowsPerStrip(
				TiffConstants.PLANAR_CONFIGURATION_CHUNKY);

		FileDirectory directory = new FileDirectory();
		directory.setImageWidth(width);
		directory.setImageHeight(height);
		directory.setBitsPerSample(bitsPerSample);
		directory.setCompression(TiffConstants.COMPRESSION_NO);
		directory.setPhotometricInterpretation(
				TiffConstants.PHOTOMETRIC_INTERPRETATION_BLACK_IS_ZERO);
		directory.setSamplesPerPixel(samplesPerPixel);
		directory.setRowsPerStrip(rowsPerStrip);
		directory.setPlanarConfiguration(
				TiffConstants.PLANAR_CONFIGURATION_CHUNKY);
		directory.setSampleFormat(TiffConstants.SAMPLE_FORMAT_FLOAT);
		directory.setWriteRasters(rasters);

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				float pixelValue = 1.0f; // any pixel value
				rasters.setFirstPixelSample(x, y, pixelValue);
			}
		}

		TIFFImage tiffImage = new TIFFImage();
		tiffImage.add(directory);
		byte[] bytes = TiffWriter.writeTiffToBytes(tiffImage);
		// or
		// File file = ...
		// TiffWriter.writeTiff(file, tiffImage);

		return bytes;
	}

}
