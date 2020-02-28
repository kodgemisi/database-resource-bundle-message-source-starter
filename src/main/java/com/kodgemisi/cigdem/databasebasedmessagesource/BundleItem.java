package com.kodgemisi.cigdem.databasebasedmessagesource;

import java.io.Serializable;
import java.util.Comparator;

/**
 * <p>You should provide an implementation of this interface as an {@link javax.persistence.Entity} when using {@link JpaBundleContentLoaderStrategy}.</p>
 *
 * <p>The implementation class should satisfy following conditions:</p>
 *
 * <ul>
 *   <li>Should have {@link javax.persistence.Entity} annotation.</li>
 *   <li>
 *     Should have unique key constraint for {@code "baseName", "key", "language", "country", "variant"} columns.
 *     <ul>
 *       <li>You can achieve that by using: {@code @Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "baseName", "key", "language", "country", "variant" }) })}</li>
 *     </ul>
 *   </li>
 *   <li>You should not enable second level JPA cache for this entity.</li>
 * </ul>
 *
 * <p>
 *     You can optionally use {@link com.kodgemisi.cigdem.databaseresourcbundle.BundleContentLoaderStrategy#DEFAULT_TABLE_NAME} as table name.
 * 	In which case the database setup would be compatible with {@link com.kodgemisi.cigdem.databaseresourcbundle.DefaultBundleContentLoaderStrategy} as well.
 * </p>
 *
 * Created on January, 2018
 *
 * @author destans
 */
public interface BundleItem extends Comparable<BundleItem> {

	Comparator<String> keyComparator = (s1, s2) -> {
		final int s1DotCount = StringUtil.countMatches(s1, '.');
		final int s2DotCount = StringUtil.countMatches(s2, '.');

		if (s1DotCount == s2DotCount) {
			return s1.compareTo(s2);
		}
		return Integer.compare(s1DotCount, s2DotCount);
	};

	String getKey();

	String getValue();

	String getBaseName();

	String getLanguage();

	String getCountry();

	String getVariant();

	// This returns a boxed long because this interface is supposed to be implemented by a JPA entity among other things.
	Long getLastModified();

	default String getName() {
		final String baseName = getBaseName();
		final String language = StringUtil.getNamePart(getLanguage());
		final String country = StringUtil.getNamePart(getCountry());
		final String variant = StringUtil.getNamePart(getVariant());
		return baseName + language + country + variant;
	}

	default int compareTo(BundleItem bundleItem) {

		if (bundleItem == null) {
			return -1;
		}

		if (this.equals(bundleItem)) {
			return 0;
		}

		//FIXME objects with empty lang, country or variant should have lesser index
		return this.getKey().compareTo(bundleItem.getKey());
	}

	/**
	 * This class is indented to be used in a JPA entity as a composite id with {@code @IdClass(BundleItem.CompositeId.class)}
	 *
	 * <blockquote><pre>
	 * &#64;IdClass(BundleItem.CompositeId.class)
	 * &#64;Table(name = BundleContentLoaderStrategy.DEFAULT_TABLE_NAME)
	 * public class BundleEntity implements BundleItem, Comparable<BundleEntity> {
	 *
	 *     &#64;Id
	 *     &#64;NotBlank
	 *     private String key;
	 *
	 *     &#64;NotNull
	 *     &#64;Type(type = "text")
	 *     private String value;
	 *
	 *     &#64;Id
	 *     &#64;NotBlank
	 *     private String baseName;
	 *
	 *     &#64;Id
	 *     &#64;NotNull
	 *     private String language = "";
	 *
	 *     &#64;Id
	 *     &#64;NotNull
	 *     private String country = "";
	 *
	 *     &#64;Id
	 *     &#64;NotNull
	 *     private String variant = "";
	 *
	 *     &#64;NotNull
	 *     private Long lastModified;
	 *
	 *     // getters, setters and other methods...
	 * }
	 *
	 * </pre></blockquote>
	 *
	 */
	class CompositeId implements Serializable {

		private String key;

		private String baseName;

		private String language;

		private String country;

		private String variant;
	}

}