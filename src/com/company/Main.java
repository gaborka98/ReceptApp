package com.company;


import com.company.Forms.LoginScreen;
import com.company.MyClass.User;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowEvent;

public class Main {
    private static User loggedIn = null;

    public static void main(String[] args) {
        LoginScreen loginScreen = new LoginScreen();
        loginScreen.setVisible(true);

/*        loginScreen.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                loggedIn = loginScreen.getLoggedIn();
                if (loggedIn != null) {
                    MainMenu menu = new MainMenu(loggedIn);
                    menu.setvisible(true);
                }
            }
        });*/

        loginScreen.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentHidden(ComponentEvent e) {
                loggedIn = loginScreen.getLoggedIn();
                /*if (loggedIn != null) {
                    MainMenu menu = new MainMenu(loggedIn);
                    menu.setvisible(true);
                }*/
                System.out.println("siker");
            }
        });

    }
}
