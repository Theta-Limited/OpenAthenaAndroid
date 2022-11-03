package mil.nga.tiff;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import junit.framework.TestCase;
import mil.nga.tiff.util.TiffException;

/**
 * TIFF Test Utility methods
 * 
 * @author osbornb
 */
public class TiffTestUtils {

	/**
	 * Compare two TIFF Images
	 * 
	 * @param tiffImage1
	 *            tiff image 1
	 * @param tiffImage2
	 *            tiff image 2
	 */
	public static void compareTIFFImages(TIFFImage tiffImage1,
			TIFFImage tiffImage2) {
		compareTIFFImages(tiffImage1, tiffImage2, true, true);
	}

	/**
	 * Compare two TIFF Images
	 * 
	 * @param tiffImage1
	 *            tiff image 1
	 * @param tiffImage2
	 *            tiff image 2
	 * @param exactType
	 *            true if matching the exact data type
	 * @param sameBitsPerSample
	 *            true if should have the same bits per sample
	 */
	public static void compareTIFFImages(TIFFImage tiffImage1,
			TIFFImage tiffImage2, boolean exactType, boolean sameBitsPerSample) {

		TestCase.assertNotNull(tiffImage1);
		TestCase.assertNotNull(tiffImage2);
		TestCase.assertEquals(tiffImage1.getFileDirectories().size(),
				tiffImage2.getFileDirectories().size());
		for (int i = 0; i < tiffImage1.getFileDirectories().size(); i++) {
			FileDirectory fileDirectory1 = tiffImage1.getFileDirectory(i);
			FileDirectory fileDirectory2 = tiffImage2.getFileDirectory(i);

			Rasters sampleRasters1 = fileDirectory1.readRasters();
			compareFileDirectoryAndRastersMetadata(fileDirectory1,
					sampleRasters1);
			Rasters sampleRasters2 = fileDirectory2.readRasters();
			compareFileDirectoryAndRastersMetadata(fileDirectory2,
					sampleRasters2);
			compareRastersSampleValues(sampleRasters1, sampleRasters2,
					exactType, sameBitsPerSample);

			Rasters interleaveRasters1 = fileDirectory1
					.readInterleavedRasters();
			compareFileDirectoryAndRastersMetadata(fileDirectory1,
					interleaveRasters1);
			Rasters interleaveRasters2 = fileDirectory2
					.readInterleavedRasters();
			compareFileDirectoryAndRastersMetadata(fileDirectory2,
					interleaveRasters2);
			compareRastersInterleaveValues(interleaveRasters1,
					interleaveRasters2, exactType, sameBitsPerSample);

			compareRasters(fileDirectory1, sampleRasters1, fileDirectory2,
					interleaveRasters2, exactType, sameBitsPerSample);
			compareRasters(fileDirectory1, interleaveRasters1, fileDirectory2,
					sampleRasters2, exactType, sameBitsPerSample);
		}

	}

	/**
	 * Compare the metadata between a file directory and rasters
	 * 
	 * @param fileDirectory
	 *            file directory
	 * @param rasters
	 *            rasters
	 */
	private static void compareFileDirectoryAndRastersMetadata(
			FileDirectory fileDirectory, Rasters rasters) {
		TestCase.assertEquals(fileDirectory.getImageWidth(), rasters.getWidth());
		TestCase.assertEquals(fileDirectory.getImageHeight(),
				rasters.getHeight());
		TestCase.assertEquals(fileDirectory.getSamplesPerPixel(),
				rasters.getSamplesPerPixel());
		TestCase.assertEquals(fileDirectory.getBitsPerSample().size(), rasters
				.getBitsPerSample().size());
		for (int i = 0; i < fileDirectory.getBitsPerSample().size(); i++) {
			TestCase.assertEquals(fileDirectory.getBitsPerSample().get(i),
					rasters.getBitsPerSample().get(i));
		}
	}

