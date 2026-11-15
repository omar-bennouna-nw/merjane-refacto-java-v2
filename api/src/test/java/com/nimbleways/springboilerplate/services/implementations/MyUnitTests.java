package com.nimbleways.springboilerplate.services.implementations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.exceptions.UnknownProductTypeException;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.utils.Annotations.UnitTest;

@ExtendWith(SpringExtension.class)
@UnitTest
public class MyUnitTests {

    @Mock
    private NotificationService notificationService;
    @Mock
    private ProductRepository productRepository;
    @InjectMocks
    private ProductService productService;

    private final LocalDate today = LocalDate.now();

    @Test
    public void notifyDelaySavesLeadTimeAndNotifies() {
        Product product = new Product(null, 15, 0, "NORMAL", "RJ45 Cable", null, null, null);

        productService.notifyDelay(product.getLeadTime(), product);

        assertEquals(0, product.getAvailable());
        assertEquals(15, product.getLeadTime());
        verify(productRepository).save(product);
        verify(notificationService).sendDelayNotification(15, "RJ45 Cable");
    }

    @ParameterizedTest
    @CsvSource({
            // available, leadTime, expectedAvailable, delayNotified
            "5, 10, 4, false",
            "0, 10, 0, true",
            "0, 0, 0, false"
    })
    public void normalProduct(int available, int leadTime, int expectedAvailable, boolean delayNotified) {
        Product product = new Product(null, leadTime, available, "NORMAL", "USB Cable", null, null, null);

        productService.processProduct(product);

        assertEquals(expectedAvailable, product.getAvailable());
        verify(notificationService, delayNotified ? times(1) : never())
                .sendDelayNotification(anyInt(), anyString());
        verify(notificationService, never()).sendOutOfStockNotification(anyString());
    }

    @ParameterizedTest
    @CsvSource({
            // startOffset, endOffset, available, leadTime, expectedAvailable, outOfStock, delay
            "-2, 58, 30, 15, 29, false, false",
            "-2, 58, 0, 15, 0, false, true",
            "-2, 10, 0, 15, 0, true, false",
            "180, 240, 30, 15, 30, true, false",
            "-10, -1, 30, 15, 0, true, false"
    })
    public void seasonalProduct(int startOffset, int endOffset, int available, int leadTime,
            int expectedAvailable, boolean outOfStock, boolean delay) {
        Product product = new Product(null, leadTime, available, "SEASONAL", "Watermelon", null,
                today.plusDays(startOffset), today.plusDays(endOffset));

        productService.processProduct(product);

        assertEquals(expectedAvailable, product.getAvailable());
        verify(notificationService, outOfStock ? times(1) : never()).sendOutOfStockNotification("Watermelon");
        verify(notificationService, delay ? times(1) : never()).sendDelayNotification(anyInt(), anyString());
    }

    @ParameterizedTest
    @CsvSource({
            // expiryOffset, available, expectedAvailable, expirationNotified
            "26, 30, 29, false",
            "-2, 6, 0, true",
            "26, 0, 0, true"
    })
    public void expirableProduct(int expiryOffset, int available, int expectedAvailable,
            boolean expirationNotified) {
        LocalDate expiry = today.plusDays(expiryOffset);
        Product product = new Product(null, 15, available, "EXPIRABLE", "Milk", expiry, null, null);

        productService.processProduct(product);

        assertEquals(expectedAvailable, product.getAvailable());
        verify(notificationService, expirationNotified ? times(1) : never())
                .sendExpirationNotification(anyString(), any());
    }

    @ParameterizedTest
    @CsvSource(value = { "FOO", "normal", "null" }, nullValues = "null")
    public void unknownTypeFailsFast(String type) {
        Product product = new Product(null, 1, 1, type, "Mystery", null, null, null);

        assertThrows(UnknownProductTypeException.class, () -> productService.processProduct(product));

        verifyNoInteractions(productRepository, notificationService);
    }
}

