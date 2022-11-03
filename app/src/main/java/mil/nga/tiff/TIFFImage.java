package mil.nga.tiff;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mil.nga.tiff.util.TiffConstants;

/**
 * TIFF Image containing the File Directories
 * 
 * @author osbornb
 */
public class TIFFImage {

	/**
	 * File directories
	 */
	private final List<FileDirectory> fileDirectories = new ArrayList<>();

	/**
	 * Constructor
	 */
	public TIFFImage() {

	}

	/**
	 * Constructor, single file directory
	 * 
	 * @param fileDirectory
	 *            file directory
	 */
	public TIFFImage(FileDirectory fileDirectory) {
		this.fileDirectories.add(fileDirectory);
	}

	/**
	 * Constructor, multiple file directories
	 * 
	 * @param fileDirectories
	 *            file directories
	 */
	public TIFFImage(List<FileDirectory> fileDirectories) {
		this.fileDirectories.addAll(fileDirectories);
	}

	/**
	 * Add a file directory
	 * 
	 * @param fileDirectory
	 *            file directory
	 */
	public void add(FileDirectory fileDirectory) {
		fileDirectories.add(fileDirectory);
	}

	/**
	 * Get the file directories
	 * 
	 * @return file directories
	 */
	public List<FileDirectory> getFileDirectories() {
		return Collections.unmodifiableList(fileDirectories);
	}

	/**
	 * Get the default, first, or only file directory
	 * 
	 * @return file directory
	 */
	public FileDirectory getFileDirectory() {
		return getFileDirectory(0);
	}

	/**
	 * Get the file directory at the index
	 * 
	 * @param index
	 *            index
	 * @return file directory
	 */
	public FileDirectory getFileDirectory(int index) {
		return fileDirectories.get(index);
	}

	/**
	 * Size in bytes of the TIFF header and file directories with their entries
	 * 
	 * @return size in bytes
	 */
	public long sizeHeaderAndDirectories() {
		long size = TiffConstants.HEADER_BYTES;
		for (FileDirectory directory : fileDirectories) {
			size += directory.size();
		}
		return size;
	}

	/**
	 * Size in bytes of the TIFF header and file directories with their entries
	 * and entry values
	 * 
	 * @return size in bytes
	 */
	public long sizeHeaderAndDirectoriesWithValues() {
		long size = TiffConstants.HEADER_BYTES;
		for (FileDirectory directory : fileDirectories) {
			size += directory.sizeWithValues();
		}
		return size;
	}

}
