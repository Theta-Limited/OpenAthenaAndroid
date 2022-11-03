package mil.nga.tiff;

import mil.nga.tiff.util.TiffConstants;

/**
 * TIFF File Directory Entry
 * 
 * @author osbornb
 */
public class FileDirectoryEntry implements Comparable<FileDirectoryEntry> {

	/**
	 * Field Tag Type
	 */
	private final FieldTagType fieldTag;

	/**
	 * Field Type
	 */
	private final FieldType fieldType;

	/**
	 * Type Count
	 */
	private final long typeCount;

	/**
	 * Values
	 */
	private final Object values;

	/**
	 * Constructor
	 * 
	 * @param fieldTag
	 *            field tag type
	 * @param fieldType
	 *            field type
	 * @param typeCount
	 *            type count
	 * @param values
	 *            values
	 */
	public FileDirectoryEntry(FieldTagType fieldTag, FieldType fieldType,
			long typeCount, Object values) {
		this.fieldTag = fieldTag;
		this.fieldType = fieldType;
		this.typeCount = typeCount;
		this.values = values;
	}

	/**
	 * Get the field tag type
	 * 
	 * @return field tag type
	 */
	public FieldTagType getFieldTag() {
		return fieldTag;
	}

	/**
	 * Get the field type
	 * 
	 * @return field type
	 */
	public FieldType getFieldType() {
		return fieldType;
	}

	/**
	 * Get the type count
	 * 
	 * @return type count
	 */
	public long getTypeCount() {
		return typeCount;
	}

	/**
	 * Get the values
	 * 
	 * @return values
	 */
	public Object getValues() {
		return values;
	}

	/**
	 * Size in bytes of the image file directory entry and its values (not
	 * contiguous bytes)
	 * 
	 * @return size in bytes
	 */
	public long sizeWithValues() {
		long size = TiffConstants.IFD_ENTRY_BYTES + sizeOfValues();
		return size;
	}

	/**
	 * Size of the values not included in the directory entry bytes
	 * 
	 * @return size in bytes
	 */
	public long sizeOfValues() {
		long size = 0;
		long valueBytes = fieldType.getBytes() * typeCount;
		if (valueBytes > 4) {
			size = valueBytes;
		}
		return size;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int compareTo(FileDirectoryEntry other) {
		return fieldTag.getId() - other.getFieldTag().getId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return fieldTag.getId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FileDirectoryEntry other = (FileDirectoryEntry) obj;
		if (fieldTag != other.fieldTag)
			return false;
		return true;
	}

}
