package com.ead.customerservice;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ActiveOrder {
    private String OrderId;
    private OrderStatus orderStatus;
}
