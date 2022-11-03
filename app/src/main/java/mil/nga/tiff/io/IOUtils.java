package mil.nga.tiff.io;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Input / Output utility methods
 * 
 * @author osbornb
 */
public class IOUtils {

	/**
	 * Copy stream buffer chunk size in bytes
	 * 
	 * @since 2.0.3
	 */
	public static int COPY_BUFFER_SIZE = 8192;

	/**
	 * Copy a file to a file location
	 * 
	 * @param copyFrom
	 *            file to copy
	 * @param copyTo
	 *            file to copy to
	 * @throws IOException
	 *             upon failure to copy file
	 */
	public static void copyFile(File copyFrom, File copyTo) throws IOException {

		InputStream from = new FileInputStream(copyFrom);
		OutputStream to = new FileOutputStream(copyTo);

		copyStream(from, to);
	}

	/**
	 * Copy an input stream to a file location
	 * 
	 * @param copyFrom
	 *            stream to copy
	 * @param copyTo
	 *            file to copy to
	 * @throws IOException
	 *             upon failure to copy the stream
	 */
	public static void copyStream(InputStream copyFrom, File copyTo)
			throws IOException {

		OutputStream to = new FileOutputStream(copyTo);

		copyStream(copyFrom, to);
	}

	/**
	 * Get the file bytes
	 * 
	 * @param file
	 *            file
	 * @return bytes
	 * @throws IOException
	 *             upon failure to read the file
	 */
	public static byte[] fileBytes(File file) throws IOException {

		FileInputStream fis = new FileInputStream(file);

		return streamBytes(fis);
	}

	/**
	 * Get the stream bytes
	 * 
	 * @param stream
	 *            input stream
	 * @throws IOException
	 *             upon failure to read stream bytes
	 * @return bytes
	 */
	public static byte[] streamBytes(InputStream stream) throws IOException {

		ByteArrayOutputStream bytes = new ByteArrayOutputStream();

		copyStream(stream, bytes);

		return bytes.toByteArray();
	}

	/**
	 * Copy an input stream to an output stream
	 * 
	 * @param copyFrom
	 *            stream to copy
	 * @param copyTo
	 *            stream to copy to
	 * @throws IOException
	 *             upon failure to copy stream
	 */
	public static void copyStream(InputStream copyFrom, OutputStream copyTo)
			throws IOException {

		byte[] buffer = new byte[COPY_BUFFER_SIZE];
		int length;
		while ((length = copyFrom.read(buffer)) > 0) {
			copyTo.write(buffer, 0, length);
		}

		copyTo.flush();
		copyTo.close();
		copyFrom.close();
	}

}