	/**
	 * Compare rasters sample values
	 * 
	 * @param rasters1
	 *            rasters 1
	 * @param rasters2
	 *            rasters 2
	 */
	public static void compareRastersSampleValues(Rasters rasters1,
			Rasters rasters2) {
		compareRastersSampleValues(rasters1, rasters2, true, true);
	}

	/**
	 * Compare rasters sample values
	 * 
	 * @param rasters1
	 *            rasters 1
	 * @param rasters2
	 *            rasters 2
	 * @param exactType
	 *            true if matching the exact data type
	 * @param sameBitsPerSample
	 *            true if should have the same bits per sample
	 */
	public static void compareRastersSampleValues(Rasters rasters1,
			Rasters rasters2, boolean exactType, boolean sameBitsPerSample) {

		compareRastersMetadata(rasters1, rasters2, sameBitsPerSample);

		TestCase.assertNotNull(rasters1.getSampleValues());
		TestCase.assertNotNull(rasters2.getSampleValues());
		TestCase.assertEquals(rasters1.getSampleValues().length,
				rasters2.getSampleValues().length);

		for (int i = 0; i < rasters1.getSampleValues().length; i++) {
			TestCase.assertEquals(
					rasters1.getSampleValues()[i].capacity()
							/ rasters1.getFieldTypes()[i].getBytes(),
					rasters2.getSampleValues()[i].capacity()
							/ rasters2.getFieldTypes()[i].getBytes());

			for (int x = 0; x < rasters1.getWidth(); ++x) {
				for (int y = 0; y < rasters1.getHeight(); ++y) {
					compareNumbers(rasters1.getPixelSample(i, x, y),
							rasters2.getPixelSample(i, x, y), exactType);
				}
			}
		}
	}

	/**
	 * Compare rasters interleave values
	 * 
	 * @param rasters1
	 *            rasters 1
	 * @param rasters2
	 *            rasters 2
	 */
	public static void compareRastersInterleaveValues(Rasters rasters1,
			Rasters rasters2) {
		compareRastersInterleaveValues(rasters1, rasters2, true, true);
	}

	/**
	 * Compare rasters interleave values
	 * 
	 * @param rasters1
	 *            rasters 1
	 * @param rasters2
	 *            rasters 2
	 * @param exactType
	 *            true if matching the exact data type
	 * @param sameBitsPerSample
	 *            true if should have the same bits per sample
	 */
	public static void compareRastersInterleaveValues(Rasters rasters1,
			Rasters rasters2, boolean exactType, boolean sameBitsPerSample) {

		compareRastersMetadata(rasters1, rasters2, sameBitsPerSample);

		TestCase.assertNotNull(rasters1.getInterleaveValues());
		TestCase.assertNotNull(rasters2.getInterleaveValues());
		TestCase.assertEquals(rasters1.getInterleaveValues().capacity()
				/ rasters1.sizePixel(), rasters2.getInterleaveValues()
				.capacity() / rasters2.sizePixel());

		for (int i = 0; i < rasters1.getSamplesPerPixel(); i++) {
			for (int x = 0; x < rasters1.getWidth(); ++x) {
				for (int y = 0; y < rasters1.getHeight(); ++y) {
					compareNumbers(rasters1.getPixelSample(i, x, y),
							rasters2.getPixelSample(i, x, y), exactType);
				}
			}
		}
	}

