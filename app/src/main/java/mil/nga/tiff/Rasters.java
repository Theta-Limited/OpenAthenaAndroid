package mil.nga.tiff;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mil.nga.tiff.util.TiffConstants;
import mil.nga.tiff.util.TiffException;

/**
 * Raster image values
 * 
 * @author osbornb
 */
public class Rasters implements Serializable {
	/**
	 * serialVersionUID for serialization compatibility
	 */
	private static final long serialVersionUID = 2000L;

	/**
	 * Values separated by sample
	 */
	private transient ByteBuffer[] sampleValues;

	/**
	 * Interleaved pixel sample values
	 */
	private transient ByteBuffer interleaveValues;

	/**
	 * Width of pixels
	 */
	private final int width;

	/**
	 * Height of pixels
	 */
	private final int height;

	/**
	 * Field type for each sample
	 */
	private final FieldType[] fieldTypes;

	// Calculated values

	/**
	 * Calculated pixel size in bytes
	 */
	private Integer pixelSize;

	/**
	 * @see #getBitsPerSample()
	 */
	private List<Integer> bitsPerSample;

	/**
	 * @see #getSampleFormat()
	 */
	private List<Integer> sampleFormat;

	/**
	 * Constructor
	 * 
	 * @param width
	 *            width of pixels
	 * @param height
	 *            height of pixels
	 * @param fieldTypes
	 *            field type for each sample
	 * @param sampleValues
	 *            empty sample values buffer array
	 * @since 2.0.0
	 */
	public Rasters(int width, int height, FieldType[] fieldTypes,
			ByteBuffer[] sampleValues) {
		this(width, height, fieldTypes, sampleValues, null);
	}

	/**
	 * Constructor
	 * 
	 * @param width
	 *            width of pixels
	 * @param height
	 *            height of pixels
	 * @param fieldTypes
	 *            field type for each sample
	 * @param interleaveValues
	 *            empty interleaved values buffer
	 * @since 2.0.0
	 */
	public Rasters(int width, int height, FieldType[] fieldTypes,
			ByteBuffer interleaveValues) {
		this(width, height, fieldTypes, null, interleaveValues);
	}

	/**
	 * Constructor
	 * 
	 * @param width
	 *            width of pixels
	 * @param height
	 *            height of pixels
	 * @param fieldTypes
	 *            Field type for each sample
	 * @param sampleValues
	 *            empty sample values buffer array
	 * @param interleaveValues
	 *            empty interleaved values buffer
	 * @since 2.0.0
	 */
	public Rasters(int width, int height, FieldType[] fieldTypes,
			ByteBuffer[] sampleValues, ByteBuffer interleaveValues) {
		this.width = width;
		this.height = height;
		this.fieldTypes = fieldTypes;
		this.sampleValues = sampleValues;
		this.interleaveValues = interleaveValues;
		validateValues();
	}

	/**
	 * Constructor
	 *
	 * Creates Rasters object where given field type used for each sample.
	 *
	 * @param width
	 *            width of pixels
	 * @param height
	 *            height of pixels
	 * @param samplesPerPixel
	 *            number of samples per pixel
	 * @param fieldType
	 *            type of field for each sample
	 * @since 2.0.0
	 */
	public Rasters(int width, int height, int samplesPerPixel,
			FieldType fieldType) {
		this(width, height, createFieldTypeArray(samplesPerPixel, fieldType));
	}

	/**
	 * Constructor
	 *
	 * Creates Rasters object where given field type used for each sample.
	 *
	 * @param width
	 *            width of pixels
	 * @param height
	 *            height of pixels
	 * @param samplesPerPixel
	 *            number of samples per pixel
	 * @param fieldType
	 *            type of field for each sample
	 * @param order
	 *            byte order
	 * @since 2.0.0
	 */
	public Rasters(int width, int height, int samplesPerPixel,
			FieldType fieldType, ByteOrder order) {
		this(width, height, createFieldTypeArray(samplesPerPixel, fieldType),
				order);
	}

