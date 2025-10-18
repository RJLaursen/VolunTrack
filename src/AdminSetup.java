//(Mostly a testing file, do not mind)
//(Primarily it's to create the admin user, there's probably a better way but this works fine)

import model.DatabaseManager;
import model.UserDirectAccessObject;

public class AdminSetup {
    public static void main(String[] args) {
        try {
            DatabaseManager db = DatabaseManager.getInstance();
            UserDirectAccessObject userDAO = new UserDirectAccessObject(db.getConnection());

            //Admin name email and boolean (All users are set to "false" except for Admin)
            boolean adminCreated = userDAO.registerUser(
                "System Administrator",
                "admin",
                "admin@voluntrack.com",
                "Admin654!@",
                true
            );
            System.out.println("Admin created: " + adminCreated);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
