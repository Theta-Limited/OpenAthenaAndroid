package mil.nga.tiff;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Test;

import junit.framework.TestCase;
import mil.nga.tiff.util.TiffConstants;

/**
 * TIFF Write tests
 * 
 * @author osbornb
 */
public class TiffWriteTest {

	/**
	 * Test writing and reading a stripped TIFF file
	 * 
	 * @throws IOException
	 *             upon error
	 */
	@Test
	public void testWriteStrippedChunky() throws IOException {

		File strippedFile = TiffTestUtils
				.getTestFile(TiffTestConstants.FILE_STRIPPED);
		TIFFImage strippedTiff = TiffReader.readTiff(strippedFile);

		FileDirectory fileDirectory = strippedTiff.getFileDirectory();
		Rasters rasters = fileDirectory.readRasters();
		Rasters rastersInterleaved = fileDirectory.readInterleavedRasters();

		fileDirectory.setWriteRasters(rasters);
		fileDirectory.setCompression(TiffConstants.COMPRESSION_NO);
		fileDirectory.setPlanarConfiguration(
				TiffConstants.PLANAR_CONFIGURATION_CHUNKY);
		int rowsPerStrip = rasters
				.calculateRowsPerStrip(fileDirectory.getPlanarConfiguration());
		fileDirectory.setRowsPerStrip(rowsPerStrip);
		byte[] tiffBytes = TiffWriter.writeTiffToBytes(strippedTiff);

		TIFFImage readTiffImage = TiffReader.readTiff(tiffBytes);
		FileDirectory fileDirectory2 = readTiffImage.getFileDirectory();
		Rasters rasters2 = fileDirectory2.readRasters();
		Rasters rasters2Interleaved = fileDirectory2.readInterleavedRasters();

		TiffTestUtils.compareRastersSampleValues(rasters, rasters2);
		TiffTestUtils.compareRastersInterleaveValues(rastersInterleaved,
				rasters2Interleaved);

	}

	/**
	 * Test writing and reading a stripped TIFF file
	 * 
	 * @throws IOException
	 *             upon error
	 */
	@Test
	public void testWriteStrippedPlanar() throws IOException {

		File strippedFile = TiffTestUtils
				.getTestFile(TiffTestConstants.FILE_STRIPPED);
		TIFFImage strippedTiff = TiffReader.readTiff(strippedFile);

		FileDirectory fileDirectory = strippedTiff.getFileDirectory();
		Rasters rasters = fileDirectory.readRasters();
		Rasters rastersInterleaved = fileDirectory.readInterleavedRasters();

		fileDirectory.setWriteRasters(rasters);
		fileDirectory.setCompression(TiffConstants.COMPRESSION_NO);
		fileDirectory.setPlanarConfiguration(
				TiffConstants.PLANAR_CONFIGURATION_PLANAR);
		int rowsPerStrip = rasters
				.calculateRowsPerStrip(fileDirectory.getPlanarConfiguration());
		fileDirectory.setRowsPerStrip(rowsPerStrip);
		byte[] tiffBytes = TiffWriter.writeTiffToBytes(strippedTiff);

		TIFFImage readTiffImage = TiffReader.readTiff(tiffBytes);
		FileDirectory fileDirectory2 = readTiffImage.getFileDirectory();
		Rasters rasters2 = fileDirectory2.readRasters();
		Rasters rasters2Interleaved = fileDirectory2.readInterleavedRasters();

		TiffTestUtils.compareRastersSampleValues(rasters, rasters2);
		TiffTestUtils.compareRastersInterleaveValues(rastersInterleaved,
				rasters2Interleaved);

	}

