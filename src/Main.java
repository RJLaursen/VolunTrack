//(Mostly a testing file, do not mind)

import model.DatabaseManager;
import model.UserDirectAccessObject;

public class Main {
    public static void main(String[] args) {
        try {
            DatabaseManager db = DatabaseManager.getInstance();
            UserDirectAccessObject userDAO = new UserDirectAccessObject(db.getConnection());

            //Test signup
            boolean created = userDAO.registerUser(
                "John Doe", "johndoe", "john@example.com", "Password123!", false
            );
            System.out.println("User created: " + created);

            //Test login
            boolean loginOk = userDAO.loginUser("johndoe", "Password123!");
            System.out.println("Login success: " + loginOk);

            //Test login with wrong password
            boolean loginFail = userDAO.loginUser("johndoe", "wrongpass");
            System.out.println("Login success with wrong pass: " + loginFail);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
