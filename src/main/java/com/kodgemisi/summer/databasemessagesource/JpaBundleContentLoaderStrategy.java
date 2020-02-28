package com.kodgemisi.summer.databasemessagesource;

import com.kodgemisi.summer.databaseresourcbundle.BundleContentLoaderStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.*;
import java.util.stream.Collectors;

/**
 * You should provide an entity which implements {@link BundleItem} when using this class as
 * {@link com.kodgemisi.summer.databaseresourcbundle.BundleContentLoaderStrategy} for {@link com.kodgemisi.summer.databaseresourcbundle.DatabaseResourceBundle}.
 *
 * Created on June, 2018
 *
 * @see BundleItem
 * @author destan
 */
@Slf4j
@RequiredArgsConstructor
public class JpaBundleContentLoaderStrategy<E extends BundleItem> implements BundleContentLoaderStrategy {

	private final EntityManager entityManager;

	private final Class<E> clazz;

	@Override
	public Map<String, Object> loadFromDatabase(String bundleName) {
		final BundleMetaData bundleMetaData = new BundleMetaData(bundleName);

		try {
			final CriteriaBuilder builder = entityManager.getCriteriaBuilder();
			final CriteriaQuery<Tuple> query = builder.createQuery(Tuple.class);
			final Root<E> root = query.from(clazz);

			query.select(builder.construct(Tuple.class, root.get("key"), root.get("value")));

			Predicate predicate = builder.equal(root.get("language"), bundleMetaData.language);
			predicate = builder.and(predicate, builder.equal(root.get("country"), bundleMetaData.country));
			predicate = builder.and(predicate, builder.equal(root.get("baseName"), bundleMetaData.basename));
			predicate = builder.and(predicate, builder.equal(root.get("variant"), bundleMetaData.variant));

			query.distinct(true);
			query.where(predicate);

			final TypedQuery<Tuple> typedQuery = entityManager.createQuery(query);

			final List<Tuple> resources = typedQuery.getResultList();
			return resources.parallelStream().collect(Collectors.toMap(t -> (String) t.get(0), t -> t.get(1)));
		}
		catch (Exception e) {
			// don't let host application crash just because of a database error while getting messages, that would be very unpleasant!
			log.error(e.getMessage(), e);
		}

		return Collections.emptyMap();
	}

	@Override
	public boolean needsReload(String baseName, Locale locale, String format, ResourceBundle bundle, long loadTime) {
		// Note that this method is only called for expired bundles
		try {
			final CriteriaBuilder builder = entityManager.getCriteriaBuilder();
			final CriteriaQuery<Long> query = builder.createQuery(Long.class);
			final Root<E> root = query.from(clazz);

			query.select(builder.count(root.get("lastModified")));
			query.where(builder.greaterThan(root.get("lastModified"), loadTime));

			final long count = entityManager.createQuery(query).getSingleResult();
			return count > 0;
		}
		catch (Exception e) {
			// don't let host application crash just because of a database error while getting messages, that would be very unpleasant!
			log.error(e.getMessage(), e);
			return true;
		}
	}

}
