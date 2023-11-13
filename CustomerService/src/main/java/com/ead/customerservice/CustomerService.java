package com.ead.customerservice;

import com.ead.customerservice.Exceptions.UserAlreadyExistsException;
import com.ead.customerservice.Exceptions.UserNotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    private final KafkaTemplate<String, String> kafkaTemplate;

    public CustomerService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    RestTemplate restTemplate = new RestTemplate();


    public String generateUserId() {
        Customer lastCustomer = customerRepository.findFirstByOrderByUserIdDesc();
        if (lastCustomer != null) {
            int lastUserId = Integer.parseInt(lastCustomer.getUserId().substring(1));
            int newUserId = lastUserId + 1;
            return "U" + newUserId;
        } else {
            return "U1";
        }
    }

    public ResponseEntity<String> saveCustomer(Customer customer) {
        try {
            Customer existingCustomer = customerRepository.findByEmail(customer.getEmail());
            customer.setUserId(generateUserId());
            if (existingCustomer != null) {
                throw new UserAlreadyExistsException("Email: " + customer.getEmail() + " is already exist");
            }
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<?> response = restTemplate.getForEntity(
                    "http://localhost:8081/auth/checkEmailExists/{email}",
                    String.class,
                    customer.getEmail()
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                String requestJson = "{" +
                        "\"userId\":\"" + customer.getUserId() + "\"," +
                        "\"firstName\":\"" + customer.getFirstName() + "\"," +
                        "\"lastName\":\"" + customer.getLastName() + "\"," +
                        "\"email\":\"" + customer.getEmail() + "\"," +
                        "\"password\":\"" + customer.getPassword() + "\"," +
                        "\"role\":\"CUSTOMER\"" +
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
                    customerRepository.save(customer);
                    return ResponseEntity.ok("Customer saved successfully");
                } else {
                    throw new UserAlreadyExistsException("Error registering user");
                }
            } else {
                throw new UserAlreadyExistsException("Email: " + customer.getEmail() + " is already exist");
            }
        } catch (UserAlreadyExistsException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (HttpClientErrorException e) {
            return ResponseEntity.badRequest().body("Email: " + customer.getEmail() + " is already exist");
        }
    }

    public ResponseEntity<String> updateCustomer(UpdateCustomer customer, String userId) {
        try {
            Customer existingCustomer = customerRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
            existingCustomer.setFirstName(customer.getFirstName());
            existingCustomer.setLastName(customer.getLastName());
            existingCustomer.setContactNumber(customer.getContactNumber());
            existingCustomer.setAddress(customer.getAddress());
            customerRepository.save(existingCustomer);
            return ResponseEntity.ok("Customer updated successfully");
        } catch (UserNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public Customer getCustomerById(String userId) throws UserNotFoundException {
        return customerRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
    }

    public ResponseEntity<String> deleteCustomerById(String userId) {
        try {
            Customer customer = customerRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
            customerRepository.delete(customer);
            return ResponseEntity.ok("Customer deleted successfully");
        } catch (UserNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    public ResponseEntity<?> getCartItems(String userId) throws UserNotFoundException {
        try {
            Customer customer = customerRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
            if (customer.getCartItems() == null) {
                return ResponseEntity.ok(new CartItem());
            }
            return ResponseEntity.ok(customer.getCartItems());
        } catch (UserNotFoundException e) {
            return null;
        }
    }

    public ResponseEntity<String> userCartAdd(CartItem cartItem, String userId) {
        try {
            Customer customer = customerRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
            String productId = cartItem.getProductId();
            String productUrl = "http://localhost:8070/products/getById/" + productId;
            ResponseEntity<String> productResponse = restTemplate.getForEntity(productUrl, String.class);
            if (productResponse.getStatusCode().is2xxSuccessful()) {
                if (productResponse.getBody() == null) {
                    return ResponseEntity.badRequest().body("Product not found with id: " + productId);
                }
                ObjectMapper objectMapper = new ObjectMapper();
                Product productDetails = objectMapper.readValue(productResponse.getBody(), Product.class);
                if(cartItem.getQuantity() <1){
                    return ResponseEntity.badRequest().body("Invalid quantity");
                }
                if(productDetails.getQuantity() < cartItem.getQuantity()){
                    return ResponseEntity.badRequest().body("Product quantity is not enough");
                }
                List<CartItem> currentCartItems = customer.getCartItems();
                if (currentCartItems == null) {
                    currentCartItems = new ArrayList<>();
                    customer.setCartItems(currentCartItems);
                }
                cartItem.setUnitPrice(productDetails.getUnitPrice());
                currentCartItems.add(cartItem);
                customer.setTotalCost(customer.getTotalCost() + (productDetails.getUnitPrice() * cartItem.getQuantity()));
                customerRepository.save(customer);
                return ResponseEntity.ok("Cart item added successfully");
            } else {
                return ResponseEntity.badRequest().body("Error fetching product details");
            }
        } catch (UserNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public ResponseEntity<?> userCartUpdate(CartItem cartItem, String userId) {
        try {
            Customer customer = customerRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
            String productId = cartItem.getProductId();
            String productUrl = "http://localhost:8070/products/getById/" + productId;
            ResponseEntity<String> productResponse = restTemplate.getForEntity(productUrl, String.class);
            if (productResponse.getStatusCode().is2xxSuccessful()) {
                if (productResponse.getBody() == null) {
                    return ResponseEntity.badRequest().body("Product not found with id: " + productId);
                }
                ObjectMapper objectMapper = new ObjectMapper();
                Product productDetails = objectMapper.readValue(productResponse.getBody(), Product.class);
                if(cartItem.getQuantity() <1){
                    return ResponseEntity.badRequest().body("Invalid quantity");
                }
                if(productDetails.getQuantity() < cartItem.getQuantity()){
                    return ResponseEntity.badRequest().body("Product quantity is not enough");
                }
                List<CartItem> currentCartItems = customer.getCartItems();
                if (currentCartItems == null) {
                    return ResponseEntity.badRequest().body("Cart is empty");
                }
                for (CartItem item : currentCartItems) {
                    if (item.getProductId().equals(cartItem.getProductId())) {
                        customer.setTotalCost(customer.getTotalCost() - (productDetails.getUnitPrice() * item.getQuantity()));
                        item.setQuantity(cartItem.getQuantity());
                        customer.setTotalCost(customer.getTotalCost() + (productDetails.getUnitPrice() * cartItem.getQuantity()));
                        customerRepository.save(customer);
                        return ResponseEntity.ok("Cart item updated successfully");
                    }
                }
                return ResponseEntity.badRequest().body("Cart item not found");
            } else {
                return ResponseEntity.badRequest().body("Error fetching product details");
            }
        } catch (UserNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public ResponseEntity<?> removeCartItem (String userId, String productId){
        try {
            Customer customer = customerRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
            List<CartItem> currentCartItems = customer.getCartItems();
            if (currentCartItems == null) {
                return ResponseEntity.badRequest().body("Cart is empty");
            }
            for (CartItem item : currentCartItems) {
                if (item.getProductId().equals(productId)) {
                    customer.setTotalCost(customer.getTotalCost() - (item.getUnitPrice() * item.getQuantity()));
                    currentCartItems.remove(item);
                    customerRepository.save(customer);
                    return ResponseEntity.ok("Cart item removed successfully");
                }
            }
            return ResponseEntity.badRequest().body("Cart item not found");
        }catch (UserNotFoundException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    public ResponseEntity<?> getOrderStatus(String userId,String orderId) {
        try {
            Customer customer = customerRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
            List<PlacedOrder> placedOrders = customer.getAllOrders();
            if(placedOrders == null){
                return ResponseEntity.ok("No active orders Found");
            }
            for(PlacedOrder placedOrder : placedOrders){
                if(Objects.equals(placedOrder.getOrderId(), orderId)){
                    return ResponseEntity.ok(placedOrder.getOrderStatus());
                }
            }
            return ResponseEntity.ok("No order found with id: " + orderId);
        }catch (UserNotFoundException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }catch (Exception e){
            return ResponseEntity.badRequest().body(null);
        }
    }

    public ResponseEntity<?> setOrderStatus (String userId, String orderId,String status){
        try {
            Customer customer = customerRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
            List<PlacedOrder> placedOrders = customer.getAllOrders();
            if(placedOrders ==null){
                return ResponseEntity.ok("There are no any available Active Orders");
            }
            for(PlacedOrder placedOrder : placedOrders){
                if(Objects.equals(placedOrder.getOrderId(), orderId)){
                    if(status.equals("IN_QUEUE") || status.equals("PACKING") || status.equals("READY_TO_DISPATCH") || status.equals("SHIPPED") || status.equals("DELIVERED") || status.equals("NOT_APPLICABLE") || status.equals("CANCELLED")){
                        placedOrder.setOrderStatus(OrderStatus.valueOf(status));
                        customerRepository.save(customer);
                        ArrayList<String> emailData = new ArrayList<>();
                        emailData.add(customer.getEmail());
                        emailData.add(status);
                        emailData.add(orderId);
                        ObjectMapper objectMapper = new ObjectMapper();
                        String emailDataString = objectMapper.writeValueAsString(emailData);
                        kafkaTemplate.send(status,emailDataString);
                        return ResponseEntity.ok("Order status updated successfully");
                    }
                    return ResponseEntity.ok("Invalid Order Status");
                }
            }
            return ResponseEntity.badRequest().body("Order Id Not Found");
        } catch (Exception e){
            return ResponseEntity.badRequest().body(null);
        }
    }

    public ResponseEntity<?> placeOrder(String userId){
        try {
            Customer customer = customerRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
            List<CartItem> currentCartItems = customer.getCartItems();
            if (currentCartItems == null) {
                return ResponseEntity.badRequest().body("Cart is empty");
            }
            for (CartItem item : currentCartItems) {
                String productId = item.getProductId();
                String productUrl = "http://localhost:8070/products/checkAvailability/" + productId + "/" + item.getQuantity();
                ResponseEntity<Integer> productResponse = restTemplate.getForEntity(productUrl, Integer.class);
                if (productResponse.getStatusCode().is2xxSuccessful()) {
                    if (productResponse.getBody() == null) {
                        return ResponseEntity.badRequest().body("Product not found with id: " + productId);
                    }
                    if(productResponse.getBody() == 0){
                        removeCartItem(userId,productId);
                        return ResponseEntity.badRequest().body("Product "+item.getProductId()+" quantity is not enough. Please try again");
                    }
                } else {
                    return ResponseEntity.badRequest().body("Error fetching product details");
                }
            }
            for(CartItem item: currentCartItems){
                String productId = item.getProductId();
                String productUrl = "http://localhost:8070/products/updateQuantity/" + productId + "/" + item.getQuantity();
                restTemplate.put(productUrl,null);
            }
            Order order = new Order();
            order.setCustomerId(userId);
            order.setOrderItems(currentCartItems);
            String orderUrl = "http://localhost:8060/orders/add";
            ResponseEntity<Order> savedOrder = restTemplate.postForEntity(orderUrl,order,Order.class);
            if (savedOrder.getStatusCode().is2xxSuccessful()) {
                Order savedOrderObject = savedOrder.getBody();
                if (savedOrderObject != null) {
                    if(customer.getAllOrders()==null){
                        customer.setAllOrders(new ArrayList<>());
                    }
                    PlacedOrder placedOrder = new PlacedOrder();
                    placedOrder.setOrderId(savedOrderObject.getOrderId());
                    placedOrder.setOrderStatus(OrderStatus.IN_QUEUE);
                    customer.getAllOrders().add(placedOrder);
                    customer.setTotalCost(0);
                    customer.setCartItems(null);

                    ArrayList<String> emailData = new ArrayList<>();
                    emailData.add(customer.getEmail());
                    emailData.add("IN_QUEUE");
                    emailData.add(savedOrderObject.getOrderId());
                    ObjectMapper objectMapper = new ObjectMapper();
                    String emailDataString = objectMapper.writeValueAsString(emailData);
                    kafkaTemplate.send("IN_QUEUE",emailDataString);

                    customerRepository.save(customer);
                    return ResponseEntity.ok("Order placed successfully");
                } else {
                    return ResponseEntity.badRequest().body("Error: Response body is null");
                }
            } else {
                return ResponseEntity.badRequest().body("Error: Request was not successful. Status code: " + savedOrder.getStatusCodeValue());
            }
        }catch (UserNotFoundException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }catch (Exception e){
            return ResponseEntity.badRequest().body(null);
        }
    }

    public ResponseEntity<String> clearCart(String userId){
        try {
            Customer customer = customerRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
            customer.setCartItems(null);
            customerRepository.save(customer);
            return ResponseEntity.ok("Cart cleared successfully");
        }catch (UserNotFoundException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    public ResponseEntity<?> cancelOrder(String customerId, String orderId) throws JsonProcessingException {
        Customer currentCustomer = customerRepository.findById(customerId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + customerId));

        List<PlacedOrder> placedOrders = currentCustomer.getAllOrders();
        if (placedOrders == null) {
            return ResponseEntity.badRequest().body("No active orders found");
        }
        for (PlacedOrder placedOrder : placedOrders) {
            if (Objects.equals(placedOrder.getOrderId(), orderId)) {
                if (!placedOrder.getOrderStatus().equals(OrderStatus.IN_QUEUE)) {
                    return ResponseEntity.badRequest().body("Order cannot be cancelled. Order In Processing");
                }
                String orderUrl = "http://localhost:8060/orders/cancelOrder/" + orderId;
                ResponseEntity<String> response = restTemplate.exchange(orderUrl, HttpMethod.PUT, null, String.class);
                if (response.getStatusCode().is2xxSuccessful()) {
                    placedOrder.setOrderStatus(OrderStatus.CANCELLED);

                    ArrayList<String> emailData = new ArrayList<>();
                    emailData.add(currentCustomer.getEmail());
                    emailData.add("CANCELLED");
                    emailData.add(orderId);
                    ObjectMapper objectMapper = new ObjectMapper();
                    String emailDataString = objectMapper.writeValueAsString(emailData);
                    kafkaTemplate.send("CANCELLED",emailDataString);


                    customerRepository.save(currentCustomer);
                    return ResponseEntity.ok("Order cancelled successfully");
                }
            }
        }
        return ResponseEntity.badRequest().body("Order not found to cancel");
    }
}
