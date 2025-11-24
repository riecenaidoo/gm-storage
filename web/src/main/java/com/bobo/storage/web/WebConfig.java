package com.bobo.storage.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@EnableWebMvc
class WebConfig implements WebMvcConfigurer {

	/**
	 * @implNote In future, we should define a {@code Properties} object for the {@code web} module
	 *     and define our defaults there. We would want to have a flexible signature here and allow
	 *     CORS for multiple client urls.
	 */
	@Value("${app.client.url}")
	private String clientUrl;

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/api/v2/**").allowedMethods("*").allowedOrigins(clientUrl);
	}
}
