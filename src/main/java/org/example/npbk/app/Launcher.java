package org.example.npbk.app;

/**
 * Backward-compatible launcher name.
 *
 * EclipseLauncher is the preferred class to run/debug from Eclipse, but this
 * class is kept so existing Maven and launch configurations continue to work.
 */
public final class Launcher {
    private Launcher() {
    }

    public static void main(String[] args) {
        EclipseLauncher.main(args);
    }
}
