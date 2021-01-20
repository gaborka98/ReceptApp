package com.company;

import com.company.MyClass.User;
import com.mysql.cj.protocol.Resultset;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

public class MysqlConnector {
    private static MysqlConnector instance = null;
    private Connection conn ;

    private static final String host = "jdbc:mysql://192.168.1.56:3306/ReceptApp";

    public static MysqlConnector getInstance() {
        if (instance == null){
            instance = new MysqlConnector();
        }
        return instance;
    }

    private MysqlConnector() {
        try {
            System.out.println("Kapcsolódás...");
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(host, "recept", "recept");
            System.out.println("Kapcsolat letrejott");
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    private void checkConnection(){
        if (conn == null) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                conn = DriverManager.getConnection(host, "recept", "recept");
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
            prep.setBoolean(4, userToAdd.getModerator());

            action = prep.executeUpdate();

            prep.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return action > 0;
    }

    public Boolean changePasswordByEmail(String email, String hash) {
        checkConnection();
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

    public void updatePlayer(User updatedPlayer) {
        // TODO
    }

    public List<User> getAllModerator() {
        checkConnection();
        List<User> moderators = new ArrayList<>();
        try {
            PreparedStatement prep = conn.prepareStatement("SELECT * FROM users WHERE moderator = ?");
            prep.setBoolean(1, true);

            ResultSet rs = prep.executeQuery();

            while (rs.next()) {
                moderators.add(new User(rs.getString("username"), rs.getString("hash"), rs.getString("email"), rs.getBoolean("moderator")));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return moderators;
    }

    public String encryptStringSha256(char[] text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(charsToBytes(text));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] charsToBytes(char[] chars){
        Charset charset = Charset.forName("UTF-8");
        ByteBuffer byteBuffer = charset.encode(CharBuffer.wrap(chars));
        return Arrays.copyOf(byteBuffer.array(), byteBuffer.limit());
    }
}
