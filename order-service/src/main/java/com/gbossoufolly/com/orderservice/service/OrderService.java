package com.gbossoufolly.com.orderservice.service;

import com.gbossoufolly.com.orderservice.dto.InventoryResponse;
import com.gbossoufolly.com.orderservice.dto.OrderLineItemsDto;
import com.gbossoufolly.com.orderservice.dto.OrderRequest;
import com.gbossoufolly.com.orderservice.event.OrderPlacedEvent;
import com.gbossoufolly.com.orderservice.models.Order;
import com.gbossoufolly.com.orderservice.models.OrderLineItems;
import com.gbossoufolly.com.orderservice.repository.OrderRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final WebClient.Builder webClientBuilder;
    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

    public OrderService(OrderRepository orderRepository, WebClient.Builder webClientBuilder, KafkaTemplate kafkaTemplate) {
        this.orderRepository = orderRepository;
        this.webClientBuilder = webClientBuilder;
        this.kafkaTemplate = kafkaTemplate;
    }

    public String placeOrder(OrderRequest orderRequest) throws IllegalAccessException {
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());

        List<OrderLineItems> orderLineItems =  orderRequest.getOrderLineItemsDtoList()
                .stream()
                .map(this::mapToDto)
                .toList();

        order.setOrderLineItemsList(orderLineItems);

        List<String> skuCodes = order.getOrderLineItemsList().stream()
                .map(OrderLineItems::getSkuCode)
                .toList();


        // Call Inventory  Service, and place order if product is in
        InventoryResponse[] inventoryResponsesArray = webClientBuilder.build().get()
                .uri("http://inventory-service/api/inventory",
                        uriBuilder -> uriBuilder.queryParam("skuCode", skuCodes)
                                .build())
                .retrieve()
                .bodyToMono(InventoryResponse[].class)
                .block();

        boolean allProductsInStock =  Arrays.stream(inventoryResponsesArray).allMatch(
                InventoryResponse::isInStock);

        if(allProductsInStock) {
            orderRepository.save(order);
            kafkaTemplate.send("notificationTopic", new OrderPlacedEvent(order.getOrderNumber()));

            return "Order Placed Successfully";
        } else {
            throw new IllegalAccessException("Product is not in stock, please try again later");
        }
    }

    private OrderLineItems mapToDto(OrderLineItemsDto orderLineItemsDto) {
        OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setPrice(orderLineItemsDto.getPrice());
        orderLineItems.setQuantity(orderLineItemsDto.getQuantity());
        orderLineItems.setSkuCode(orderLineItemsDto.getSkuCode());

        return orderLineItems;
    }
}
