package com.ead.productservice;

public class ProductNotFountException extends RuntimeException {
    public ProductNotFountException(String message) {
        super(message);
    }
}
