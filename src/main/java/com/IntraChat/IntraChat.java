
package com.IntraChat;

import com.IntraChat.gui.Structure;

/**
 * Main application launcher for IntraChat.
 *
 * @author bonginkosi0607
 */
public class IntraChat {

    /**
     *Entry point of the IntraChat application.
     * Initializes the user interface theme and
     * launches the login window.
     *
     * @param args Command-line argument
     */
    public static void main(String[] args) {
        try {
            com.formdev.flatlaf.FlatDarkLaf.setup();
        } catch (Throwable ignored) {
            System.out.print("unable to use FlatDark");
        }
        // y

        var frame = new Structure();
        frame.start();
    }
}
