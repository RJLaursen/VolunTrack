//(Mostly a testing file, do not mind)

import model.DatabaseManager;
import model.UserDirectAccessObject;

public class TestUpdatePassword {
    public static void main(String[] args) {
        try {
            UserDirectAccessObject userDAO =
                new UserDirectAccessObject(DatabaseManager.getInstance().getConnection());

            int userId = 1; //User ID
            String newPassword = "newSecurePass123"; //Test new password

            //Validate password before updating
            if (!UserDirectAccessObject.isValidPassword(newPassword)) {
                System.out.println("Password does not meet requirements.");
                return;
            }

            //Attempt update
            boolean success = userDAO.updatePassword(userId, newPassword);
            if (success) {
                System.out.println("Password updated successfully for user ID " + userId);
            } else {
                System.out.println("assword update failed.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
