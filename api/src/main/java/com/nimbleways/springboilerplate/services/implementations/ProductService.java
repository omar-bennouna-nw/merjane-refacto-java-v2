package com.nimbleways.springboilerplate.services.implementations;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.exceptions.UnknownProductTypeException;
import com.nimbleways.springboilerplate.repositories.ProductRepository;

/**
 * Logique métier des produits : décrémentation du stock et notifications
 * selon le type de produit (NORMAL, SEASONAL, EXPIRABLE).
 */
@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final NotificationService notificationService;

    /**
     * @param productRepository   dépôt des produits
     * @param notificationService service de notification des clients
     */
    public ProductService(ProductRepository productRepository, NotificationService notificationService) {
        this.productRepository = productRepository;
        this.notificationService = notificationService;
    }

    /**
     * Traite la commande d'une unité d'un produit selon son type.
     *
     * @param product le produit commandé
     * @throws UnknownProductTypeException si le type du produit n'est pas reconnu
     */
    public void processProduct(Product product) {
        String type = product.getType();
        if (type == null) {
            throw new UnknownProductTypeException(product.getName(), null);
        }
        switch (type) {
            case "NORMAL" -> processNormal(product);
            case "SEASONAL" -> processSeasonal(product);
            case "EXPIRABLE" -> processExpirable(product);
            default -> throw new UnknownProductTypeException(product.getName(), type);
        }
    }

    /**
     * Enregistre le délai de réapprovisionnement du produit et en notifie les clients.
     *
     * @param leadTime délai de réapprovisionnement en jours
     * @param product  le produit concerné
     */
    public void notifyDelay(int leadTime, Product product) {
        product.setLeadTime(leadTime);
        productRepository.save(product);
        notificationService.sendDelayNotification(leadTime, product.getName());
    }

    private void processNormal(Product product) {
        if (product.getAvailable() > 0) {
            decrementStock(product);
        } else if (product.getLeadTime() > 0) {
            notifyDelay(product.getLeadTime(), product);
        }
    }

    private void processSeasonal(Product product) {
        LocalDate today = LocalDate.now();
        boolean inSeason = today.isAfter(product.getSeasonStartDate())
                && today.isBefore(product.getSeasonEndDate());
        if (inSeason && product.getAvailable() > 0) {
            decrementStock(product);
        } else if (today.plusDays(product.getLeadTime()).isAfter(product.getSeasonEndDate())) {
            notificationService.sendOutOfStockNotification(product.getName());
            product.setAvailable(0);
            productRepository.save(product);
        } else if (product.getSeasonStartDate().isAfter(today)) {
            notificationService.sendOutOfStockNotification(product.getName());
            productRepository.save(product);
        } else {
            notifyDelay(product.getLeadTime(), product);
        }
    }

    private void processExpirable(Product product) {
        if (product.getAvailable() > 0 && product.getExpiryDate().isAfter(LocalDate.now())) {
            decrementStock(product);
        } else {
            notificationService.sendExpirationNotification(product.getName(), product.getExpiryDate());
            product.setAvailable(0);
            productRepository.save(product);
        }
    }

    private void decrementStock(Product product) {
        product.setAvailable(product.getAvailable() - 1);
        productRepository.save(product);
    }
}