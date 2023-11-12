package com.example.kafkaserver;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaListeners {

    @Autowired
    private EmailService emailService;

    @KafkaListener(
            topics = "IN_QUEUE",
            groupId = "Order_Progress"
    )
    void Listener_InQueue(String data){

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String[] array = objectMapper.readValue(data, String[].class);

            // Output the elements of the array
            for (String element : array) {
                System.out.println(element);
            }

            emailService.sendEmail(array[0],"Order Placed","Your order has been placed successfully.\n\nOrder ID : " + array[2]);
            System.out.println("IN_QUEUE Listener : "+data);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @KafkaListener(
            topics = "SHIPPED",
            groupId = "Order_Progress"
    )
    void Listener_SHIPPED(String data){

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String[] array = objectMapper.readValue(data, String[].class);

            // Output the elements of the array
            for (String element : array) {
                System.out.println(element);
            }

            emailService.sendEmail(array[0],"Order Shipped","Your order is shipped successfully.\n\nOrder ID : " + array[2]);
            System.out.println("IN_QUEUE Listener : "+data);

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("SHIPPED Listener : "+data);
    }

    @KafkaListener(
            topics = "CANCELLED",
            groupId = "Order_Progress"
    )
    void Listener_CANCELLED(String data){

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String[] array = objectMapper.readValue(data, String[].class);

            for (String element : array) {
                System.out.println(element);
            }

            emailService.sendEmail(array[0],"Order CANCELLED","Your order is cancelled successfully.\n\nOrder ID : " + array[2]);
            System.out.println("IN_QUEUE Listener : "+data);

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("CANCELLED Listener : "+data);
    }

}
