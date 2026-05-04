package com.michelet.common.auth.feign.interceptor;

import com.michelet.common.auth.core.constants.AuthHeaders;
import com.michelet.common.auth.core.constants.InternalAuthHeaders;
import com.michelet.common.auth.feign.config.InternalFeignProperties;
import com.michelet.common.auth.feign.internal.InternalTokenIssuer;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class InternalFeignInterceptor implements RequestInterceptor {

    private final InternalTokenIssuer internalTokenIssuer;

    public InternalFeignInterceptor(
            InternalTokenIssuer internalTokenIssuer,
            InternalFeignProperties properties
    ) {
        this.internalTokenIssuer = internalTokenIssuer;
    }

    @Override
    public void apply(RequestTemplate requestTemplate) {
        if (requestTemplate.feignTarget() == null || requestTemplate.feignTarget().name() == null) {
            throw new IllegalStateException("Cannot resolve feign client name from request template.");
        }
        String audience = requestTemplate.feignTarget().name();

        if (audience == null || audience.isBlank()) {
            throw new IllegalStateException(
                    "No internal auth audience configured for feign client: " + audience
            );
        }
        requestTemplate.header(InternalAuthHeaders.INTERNAL_TOKEN, internalTokenIssuer.issue(audience));

        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (!(requestAttributes instanceof ServletRequestAttributes servletRequestAttributes)) {
            return;
        }

        HttpServletRequest request = servletRequestAttributes.getRequest();
        copyHeader(request, requestTemplate, AuthHeaders.USER_ID);
        copyHeader(request, requestTemplate, AuthHeaders.USER_ROLE);
    }

    private void copyHeader(HttpServletRequest request, RequestTemplate template, String headerName) {
        String value = request.getHeader(headerName);
        if (value != null && !value.isBlank()) {
            template.header(headerName, value);
        }
    }
}
