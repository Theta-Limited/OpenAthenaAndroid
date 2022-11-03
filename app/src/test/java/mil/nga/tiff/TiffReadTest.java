package mil.nga.tiff;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import junit.framework.TestCase;
import mil.nga.tiff.util.TiffException;

/**
 * TIFF Read tests
 * 
 * @author osbornb
 */
public class TiffReadTest {

	/**
	 * Test the stripped TIFF file vs the same data tiled
	 * 
	 * @throws IOException
	 *             upon error
	 */
	@Test
	public void testStrippedVsTiled() throws IOException {

		File strippedFile = TiffTestUtils
				.getTestFile(TiffTestConstants.FILE_STRIPPED);
		TIFFImage strippedTiff = TiffReader.readTiff(strippedFile);

		File file = TiffTestUtils.getTestFile(TiffTestConstants.FILE_TILED);
		TIFFImage tiff = TiffReader.readTiff(file);

		TiffTestUtils.compareTIFFImages(strippedTiff, tiff);

	}

	/**
	 * Test the stripped TIFF file vs the same data as int 32
	 * 
	 * @throws IOException
	 *             upon error
	 */
	@Test
	public void testStrippedVsInt32() throws IOException {

		File strippedFile = TiffTestUtils
				.getTestFile(TiffTestConstants.FILE_STRIPPED);
		TIFFImage strippedTiff = TiffReader.readTiff(strippedFile);

		File file = TiffTestUtils.getTestFile(TiffTestConstants.FILE_INT32);
		TIFFImage tiff = TiffReader.readTiff(file);

		TiffTestUtils.compareTIFFImages(strippedTiff, tiff, true, false);

	}

	/**
	 * Test the stripped TIFF file vs the same data as unsigned int 32
	 * 
	 * @throws IOException
	 *             upon error
	 */
	@Test
	public void testStrippedVsUInt32() throws IOException {

		File strippedFile = TiffTestUtils
				.getTestFile(TiffTestConstants.FILE_STRIPPED);
		TIFFImage strippedTiff = TiffReader.readTiff(strippedFile);

		File file = TiffTestUtils.getTestFile(TiffTestConstants.FILE_UINT32);
		TIFFImage tiff = TiffReader.readTiff(file);

		TiffTestUtils.compareTIFFImages(strippedTiff, tiff, false, false);

	}

	/**
	 * Test the stripped TIFF file vs the same data as float 32
	 * 
	 * @throws IOException
	 *             upon error
	 */
	@Test
	public void testStrippedVsFloat32() throws IOException {

		File strippedFile = TiffTestUtils
				.getTestFile(TiffTestConstants.FILE_STRIPPED);
		TIFFImage strippedTiff = TiffReader.readTiff(strippedFile);

		File file = TiffTestUtils.getTestFile(TiffTestConstants.FILE_FLOAT32);
		TIFFImage tiff = TiffReader.readTiff(file);

		TiffTestUtils.compareTIFFImages(strippedTiff, tiff, false, false);

	}

	/**
	 * Test the stripped TIFF file vs the same data as float 64
	 * 
	 * @throws IOException
	 *             upon error
	 */
	@Test
	public void testStrippedVsFloat64() throws IOException {

		File strippedFile = TiffTestUtils
				.getTestFile(TiffTestConstants.FILE_STRIPPED);
		TIFFImage strippedTiff = TiffReader.readTiff(strippedFile);

		File file = TiffTestUtils.getTestFile(TiffTestConstants.FILE_FLOAT64);
		TIFFImage tiff = TiffReader.readTiff(file);

		TiffTestUtils.compareTIFFImages(strippedTiff, tiff, false, false);

	}

