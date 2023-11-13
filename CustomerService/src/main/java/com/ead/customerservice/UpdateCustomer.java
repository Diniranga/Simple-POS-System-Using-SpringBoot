package com.ead.customerservice;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UpdateCustomer {
    private String firstName;
    private String lastName;
    private String address;
    private String contactNumber;
}
