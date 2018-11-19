package com.kodgemisi.cigdem.databasebasedmessagesource;

/**
 * <p>You should provide an implementation of this interface as an {@link javax.persistence.Entity} when using {@link JpaBundleContentLoaderStrategy}.</p>
 *
 * <p>The implementation class should satisfy following conditions:</p>
 *
 * <ul>
 *   <li>Should have {@link javax.persistence.Entity} annotation.</li>
 *   <li>
 *     Should have unique key constraint for {@code "name", "key", "language", "country", "variant"} columns.
 *     <ul>
 *       <li>You can achieve that by using: {@code @Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "name", "key", "language", "country", "variant" }) })}</li>
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
public interface BundleEntity {

	String getKey();

	String getValue();

	String getName();

	String getLanguage();

	String getCountry();

	String getVariant();

	Long getLastModified();
}