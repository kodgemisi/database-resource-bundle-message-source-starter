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
import java.time.Duration;
import java.util.Set;

/**
 * Created on May, 2018
 *
 * @author destan
 */
@Configuration
@AutoConfigureBefore(MessageSourceAutoConfiguration.class)
@Slf4j
class DatabaseResourceBundleAutoconfiguration {

	@Bean
	@ConfigurationProperties(prefix = "spring.messages")
	MessageSourceProperties messageSourceProperties() {
		return new MessageSourceProperties();
	}

	@Bean
	@ConditionalOnMissingBean(BundleContentLoaderStrategy.class)
	BundleContentLoaderStrategy bundleContentLoaderStrategy(EntityManager entityManager) throws ClassNotFoundException {
		return new JpaBundleContentLoaderStrategy<>(entityManager, findBundleEntityConcreteClass());
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
	private Class<? extends BundleEntity> findBundleEntityConcreteClass() throws ClassNotFoundException {
		final ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);

		scanner.addIncludeFilter(new AnnotationTypeFilter(Entity.class));
		scanner.addIncludeFilter(new AssignableTypeFilter(BundleEntity.class));

		final StopWatch stopWatch = new StopWatch();
		stopWatch.start("findCandidateComponents with 'com'");

		Set<BeanDefinition> entities = scanner.findCandidateComponents(
				"com");//first try this because giving a base significantly improves performance
		if (entities.isEmpty()) {
			stopWatch.stop();
			stopWatch.start("findCandidateComponents with '*'");
			entities = scanner.findCandidateComponents("");
		}
		stopWatch.stop();

		log.debug("Scanned in {}", stopWatch.prettyPrint());

		if (entities.size() > 1) {
			throw new IllegalStateException("There must be exactly one implementation of BundleEntity annotated with @Entity.");
		}

		for (BeanDefinition bd : entities) {
			try {
				return (Class<? extends BundleEntity>) Class.forName(bd.getBeanClassName());
			}
			catch (ClassNotFoundException e) {
				log.error(e.getMessage(), e);
			}
		}

		throw new ClassNotFoundException("There should be one class annotated with @Entity and implements BundleEntity.");
	}

}

// Implementation Notes
// ====================
//
// Configuration of messageSource is taken from org.springframework.boot.autoconfigure.context.MessageSourceAutoConfiguration#messageSource()