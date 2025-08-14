package com.wuzk.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();

        // 设置拦截器，打印请求和响应
        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
        interceptors.add(new LoggingInterceptor());
        restTemplate.setInterceptors(interceptors);

        // 设置超时（Spring Boot 3.x 用 java.net.http.HttpClient 或 HttpComponents 可选）
        restTemplate.setRequestFactory(
                clientHttpRequestFactory()
        );

        return restTemplate;
    }

    private org.springframework.http.client.ClientHttpRequestFactory clientHttpRequestFactory() {
        var factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) Duration.ofSeconds(5).toMillis()); // 连接超时 5s
        factory.setReadTimeout((int) Duration.ofSeconds(10).toMillis());   // 读取超时 10s
        return factory;
    }

    /**
     * 请求/响应日志拦截器
     */
    static class LoggingInterceptor implements ClientHttpRequestInterceptor {

        @Override
        public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                            ClientHttpRequestExecution execution) throws IOException {
            logRequest(request, body);
            ClientHttpResponse response = execution.execute(request, body);
            logResponse(response);
            return response;
        }

        private void logRequest(HttpRequest request, byte[] body) {
            log.info("➡️ 请求 URI: {}", request.getURI());
            log.info("➡️ 请求方法: {}", request.getMethod());
            log.info("➡️ 请求头: {}", request.getHeaders());
            log.info("➡️ 请求体: {}", new String(body, StandardCharsets.UTF_8));
        }

        private void logResponse(ClientHttpResponse response) throws IOException {
            String body = new BufferedReader(new InputStreamReader(response.getBody(), StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));
            log.info("⬅️ 响应状态码: {}", response.getStatusCode());
            log.info("⬅️ 响应头: {}", response.getHeaders());
            log.info("⬅️ 响应体: {}", body);
        }
    }
}
