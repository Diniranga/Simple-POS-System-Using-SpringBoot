package com.ead.deliveryservice.Service;

import com.ead.deliveryservice.Entity.ActiveOrder;
import com.ead.deliveryservice.Entity.Delivery;
import com.ead.deliveryservice.Entity.DeliveryPerson;
import com.ead.deliveryservice.Entity.OrderRequest;
import com.ead.deliveryservice.Enum.DeliveryStatus;
import com.ead.deliveryservice.Repository.DeliveryPersonRepository;
import com.ead.deliveryservice.Exceptions.UserAlreadyExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DeliveryPersonService {

    @Autowired
    private DeliveryPersonRepository deliveryPersonRepository;

    @Autowired
    private DeliveryService deliveryService;

    public String generateDeliveryPersonId() {
        DeliveryPerson deliveryPerson = deliveryPersonRepository.findFirstByOrderByDeliveryPersonIdDesc();
        if (deliveryPerson != null) {
            String deliveryPersonId = deliveryPerson.getDeliveryPersonId();
            int id = Integer.parseInt(deliveryPersonId.substring(2)) + 1;
            return "DP" + id;
        } else {
            return "DP1";
        }
    }

    public ResponseEntity<?> getAllDeliveryPerson() {
        return deliveryPersonRepository.findAll().isEmpty() ? ResponseEntity.ok("No Delivery Person Found") : ResponseEntity.ok(deliveryPersonRepository.findAll());
    }

    public DeliveryPerson getDeliverPersonById(String deliverPersonId){
        return deliveryPersonRepository.getDeliveryPersonByDeliveryPersonId(deliverPersonId);
    }

    public ResponseEntity<String> saveDeliveryPerson(DeliveryPerson deliveryPerson) {
        try {
            DeliveryPerson existingCustomer = deliveryPersonRepository.getDeliveryPersonByEmail(deliveryPerson.getEmail());
            deliveryPerson.setDeliveryPersonId(generateDeliveryPersonId());
            if (existingCustomer != null) {
                throw new UserAlreadyExistsException("Email: " + deliveryPerson.getEmail() + " is already exist");
            }
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<?> response = restTemplate.getForEntity(
                    "http://localhost:8081/auth/checkEmailExists/{email}",
                    String.class,
                    deliveryPerson.getEmail()
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                String requestJson = "{" +
                        "\"userId\":\"" + deliveryPerson.getDeliveryPersonId() + "\"," +
                        "\"firstName\":\"" + deliveryPerson.getFirstName() + "\"," +
                        "\"lastName\":\"" + deliveryPerson.getLastName() + "\"," +
                        "\"email\":\"" + deliveryPerson.getEmail() + "\"," +
                        "\"password\":\"" + deliveryPerson.getPassword() + "\"," +
                        "\"role\":\"DELIVERY_PERSON\"" +
                        "}";

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                HttpEntity<String> requestEntity = new HttpEntity<>(requestJson, headers);
                ResponseEntity<String> registerResponse = restTemplate.postForEntity(
                        "http://localhost:8081/auth/register",
                        requestEntity,
                        String.class
                );

                if (registerResponse.getStatusCode() == HttpStatus.OK) {
                    deliveryPersonRepository.save(deliveryPerson);
                    return ResponseEntity.ok("Delivery Person saved successfully");
                } else {
                    throw new UserAlreadyExistsException("Error registering user");
                }
            } else {
                throw new UserAlreadyExistsException("Email: " + deliveryPerson.getEmail() + " is already exist");
            }
        } catch (UserAlreadyExistsException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (HttpClientErrorException e) {
            return ResponseEntity.badRequest().body("Email: " + deliveryPerson.getEmail() + " is already exist");
        }
    }

    public ResponseEntity<?> updateDeliveryPerson(String deliverPersonId,DeliveryPerson deliveryPerson){
        DeliveryPerson updatedDeliverPerson = deliveryPersonRepository.getDeliveryPersonByDeliveryPersonId(deliverPersonId);
        if(updatedDeliverPerson == null){
            return ResponseEntity.badRequest().body("No Delivery Person Found");
        }
        updatedDeliverPerson.setFirstName(deliveryPerson.getFirstName());
        updatedDeliverPerson.setLastName(deliveryPerson.getLastName());
        updatedDeliverPerson.setContactNumber(deliveryPerson.getContactNumber());
        updatedDeliverPerson.setAddress(deliveryPerson.getAddress());
        return ResponseEntity.ok(deliveryPersonRepository.save(updatedDeliverPerson));
    }

    public ResponseEntity<?> assignOrder(OrderRequest orderRequest){
        DeliveryPerson deliveryPerson = deliveryPersonRepository.getDeliveryPersonByDeliveryPersonId(orderRequest.getDeliverPersonId());
        if(deliveryPerson == null){
            return ResponseEntity.badRequest().body("No Delivery Person Found");
        }
        if(deliveryPerson.getOrdersToDeliver()==null){
            deliveryPerson.setOrdersToDeliver(new ArrayList<>());
        }
        ActiveOrder activeOrder = new ActiveOrder();
        activeOrder.setOrderId(orderRequest.getOrderId());
        activeOrder.setStatus(DeliveryStatus.SHIPPED.toString());
        deliveryPerson.getOrdersToDeliver().add(activeOrder);

        Delivery delivery = new Delivery();
        delivery.setDeliveryId(deliveryService.generateDeliveryId());
        delivery.setDeliveryStatus(DeliveryStatus.SHIPPED);
        delivery.setDeliveryPersonId(orderRequest.getDeliverPersonId());
        delivery.setCustomerId(orderRequest.getCustomerId());
        delivery.setDeliveryAddress(orderRequest.getDeliveryAddress());

        RestTemplate restTemplate = new RestTemplate();
        String setStatusUrl = "http://localhost:8060/orders/setStatus/{orderId}/{orderStatus}";

        Map<String, String> params = new HashMap<>();
        params.put("orderId", orderRequest.getOrderId());
        params.put("orderStatus", DeliveryStatus.SHIPPED.toString());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<String> responseEntity = restTemplate.exchange(
                setStatusUrl,
                HttpMethod.PUT,
                requestEntity,
                String.class,  // Specify the expected response type
                params
        );

        String responseBody = responseEntity.getBody();

        if(responseEntity.getStatusCode() == HttpStatus.OK) {
            deliveryService.addDelivery(delivery);
            return ResponseEntity.ok(deliveryPersonRepository.save(deliveryPerson));
        }
        return ResponseEntity.ok("Error assigning order status");
    }

    public ResponseEntity<?> updateStatus(String deliveryPersonId, String orderID, String status) {
        DeliveryPerson deliveryPerson = deliveryPersonRepository.getDeliveryPersonByDeliveryPersonId(deliveryPersonId);
        System.out.println("Delivery Person: " + deliveryPerson.getFirstName());
        if(deliveryPerson == null){
            return ResponseEntity.badRequest().body("No Delivery Person Found");
        }
        System.out.println("came here");
        List<ActiveOrder> ordersToDeliver = deliveryPerson.getOrdersToDeliver();
        System.out.println("Orders to Deliver: " + ordersToDeliver);
        if(ordersToDeliver == null) {
            return ResponseEntity.badRequest().body("No Orders Found");
        }
        System.out.println("Order ID: " + orderID);
        for(ActiveOrder activeOrder : ordersToDeliver) {
            if(activeOrder.getOrderId().equals(orderID)) {
                RestTemplate restTemplate = new RestTemplate();
                String setStatusUrl = "http://localhost:8060/orders/setStatus/{orderId}/{orderStatus}";

                Map<String, String> params = new HashMap<>();
                params.put("orderId", orderID);
                params.put("orderStatus", status);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                HttpEntity<String> requestEntity = new HttpEntity<>(headers);

                ResponseEntity<String> responseEntity = restTemplate.exchange(
                        setStatusUrl,
                        HttpMethod.PUT,
                        requestEntity,
                        String.class,
                        params
                );

                if(responseEntity.getStatusCode() == HttpStatus.OK) {
                    if(status.equals(DeliveryStatus.DELIVERED.toString())) {
                        deliveryPerson.getOrdersToDeliver().remove(activeOrder);
                        if(deliveryPerson.getDeliveredOrders() == null) {
                            deliveryPerson.setDeliveredOrders(new ArrayList<>());
                        }
                        deliveryPerson.getDeliveredOrders().add(activeOrder.getOrderId());
                        deliveryPersonRepository.save(deliveryPerson);
                    }
                }
                return ResponseEntity.ok("Order status updated successfully");
            }
        }
        return ResponseEntity.ok("No Orders Found");
    }
}
