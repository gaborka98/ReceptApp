package com.company;

import com.company.MyClass.Recipe;
import com.company.MyClass.User;
import com.mysql.cj.protocol.Resultset;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.*;

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
                User loggedIn = new User(rs.getInt("user_id"), rs.getString("username"), rs.getString("hash"), rs.getString("email"), rs.getBoolean("moderator"));
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
                return new User(rs.getInt("user_id"), rs.getString("username"), rs.getString("hash"), rs.getString("email"), rs.getBoolean("moderator"), rs.getInt("storage_id"));
            }
            prep.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Boolean addUserToDatabase(User userToAdd) {
        checkConnection();
        try {
            PreparedStatement prep = conn.prepareStatement("INSERT INTO users (username, hash, email, moderator) VALUES (?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
            prep.setString(1, userToAdd.getUsername());
            prep.setString(2, userToAdd.getHash());
            prep.setString(3, userToAdd.getEmail());
            prep.setBoolean(4, userToAdd.getModerator());

            prep.executeUpdate();
            int generatedId = -1;
            ResultSet rs = prep.getGeneratedKeys();
            while (rs.next()){
                generatedId = rs.getInt(1);
            }

            prep.close();

            userToAdd.setId(generatedId);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public Boolean changePasswordByEmail(String email, String hash) {
        checkConnection();
        try {
            PreparedStatement prep = conn.prepareStatement("UPDATE users SET hash = ? WHERE email = ?");
            prep.setString(1, hash);
            prep.setString(2, email);

            prep.executeUpdate();

            prep.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        return true;
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

    public Boolean updatePlayerModeratorByUsername(String username, Boolean state) {
        checkConnection();
        try {
            PreparedStatement prep = conn.prepareStatement("UPDATE users SET moderator = ? WHERE username = ?");
            prep.setBoolean(1,state);
            prep.setString(2, username);

            prep.executeUpdate();

            prep.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public ArrayList<User> getAllModerator() {
        checkConnection();
        ArrayList<User> moderators = new ArrayList<>();
        try {
            PreparedStatement prep = conn.prepareStatement("SELECT * FROM users WHERE moderator = ?");
            prep.setBoolean(1, true);

            ResultSet rs = prep.executeQuery();

            while (rs.next()) {
                moderators.add(new User(rs.getInt("user_id"), rs.getString("username"), rs.getString("hash"), rs.getString("email"), rs.getBoolean("moderator")));
            }

            prep.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return moderators;
    }

    public Boolean createStorageByUser(User user) {
        checkConnection();
        try {
            Statement stmt = conn.createStatement();
            int insertedId = 0;
            stmt.executeUpdate("INSERT INTO storages VALUES()", Statement.RETURN_GENERATED_KEYS);

            try(ResultSet rs = stmt.getGeneratedKeys()){
                if (rs.next()) {
                    insertedId = rs.getInt(1);
                }
                else throw new SQLException("Failed to get last inserted row's id");
            }
            stmt.close();
            PreparedStatement prep = conn.prepareStatement("UPDATE users SET storage_id = ? WHERE id = ?");
            prep.setInt(1, insertedId);
            prep.setInt(2, user.getId());
            prep.executeUpdate();

            user.setStorageId(insertedId);
            prep.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public Boolean deleteStorageByUser(User loggedIn) {
        checkConnection();
        try {
            PreparedStatement prep = conn.prepareStatement("DELETE FROM storages WHERE id = ?");
            prep.setInt(1, loggedIn.getStorageId());
            prep.executeUpdate();
            prep.close();

            Statement stmt = conn.createStatement();
            stmt.executeUpdate("UPDATE users SET storage_id = -1 WHERE id = " + loggedIn.getId());
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public ArrayList<Recipe> getAllRecipe() {
        checkConnection();
        ArrayList<Recipe> toReturn = new ArrayList<>();
        try {
            PreparedStatement prep = conn.prepareStatement("SELECT * FROM recipes LEFT JOIN allergies ON allergies_id = allergies.allergie_id");
            ResultSet rs = prep.executeQuery();
            while(rs.next()) {
                rs.getInt("allergies_id");
                HashMap<String, Boolean> toAdd = new HashMap<>();
                if (!rs.wasNull()) {
                    toAdd.put("laktoz", rs.getBoolean("laktoz"));
                    toAdd.put("gluten", rs.getBoolean("gluten"));
                    toAdd.put("hus", rs.getBoolean("hus"));
                    toAdd.put("tojas", rs.getBoolean("tojas"));
                    toAdd.put("cukor", rs.getBoolean("cukor"));
                }

                toReturn.add(new Recipe(rs.getInt("recipe_id"), rs.getString("name"), rs.getString("description"), rs.getString("category"), rs.getInt("difficulty"), toAdd));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return toReturn;
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
