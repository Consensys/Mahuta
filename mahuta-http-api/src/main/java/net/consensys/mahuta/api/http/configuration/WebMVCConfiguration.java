package net.consensys.mahuta.api.http.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMVCConfiguration implements WebMvcConfigurer {

    private static final String MAPPING_PATH = "/**";

    public final String[] allowedOrigin;
    public final String[] allowedMethods;
    public final String[] allowedHeaders;
    public final boolean allowCredentials;
    public final Integer maxAge;

    @Autowired
    public WebMVCConfiguration(
            @Value("${mahuta.security.cors.origins:*}") String corsOrigins, 
            @Value("${mahuta.security.cors.methods:GET,POST,PUT,OPTIONS,DELETE,PATCH}") String corsMethods,
            @Value("${mahuta.security.cors.headers:Access-Control-Allow-Headers,Origin,X-Requested-With,Content-Type,Accept}") String corsHeaders,
            @Value("${mahuta.security.cors.credentials:false}") boolean corsCredentials) {

        this.allowedOrigin = corsOrigins.split(",");
        this.allowedMethods = corsMethods.split(",");
        this.allowedHeaders = corsHeaders.split(",");
        this.allowCredentials = corsCredentials;
        this.maxAge = 3600;

    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {

        registry.addMapping(MAPPING_PATH)
                .allowedOrigins(this.allowedOrigin)
                .allowedMethods(this.allowedMethods)
                .allowedHeaders(this.allowedHeaders)
                .allowCredentials(this.allowCredentials)
                .maxAge(this.maxAge);
    }
}
