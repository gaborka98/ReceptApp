package com.company;

import com.company.MyClass.Filter;
import com.company.MyClass.Ingredient;
import com.company.MyClass.Recipe;
import com.company.MyClass.User;
import com.mysql.cj.QueryResult;
import com.mysql.cj.protocol.Resultset;
import com.mysql.cj.x.protobuf.MysqlxPrepare;

import javax.xml.transform.Result;
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
                User loggedIn = new User(rs.getInt("user_id"), rs.getString("username"), rs.getString("hash"), rs.getString("email"), rs.getBoolean("moderator"), rs.getInt("storage_id"));
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
                } else throw new SQLException("Hiba az utoljára beszúrt sor lekérése közben");
            }
            stmt.close();
            PreparedStatement prep = conn.prepareStatement("UPDATE users SET storage_id = ? WHERE user_id = ?");
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
            PreparedStatement prep = conn.prepareStatement("DELETE FROM storages WHERE storage_id = ?");
            prep.setInt(1, loggedIn.getStorageId());
            prep.executeUpdate();
            prep.close();

            Statement stmt = conn.createStatement();
            stmt.executeUpdate("UPDATE users SET storage_id = -1 WHERE user_id = " + loggedIn.getId());

            deleteAllIngredientsByStorageId(loggedIn.getStorageId());
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        loggedIn.setStorageId(-1);
        return true;
    }

    private void deleteAllIngredientsByStorageId(int storageId) {
        checkConnection();
        try {
            PreparedStatement prep = conn.prepareStatement("delete from ingredients where storage_id = ?");
            prep.setInt(1, storageId);

            prep.executeUpdate();

            prep.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Recipe> getAllRecipe() {
        checkConnection();
        ArrayList<Recipe> toReturn = new ArrayList<>();
        try {
            PreparedStatement prep = conn.prepareStatement("select * from list_all_recipes");
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
            prep.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return toReturn;
    }

    public HashMap<String, Boolean> getAllergiesByRecipeId(int recipeId) {
        checkConnection();
        HashMap<String, Boolean> toAdd = new HashMap<>();
        try {
            PreparedStatement prep = conn.prepareStatement("SELECT * FROM list_all_recipes WHERE recipe_id = ?");
            prep.setInt(1, recipeId);
            ResultSet rs = prep.executeQuery();
            while (rs.next()) {
                rs.getInt("allergies_id");
                toAdd = new HashMap<>();
                if (!rs.wasNull()) {
                    toAdd.put("laktoz", rs.getBoolean("laktoz"));
                    toAdd.put("gluten", rs.getBoolean("gluten"));
                    toAdd.put("hus", rs.getBoolean("hus"));
                    toAdd.put("tojas", rs.getBoolean("tojas"));
                    toAdd.put("cukor", rs.getBoolean("cukor"));
                }
            }
            prep.close();
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return toAdd;
    }

    public Recipe getRecipeById(int id) {
        checkConnection();
        Recipe recipeToReturn = null;
        try {
            PreparedStatement prep = conn.prepareStatement("SELECT * FROM list_all_recipes WHERE recipe_id = ?");
            prep.setInt(1, id);
            ResultSet rs = prep.executeQuery();
            while (rs.next()) {
                HashMap<String, Boolean> tempAllergies = getAllergiesByRecipeId(id);
                if (tempAllergies.isEmpty()) {tempAllergies = null;}
                recipeToReturn = new Recipe(rs.getInt("recipe_id"), rs.getString("name"), rs.getString("description"), rs.getString("category"), rs.getInt("difficulty"), tempAllergies);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return recipeToReturn;
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

    public ArrayList<Recipe> getRecipesBySearch(String search) {
        checkConnection();
        ArrayList<Recipe> recipes = new ArrayList<>();
        try {
            PreparedStatement prep = conn.prepareStatement("SELECT * FROM list_all_recipes WHERE name like ?");
            prep.setString(1, '%' + search + '%');

            ResultSet rs = prep.executeQuery();

            while (rs.next()) {
                HashMap<String, Boolean> allergies = getAllergiesByRecipeId(rs.getInt("recipe_id"));
                if (allergies.isEmpty()) { allergies = null; }
                recipes.add(new Recipe(rs.getInt("recipe_id"), rs.getString("name"), rs.getString("description"), rs.getString("category"), rs.getInt("difficulty"), allergies));
            }
            prep.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if(recipes.isEmpty()) { return null; }
        return recipes;
    }

    public ArrayList<Recipe> getRecipesByFilter(Filter filter, User user) {
        checkConnection();
        ArrayList<Recipe> recipes = new ArrayList<>();
        if (filter == null) { return getAllRecipe(); }
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(filter.getCategory() == null ? "" : " AND categories_id = " + filter.getCategory());
            sb.append(filter.getDifficulty() == 0 ? "" : " AND difficulty <= " + filter.getDifficulty());

            sb.append((filter.getHus() ? " AND hus = 0" : ""));
            sb.append((filter.getLaktoz() ? " AND laktoz = 0" : ""));
            sb.append((filter.getCukor() ? " AND cukor = 0" : ""));
            sb.append((filter.getTojas() ? " AND tojas = 0" : ""));
            sb.append((filter.getGluten() ? " AND gluten = 0" : ""));

            PreparedStatement prep;
            if(filter.getFavorites()) {
                prep = conn.prepareStatement("select r.* from list_all_recipes r INNER JOIN favorites f ON f.recipe_id = r.recipe_id INNER JOIN users u ON u.user_id = f.user_id WHERE f.user_id = ? " + sb.toString());
                prep.setInt(1, user.getId());
            } else {
                sb.delete(0,5);
                prep = conn.prepareStatement("SELECT * FROM list_all_recipes " + (sb.length() == 0 ? "" : "WHERE") + sb.toString());
            }
            ResultSet rs = prep.executeQuery();
            while (rs.next()) {
                HashMap<String, Boolean> allergies = getAllergiesByRecipeId(rs.getInt("recipe_id"));
                if (allergies.isEmpty()) { allergies = null; }
                recipes.add(new Recipe(rs.getInt("recipe_id"), rs.getString("name"), rs.getString("description"), rs.getString("category"), rs.getInt("difficulty"), allergies));
            }
            prep.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if(recipes.isEmpty()) { return null; }
        return recipes;
    }

    public ArrayList<String> getAllCategory() {
        ArrayList<String> categories = new ArrayList<>();
        try {
            Statement stmt = conn.createStatement();

            ResultSet rs = stmt.executeQuery("SELECT name FROM categories");

            while (rs.next()) {
                categories.add(rs.getString("name"));
            }
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (categories.isEmpty()) { return null; }
        return categories;
    }

    public void removeFavorite(User loggedIn, String selectedRecipeId) {
        checkConnection();
        try {
            PreparedStatement prep = conn.prepareStatement("DELETE FROM favorites WHERE user_id = ? AND recipe_id = ?");
            prep.setInt(1, loggedIn.getId());
            prep.setInt(2, Integer.parseInt(selectedRecipeId));

            if (favoriteExist(loggedIn, selectedRecipeId)) {
                prep.executeUpdate();
            }

            prep.close();
        } catch (SQLException | NumberFormatException e) {
            e.printStackTrace();
        }
    }

    public void addFavorite(User loggedIn, String selectedRecipeId) {
        checkConnection();
        try {
            PreparedStatement prep = conn.prepareStatement("INSERT INTO favorites (user_id, recipe_id) VALUES (?,?)");
            prep.setInt(1, loggedIn.getId());
            prep.setInt(2, Integer.parseInt(selectedRecipeId));

            if (!favoriteExist(loggedIn, selectedRecipeId)) {
                prep.executeUpdate();
            }

            prep.close();
        } catch (SQLException | NumberFormatException e) {
            e.printStackTrace();
        }
    }

    public boolean favoriteExist(User loggedIn, String selectedRecipeId) {
        checkConnection();
        try {
            PreparedStatement prep = conn.prepareStatement("SELECT * FROM favorites WHERE user_id = ? AND recipe_id = ?");
            prep.setInt(1, loggedIn.getId());
            prep.setInt(2, Integer.parseInt(selectedRecipeId));

            ResultSet rs = prep.executeQuery();

            if (rs.next()) { return true; }

            prep.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    public boolean favoriteExist(User loggedIn, Recipe recipe) {
        checkConnection();
        try {
            PreparedStatement prep = conn.prepareStatement("SELECT * FROM favorites WHERE user_id = ? AND recipe_id = ?");
            prep.setInt(1, loggedIn.getId());
            prep.setInt(2, recipe.getId());

            ResultSet rs = prep.executeQuery();

            if (rs.next()) { return true; }

            prep.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public ArrayList<Ingredient> getAllStorageIngredientByStorageId(int id) {
        ArrayList<Ingredient> toReturn = new ArrayList<>();
        checkConnection();
        try {
            PreparedStatement prep = conn.prepareStatement("select i.ingredient_id as ingredient_id, i.name as name, i.measure as measure, pi.`group` as 'group' from ingredients i inner join pre_ingrediets pi on i.name=pi.name  where storage_id = ?");
            prep.setInt(1, id);

            ResultSet rs = prep.executeQuery();

            while (rs.next()) {
                toReturn.add(new Ingredient(rs.getInt("ingredient_id"), rs.getString("name"), rs.getInt("measure"), rs.getInt("group"), getDefaultUnitByGroup(rs.getInt("group"))));
            }

            prep.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return toReturn;
    }

    public int getMultipliByUnit(String unit) {
        String result = "";
        checkConnection();
        try {
            PreparedStatement prep = conn.prepareStatement("select multipli from ing_groups where measure = ?");
            prep.setString(1, unit);

            ResultSet rs = prep.executeQuery();

            while (rs.next()) {
                result = rs.getString("multipli");
            }

            prep.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Integer.parseInt(result);
    }

    public String getDefaultUnitByGroup(int group) {
        checkConnection();
        try {
            PreparedStatement prep = conn.prepareStatement("select measure from ing_groups where `group` = ? AND multipli = 1");
            prep.setInt(1, group);

            ResultSet rs = prep.executeQuery();

            while (rs.next()) {
                return rs.getString("measure");
            }
            prep.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    public void deleteIngredientsById(int id) {
        checkConnection();
        try {
            PreparedStatement prep = conn.prepareStatement("delete from ingredients where ingredient_id = ?");
            prep.setInt(1, id);

            prep.executeUpdate();

            prep.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<String> getAllUnitByIngredientName(String ingredietName) {
        ArrayList<String> toReturn = new ArrayList<>();
        checkConnection();
        try {
            PreparedStatement prep = conn.prepareStatement("select measure from ing_groups ig where `group` = (select `group` from pre_ingrediets pi left join ingredients i on pi.name = i.name where pi.name = ?)");
            prep.setString(1, ingredietName);

            ResultSet rs = prep.executeQuery();

            while (rs.next()) {
                toReturn.add(rs.getString("measure"));
            }

            prep.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return toReturn;
    }

    public ArrayList<String> getAllIngredients() {
        ArrayList<String> toReturn = new ArrayList<>();
        checkConnection();
        try {
            PreparedStatement prep = conn.prepareStatement("select name from pre_ingrediets");

            ResultSet rs = prep.executeQuery();

            while (rs.next()) {
                toReturn.add(rs.getString("name"));
            }

            prep.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return toReturn;
    }

    public void addIngredientToStorage(int storageId, Ingredient add) {
        checkConnection();
        try {
            PreparedStatement prep = conn.prepareStatement("insert into ingredients(ingredient_id, name, measure, storage_id) VALUES (?, ?, ?, ?) on duplicate key update measure = measure + ?;");
            int id = getIngredientIdByNameAndStorageId(add.getName(), storageId);

            if (id == -1) {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT `auto_increment` FROM INFORMATION_SCHEMA.TABLES WHERE table_name = 'ingredients'");
                if (rs.next()) {
                    prep.setInt(1, rs.getInt("auto_increment"));
                }
            }
            else prep.setInt(1, id);

            prep.setString(2, add.getName());
            prep.setDouble(3, add.getMeasure());
            prep.setInt(4, storageId);
            prep.setDouble(5, add.getMeasure());

            prep.executeUpdate();

            prep.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int getIngredientIdByNameAndStorageId(String name, int storageId) {
        checkConnection();
        try {
            PreparedStatement prep = conn.prepareStatement("select ingredient_id from ingredients where name = ? AND storage_id = ?");
            prep.setString(1,name);
            prep.setInt(2, storageId);

            ResultSet rs = prep.executeQuery();

            while (rs.next()) { return rs.getInt("ingredient_id"); }

            prep.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
}
