package org.example.npbk.app;

import javafx.application.Application;

/**
 * Eclipse/debug-friendly main class.
 *
 * Run this class with Debug As > Java Application instead of running
 * NonprofitBookkeepingApp directly. This class intentionally does not extend
 * javafx.application.Application, so Eclipse treats it as an ordinary Java main
 * class while Maven still supplies the OpenJFX dependencies.
 */
public final class EclipseLauncher {
    private EclipseLauncher() {
    }

    public static void main(String[] args) {
        Application.launch(NonprofitBookkeepingApp.class, args);
    }
}
