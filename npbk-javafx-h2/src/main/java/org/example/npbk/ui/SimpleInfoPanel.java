package org.example.npbk.ui;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

/** Simple placeholder panel used for shell destinations that are not implemented yet. */
public class SimpleInfoPanel implements AppPanel {
    private final String title;
    private final BorderPane root = new BorderPane();

    public SimpleInfoPanel(String title, String heading, String body) {
        this.title = title;
        Label h = new Label(heading);
        h.getStyleClass().add("h1");
        Label b = new Label(body);
        b.setWrapText(true);
        VBox box = new VBox(10, h, b);
        box.setPadding(new Insets(16));
        root.setCenter(box);
    }

    @Override
    public String title() {
        return title;
    }

    @Override
    public Node root() {
        return root;
    }
}
