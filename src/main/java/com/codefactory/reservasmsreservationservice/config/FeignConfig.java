package com.codefactory.reservasmsreservationservice.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.httpclient.ApacheHttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignConfig implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes != null) {
            String authHeader = attributes.getRequest().getHeader("Authorization");
            if (authHeader != null) {
                template.header("Authorization", authHeader);
            }
        }
    }

    /**
     * HTTP client with connection pooling for better performance.
     * Reuses connections instead of creating new ones for each request.
     */
    @Bean
    public ApacheHttpClient httpClient() {
        // Connection pool manager
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(50);           // Max total connections
        connectionManager.setDefaultMaxPerRoute(10); // Max connections per route (host)

        // Request config with timeouts
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(5000)  // 5 seconds - getting connection from pool
                .setSocketTimeout(10000)            // 10 seconds - server response timeout
                .setConnectTimeout(5000)             // 5 seconds - connection timeout
                .build();

        // Build HTTP client with pooling
        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .build();

        return new ApacheHttpClient(httpClient);
    }
}
