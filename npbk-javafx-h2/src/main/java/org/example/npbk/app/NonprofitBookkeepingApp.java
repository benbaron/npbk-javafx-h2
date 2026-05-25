package org.example.npbk.app;

import org.example.npbk.db.Database;
import org.example.npbk.ui.MainWindow;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class NonprofitBookkeepingApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        Database database = new Database();
        database.initialize();

        MainWindow mainWindow = new MainWindow(database);
        Scene scene = new Scene(mainWindow, 1350, 820);
        scene.getStylesheets().add(getClass().getResource("/org/example/npbk/app/app.css").toExternalForm());

        stage.setTitle("NonprofitBookkeeping - Workbook-Modeled Accounting Prototype");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
