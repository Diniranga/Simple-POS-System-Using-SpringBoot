package com.ead.deliveryservice.Entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderRequest {
    private String deliverPersonId;
    private String orderId;
    private String customerId;
    private String deliveryAddress;

}