	/**
	 * Test the stripped TIFF file vs the same data compressed as LZW
	 * 
	 * @throws IOException
	 *             upon error
	 */
	@Test
	public void testStrippedVsLzw() throws IOException {

		File strippedFile = TiffTestUtils
				.getTestFile(TiffTestConstants.FILE_STRIPPED);
		TIFFImage strippedTiff = TiffReader.readTiff(strippedFile);

		File file = TiffTestUtils.getTestFile(TiffTestConstants.FILE_LZW);
		TIFFImage tiff = TiffReader.readTiff(file);

		TiffTestUtils.compareTIFFImages(strippedTiff, tiff);

	}

	/**
	 * Test the stripped TIFF file vs the same data compressed as Packbits
	 * 
	 * @throws IOException
	 *             upon error
	 */
	@Test
	public void testStrippedVsPackbits() throws IOException {

		File strippedFile = TiffTestUtils
				.getTestFile(TiffTestConstants.FILE_STRIPPED);
		TIFFImage strippedTiff = TiffReader.readTiff(strippedFile);

		File file = TiffTestUtils.getTestFile(TiffTestConstants.FILE_PACKBITS);
		TIFFImage tiff = TiffReader.readTiff(file);

		TiffTestUtils.compareTIFFImages(strippedTiff, tiff);

	}

	/**
	 * Test the stripped TIFF file vs the same data as interleaved
	 * 
	 * @throws IOException
	 *             upon error
	 */
	@Test
	public void testStrippedVsInterleave() throws IOException {

		File strippedFile = TiffTestUtils
				.getTestFile(TiffTestConstants.FILE_STRIPPED);
		TIFFImage strippedTiff = TiffReader.readTiff(strippedFile);

		File file = TiffTestUtils
				.getTestFile(TiffTestConstants.FILE_INTERLEAVE);
		TIFFImage tiff = TiffReader.readTiff(file);

		TiffTestUtils.compareTIFFImages(strippedTiff, tiff);

	}

	/**
	 * Test the stripped TIFF file vs the same data as tiled planar
	 * 
	 * @throws IOException
	 *             upon error
	 */
	@Test
	public void testStrippedVsTiledPlanar() throws IOException {

		File strippedFile = TiffTestUtils
				.getTestFile(TiffTestConstants.FILE_STRIPPED);
		TIFFImage strippedTiff = TiffReader.readTiff(strippedFile);

		File file = TiffTestUtils
				.getTestFile(TiffTestConstants.FILE_TILED_PLANAR);
		TIFFImage tiff = TiffReader.readTiff(file);

		TiffTestUtils.compareTIFFImages(strippedTiff, tiff);

	}

	/**
	 * Test the JPEG file header
	 * 
	 * @throws IOException
	 *             upon error
	 */
	@Test
	public void testJPEGHeader() throws IOException {

		File file = TiffTestUtils.getTestFile(TiffTestConstants.FILE_JPEG);
		TIFFImage tiff = TiffReader.readTiff(file);

		TestCase.assertNotNull(tiff);
		TestCase.assertTrue(tiff.getFileDirectories().size() > 0);
		for (int i = 0; i < tiff.getFileDirectories().size(); i++) {
			FileDirectory fileDirectory = tiff.getFileDirectory(i);
			TestCase.assertNotNull(fileDirectory);
			try {
				fileDirectory.readRasters();
				TestCase.fail(
						"JPEG compression was not expected to be implemented");
			} catch (Exception e) {
				// expected
			}

		}

	}

	/**
	 * Test an invalid offset value
	 */
	@Test
	public void testInvalidOffset() {

		String base64Bytes = "TU0AKoAAAAAAAAAAAAAAAQAAKgAAGABNAA==";
		byte[] bytes = java.util.Base64.getDecoder().decode(base64Bytes);
		try {
			TiffReader.readTiff(bytes);
			fail("Unexpected success");
		} catch (TiffException e) {
			// expected
		}

	}

