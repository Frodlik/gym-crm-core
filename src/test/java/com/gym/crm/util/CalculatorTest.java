package com.gym.crm.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CalculatorTest {

    private final Calculator calculator = new Calculator();

    @Test
    void add_ShouldReturnCorrectSum() {
        int result = calculator.add(5, 3);
        assertEquals(8, result);
    }

    @Test
    void divide_ShouldReturnQuotient_WhenDividingNonZero() {
        int result = calculator.divide(10, 2);
        assertEquals(5, result);
    }

    @Test
    void divide_ShouldThrowException_WhenDividingByZero() {
        ArithmeticException exception = assertThrows(
                ArithmeticException.class,
                () -> calculator.divide(10, 0)
        );
        assertEquals("Cannot divide by zero", exception.getMessage());
    }
}
