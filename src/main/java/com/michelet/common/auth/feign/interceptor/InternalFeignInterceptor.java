package com.michelet.common.auth.feign.interceptor;

import com.michelet.common.auth.core.constants.AuthHeaders;
import com.michelet.common.auth.core.constants.InternalAuthHeaders;
import com.michelet.common.auth.feign.config.InternalFeignProperties;
import com.michelet.common.auth.feign.internal.InternalTokenIssuer;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class InternalFeignInterceptor implements RequestInterceptor {

    private final InternalTokenIssuer internalTokenIssuer;
    private final Map<String, String> audiences;

    public InternalFeignInterceptor(
            InternalTokenIssuer internalTokenIssuer,
            InternalFeignProperties properties
    ) {
        this.internalTokenIssuer = internalTokenIssuer;
        this.audiences = properties.getFeign().getAudiences();
    }

    @Override
    public void apply(RequestTemplate requestTemplate) {
        String clientName = resolveClientName(requestTemplate);
        String audience = audiences.get(clientName);

        if (audience == null || audience.isBlank()) {
            throw new IllegalStateException(
                    "No internal auth audience configured for feign client: " + clientName
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

    private String resolveClientName(RequestTemplate template) {
        if (template.feignTarget() == null || template.feignTarget().name() == null) {
            throw new IllegalStateException("Cannot resolve feign client name from request template.");
        }
        return template.feignTarget().name();
    }

    private void copyHeader(HttpServletRequest request, RequestTemplate template, String headerName) {
        String value = request.getHeader(headerName);
        if (value != null && !value.isBlank()) {
            template.header(headerName, value);
        }
    }
}
