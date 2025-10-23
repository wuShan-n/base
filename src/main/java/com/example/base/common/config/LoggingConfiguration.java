package com.example.base.common.config;

import com.example.base.common.logging.RequestContextLoggingFilter;
import io.micrometer.tracing.Tracer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoggingConfiguration {

    @Bean
    public FilterRegistrationBean<RequestContextLoggingFilter> requestContextLoggingFilter(ObjectProvider<Tracer> tracerProvider) {
        FilterRegistrationBean<RequestContextLoggingFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new RequestContextLoggingFilter(tracerProvider));
        registration.setOrder(1);
        registration.setName("requestContextLoggingFilter");
        return registration;
    }
}
