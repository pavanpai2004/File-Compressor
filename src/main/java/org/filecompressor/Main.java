package org.filecompressor;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import java.net.URL;
import java.util.logging.Logger;

public class Main extends Application {

    Logger logger = Logger.getLogger(getClass().getName());

    @Override
    public void start(Stage primaryStage) {

        try {
            URL fxmlUrl = getClass().getResource("file_compressor.fxml");

            if (fxmlUrl == null) {
                throw new NullPointerException("FXML file not found");
            }

            Parent root = FXMLLoader.load(fxmlUrl);
            Scene scene = new Scene(root,720,550);


            primaryStage.setScene(scene);
            primaryStage.setTitle("File Compressor");
            primaryStage.setResizable(false);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        launch(args);
    }
}
