package net.consensys.mahuta.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import net.consensys.mahuta.Settings;

@Configuration
public class WebMVCConfiguration extends WebMvcConfigurerAdapter {

    private static final String MAPPING_PATH = "/**";

    public final String[] allowedOrigin;
    public final String[] allowedMethods;
    public final String[] allowedHeaders;
    public final boolean allowCredentials;
    public final Integer maxAge;

    @Autowired
    public WebMVCConfiguration(Settings settings) {
        if (settings == null || settings.getCorsOrigins() == null
                || settings.getCorsHeaders() == null || settings.getCorsMethods() == null) {

            throw new IllegalArgumentException("CORS Settings can't be null");
        }

        this.allowedOrigin = settings.getCorsOrigins().split(",");
        this.allowedMethods = settings.getCorsMethods().split(",");
        this.allowedHeaders = settings.getCorsHeaders().split(",");
        this.allowCredentials = settings.isCorsCredentials();
        this.maxAge = 3600;

    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {

        registry.addMapping(MAPPING_PATH).allowedOrigins(this.allowedOrigin)
                .allowedMethods(this.allowedMethods).allowedHeaders(this.allowedHeaders)
                .allowCredentials(this.allowCredentials).maxAge(this.maxAge);
    }
}
