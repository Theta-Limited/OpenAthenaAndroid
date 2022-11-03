package mil.nga.tiff;

/**
 * Coordinates of a window over a portion or the entire image coordinates
 * 
 * @author osbornb
 */
public class ImageWindow {

	/**
	 * Min x
	 */
	private int minX;

	/**
	 * Min y
	 */
	private int minY;

	/**
	 * Max x
	 */
	private int maxX;

	/**
	 * Max y
	 */
	private int maxY;

	/**
	 * Constructor
	 * 
	 * @param minX
	 *            min x (inclusive)
	 * @param minY
	 *            min y (inclusive)
	 * @param maxX
	 *            max x (exclusive)
	 * @param maxY
	 *            max y (exclusive)
	 */
	public ImageWindow(int minX, int minY, int maxX, int maxY) {
		this.minX = minX;
		this.minY = minY;
		this.maxX = maxX;
		this.maxY = maxY;
	}

	/**
	 * Constructor for a single coordinate
	 * 
	 * @param x
	 *            x coordinate
	 * @param y
	 *            y coordinate
	 */
	public ImageWindow(int x, int y) {
		this(x, y, x + 1, y + 1);
	}

	/**
	 * Constructor, full image size
	 * 
	 * @param fileDirectory
	 *            file directory
	 */
	public ImageWindow(FileDirectory fileDirectory) {
		this.minX = 0;
		this.minY = 0;
		this.maxX = fileDirectory.getImageWidth().intValue();
		this.maxY = fileDirectory.getImageHeight().intValue();
	}

	/**
	 * Get the min x
	 * 
	 * @return min x
	 */
	public int getMinX() {
		return minX;
	}

	/**
	 * Set the min x
	 * 
	 * @param minX
	 *            min x
	 */
	public void setMinX(int minX) {
		this.minX = minX;
	}

	/**
	 * Get the min y
	 * 
	 * @return min y
	 */
	public int getMinY() {
		return minY;
	}

	/**
	 * Set the min y
	 * 
	 * @param minY
	 *            min y
	 */
	public void setMinY(int minY) {
		this.minY = minY;
	}

	/**
	 * Get the max x
	 * 
	 * @return max x
	 */
	public int getMaxX() {
		return maxX;
	}

	/**
	 * Set the max x
	 * 
	 * @param maxX
	 *            max x
	 */
	public void setMaxX(int maxX) {
		this.maxX = maxX;
	}

	/**
	 * Get the max y
	 * 
	 * @return max y
	 */
	public int getMaxY() {
		return maxY;
	}

	/**
	 * Set the max y
	 * 
	 * @param maxY
	 *            max y
	 */
	public void setMaxY(int maxY) {
		this.maxY = maxY;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "ImageWindow [minX=" + minX + ", minY=" + minY + ", maxX="
				+ maxX + ", maxY=" + maxY + "]";
	}

}
