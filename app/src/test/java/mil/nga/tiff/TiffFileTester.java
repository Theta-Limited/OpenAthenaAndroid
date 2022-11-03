package mil.nga.tiff;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Test reading an argument provided TIFF file
 * 
 * @author osbornb
 */
public class TiffFileTester {

	/**
	 * Main method, provide a single file path argument
	 * 
	 * @param args
	 *            arguments
	 * @throws IOException
	 *             upon error
	 */
	public static void main(String[] args) throws IOException {

		if (args.length != 1) {
			throw new IllegalArgumentException(
					"Provide a single TIFF file path argument");
		}

		File file = new File(args[0]);
		if (!file.exists()) {
			throw new IllegalArgumentException(
					"TIFF file does not exist: " + file.getAbsolutePath());
		}

		TIFFImage tiffImage = TiffReader.readTiff(file);

		System.out.println("TIFF Image: " + file.getName());

		List<FileDirectory> fileDirectories = tiffImage.getFileDirectories();
		for (int i = 0; i < fileDirectories.size(); i++) {
			FileDirectory fileDirectory = fileDirectories.get(i);

			System.out.println();
			System.out.print("-- File Directory ");
			if (fileDirectories.size() > 1) {
				System.out.print((i + 1) + " ");
			}
			System.out.println("--");

			for (FileDirectoryEntry entry : fileDirectory.getEntries()) {

				System.out.println();
				System.out.println(entry.getFieldTag() + " ("
						+ entry.getFieldTag().getId() + ")");
				System.out.println(entry.getFieldType() + " ("
						+ entry.getFieldType().getBytes() + " bytes)");
				System.out.println("Count: " + entry.getTypeCount());
				System.out.println("Values: " + entry.getValues());
			}

			Rasters rasters = fileDirectory.readRasters();
			System.out.println();
			System.out.println("-- Rasters --");
			System.out.println();
			System.out.println("Width: " + rasters.getWidth());
			System.out.println("Height: " + rasters.getHeight());
			System.out.println("Number of Pixels: " + rasters.getNumPixels());
			System.out.println(
					"Samples Per Pixel: " + rasters.getSamplesPerPixel());
			System.out
					.println("Bits Per Sample: " + rasters.getBitsPerSample());

			System.out.println();
			printPixel(rasters, 0, 0);
			printPixel(rasters, (int) (rasters.getWidth() / 2.0),
					(int) (rasters.getHeight() / 2.0));
			printPixel(rasters, rasters.getWidth() - 1,
					rasters.getHeight() - 1);

			System.out.println();
		}

	}

	/**
	 * Print a pixel from the rasters
	 * 
	 * @param rasters
	 *            rasters
	 * @param x
	 *            x coordinate
	 * @param y
	 *            y coordinate
	 */
	private static void printPixel(Rasters rasters, int x, int y) {
		System.out.print("Pixel x = " + x + ", y = " + y + ": [");
		Number[] pixel = rasters.getPixel(x, y);
		for (int i = 0; i < pixel.length; i++) {
			if (i > 0) {
				System.out.print(", ");
			}
			System.out.print(pixel[i]);
		}
		System.out.println("]");
	}

}