	/**
	 * Compare rasters pixel values
	 * 
	 * @param fileDirectory1
	 *            file directory 1
	 * @param rasters1
	 *            rasters 1
	 * @param fileDirectory2
	 *            file directory 2
	 * @param rasters2
	 *            rasters 2
	 * @param exactType
	 *            true if matching the exact data type
	 * @param sameBitsPerSample
	 *            true if should have the same bits per sample
	 */
	public static void compareRasters(FileDirectory fileDirectory1,
			Rasters rasters1, FileDirectory fileDirectory2, Rasters rasters2,
			boolean exactType, boolean sameBitsPerSample) {

		compareRastersMetadata(rasters1, rasters2, sameBitsPerSample);

		int randomX = (int) (Math.random() * rasters1.getWidth());
		int randomY = (int) (Math.random() * rasters1.getHeight());

		for (int x = 0; x < rasters1.getWidth(); x++) {
			for (int y = 0; y < rasters1.getHeight(); y++) {

				Number[] pixel1 = rasters1.getPixel(x, y);
				Number[] pixel2 = rasters2.getPixel(x, y);

				Rasters rasters3 = null;
				Rasters rasters4 = null;
				if ((x == 0 && y == 0)
						|| (x == rasters1.getWidth() - 1 && y == rasters1
								.getHeight() - 1)
						|| (x == randomX && y == randomY)) {
					ImageWindow window = new ImageWindow(x, y);
					rasters3 = fileDirectory1.readRasters(window);
					TestCase.assertEquals(1, rasters3.getNumPixels());
					rasters4 = fileDirectory2.readInterleavedRasters(window);
					TestCase.assertEquals(1, rasters4.getNumPixels());
				}

				for (int sample = 0; sample < rasters1.getSamplesPerPixel(); sample++) {
					Number sample1 = rasters1.getPixelSample(sample, x, y);
					Number sample2 = rasters2.getPixelSample(sample, x, y);
					compareNumbers(sample1, sample2, exactType);
					compareNumbers(pixel1[sample], sample1, exactType);
					compareNumbers(pixel1[sample], pixel2[sample], exactType);

					if (rasters3 != null) {
						Number sample3 = rasters3.getPixelSample(sample, 0, 0);
						Number sample4 = rasters4.getPixelSample(sample, 0, 0);
						compareNumbers(pixel1[sample], sample3, exactType);
						compareNumbers(pixel1[sample], sample4, exactType);
					}
				}
			}
		}
	}

	/**
	 * Compare the rasters metadata
	 * 
	 * @param rasters1
	 *            rasters 1
	 * @param rasters2
	 *            rasters 2
	 * @param sameBitsPerSample
	 *            true if should have the same bits per sample
	 */
	private static void compareRastersMetadata(Rasters rasters1,
			Rasters rasters2, boolean sameBitsPerSample) {
		TestCase.assertNotNull(rasters1);
		TestCase.assertNotNull(rasters2);
		TestCase.assertEquals(rasters1.getWidth(), rasters2.getWidth());
		TestCase.assertEquals(rasters1.getHeight(), rasters2.getHeight());
		TestCase.assertEquals(rasters1.getNumPixels(), rasters2.getNumPixels());
		TestCase.assertEquals(rasters1.getBitsPerSample().size(), rasters2
				.getBitsPerSample().size());
		if (sameBitsPerSample) {
			for (int i = 0; i < rasters1.getBitsPerSample().size(); i++) {
				TestCase.assertEquals(rasters1.getBitsPerSample().get(i),
						rasters2.getBitsPerSample().get(i));
			}
		}
	}

	/**
	 * Compare the two numbers, either exactly or as double values
	 * 
	 * @param number1
	 *            number 1
	 * @param number2
	 *            number 2
	 * @param exactType
	 *            true if matching the exact data type
	 */
	private static void compareNumbers(Number number1, Number number2,
			boolean exactType) {
		if (exactType) {
			TestCase.assertEquals(number1, number2);
		} else {
			TestCase.assertEquals(number1.doubleValue(), number2.doubleValue());
		}
	}

	/**
	 * Get the file
	 * 
	 * @param fileName
	 *            file name
	 * @return file
	 */
	public static File getTestFile(String fileName) {
		URL resourceUrl = TiffTestUtils.class.getResource("/" + fileName);
		Path resourcePath;
		try {
			resourcePath = Paths.get(resourceUrl.toURI());
		} catch (URISyntaxException e) {
			throw new TiffException("Failed to get test file path", e);
		}
		File file = resourcePath.toFile();
		return file;
	}

}
