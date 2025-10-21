package test;

import model.Project;
import util.CartManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;

//JUnit Test (adding, duplication check, deleting , and clearing cart)
public class CartLogicTest {

    private static Project sampleProject;

    @BeforeEach
    void setup() {
        CartManager.clearCart();
        sampleProject = new Project(1, "Test Project", "Melbourne", "Mon", 20.0, 10, 0, true);
    }

    @Test
    void testAddToCart_Success() {
        assertTrue(CartManager.addToCart(sampleProject), "Project should be added to cart");
    }

    @Test
    void testAddToCart_Duplicate() {
        CartManager.addToCart(sampleProject);
        assertFalse(CartManager.addToCart(sampleProject), "Duplicate project should not be added");
    }

    @Test
    void testRemoveFromCart() {
        CartManager.addToCart(sampleProject);
        CartManager.removeFromCart(sampleProject);
        assertTrue(CartManager.isEmpty(), "Cart should be empty after removal");
    }

    @Test
    void testClearCart() {
        CartManager.addToCart(sampleProject);
        CartManager.clearCart();
        assertTrue(CartManager.isEmpty(), "Cart should be cleared");
    }
}
