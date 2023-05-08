package mil.nga.tiff;

import java.io.Serializable;

import mil.nga.tiff.util.TiffConstants;
import mil.nga.tiff.util.TiffException;

/**
 * Field Types
 * 
 * @author osbornb
 */
public enum FieldType implements Serializable {

	/**
	 * 8-bit unsigned integer
	 */
	BYTE(1),

	/**
	 * 8-bit byte that contains a 7-bit ASCII code; the last byte must be NUL
	 * (binary zero)
	 */
	ASCII(1),

	/**
	 * 16-bit (2-byte) unsigned integer
	 */
	SHORT(2),

	/**
	 * 32-bit (4-byte) unsigned integer
	 */
	LONG(4),

	/**
	 * Two LONGs: the first represents the numerator of a fraction; the second,
	 * the denominator
	 */
	RATIONAL(8),

	/**
	 * An 8-bit signed (twos-complement) integer
	 */
	SBYTE(1),

	/**
	 * An 8-bit byte that may contain anything, depending on the definition of
	 * the field
	 */
	UNDEFINED(1),

	/**
	 * A 16-bit (2-byte) signed (twos-complement) integer
	 */
	SSHORT(2),

	/**
	 * A 32-bit (4-byte) signed (twos-complement) integer
	 */
	SLONG(4),

	/**
	 * Two SLONG’s: the first represents the numerator of a fraction, the second
	 * the denominator
	 */
	SRATIONAL(8),

	/**
	 * Single precision (4-byte) IEEE format
	 */
	FLOAT(4),

	/**
	 * Double precision (8-byte) IEEE format
	 */
	DOUBLE(8);

	/**
	 * Number of bytes per field value
	 */
	private final int bytes;

	/**
	 * Constructor
	 * 
	 * @param bytes
	 *            bytes per value
	 */
	private FieldType(int bytes) {
		this.bytes = bytes;
	}

	/**
	 * Get the field type value
	 * 
	 * @return field type value
	 */
	public int getValue() {
		return ordinal() + 1;
	}

	/**
	 * Get the number of bytes per value
	 * 
	 * @return number of bytes
	 */
	public int getBytes() {
		return bytes;
	}

	/**
	 * Get the number of bits per value
	 * 
	 * @return number of bits
	 * @since 2.0.0
	 */
	public int getBits() {
		return bytes * 8;
	}

	/**
	 * Get the field type
	 * 
	 * @param fieldType
	 *            field type number
	 * @return field type
	 */
	public static FieldType getFieldType(int fieldType) {
		return FieldType.values()[fieldType - 1];
	}

	/**
	 * Get the field type of the sample format and bits per sample
	 * 
	 * @param sampleFormat
	 *            sample format
	 * @param bitsPerSample
	 *            bits per sample
	 * @return field type
	 * @since 2.0.0
	 */
	public static FieldType getFieldType(int sampleFormat, int bitsPerSample) {

		FieldType fieldType = null;

		switch (sampleFormat) {
		case TiffConstants.SAMPLE_FORMAT_UNSIGNED_INT:
			switch (bitsPerSample) {
			case 8:
				fieldType = FieldType.BYTE;
				break;
			case 16:
				fieldType = FieldType.SHORT;
				break;
			case 32:
				fieldType = FieldType.LONG;
				break;
			}
			break;
		case TiffConstants.SAMPLE_FORMAT_SIGNED_INT:
			switch (bitsPerSample) {
			case 8:
				fieldType = FieldType.SBYTE;
				break;
			case 16:
				fieldType = FieldType.SSHORT;
				break;
			case 32:
				fieldType = FieldType.SLONG;
				break;
			}
			break;
		case TiffConstants.SAMPLE_FORMAT_FLOAT:
			switch (bitsPerSample) {
			case 32:
				fieldType = FieldType.FLOAT;
				break;
			case 64:
				fieldType = FieldType.DOUBLE;
				break;
			}
			break;
		}

		if (fieldType == null) {
			throw new TiffException(
					"Unsupported field type for sample format: " + sampleFormat
							+ ", bits per sample: " + bitsPerSample);
		}

		return fieldType;
	}

	/**
	 * Get the sample format of the field type
	 * 
	 * @param fieldType
	 *            field type
	 * @return sample format
	 * @since 2.0.0
	 */
	public static int getSampleFormat(FieldType fieldType) {

		int sampleFormat;

		switch (fieldType) {
		case BYTE:
		case SHORT:
		case LONG:
			sampleFormat = TiffConstants.SAMPLE_FORMAT_UNSIGNED_INT;
			break;
		case SBYTE:
		case SSHORT:
		case SLONG:
			sampleFormat = TiffConstants.SAMPLE_FORMAT_SIGNED_INT;
			break;
		case FLOAT:
		case DOUBLE:
			sampleFormat = TiffConstants.SAMPLE_FORMAT_FLOAT;
			break;
		default:
			throw new TiffException(
					"Unsupported sample format for field type: " + fieldType);
		}

		return sampleFormat;
	}

}
