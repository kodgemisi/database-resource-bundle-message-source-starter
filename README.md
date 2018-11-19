# Database Resource Bundle Message Source Spring Boot Starter

This is a Spring Boot autoconfiguration and starter artifact for [Database Resource Bundle](https://github.com/kodgemisi/database-resource-bundle)
which is an database backed implementation of `java.util.ResourceBundle`.

# Quick Start

1. Add dependency via Jitpack
2. Create a JPA entity class which implements `BundleEntity`
3. Populate the table which corresponds to the entity class you've declared in step 2 and you're ready to roll.

See [usage demo](#) project for code samples.

# Usage

This starter instantiates a bean of `JpaBundleContentLoaderStrategy` if there is no bean of type `BundleContentLoaderStrategy` is present in context.

## Using `JpaBundleContentLoaderStrategy` (Default with this starter)

When using `JpaBundleContentLoaderStrategy` you need to have an entity which implements `BundleEntity`.

By default, this starter will scan for an entity which implements `BundleEntity`. However, this takes time (~1s) and decrease your startup time 
(but has no effect on runtime performance). If startup time is important for you then you can instantiate a bean of `JpaBundleContentLoaderStrategy` 
where you explicitly declare your entity class. So that there won't be any scanning.

### Instantiating `JpaBundleContentLoaderStrategy` manually

```
@Bean
BundleContentLoaderStrategy bundleContentLoaderStrategy(EntityManager entityManager) {
  return new JpaBundleContentLoaderStrategy<>(entityManager, MyBundleEntity.class);
}
```

## Using `DefaultBundleContentLoaderStrategy`

`DefaultBundleContentLoaderStrategy` uses a prepared statement to load resource content.

By default it assumes you have a table in your database which 
is created via `database-resource-bundle.jar/create.sql`. Hence uses following queries:

```
DEFAULT_LOAD_QUERY = "SELECT DISTINCT b.key, b.value FROM " + DEFAULT_TABLE_NAME + " b WHERE name = ? AND language = ? AND country = ? AND variant = ? ;"
DEFAULT_NEEDS_RELOAD_QUERY = "SELECT MAX(last_modified) FROM " + DEFAULT_TABLE_NAME + " b WHERE name = ? ;";
```

`DEFAULT_TABLE_NAME` is `Bundle` and coming from `BundleContentLoaderStrategy.DEFAULT_TABLE_NAME`.


### Using a different Table name or completely different Table structure

You can override those queries by using `DefaultBundleContentLoaderStrategy`'s 3-parameter constructor. When you do this, you can use any database 
table structure or name as you like as long as you conform to expected return values of the queries.

Expected return values of the `load query` is a key-value string pair.

Expected return values of the `needs reload query` is a long value which represents the epoch milliseconds of latest change on the table.

# Defaults

This starter;

1. Instantiates a bean of `JpaBundleContentLoaderStrategy` if there is no bean of type `BundleContentLoaderStrategy` is present in context.
  If there is a bean of type `BundleContentLoaderStrategy`, for example `DefaultBundleContentLoaderStrategy` or your custom implementation, then `JpaBundleContentLoaderStrategy` is
  not instantiated (hence there won't be a class scanning for an entity class implementing `BundleEntity`) and the existing bean is used.
2. Configures a `DatabaseResourceBundleMessageSource` which uses the `BundleContentLoaderStrategy` implementation.
3. `DatabaseResourceBundleMessageSource` is a [`MessageSource`](org.springframework.context.MessageSource) so you don't need to configure anything 
else, just use Spring's `messages.properties` support as before but this time instead of a `properties` file it's backed by a database table.

# Configuration

You can use Spring Boot's message source properties to change `DatabaseResourceBundleMessageSource`'s behavior:

```
spring.messages.always-use-message-format=false # Whether to always apply the MessageFormat rules, parsing even messages without arguments.
spring.messages.basename=messages # Comma-separated list of basenames (essentially a fully-qualified classpath location), each following the ResourceBundle convention with relaxed support for slash based locations.
spring.messages.cache-duration= # Loaded resource bundle files cache duration. When not set, bundles are cached forever. If a duration suffix is not specified, seconds will be used.
spring.messages.encoding=UTF-8 # Message bundles encoding.
spring.messages.fallback-to-system-locale=true # Whether to fall back to the system Locale if no files for a specific Locale have been found.
spring.messages.use-code-as-default-message=false # Whether to use the message code as the default message instead of throwing a "NoSuchMessageException". Recommended during development only.
```

See also [MessageSourceProperties](https://docs.spring.io/spring-boot/docs/2.0.x/api/org/springframework/boot/autoconfigure/context/MessageSourceProperties.html);

# LICENSE

© Copyright 2018 Kod Gemisi Ltd.

Mozilla Public License 2.0 (MPL-2.0)

https://tldrlegal.com/license/mozilla-public-license-2.0-(mpl-2)

MPL is a copyleft license that is easy to comply with. You must make the source code for any of your changes available under MPL, 
but you can combine the MPL software with proprietary code, as long as you keep the MPL code in separate files. 
Version 2.0 is, by default, compatible with LGPL and GPL version 2 or greater. You can distribute binaries under a proprietary license, 
as long as you make the source available under MPL.

[See Full License Here](https://www.mozilla.org/en-US/MPL/2.0/) 