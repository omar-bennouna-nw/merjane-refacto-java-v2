package com.nimbleways.springboilerplate.services.implementations;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.exceptions.OrderNotFoundException;
import com.nimbleways.springboilerplate.repositories.OrderRepository;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductService productService;

    /**
     * @param orderRepository dépôt des commandes
     * @param productService  service de gestion des produits
     */
    public OrderService(OrderRepository orderRepository, ProductService productService) {
        this.orderRepository = orderRepository;
        this.productService = productService;
    }

    /**
     * Traite chaque produit d'une commande. L'opération est atomique : si un
     * produit est invalide, aucune modification n'est conservée.
     *
     * @param orderId identifiant de la commande
     * @return l'identifiant de la commande traitée
     * @throws OrderNotFoundException      si la commande n'existe pas
     * @throws com.nimbleways.springboilerplate.exceptions.UnknownProductTypeException
     *                                     si un produit a un type non reconnu
     */
    @Transactional
    public Long processOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        order.getItems().forEach(productService::processProduct);
        return order.getId();
    }
}