	/**
	 * Constructor
	 * 
	 * Creates Rasters object where one bits per sample and sample format is
	 * provided for each sample
	 * 
	 * @param width
	 *            width of pixels
	 * @param height
	 *            height of pixels
	 * @param bitsPerSamples
	 *            bits per samples
	 * @param sampleFormats
	 *            sample formats
	 * @since 2.0.0
	 */
	public Rasters(int width, int height, int[] bitsPerSamples,
			int[] sampleFormats) {
		this(width, height, createFieldTypeArray(bitsPerSamples, sampleFormats));
	}

	/**
	 * Constructor
	 * 
	 * Creates Rasters object where one bits per sample and sample format is
	 * provided for each sample
	 * 
	 * @param width
	 *            width of pixels
	 * @param height
	 *            height of pixels
	 * @param bitsPerSamples
	 *            bits per samples
	 * @param sampleFormats
	 *            sample formats
	 * @param order
	 *            byte order
	 * @since 2.0.0
	 */
	public Rasters(int width, int height, int[] bitsPerSamples,
			int[] sampleFormats, ByteOrder order) {
		this(width, height,
				createFieldTypeArray(bitsPerSamples, sampleFormats), order);
	}

	/**
	 * Constructor
	 * 
	 * Creates Rasters object where given bits per sample and sample format is
	 * used for each sample
	 * 
	 * @param width
	 *            width of pixels
	 * @param height
	 *            height of pixels
	 * @param samplesPerPixel
	 *            number of samples per pixel
	 * @param bitsPerSample
	 *            bits per each sample
	 * @param sampleFormat
	 *            format for each sample
	 * @since 2.0.0
	 */
	public Rasters(int width, int height, int samplesPerPixel,
			int bitsPerSample, int sampleFormat) {
		this(width, height, samplesPerPixel, FieldType.getFieldType(
				sampleFormat, bitsPerSample));
	}

	/**
	 * Constructor
	 * 
	 * Creates Rasters object where given bits per sample and sample format is
	 * used for each sample
	 * 
	 * @param width
	 *            width of pixels
	 * @param height
	 *            height of pixels
	 * @param samplesPerPixel
	 *            number of samples per pixel
	 * @param bitsPerSample
	 *            bits per each sample
	 * @param sampleFormat
	 *            format for each sample
	 * @param order
	 *            byte order
	 * @since 2.0.0
	 */
	public Rasters(int width, int height, int samplesPerPixel,
			int bitsPerSample, int sampleFormat, ByteOrder order) {
		this(width, height, samplesPerPixel, FieldType.getFieldType(
				sampleFormat, bitsPerSample), order);
	}

	/**
	 * Constructor
	 * 
	 * @param width
	 *            width of pixels
	 * @param height
	 *            height of pixels
	 * @param fieldTypes
	 *            field types per sample
	 * @since 2.0.0
	 */
	public Rasters(int width, int height, FieldType[] fieldTypes) {
		this(width, height, fieldTypes, ByteOrder.nativeOrder());
	}

	/**
	 * Constructor
	 * 
	 * @param width
	 *            width of pixels
	 * @param height
	 *            height of pixels
	 * @param fieldTypes
	 *            field types per sample
	 * @param order
	 *            byte order
	 * @since 2.0.0
	 */
	public Rasters(int width, int height, FieldType[] fieldTypes,
			ByteOrder order) {
		this(width, height, fieldTypes, new ByteBuffer[fieldTypes.length]);
		for (int i = 0; i < sampleValues.length; ++i) {
			sampleValues[i] = ByteBuffer.allocateDirect(
					width * height * fieldTypes[i].getBytes()).order(order);
		}
	}

	/**
	 * Custom Serialization for Rasters Object
	 * @param out the ObjectOutputStream to write serialized data to
	 * @throws IOException if cannot complete write operation
	 */
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject(); // Serialize all non-transient fields

