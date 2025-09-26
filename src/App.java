import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

//JavaFX entry point for VolunTrack
//Loads the login screen as the first scene
public class App extends Application {
    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Login.fxml"));

        Scene scene = new Scene(loader.load());
        stage.setTitle("VolunTrack - Login");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
