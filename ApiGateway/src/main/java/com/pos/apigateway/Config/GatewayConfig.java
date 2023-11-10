package com.pos.apigateway.Config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Autowired
    private AuthGatewayFilterFactory authGatewayFilterFactory;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth-service", r -> r
                        .path("/auth/login")
                        .uri("http://localhost:8081"))
                .route("add-customer", r -> r
                        .path("/customers/add")
                        .uri("http://localhost:8040"))
                .route("delivery-person", r -> r
                        .path("/deliveryPerson/add")
                        .filters(f -> f.filter(authGatewayFilterFactory.apply(new AuthGatewayFilterFactory.Config(new String[]{"ADMIN"}))))
                        .uri("http://localhost:8050"))
                .route("inventory-manager-service", r -> r
                        .path("/inventoryManager/add")
                        .filters(f -> f.filter(authGatewayFilterFactory.apply(new AuthGatewayFilterFactory.Config(new String[]{"ADMIN"}))))
                        .uri("http://localhost:8030"))
                .route("inventory-manager-service.", r -> r
                        .path("/inventoryManager/**")
                        .filters(f -> f.filter(authGatewayFilterFactory.apply(new AuthGatewayFilterFactory.Config(new String[]{"CUSTOMER", "ADMIN"}))))
                        .uri("http://localhost:8030"))
                .route("customer-service", r -> r
                        .path("/customers/**")
                        .filters(f -> f.filter(authGatewayFilterFactory.apply(new AuthGatewayFilterFactory.Config(new String[]{"CUSTOMER", "ADMIN"}))))
                        .uri("http://localhost:8040"))
                .route("product-service", r -> r
                        .path("/products/**")
                        .filters(f -> f.filter(authGatewayFilterFactory.apply(new AuthGatewayFilterFactory.Config(new String[]{"INVENTORY_KEEPER","ADMIN"}))))
                        .uri("http://localhost:8070"))
                .route("delivery-service", r -> r
                        .path("/delivery/**")
                        .filters(f -> f.filter(authGatewayFilterFactory.apply(new AuthGatewayFilterFactory.Config(new String[]{"DELIVERY_PERSON","ADMIN"}))))
                        .uri("http://localhost:8050"))
                .route("delivery-person", r -> r
                        .path("/deliveryPerson/**")
                        .filters(f -> f.filter(authGatewayFilterFactory.apply(new AuthGatewayFilterFactory.Config(new String[]{"DELIVERY_PERSON","ADMIN"}))))
                        .uri("http://localhost:8050"))
                .route("order-service", r -> r
                        .path("/orders/**")
                        .filters(f -> f.filter(authGatewayFilterFactory.apply(new AuthGatewayFilterFactory.Config(new String[]{"DELIVERY_PERSON","ADMIN"}))))
                        .uri("http://localhost:8060"))
                .build();
    }
}


