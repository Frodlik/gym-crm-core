package com.gym.crm.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(MockitoExtension.class)
class CachingFilterTest {
    private final CachingFilter cachingFilter = new CachingFilter();
    @Spy
    private final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
    @Spy
    private final MockHttpServletResponse response = new MockHttpServletResponse();
    @Spy
    private final FilterChain filterChain = new FilterChain() {
        @Override
        public void doFilter(ServletRequest req, ServletResponse res) {
        }
    };

    @Test
    void doFilter_shouldWrapRequestAndResponse_correctly() throws Exception {
        var wrappedRequest = new ContentCachingRequestWrapper(request);
        var wrappedResponse = new ContentCachingResponseWrapper(response);

        cachingFilter.doFilter(wrappedRequest, wrappedResponse, filterChain);

        wrappedResponse.copyBodyToResponse();
        assertThat(wrappedRequest).isInstanceOf(ContentCachingRequestWrapper.class);
        assertThat(wrappedResponse).isInstanceOf(ContentCachingResponseWrapper.class);
        assertThat(response.getContentAsByteArray()).isNotNull();
    }
}
