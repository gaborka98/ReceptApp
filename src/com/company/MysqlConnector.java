package com.company;

import com.company.MyClass.User;

import java.sql.*;

public class MysqlConnector {
    private Connection conn ;

    public MysqlConnector() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://192.168.1.56:3306/ReceptApp", "recept", "recept");

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    private void checkConnection(){
        if (conn == null) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/ReceptApp", "recept", "recept");
            } catch (ClassNotFoundException | SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public User getLoggedInUser(String username) {
        checkConnection();
        try {
            PreparedStatement prep = conn.prepareStatement("SELECT * FROM users WHERE username = ?");
            prep.setString(1, username);
            ResultSet rs = prep.executeQuery();
            while (rs.next()) {
                User loggedIn = new User(rs.getString("username"), rs.getString("hash"), rs.getString("email"), rs.getBoolean("moderator"));
                return loggedIn;
            }
            prep.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    public User getLoggedInUserByEmail(String email) {
        checkConnection();
        try {
            PreparedStatement prep = conn.prepareStatement("SELECT * FROM users WHERE email = ?");
            prep.setString(1, email);
            ResultSet rs = prep.executeQuery();
            while (rs.next()) {
                return new User(rs.getString("username"), rs.getString("hash"), rs.getString("email"), rs.getBoolean("moderator"));
            }
            prep.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Boolean addUserToDatabase(User userToAdd) {
        checkConnection();
        int action = -2;
        try {
            PreparedStatement prep = conn.prepareStatement("INSERT INTO users (username, hash, email, moderator) VALUES (?,?,?,?)");
            prep.setString(1, userToAdd.getUsername());
            prep.setString(2, userToAdd.getHash());
            prep.setString(3, userToAdd.getEmail());
            prep.setBoolean(4, userToAdd.isModerator());

            action = prep.executeUpdate();

            prep.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return action > 0;
    }

    public Boolean changePasswordByEmail(String email, String hash) {
        try {
            PreparedStatement prep = conn.prepareStatement("UPDATE users SET hash = ? WHERE email = ?");
            prep.setString(1, hash);
            prep.setString(2, email);

            prep.executeUpdate();

            prep.close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getPasswordHashByUsername(String username) {
        checkConnection();
        String passwordHash = "";
        try {
            PreparedStatement prep = conn.prepareStatement("SELECT hash FROM users WHERE username = ?", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            prep.setString(1, username);
            ResultSet rs = prep.executeQuery();
            if (!rs.next()) { return "nincs"; }
            rs.beforeFirst();
            while(rs.next()){
                passwordHash = rs.getString("hash");
            }
            prep.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return passwordHash;
    }
}
