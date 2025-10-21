package test;

import model.UserDirectAccessObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

//JUnit Test (password and email validation tests)
public class ValidationTest {

    @Test
    void testPasswordValidation_Valid() {
        assertTrue(UserDirectAccessObject.isValidPassword("Password123!"), "Valid password should pass");
    }

    @Test
    void testPasswordValidation_Invalid_NoUppercase() {
        assertFalse(UserDirectAccessObject.isValidPassword("password123!"), "Missing uppercase should fail");
    }

    @Test
    void testPasswordValidation_Invalid_NoNumber() {
        assertFalse(UserDirectAccessObject.isValidPassword("Password!"), "Missing number should fail");
    }

    @Test
    void testPasswordValidation_Invalid_NoSymbol() {
        assertFalse(UserDirectAccessObject.isValidPassword("Password123"), "Missing special char should fail");
    }

    @Test
    void testEmailValidation_Pattern() {
        String validEmail = "user@example.com";
        String invalidEmail = "userexample.com";

        assertTrue(validEmail.matches("^[\\w.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"), "Valid email should match");
        assertFalse(invalidEmail.matches("^[\\w.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"), "Invalid email should fail");
    }
}