		// Serialize ByteBuffer arrays
		for (ByteBuffer bb : sampleValues) {
			if (bb != null) {
				int remaining = bb.remaining();
				out.writeInt(remaining);
				byte[] bytes = new byte[remaining];
				bb.get(bytes);
				out.write(bytes);
			} else {
				out.writeInt(-1);
			}
		}
		if (interleaveValues != null) {
			int remaining = interleaveValues.remaining();
			out.writeInt(remaining);
			byte[] bytes = new byte[remaining];
			interleaveValues.get(bytes);
			out.write(bytes);
		} else {
			out.writeInt(-1);
		}
	}

	/**
	 * Custom De-serialization for Rasters Object
	 * @param in the ObjectInputStream to read from
	 * @throws IOException if cannot complete the read operation
	 * @throws ClassNotFoundException if the UID of the serialized object is mismatched
	 */
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject(); // Deserialize all non-transient fields

		// Deserialize ByteBuffer arrays
		for (int i = 0; i < sampleValues.length; i++) {
			int remaining = in.readInt();
			if (remaining != -1) {
				byte[] bytes = new byte[remaining];
				in.readFully(bytes);
				sampleValues[i] = ByteBuffer.wrap(bytes);
			} else {
				sampleValues[i] = null;
			}
		}
		int remaining = in.readInt();
		if (remaining != -1) {
			byte[] bytes = new byte[remaining];
			in.readFully(bytes);
			interleaveValues = ByteBuffer.wrap(bytes);
		} else {
			interleaveValues = null;
		}
	}


	/**
	 * Validate that either sample or interleave values exist
	 */
	private void validateValues() {
		if (sampleValues == null && interleaveValues == null) {
			throw new TiffException(
					"Results must be sample and/or interleave based");
		}
	}

	/**
	 * Create {@link FieldType} filled array for samples per pixel size
	 * 
	 * @param samplesPerPixel
	 *            number of samples per pixel
	 * @param fieldType
	 *            type of field for each sample
	 * @return field type array
	 */
	private static FieldType[] createFieldTypeArray(int samplesPerPixel,
			FieldType fieldType) {
		FieldType[] result = new FieldType[samplesPerPixel];
		Arrays.fill(result, fieldType);
		return result;
	}

	/**
	 * Create {@link FieldType} array for the bits per samples and sample
	 * formats
	 * 
	 * @param bitsPerSamples
	 *            bits per samples
	 * @param sampleFormats
	 *            sample formats
	 * @return field type array
	 */
	private static FieldType[] createFieldTypeArray(int[] bitsPerSamples,
			int[] sampleFormats) {
		if (bitsPerSamples.length != sampleFormats.length) {
			throw new TiffException(
					"Equal number of bits per samples and sample formats expected. "
							+ "Bits Per Samples: " + bitsPerSamples
							+ ", Sample Formats: " + sampleFormats);
		}
		FieldType[] result = new FieldType[bitsPerSamples.length];
		for (int i = 0; i < bitsPerSamples.length; i++) {
			result[i] = FieldType.getFieldType(sampleFormats[i],
					bitsPerSamples[i]);
		}
		return result;
	}

	/**
	 * True if the results are stored by samples
	 * 
	 * @return true if results exist
	 */
	public boolean hasSampleValues() {
		return sampleValues != null;
	}

	/**
	 * True if the results are stored interleaved
	 * 
	 * @return true if results exist
	 */
	public boolean hasInterleaveValues() {
		return interleaveValues != null;
	}

	/**
	 * Updates sample to given value in buffer.
	 *
	 * @param buffer
	 *            A buffer to be updated.
	 * @param bufferIndex
	 *            Position in buffer where to update.
	 * @param sampleIndex
	 *            Sample index in sampleFieldTypes. Needed for determining
	 *            sample size.
	 * @param value
	 *            A Number value to be put in buffer. Has to be same size as
	 *            sampleFieldTypes[sampleIndex].
	 */
	private void updateSampleInByteBuffer(ByteBuffer buffer, int bufferIndex,
			int sampleIndex, Number value) {
		if (bufferIndex < 0 || bufferIndex >= buffer.capacity()) {
			throw new IndexOutOfBoundsException("index: " + bufferIndex
					+ ". Buffer capacity: " + buffer.capacity());
		}

		buffer.position(bufferIndex);
		writeSample(buffer, fieldTypes[sampleIndex], value);
	}

	/**
	 * Reads sample from given buffer.
	 *
	 * @param buffer
	 *            A buffer to read from
	 * @param index
	 *            Position in buffer where to read from
	 * @param sampleIndex
	 *            Index of sample type to read
	 * @return Number read from buffer
	 */
	private Number getSampleFromByteBuffer(ByteBuffer buffer, int index,
			int sampleIndex) {

		if (index < 0 || index >= buffer.capacity()) {
			throw new IndexOutOfBoundsException("Requested index: " + index
					+ ", but size of buffer is: " + buffer.capacity());
		}

		buffer.position(index);
		return readSample(buffer, fieldTypes[sampleIndex]);
	}

	/**
	 * Add a value to the sample results
	 * 
	 * @param sampleIndex
	 *            sample index
	 * @param coordinate
	 *            coordinate location
	 * @param value
	 *            value
	 */
	public void addToSample(int sampleIndex, int coordinate, Number value) {
		updateSampleInByteBuffer(sampleValues[sampleIndex], coordinate
				* fieldTypes[sampleIndex].getBytes(), sampleIndex, value);
	}

	/**
	 * Add a value to the interleaved results
	 * 
	 * @param sampleIndex
	 *            sample index
	 * @param coordinate
	 *            coordinate location
	 * @param value
	 *            value
	 * @since 2.0.0
	 */
	public void addToInterleave(int sampleIndex, int coordinate, Number value) {
		int bufferPos = coordinate * sizePixel();
		for (int i = 0; i < sampleIndex; ++i) {
			bufferPos += fieldTypes[i].getBytes();
		}

		updateSampleInByteBuffer(interleaveValues, bufferPos, sampleIndex,
				value);
	}

	/**
	 * Get the width of pixels
	 * 
	 * @return width
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Get the height of pixels
	 * 
	 * @return height
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * Return the number of pixels
	 * 
	 * @return number of pixels
	 */
	public int getNumPixels() {
		return width * height;
	}

	/**
	 * Get the number of samples per pixel
	 * 
	 * @return samples per pixel
	 */
	public int getSamplesPerPixel() {
		return fieldTypes.length;
	}

	/**
	 * Get the bits per sample
	 * 
	 * @return bits per sample
	 */
	public List<Integer> getBitsPerSample() {
		List<Integer> result = bitsPerSample;
		if (result == null) {
			result = new ArrayList<>(fieldTypes.length);
			for (FieldType fieldType : fieldTypes) {
				result.add(fieldType.getBits());
			}
			bitsPerSample = result;
		}
		return result;
	}

	/**
	 * Returns list of sample types constants
	 *
	 * Returns list of sample types constants (SAMPLE_FORMAT_UNSIGNED_INT,
	 * SAMPLE_FORMAT_SIGNED_INT or SAMPLE_FORMAT_FLOAT) for each sample in
	 * sample list @see getFieldTypes(). @see {@link TiffConstants}
	 * 
	 * @return list of sample type constants
	 * @since 2.0.0
	 */
	public List<Integer> getSampleFormat() {
		List<Integer> result = sampleFormat;
		if (result == null) {
			result = new ArrayList<>(fieldTypes.length);
			for (FieldType fieldType : fieldTypes) {
				result.add(FieldType.getSampleFormat(fieldType));
			}
			sampleFormat = result;
		}
		return result;
	}

	/**
	 * Get the results stored by samples
	 * 
	 * @return sample values
	 * @since 2.0.0
	 */
	public ByteBuffer[] getSampleValues() {
		for (int i = 0; i < sampleValues.length; ++i) {
			sampleValues[i].rewind();
		}
		return sampleValues;
	}

	/**
	 * Set the results stored by samples
	 * 
	 * @param sampleValues
	 *            sample values
	 * @since 2.0.0
	 */
	public void setSampleValues(ByteBuffer[] sampleValues) {
		this.sampleValues = sampleValues;
		this.sampleFormat = null;
		this.bitsPerSample = null;
		this.pixelSize = null;
		validateValues();
	}

	/**
	 * Get the results stored as interleaved pixel samples
	 * 
	 * @return interleaved values
	 * @since 2.0.0
	 */
	public ByteBuffer getInterleaveValues() {
		interleaveValues.rewind();
		return interleaveValues;
	}

	/**
	 * Set the results stored as interleaved pixel samples
	 * 
	 * @param interleaveValues
	 *            interleaved values
	 * @since 2.0.0
	 */
	public void setInterleaveValues(ByteBuffer interleaveValues) {
		this.interleaveValues = interleaveValues;
		validateValues();
	}

	/**
	 * Get the pixel sample values
	 * 
	 * @param x
	 *            x coordinate (&gt;= 0 &amp;&amp; &lt; {@link #getWidth()})
	 * @param y
	 *            y coordinate (&gt;= 0 &amp;&amp; &lt; {@link #getHeight()})
	 * @return pixel sample values
	 */
	public Number[] getPixel(int x, int y) {

		validateCoordinates(x, y);

		// Pixel with each sample value
		Number[] pixel = new Number[getSamplesPerPixel()];

		// Get the pixel values from each sample
		if (sampleValues != null) {
			int sampleIndex = getSampleIndex(x, y);
			for (int i = 0; i < getSamplesPerPixel(); i++) {
				int bufferIndex = sampleIndex * fieldTypes[i].getBytes();
				pixel[i] = getSampleFromByteBuffer(sampleValues[i],
						bufferIndex, i);
			}
		} else {
			int interleaveIndex = getInterleaveIndex(x, y);
			for (int i = 0; i < getSamplesPerPixel(); i++) {
				pixel[i] = getSampleFromByteBuffer(interleaveValues,
						interleaveIndex, i);
				interleaveIndex += fieldTypes[i].getBytes();
			}
		}

		return pixel;
	}

	/**
	 * Set the pixel sample values
	 * 
	 * @param x
	 *            x coordinate (&gt;= 0 &amp;&amp; &lt; {@link #getWidth()})
	 * @param y
	 *            y coordinate (&gt;= 0 &amp;&amp; &lt; {@link #getHeight()})
	 * @param values
	 *            pixel values
	 */
	public void setPixel(int x, int y, Number[] values) {

		validateCoordinates(x, y);
		validateSample(values.length + 1);

		// Set the pixel values from each sample
		if (sampleValues != null) {
			for (int i = 0; i < getSamplesPerPixel(); i++) {
				int bufferIndex = getSampleIndex(x, y)
						* fieldTypes[i].getBytes();
				updateSampleInByteBuffer(sampleValues[i], bufferIndex, i,
						values[i]);
			}
		} else {
			int interleaveIndex = getSampleIndex(x, y) * sizePixel();
			for (int i = 0; i < getSamplesPerPixel(); i++) {
				updateSampleInByteBuffer(interleaveValues, interleaveIndex, i,
						values[i]);
				interleaveIndex += fieldTypes[i].getBytes();
			}
		}
	}

	/**
	 * Returns byte array of pixel row.
	 *
	 * @param y
	 *            Row index
	 * @param newOrder
	 *            Desired byte order of result byte array
	 * @return Byte array of pixel row
	 * @since 2.0.0
	 */
	public byte[] getPixelRow(int y, ByteOrder newOrder) {
		ByteBuffer outBuffer = ByteBuffer.allocate(getWidth() * sizePixel());
		outBuffer.order(newOrder);

		if (sampleValues != null) {
			for (int i = 0; i < getSamplesPerPixel(); ++i) {
				sampleValues[i].position(y * getWidth()
						* fieldTypes[i].getBytes());
			}
			for (int i = 0; i < getWidth(); ++i) {
				for (int j = 0; j < getSamplesPerPixel(); ++j) {
					writeSample(outBuffer, sampleValues[j], fieldTypes[j]);
				}
			}
		} else {
			interleaveValues.position(y * getWidth() * sizePixel());

			for (int i = 0; i < getWidth(); ++i) {
				for (int j = 0; j < getSamplesPerPixel(); ++j) {
					writeSample(outBuffer, interleaveValues, fieldTypes[j]);
				}
			}
		}

		return outBuffer.array();
	}

	/**
	 * Returns byte array of sample row.
	 *
	 * @param y
	 *            Row index
	 * @param sample
	 *            Sample index
	 * @param newOrder
	 *            Desired byte order of resulting byte array
	 * @return Byte array of sample row
	 * @since 2.0.0
	 */
	public byte[] getSampleRow(int y, int sample, ByteOrder newOrder) {
		ByteBuffer outBuffer = ByteBuffer.allocate(getWidth()
				* fieldTypes[sample].getBytes());
		outBuffer.order(newOrder);

		if (sampleValues != null) {
			sampleValues[sample].position(y * getWidth()
					* fieldTypes[sample].getBytes());
			for (int x = 0; x < getWidth(); ++x) {
				writeSample(outBuffer, sampleValues[sample], fieldTypes[sample]);
			}
		} else {
			int sampleOffset = 0;
			for (int i = 0; i < sample; ++i) {
				sampleOffset += fieldTypes[sample].getBytes();
			}

			for (int i = 0; i < getWidth(); ++i) {
				interleaveValues.position((y * getWidth() + i) * sizePixel()
						+ sampleOffset);
				writeSample(outBuffer, interleaveValues, fieldTypes[sample]);
			}
		}

		return outBuffer.array();
	}

	/**
	 * Get a pixel sample value
	 * 
	 * @param sample
	 *            sample index (&gt;= 0 &amp;&amp; &lt; {@link #getSamplesPerPixel()})
	 * @param x
	 *            x coordinate (&gt;= 0 &amp;&amp; &lt; {@link #getWidth()})
	 * @param y
	 *            y coordinate (&gt;= 0 &amp;&amp; &lt; {@link #getHeight()})
	 * @return pixel sample
	 */
	public Number getPixelSample(int sample, int x, int y) {

		validateCoordinates(x, y);
		validateSample(sample);

		// Pixel sample value
		Number pixelSample = null;

		// Get the pixel sample
		if (sampleValues != null) {
			int bufferPos = getSampleIndex(x, y)
					* fieldTypes[sample].getBytes();
			pixelSample = getSampleFromByteBuffer(sampleValues[sample],
					bufferPos, sample);
		} else {
			int bufferPos = getInterleaveIndex(x, y);
			for (int i = 0; i < sample; i++) {
				bufferPos += fieldTypes[sample].getBytes();
			}

			pixelSample = getSampleFromByteBuffer(interleaveValues, bufferPos,
					sample);
		}

		return pixelSample;
	}

	/**
	 * Set a pixel sample value
	 * 
	 * @param sample
	 *            sample index (&gt;= 0 &amp;&amp; &lt; {@link #getSamplesPerPixel()})
	 * @param x
	 *            x coordinate (&gt;= 0 &amp;&amp; &lt; {@link #getWidth()})
	 * @param y
	 *            y coordinate (&gt;= 0 &amp;&amp; &lt; {@link #getHeight()})
	 * @param value
	 *            pixel value
	 */
	public void setPixelSample(int sample, int x, int y, Number value) {

		validateCoordinates(x, y);
		validateSample(sample);

		// Set the pixel sample
		if (sampleValues != null) {
			int sampleIndex = getSampleIndex(x, y)
					* fieldTypes[sample].getBytes();
			updateSampleInByteBuffer(sampleValues[sample], sampleIndex, sample,
					value);
		}
		if (interleaveValues != null) {
			int interleaveIndex = getSampleIndex(x, y) * sizePixel();
			for (int i = 0; i < sample; ++i) {
				interleaveIndex += fieldTypes[sample].getBytes();
			}
			updateSampleInByteBuffer(interleaveValues, interleaveIndex, sample,
					value);
		}
	}

	/**
	 * Get the first pixel sample value, useful for single sample pixels
	 * (grayscale)
	 * 
	 * @param x
	 *            x coordinate (&gt;= 0 &amp;&amp; &lt; {@link #getWidth()})
	 * @param y
	 *            y coordinate (&gt;= 0 &amp;&amp; &lt; {@link #getHeight()})
	 * @return first pixel sample
	 */
	public Number getFirstPixelSample(int x, int y) {
		return getPixelSample(0, x, y);
	}

	/**
	 * Set the first pixel sample value, useful for single sample pixels
	 * (grayscale)
	 * 
	 * @param x
	 *            x coordinate (&gt;= 0 &amp;&amp; &lt; {@link #getWidth()})
	 * @param y
	 *            y coordinate (&gt;= 0 &amp;&amp; &lt; {@link #getHeight()})
	 * @param value
	 *            pixel value
	 */
	public void setFirstPixelSample(int x, int y, Number value) {
		setPixelSample(0, x, y, value);
	}

	/**
	 * Get the sample index location
	 * 
	 * @param x
	 *            x coordinate
	 * @param y
	 *            y coordinate
	 * @return sample index
	 */
	public int getSampleIndex(int x, int y) {
		return y * width + x;
	}

	/**
	 * Get the interleave index location
	 * 
	 * @param x
	 *            x coordinate
	 * @param y
	 *            y coordinate
	 * @return interleave index
	 */
	public int getInterleaveIndex(int x, int y) {
		return (y * width * sizePixel()) + (x * sizePixel());
	}

	/**
	 * Size in bytes of the image
	 * 
	 * @return bytes
	 */
	public int size() {
		return getNumPixels() * sizePixel();
	}

	/**
	 * Size in bytes of a pixel
	 * 
	 * @return bytes
	 */
	public int sizePixel() {
		if (pixelSize != null) {
			return pixelSize;
		}

		int size = 0;
		for (int i = 0; i < getSamplesPerPixel(); i++) {
			size += fieldTypes[i].getBytes();
		}
		pixelSize = size;
		return size;
	}

	/**
	 * Validate the coordinates range
	 * 
	 * @param x
	 *            x coordinate
	 * @param y
	 *            y coordinate
	 */
	private void validateCoordinates(int x, int y) {
		if (x < 0 || x >= width || y < 0 || y > height) {
			throw new TiffException("Pixel oustide of raster range. Width: "
					+ width + ", Height: " + height + ", x: " + x + ", y: " + y);
		}
	}

	/**
	 * Validate the sample index
	 * 
	 * @param sample
	 *            sample index
	 */
	private void validateSample(int sample) {
		if (sample < 0 || sample >= getSamplesPerPixel()) {
			throw new TiffException("Pixel sample out of bounds. sample: "
					+ sample + ", samples per pixel: " + getSamplesPerPixel());
		}
	}

	/**
	 * Calculate the rows per strip to write
	 * 
	 * @param planarConfiguration
	 *            chunky or planar
	 * @return rows per strip
	 */
	public int calculateRowsPerStrip(int planarConfiguration) {
		return calculateRowsPerStrip(planarConfiguration,
				TiffConstants.DEFAULT_MAX_BYTES_PER_STRIP);
	}

	/**
	 * Calculate the rows per strip to write
	 * 
	 * @param planarConfiguration
	 *            chunky or planar
	 * @param maxBytesPerStrip
	 *            attempted max bytes per strip
	 * @return rows per strip
	 */
	public int calculateRowsPerStrip(int planarConfiguration,
			int maxBytesPerStrip) {

		Integer rowsPerStrip = null;

		if (planarConfiguration == TiffConstants.PLANAR_CONFIGURATION_CHUNKY) {
			rowsPerStrip = rowsPerStrip(sizePixel(), maxBytesPerStrip);
		} else {

			for (int sample = 0; sample < getSamplesPerPixel(); ++sample) {
				int rowsPerStripForSample = rowsPerStrip(
						fieldTypes[sample].getBytes(), maxBytesPerStrip);
				if (rowsPerStrip == null
						|| rowsPerStripForSample < rowsPerStrip) {
					rowsPerStrip = rowsPerStripForSample;
				}
			}
		}

		return rowsPerStrip;
	}

	/**
	 * Get the rows per strip based upon the bits per pixel and max bytes per
	 * strip
	 * 
	 * @param bytesPerPixel
	 *            bytes per pixel
	 * @param maxBytesPerStrip
	 *            max bytes per strip
	 * @return rows per strip
	 */
	private int rowsPerStrip(int bytesPerPixel, int maxBytesPerStrip) {

		int bytesPerRow = bytesPerPixel * width;

		int rowsPerStrip = Math.max(1, maxBytesPerStrip / bytesPerRow);

		return rowsPerStrip;
	}

	/**
	 * Reads sample from given buffer
	 *
	 * @param buffer
	 *            A buffer to read from. @note Make sure position is set.
	 * @param fieldType
	 *            field type to be read
	 * @return Sample from buffer
	 */
	private Number readSample(ByteBuffer buffer, FieldType fieldType) {
		Number sampleValue;

		switch (fieldType) {
		case BYTE:
			sampleValue = (short) (buffer.get() & 0xff);
			break;
		case SHORT:
			sampleValue = buffer.getShort() & 0xffff;
			break;
		case LONG:
			sampleValue = buffer.getInt() & 0xffffffffL;
			break;
		case SBYTE:
			sampleValue = buffer.get();
			break;
		case SSHORT:
			sampleValue = buffer.getShort();
			break;
		case SLONG:
			sampleValue = buffer.getInt();
			break;
		case FLOAT:
			sampleValue = buffer.getFloat();
			break;
		case DOUBLE:
			sampleValue = buffer.getDouble();
			break;
		default:
			throw new TiffException("Unsupported raster field type: "
					+ fieldType);
		}

		return sampleValue;
	}

	/**
	 * Writes sample into given buffer.
	 *
	 * @param buffer
	 *            A buffer to write to. @note Make sure buffer position is set.
	 * @param fieldType
	 *            field type to be written.
	 * @param value
	 *            Actual value to write.
	 */
	private void writeSample(ByteBuffer buffer, FieldType fieldType,
			Number value) {
		switch (fieldType) {
		case BYTE:
		case SBYTE:
			buffer.put(value.byteValue());
			break;
		case SHORT:
		case SSHORT:
			buffer.putShort(value.shortValue());
			break;
		case LONG:
		case SLONG:
			buffer.putInt(value.intValue());
			break;
		case FLOAT:
			buffer.putFloat(value.floatValue());
			break;
		case DOUBLE:
			buffer.putDouble(value.doubleValue());
			break;
		default:
			throw new TiffException("Unsupported raster field type: "
					+ fieldType);
		}
	}

	/**
	 * Writes sample from input buffer to given output buffer.
	 *
	 * @param outBuffer
	 *            A buffer to write to. @note Make sure buffer position is set.
	 * @param inBuffer
	 *            A buffer to read from. @note Make sure buffer position is set.
	 * @param fieldType
	 *            Field type to be read.
	 */
	private void writeSample(ByteBuffer outBuffer, ByteBuffer inBuffer,
			FieldType fieldType) {
		switch (fieldType) {
		case BYTE:
		case SBYTE:
			outBuffer.put(inBuffer.get());
			break;
		case SHORT:
		case SSHORT:
			outBuffer.putShort(inBuffer.getShort());
			break;
		case LONG:
		case SLONG:
			outBuffer.putInt(inBuffer.getInt());
			break;
		case FLOAT:
			outBuffer.putFloat(inBuffer.getFloat());
			break;
		case DOUBLE:
			outBuffer.putDouble(inBuffer.getDouble());
			break;
		default:
			throw new TiffException("Unsupported raster field type: "
					+ fieldType);
		}
	}

	/**
	 * Returns field types
	 * 
	 * @return field types
	 * @since 2.0.0
	 */
	public FieldType[] getFieldTypes() {
		return fieldTypes;
	}

}