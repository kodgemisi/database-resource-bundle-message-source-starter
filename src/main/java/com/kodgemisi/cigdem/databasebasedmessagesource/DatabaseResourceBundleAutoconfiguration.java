package com.kodgemisi.cigdem.databasebasedmessagesource;

import com.kodgemisi.cigdem.databaseresourcbundle.BundleContentLoaderStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.context.MessageSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.context.MessageSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.metamodel.EntityType;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created on May, 2018
 *
 * @author destan
 */
@Slf4j
@Configuration
@AutoConfigureBefore(MessageSourceAutoConfiguration.class)
class DatabaseResourceBundleAutoconfiguration {

	@Bean
	@ConfigurationProperties(prefix = "spring.messages")
	MessageSourceProperties messageSourceProperties() {
		return new MessageSourceProperties();
	}

	@Bean
	@ConditionalOnMissingBean(BundleContentLoaderStrategy.class)
	BundleContentLoaderStrategy bundleContentLoaderStrategy(EntityManager entityManager) throws ClassNotFoundException {
		return new JpaBundleContentLoaderStrategy<>(entityManager, findBundleEntityConcreteClass(entityManager));
	}

	@Bean
	MessageSource messageSource(BundleContentLoaderStrategy bundleContentLoaderStrategy) {
		final MessageSourceProperties properties = messageSourceProperties();
		return createMessageSource(properties, bundleContentLoaderStrategy);
	}

	private MessageSource createMessageSource(MessageSourceProperties properties, BundleContentLoaderStrategy bundleContentLoaderStrategy) {
		final ResourceBundleMessageSource messageSource = new DatabaseResourceBundleMessageSource(bundleContentLoaderStrategy);

		if (StringUtils.hasText(properties.getBasename())) {
			messageSource.setBasenames(StringUtils.commaDelimitedListToStringArray(StringUtils.trimAllWhitespace(properties.getBasename())));
		}

		if (properties.getEncoding() != null) {
			messageSource.setDefaultEncoding(properties.getEncoding().name());
		}

		messageSource.setFallbackToSystemLocale(properties.isFallbackToSystemLocale());
		Duration cacheDuration = properties.getCacheDuration();
		if (cacheDuration != null) {
			long cacheMillis = cacheDuration.toMillis();

			// spring.messages.cache-duration=-1 yields -1s which translates to -1000ms
			// Alternatively we could have forced users to use -1ms which translates to -1 and works fine
			cacheMillis = cacheMillis == -1000 ? -1 : cacheMillis;

			messageSource.setCacheMillis(cacheMillis);
		}

		messageSource.setAlwaysUseMessageFormat(properties.isAlwaysUseMessageFormat());
		messageSource.setUseCodeAsDefaultMessage(properties.isUseCodeAsDefaultMessage());
		return messageSource;
	}

	@SuppressWarnings("unchecked")
	private Class<? extends BundleEntity> findBundleEntityConcreteClass(EntityManager entityManager) throws ClassNotFoundException {

		final List<Class<?>> entities = new ArrayList<>();

		for (EntityType<?> entity : entityManager.getMetamodel().getEntities()) {
			if (BundleEntity.class.isAssignableFrom(entity.getJavaType())) {
				entities.add(entity.getJavaType());
			}
		}

		if (entities.size() == 1) {
			return (Class<? extends BundleEntity>) entities.get(0);
		}

		if (entities.size() > 1) {
			throw new IllegalStateException("There must be exactly one implementation of BundleEntity annotated with @Entity.");
		}

		throw new ClassNotFoundException("There should be one class annotated with @Entity and implements BundleEntity.");
	}

}

// Implementation Notes
// ====================
//
// Configuration of messageSource is taken from org.springframework.boot.autoconfigure.context.MessageSourceAutoConfiguration#messageSource()
