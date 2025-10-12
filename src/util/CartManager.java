package util;

import model.Project;
import java.util.ArrayList;
import java.util.List;

//Manages a temporary cart for a user's selected projects
public class CartManager {

    private static final List<Project> cart = new ArrayList<>();

    //Add a project to the cart (only if not already added)
    public static boolean addToCart(Project project) {
        if (cart.stream().anyMatch(p -> p.getId() == project.getId())) {
            return false; //Already added
        }
        cart.add(project);
        return true;
    }

    //Remove a project from the cart
    public static void removeFromCart(Project project) {
        cart.removeIf(p -> p.getId() == project.getId());
    }

    //Get all cart items
    public static List<Project> getCartItems() {
        return cart;
    }

    //Clear all items (after confirmation or logout and whatnot)
    public static void clearCart() {
        cart.clear();
    }

    //Check if the cart is empty
    public static boolean isEmpty() {
        return cart.isEmpty();
    }
}
