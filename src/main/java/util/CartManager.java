package util;

import java.util.ArrayList;
import java.util.List;

import model.Project;

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

    //Updates an existing project entry in the cart list
    public static void updateItem(Project updatedProject) {
        for (int i = 0; i < cart.size(); i++) {
            Project p = cart.get(i);
            if (p.getId() == updatedProject.getId()) {
                cart.set(i, updatedProject);
                break;
            }
        }
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
