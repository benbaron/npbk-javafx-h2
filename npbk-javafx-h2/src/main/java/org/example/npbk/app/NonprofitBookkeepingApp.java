package org.example.npbk.app;

import org.example.npbk.db.Database;
import org.example.npbk.ui.MainView;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class NonprofitBookkeepingApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        Database database = new Database();
        database.initialize();

        MainView mainView = new MainView(database);
        Scene scene = new Scene(mainView.getRoot(), 1250, 760);
        scene.getStylesheets().add(getClass().getResource("/org/example/npbk/app/app.css").toExternalForm());

        stage.setTitle("NonprofitBookkeeping - Spreadsheet-Style Prototype");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
