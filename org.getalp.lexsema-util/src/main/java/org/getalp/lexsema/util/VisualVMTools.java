package org.getalp.lexsema.util;

import java.util.Scanner;

public final class VisualVMTools {
    public static void delayUntilReturn() {
        Scanner s = new Scanner(System.in);
        //noinspection UseOfSystemOutOrSystemErr
        System.err.println("Press Enter to continue...");
        s.nextLine();
    }

    private VisualVMTools() {
    }
}
