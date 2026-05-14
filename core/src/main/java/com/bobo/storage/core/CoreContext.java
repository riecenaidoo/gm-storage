package com.bobo.storage.core;

import static java.lang.annotation.ElementType.TYPE;

import com.bobo.semantic.ModuleContext;
import com.bobo.storage.core.song.LookupConfig;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @implSpec {@link ModuleContext}
 */
@ModuleContext
@Target({TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import(CoreConfig.class)
@ComponentScan(basePackageClasses = CoreContext.class)
@EnableConfigurationProperties(LookupConfig.class)
@EnableJpaRepositories(basePackageClasses = CoreContext.class)
@EnableTransactionManagement
@EntityScan(basePackageClasses = CoreContext.class)
public @interface CoreContext {}
