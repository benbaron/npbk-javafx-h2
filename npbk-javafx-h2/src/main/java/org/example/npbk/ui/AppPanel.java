package org.example.npbk.ui;

import javafx.scene.Node;

/** Basic contract for the main workspace panels. */
public interface AppPanel {
    String title();
    Node root();

    default void onRefresh() {
    }

    default void onSave() {
    }

    default void onPrintText() {
    }

    default void onExportXlsx() {
    }

    default void onExportPdf() {
    }
}
