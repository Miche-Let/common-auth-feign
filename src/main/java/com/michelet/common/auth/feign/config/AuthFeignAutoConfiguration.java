package com.michelet.common.auth.feign.config;

import com.michelet.common.auth.feign.interceptor.InternalFeignInterceptor;
import com.michelet.common.auth.feign.internal.InternalTokenIssuer;
import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(RequestInterceptor.class)
@EnableConfigurationProperties(InternalFeignProperties.class)
public class AuthFeignAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public InternalTokenIssuer internalTokenIssuer(
            InternalFeignProperties properties,
            @Value("${spring.application.name}") String applicationName
    ){
        return new InternalTokenIssuer(
                properties.getSecret(),
                applicationName
        );
    }

    @Bean
    @ConditionalOnMissingBean
    public RequestInterceptor internalFeignRequestInterceptor(
            InternalTokenIssuer internalTokenIssuer,
            InternalFeignProperties properties
    ) {
        return new InternalFeignInterceptor(
                internalTokenIssuer,
                properties
        );
    }


}
