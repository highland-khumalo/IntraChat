
package com.IntraChat;

import com.IntraChat.gui.Structure;

/**
 * Main application launcher for IntraChat.
 *
 * @author bonginkosi0607
 */
public class IntraChat {

    public static void main(String[] args) {
        try {
            com.formdev.flatlaf.FlatDarkLaf.setup();
        } catch (Throwable ignored) {
            System.out.print("unable to use FlatDark");
        }

        var frame = new Structure();
        frame.start();
    }
}
