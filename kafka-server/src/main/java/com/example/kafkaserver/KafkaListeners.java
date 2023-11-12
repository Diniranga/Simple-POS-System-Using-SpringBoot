package com.example.kafkaserver;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaListeners {

    @KafkaListener(
            topics = "IN_QUEUE",
            groupId = "Order_Progress"
    )
    void Listener_InQueue(String data){

        System.out.println("IN_QUEUE Listener : "+data);
    }

    @KafkaListener(
            topics = "SHIPPED",
            groupId = "Order_Progress"
    )
    void Listener_SHIPPED(String data){

        System.out.println("SHIPPED Listener : "+data);
    }

}
