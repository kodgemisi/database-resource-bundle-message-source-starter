package com.kodgemisi.cigdem.databasebasedmessagesource;

import com.kodgemisi.cigdem.databaseresourcbundle.BundleContentLoaderStrategy;
import com.kodgemisi.cigdem.databaseresourcbundle.DatabaseResourceBundleControl;
import lombok.AllArgsConstructor;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.io.Reader;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 *
 * Created on May, 2018
 *
 * @author destan
 */
@AllArgsConstructor
public class DatabaseResourceBundleMessageSource extends ResourceBundleMessageSource {

	private final BundleContentLoaderStrategy bundleContentLoaderStrategy;

	@Override
	protected ResourceBundle doGetBundle(String basename, Locale locale) throws MissingResourceException {
		// We don't use getBundleClassLoader because it may return null.
		// As we won't be using it in any way, to reduce complexity we use this class' classloader.
		final ClassLoader classLoader = this.getClass().getClassLoader();

		// As we can observe here: org.springframework.context.support.ResourceBundleMessageSource.MessageSourceControl#getTimeToLive
		// MessageSourceControl's getTimeToLive method should return the same value with the ResourceBundleMessageSource#cacheMillis
		// Since we don't put DatabaseResourceBundleControl class as an inner class and since we cannot use
		// ResourceBundleMessageSource#getCacheMillis in our DatabaseResourceBundleControl class we have to sync the value by
		// explicitly setting TTL in the constructor of DatabaseResourceBundleControl

		// Also note that when `spring.messages.cache-duration` is not set, bundles are cached forever.
		// Default value of org.springframework.context.support.AbstractResourceBasedMessageSource.cacheMillis is -1 which is equal to
		// ResourceBundle.Control.TTL_DONT_CACHE so that's consistent and we don't need to do anything about default value.
		// See org.springframework.context.support.AbstractResourceBasedMessageSource.setCacheMillis
		return ResourceBundle.getBundle(basename, locale, classLoader, new DatabaseResourceBundleControl(bundleContentLoaderStrategy, getCacheMillis()));
		//FIXME DatabaseResourceBundleControl could be a field. However what about using setCacheMillis in runtime?
	}

	@Override
	protected ResourceBundle loadBundle(Reader reader) {
		// as this method is only called from org.springframework.context.support.ResourceBundleMessageSource.MessageSourceControl.newBundle
		// it should never be called for DatabaseResourceBundleMessageSource.
		// In our implementation, we expect loadBundle method to be called from DatabaseResourceBundleControl
		throw new UnsupportedOperationException();
	}

}

/*
 * Implementation notes
 * ====================
 * Let's don't override resolveCode and getResourceBundle as they don't have any ResourceBundle subclass specific operation.
 *
 * No need to override loadBundle (other than for throwing an UnsupportedOperationException exception) as it's only called from
 * Control class and we will provide our own Control class in doGetBundle.
 */