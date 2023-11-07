package com.pos.apigateway.Config;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.GatewayFilterFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class AuthGatewayFilterFactory implements GatewayFilterFactory<AuthGatewayFilterFactory.Config> {

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            HttpHeaders headers = exchange.getRequest().getHeaders();
            String jwtToken = headers.getFirst(HttpHeaders.AUTHORIZATION);
            if (jwtToken != null && jwtToken.startsWith("Bearer ")) {
                jwtToken = jwtToken.substring(7);
                HttpHeaders requestHeaders = new HttpHeaders();
                requestHeaders.set(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken);
                RestTemplate restTemplate = new RestTemplate();
                ResponseEntity<String> response = restTemplate.exchange(
                        "http://localhost:8081/auth/validateToken",
                        HttpMethod.GET,
                        new HttpEntity<>(requestHeaders),
                        String.class
                );
                if (response.getStatusCode() == HttpStatus.OK) {
                    String responseBody = response.getBody();
                    assert responseBody != null;
                    if(responseBody.equals(config.role)){
                        return chain.filter(exchange);
                    }else{
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        return exchange.getResponse().setComplete();
                    }
                }
            }
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        };
    }

    @Override
    public Config newConfig() {
        return new Config(null);
    }

    public static class Config {

        private String role;

        public Config(String myVariable) {
            this.role = myVariable;
        }

        public void setMyVariable(String myVariable) {
            this.role = myVariable;
        }
    }
}
