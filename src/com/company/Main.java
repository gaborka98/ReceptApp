package com.company;


import com.company.Forms.LoginScreen;
import com.company.Forms.MainMenu;
import com.company.MyClass.User;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowEvent;

public class Main {
    private static User loggedIn = null;

    public static void main(String[] args) {
        LoginScreen loginScreen = new LoginScreen();
        loginScreen.setVisible(true);

    }
}
