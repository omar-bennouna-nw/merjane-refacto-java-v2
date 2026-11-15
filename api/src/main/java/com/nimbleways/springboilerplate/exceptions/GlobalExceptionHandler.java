package com.nimbleways.springboilerplate.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Traduit les exceptions métier en réponses HTTP.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Corps de réponse d'erreur.
     *
     * @param message description de l'erreur
     */
    public record ErrorResponse(String message) {
    }

    /**
     * @param e exception levée pour un type de produit inconnu
     * @return une réponse 422 contenant le message d'erreur
     */
    @ExceptionHandler(UnknownProductTypeException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ErrorResponse handleUnknownProductType(UnknownProductTypeException e) {
        return new ErrorResponse(e.getMessage());
    }

    /**
     * @param e exception levée pour une commande inexistante
     * @return une réponse 404 contenant le message d'erreur
     */
    @ExceptionHandler(OrderNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleOrderNotFound(OrderNotFoundException e) {
        return new ErrorResponse(e.getMessage());
    }
}