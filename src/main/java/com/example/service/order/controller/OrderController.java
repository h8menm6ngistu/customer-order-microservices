package com.example.service.order.controller;

import com.example.service.order.dto.CustomerDTO;
import com.example.service.order.model.Order;
import com.example.service.order.repository.OrderRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderRepository orderRepository;
    private final RestClient customerRestClient;

    public OrderController(OrderRepository orderRepository, RestClient customerRestClient) {
        this.orderRepository = orderRepository;
        this.customerRestClient = customerRestClient;
    }

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody Order order) {
        fetchCustomerById(order.getCustomerId());

        if (orderRepository.existsByCustomerId(order.getCustomerId())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Customer already has an order"
            );
        }

        Order created = orderRepository.save(order);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @GetMapping("/{id}")
    public Order getOrderById(@PathVariable Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Order not found with id: " + id
                ));
    }

    @GetMapping("/customer/{customerId}")
    public List<Order> getOrdersByCustomerId(@PathVariable Long customerId) {
        return orderRepository.findByCustomerId(customerId);
    }

    @GetMapping("/{id}/customer")
    public CustomerDTO getCustomerForOrder(@PathVariable Long id) {
        Order order = getOrderById(id);
        return fetchCustomerById(order.getCustomerId());
    }

    @DeleteMapping("/{id}")
    public String deleteOrder(@PathVariable Long id) {
        if (!orderRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Order not found with id: " + id
            );
        }
        orderRepository.deleteById(id);
        return "Order deleted successfully";
    }

    private CustomerDTO fetchCustomerById(Long customerId) {
        try {
            CustomerDTO customer = customerRestClient.get()
                    .uri("/customers/{id}", customerId)
                    .retrieve()
                    .body(CustomerDTO.class);

            if (customer == null) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Customer not found with id: " + customerId
                );
            }
            return customer;
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Customer not found with id: " + customerId
                );
            }
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Failed to contact Customer Service: " + ex.getMessage()
            );
        }
    }
}