	/**
	 * Test writing and reading and custom tiff
	 * 
	 * @throws IOException
	 *             upon error
	 */
	@Test
	public void testWriteCustom() throws IOException {

		int inpWidth = 18;
		int inpHeight = 11;
		int bitsPerSample = 16;
		int samplesPerPixel = 1;
		long xResolution = 254;
		long yResolution = 254;

		int inpPixVals[] = new int[inpHeight * inpWidth];
		for (int y = 0; y < inpHeight; y++) {
			for (int x = 0; x < inpWidth; x++) {
				inpPixVals[y * inpWidth + x] = (int) (Math.random()
						* Math.pow(2.0, bitsPerSample));
			}
		}

		Rasters newRaster = new Rasters(inpWidth, inpHeight, samplesPerPixel,
				bitsPerSample, TiffConstants.SAMPLE_FORMAT_UNSIGNED_INT);
		FileDirectory fileDirs = new FileDirectory();

		int rowsPerStrip = newRaster.calculateRowsPerStrip(
				TiffConstants.PLANAR_CONFIGURATION_CHUNKY);
		fileDirs.setImageWidth(inpWidth);
		fileDirs.setImageHeight(inpHeight);
		fileDirs.setBitsPerSample(bitsPerSample);
		fileDirs.setSamplesPerPixel(samplesPerPixel);
		fileDirs.setSampleFormat(TiffConstants.SAMPLE_FORMAT_UNSIGNED_INT);
		fileDirs.setRowsPerStrip(rowsPerStrip);
		fileDirs.setResolutionUnit(TiffConstants.RESOLUTION_UNIT_INCH);
		fileDirs.setXResolution(xResolution);
		fileDirs.setYResolution(yResolution);
		fileDirs.setPhotometricInterpretation(
				TiffConstants.PHOTOMETRIC_INTERPRETATION_BLACK_IS_ZERO);
		fileDirs.setPlanarConfiguration(
				TiffConstants.PLANAR_CONFIGURATION_CHUNKY);
		fileDirs.setCompression(TiffConstants.COMPRESSION_NO);
		fileDirs.setWriteRasters(newRaster);

		for (int y = 0; y < inpHeight; y++) {
			for (int x = 0; x < inpWidth; x++) {
				newRaster.setFirstPixelSample(x, y,
						inpPixVals[y * inpWidth + x]);
			}
		}
		TIFFImage newImage = new TIFFImage();
		newImage.add(fileDirs);

		byte[] tiffBytes = TiffWriter.writeTiffToBytes(newImage);
		TestCase.assertNotNull(tiffBytes);

		TIFFImage image = TiffReader.readTiff(tiffBytes);
		TestCase.assertNotNull(image);

		FileDirectory fileDirectory = image.getFileDirectory();
		TestCase.assertEquals(inpWidth, fileDirectory.getImageWidth());
		TestCase.assertEquals(inpHeight, fileDirectory.getImageHeight());
		List<Integer> bitsPerSamp = fileDirectory.getBitsPerSample();
		TestCase.assertEquals(1, bitsPerSamp.size());
		TestCase.assertEquals(bitsPerSample, bitsPerSamp.get(0).intValue());
		TestCase.assertEquals(samplesPerPixel,
				fileDirectory.getSamplesPerPixel());
		List<Integer> sampleFormat = fileDirectory.getSampleFormat();
		TestCase.assertEquals(1, sampleFormat.size());
		TestCase.assertEquals(TiffConstants.SAMPLE_FORMAT_UNSIGNED_INT,
				sampleFormat.get(0).intValue());
		TestCase.assertEquals(rowsPerStrip, fileDirectory.getRowsPerStrip());
		TestCase.assertEquals(TiffConstants.RESOLUTION_UNIT_INCH,
				fileDirectory.getResolutionUnit().intValue());
		List<Long> xRes = fileDirectory.getXResolution();
		TestCase.assertEquals(2, xRes.size());
		TestCase.assertEquals(xResolution, xRes.get(0).longValue());
		TestCase.assertEquals(1, xRes.get(1).longValue());
		List<Long> yRes = fileDirectory.getYResolution();
		TestCase.assertEquals(2, yRes.size());
		TestCase.assertEquals(yResolution, yRes.get(0).longValue());
		TestCase.assertEquals(1, yRes.get(1).longValue());
		TestCase.assertEquals(
				TiffConstants.PHOTOMETRIC_INTERPRETATION_BLACK_IS_ZERO,
				fileDirectory.getPhotometricInterpretation().intValue());
		TestCase.assertEquals(TiffConstants.PLANAR_CONFIGURATION_CHUNKY,
				fileDirectory.getPlanarConfiguration().intValue());
		TestCase.assertEquals(TiffConstants.COMPRESSION_NO,
				fileDirectory.getCompression().intValue());

		Rasters rasters = fileDirectory.readRasters();
		TestCase.assertEquals(inpWidth * inpHeight, rasters.getNumPixels());
		TestCase.assertEquals(inpWidth, rasters.getWidth());
		TestCase.assertEquals(inpHeight, rasters.getHeight());
		TestCase.assertEquals(samplesPerPixel, rasters.getSamplesPerPixel());
		List<Integer> bps = rasters.getBitsPerSample();
		TestCase.assertEquals(1, bps.size());
		TestCase.assertEquals(bitsPerSample, bps.get(0).intValue());
		List<Integer> sf = rasters.getSampleFormat();
		TestCase.assertEquals(1, sf.size());
		TestCase.assertEquals(TiffConstants.SAMPLE_FORMAT_UNSIGNED_INT,
				sf.get(0).intValue());
		FieldType[] fieldTypes = rasters.getFieldTypes();
		TestCase.assertEquals(1, fieldTypes.length);
		TestCase.assertEquals(FieldType.SHORT, fieldTypes[0]);

		for (int y = 0; y < inpHeight; y++) {
			for (int x = 0; x < inpWidth; x++) {
				TestCase.assertEquals(inpPixVals[y * inpWidth + x],
						rasters.getPixelSample(0, x, y));
			}
		}

	}
}
