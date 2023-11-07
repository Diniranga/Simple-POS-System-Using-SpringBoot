package com.pos.apigateway.Config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Example code using Spring Cloud Gateway
@Configuration
public class GatewayConfig {

    @Autowired
    private AuthGatewayFilterFactory authGatewayFilterFactory;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("customer-service", r -> r
                        .path("/customers/**")
                        .filters(f -> f.filter(authGatewayFilterFactory.apply(new AuthGatewayFilterFactory.Config("CUSTOMER"))))
                        .uri("http://localhost:8040"))
                .route("product-service", r -> r
                        .path("/products/**")
                        .filters(f -> f.filter(authGatewayFilterFactory.apply(new AuthGatewayFilterFactory.Config("INVENTORY_KEEPER"))))
                        .uri("http://localhost:8070"))
                .build();
    }
}


