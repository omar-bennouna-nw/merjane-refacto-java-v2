package com.nimbleways.springboilerplate.exceptions;

/**
 * Erreur de données : le type d'un produit n'est pas reconnu par le système.
 */
public class UnknownProductTypeException extends RuntimeException {

    /**
     * @param productName nom du produit concerné
     * @param type        type non reconnu (peut être {@code null})
     */
    public UnknownProductTypeException(String productName, String type) {
        super("Unknown product type '" + type + "' for product '" + productName + "'");
    }
}