	/**
	 * Test the stripped TIFF file vs the same data as deflate predictor
	 * 
	 * @throws IOException
	 *             upon error
	 */
	@Test
	public void testStrippedVsDeflatePredictor() throws IOException {

		File strippedFile = TiffTestUtils
				.getTestFile(TiffTestConstants.FILE_STRIPPED);
		TIFFImage strippedTiff = TiffReader.readTiff(strippedFile);

		File file = TiffTestUtils
				.getTestFile(TiffTestConstants.FILE_DEFLATE_PREDICTOR);
		TIFFImage tiff = TiffReader.readTiff(file);

		TiffTestUtils.compareTIFFImages(strippedTiff, tiff);

	}

	/**
	 * Test the stripped TIFF file vs the same data as deflate predictor big
	 * strips
	 * 
	 * @throws IOException
	 *             upon error
	 */
	@Test
	public void testStrippedVsDeflatePredictorBigStrips() throws IOException {

		File strippedFile = TiffTestUtils
				.getTestFile(TiffTestConstants.FILE_STRIPPED);
		TIFFImage strippedTiff = TiffReader.readTiff(strippedFile);

		File file = TiffTestUtils.getTestFile(
				TiffTestConstants.FILE_DEFLATE_PREDICTOR_BIG_STRIPS);
		TIFFImage tiff = TiffReader.readTiff(file);

		TiffTestUtils.compareTIFFImages(strippedTiff, tiff);

	}

	/**
	 * Test the stripped TIFF file vs the same data as deflate predictor tiled
	 * 
	 * @throws IOException
	 *             upon error
	 */
	@Test
	public void testStrippedVsDeflatePredictorTiled() throws IOException {

		File strippedFile = TiffTestUtils
				.getTestFile(TiffTestConstants.FILE_STRIPPED);
		TIFFImage strippedTiff = TiffReader.readTiff(strippedFile);

		File file = TiffTestUtils
				.getTestFile(TiffTestConstants.FILE_DEFLATE_PREDICTOR_TILED);
		TIFFImage tiff = TiffReader.readTiff(file);

		TiffTestUtils.compareTIFFImages(strippedTiff, tiff);

	}

	/**
	 * Test the stripped TIFF file vs the same data as LZW predictor
	 * 
	 * @throws IOException
	 *             upon error
	 */
	@Test
	public void testStrippedVsLZWPredictor() throws IOException {

		File strippedFile = TiffTestUtils
				.getTestFile(TiffTestConstants.FILE_STRIPPED);
		TIFFImage strippedTiff = TiffReader.readTiff(strippedFile);

		File file = TiffTestUtils
				.getTestFile(TiffTestConstants.FILE_LZW_PREDICTOR);
		TIFFImage tiff = TiffReader.readTiff(file);

		TiffTestUtils.compareTIFFImages(strippedTiff, tiff);

	}

	/**
	 * Test the stripped TIFF file vs the same data as tiled planar LZW
	 * 
	 * @throws IOException
	 *             upon error
	 */
	@Test
	public void testStrippedVsTiledPlanarLZW() throws IOException {

		File strippedFile = TiffTestUtils
				.getTestFile(TiffTestConstants.FILE_STRIPPED);
		TIFFImage strippedTiff = TiffReader.readTiff(strippedFile);

		File file = TiffTestUtils
				.getTestFile(TiffTestConstants.FILE_TILED_PLANAR_LZW);
		TIFFImage tiff = TiffReader.readTiff(file);

		TiffTestUtils.compareTIFFImages(strippedTiff, tiff);

	}

	/**
	 * Test the float 32 TIFF file vs the same data as LZW predictor floating
	 * point
	 * 
	 * @throws IOException
	 *             upon error
	 */
	@Test
	public void testFloat32VsLZWPredictorFloatingPoint() throws IOException {

		File float32File = TiffTestUtils
				.getTestFile(TiffTestConstants.FILE_FLOAT32);
		TIFFImage float32Tiff = TiffReader.readTiff(float32File);

		File file = TiffTestUtils
				.getTestFile(TiffTestConstants.FILE_LZW_PREDICTOR_FLOATING);
		TIFFImage tiff = TiffReader.readTiff(file);

		TiffTestUtils.compareTIFFImages(float32Tiff, tiff);

	}

}
