package com.nimbleways.springboilerplate.contollers;

import com.nimbleways.springboilerplate.dto.product.ProcessOrderResponse;
import com.nimbleways.springboilerplate.services.implementations.OrderService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * API de gestion des commandes.
 */
@RestController
@RequestMapping("/orders")
public class MyController {

    private final OrderService orderService;

    /**
     * @param orderService service de gestion des commandes
     */
    public MyController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Traite une commande : décrémente le stock de ses produits et notifie les clients si besoin.
     *
     * @param orderId identifiant de la commande
     * @return l'identifiant de la commande traitée
     */
    @PostMapping("{orderId}/processOrder")
    @ResponseStatus(HttpStatus.OK)
    public ProcessOrderResponse processOrder(@PathVariable Long orderId) {
        return new ProcessOrderResponse(orderService.processOrder(orderId));
    }
}