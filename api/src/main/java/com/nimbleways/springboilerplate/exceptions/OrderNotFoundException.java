package com.nimbleways.springboilerplate.exceptions;

/**
 * La commande demandée n'existe pas.
 */
public class OrderNotFoundException extends RuntimeException {

    /**
     * @param orderId identifiant de la commande introuvable
     */
    public OrderNotFoundException(Long orderId) {
        super("Order " + orderId + " not found");
    }
}