package com.company;

import com.company.MyClass.Filter;
import com.company.MyClass.Ingredient;
import com.company.MyClass.Recipe;
import com.company.MyClass.User;

import javax.xml.transform.Result;
import java.lang.reflect.Type;
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
            if (rs.next()) {
                return new User(rs.getInt("user_id"), rs.getString("username"), rs.getString("hash"), rs.getString("email"), rs.getBoolean("moderator"), rs.getInt("storage_id"));
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
            if (rs.next()) {
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
            stmt.executeUpdate("UPDATE users SET storage_id = null WHERE user_id = " + loggedIn.getId());

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
                toReturn.add(new Recipe(rs.getInt("recipe_id"), rs.getString("name"), rs.getString("description"), rs.getString("category"), rs.getInt("difficulty"), rs.getInt("allergies_id"), toAdd, getAllRecipeIngredientByRecipeId(rs.getInt("recipe_id"))));
            }
            prep.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return toReturn;
    }

    public boolean insertRecipe(Recipe recipe) {
        checkConnection();
        int catecory_id = getCategoryIdByName(recipe.getCategory());
        int allergies_id = insertAllergies(recipe.getAllergies());
        try {
            PreparedStatement prep = conn.prepareStatement("insert into recipes(name, description, category_id, difficulty, allergies_id) values (?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
            prep.setString(1,recipe.getName());
            prep.setString(2,recipe.getDescription());
            prep.setInt(3,catecory_id);
            prep.setInt(4,recipe.getDifficulty());
            prep.setInt(5,allergies_id);

            prep.executeUpdate();

            ResultSet rs = prep.getGeneratedKeys();
            int insertedId = 0;
            if (rs.next()) { insertedId = rs.getInt(1); }

            prep.close();

            if (insertedId == 0 ) {throw new SQLException("inserted row lekerese nem sikerult");}
            for (Ingredient iter : recipe.getIngredients()) {
                addIngredientToDatabaseByRecipeId(insertedId, iter.getName(), iter.getMeasure());
            }
        } catch (SQLException e) {
            System.out.println("Recept adatbazishoz adasa kozben hiba lepett fel");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private int insertAllergies(HashMap<String, Boolean> allergies) {
        checkConnection();
        try {
            PreparedStatement prep = conn.prepareStatement("insert into allergies(laktoz, gluten, hus, tojas, cukor) values (?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
            prep.setBoolean(1, allergies.get("laktoz"));
            prep.setBoolean(2, allergies.get("gluten"));
            prep.setBoolean(3, allergies.get("hus"));
            prep.setBoolean(4, allergies.get("tojas"));
            prep.setBoolean(5, allergies.get("cukor"));

            prep.executeUpdate();

            ResultSet rs = prep.getGeneratedKeys();

            if (rs.next()) { return rs.getInt(1); }

            prep.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private int getCategoryIdByName(String name) {
        checkConnection();
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("select category_id from categories where name = '" + name + "'");
            if (rs.next()) { return rs.getInt("category_id"); }
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
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
                recipeToReturn = new Recipe(rs.getInt("recipe_id"), rs.getString("name"), rs.getString("description"), rs.getString("category"), rs.getInt("difficulty"), rs.getInt("allergies_id"), tempAllergies, getAllRecipeIngredientByRecipeId(rs.getInt("recipe_id")));
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
                recipes.add(new Recipe(rs.getInt("recipe_id"), rs.getString("name"), rs.getString("description"), rs.getString("category"), rs.getInt("difficulty"), rs.getInt("allergies_id"), allergies, getAllRecipeIngredientByRecipeId(rs.getInt("recipe_id"))));
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
                recipes.add(new Recipe(rs.getInt("recipe_id"), rs.getString("name"), rs.getString("description"), rs.getString("category"), rs.getInt("difficulty"), rs.getInt("allergies_id"), allergies, getAllRecipeIngredientByRecipeId(rs.getInt("recipe_id"))));
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
            PreparedStatement prep = conn.prepareStatement("select i.ingredient_id as ingredient_id, i.name as name, i.measure as measure, pi.`group` as 'group', i.unit as 'unit' from ingredients i inner join pre_ingrediets pi on i.name=pi.name  where storage_id = ?");
            prep.setInt(1, id);

            ResultSet rs = prep.executeQuery();

            while (rs.next()) {
                toReturn.add(new Ingredient(rs.getInt("ingredient_id"), rs.getString("name"), rs.getInt("measure"), rs.getString("unit") == null ? getDefaultUnitByGroup(rs.getInt("group")) : rs.getString("unit"), rs.getInt("group")));
            }

            prep.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return toReturn;
    }

    public ArrayList<Ingredient> getAllRecipeIngredientByRecipeId(int id) {
        ArrayList<Ingredient> toReturn = new ArrayList<>();
        checkConnection();
        try {
            PreparedStatement prep = conn.prepareStatement("select i.ingredient_id as ingredient_id, i.name as name, i.measure as measure, pi.`group` as 'group', i.unit as 'unit' from ingredients i inner join pre_ingrediets pi on i.name=pi.name  where recipe_id = ?");
            prep.setInt(1, id);

            ResultSet rs = prep.executeQuery();

            while (rs.next()) {
                toReturn.add(new Ingredient(rs.getInt("ingredient_id"), rs.getString("name"), rs.getInt("measure"), rs.getString("unit") == null ? getDefaultUnitByGroup(rs.getInt("group")) : rs.getString("unit"), rs.getInt("group")));
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

            if (rs.next()) {
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

    public ArrayList<String> getAllUnitByIngredientName(String ingredientName) {
        ArrayList<String> toReturn = new ArrayList<>();
        checkConnection();
        try {
            PreparedStatement prep = conn.prepareStatement("select measure from ing_groups ig where `group` = (select `group` from pre_ingrediets pi left join ingredients i on pi.name = i.name where pi.name = ? LIMIT 1)");
            prep.setString(1, ingredientName);

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

    public void addIngredientToDatabaseByRecipeId(int recipeId, String name, Double measure) {
        checkConnection();
        try {
            PreparedStatement prep = conn.prepareStatement("insert into ingredients(name, measure, recipe_id) values (?,?,?)");
            prep.setString(1, name);
            prep.setDouble(2, measure);
            prep.setInt(3, recipeId);
            prep.executeUpdate();
            prep.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addIngredientToStorage(int userId, int storageId, Ingredient add) {
        checkConnection();
        try {
            addStatistic(2, userId, null, add.getId());
            PreparedStatement prep = conn.prepareStatement("insert into ingredients(ingredient_id, name, measure, storage_id, unit) VALUES (?, ?, ?, ?, ?) on duplicate key update measure = measure + ?;");
            int id = getIngredientIdByNameAndStorageId(add.getName(), storageId);

            if (id == -1) {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT `auto_increment` FROM INFORMATION_SCHEMA.TABLES WHERE table_name = 'ingredients'");
                if (rs.next()) {
                    prep.setInt(1, rs.getInt("auto_increment"));
                }
            } else prep.setInt(1, id);

            prep.setString(2, add.getName());
            prep.setDouble(3, add.getMeasure());
            prep.setInt(4, storageId);
            prep.setDouble(6, add.getMeasure());
            if (add.getGroup() == 3) {
                prep.setString(5,add.getUnit());
            } else {
                prep.setNull(5, Types.NULL);
            }

            prep.executeUpdate();

            prep.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<String[]> getLowIngredientsFromStorage(int storageId) {
        checkConnection();
        ArrayList<String[]> toReturn = new ArrayList<>();
        try {
            PreparedStatement prep = conn.prepareStatement("select i.name, measure, unit from ingredients i inner join pre_ingrediets pi on i.name = pi.name where storage_id is not null and pi.`group` in (1,2) and measure < 500 and storage_id = ? union select i.name, measure, unit from ingredients i inner join pre_ingrediets pi on i.name = pi.name where storage_id is not null and pi.`group` in (3) and measure < 3 and storage_id = ?");
            prep.setInt(1, storageId);
            prep.setInt(2, storageId);

            ResultSet rs = prep.executeQuery();

            while (rs.next()) {
                String[] temp = new String[3];
                temp[0] = rs.getString(1);
                temp[1] = rs.getString(2);
                temp[2] = rs.getString(3);
                toReturn.add(temp);
            }
            prep.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return toReturn;
    }

    private int getIngredientIdByNameAndStorageId(String name, int storageId) {
        checkConnection();
        try {
            PreparedStatement prep = conn.prepareStatement("select ingredient_id from ingredients where name = ? AND storage_id = ?");
            prep.setString(1,name);
            prep.setInt(2, storageId);

            ResultSet rs = prep.executeQuery();

            if (rs.next()) { return rs.getInt("ingredient_id"); }

            prep.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int getIngredientGroupByName(String name) {
        checkConnection();
        try {
            PreparedStatement prep = conn.prepareStatement("select `group` from pre_ingrediets where name = ?");
            prep.setString(1, name);

            ResultSet rs = prep.executeQuery();
            if (rs.next()) {
                return rs.getInt("group");
            }
            prep.close();
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void makeRecipe(int recipeId, int storageId, int userId) {
        checkConnection();
        ArrayList<Ingredient> recipe = getAllRecipeIngredientByRecipeId(recipeId);
        ArrayList<Ingredient> storage = getAllStorageIngredientByStorageId(storageId);

        for (Ingredient recipeIter : recipe) {
            for (Ingredient storageIter : storage) {
                if (recipeIter.getName().equals(storageIter.getName())) {
                    double maradek = storageIter.getMeasure() - recipeIter.getMeasure();
                    try {
                        addStatistic(3, userId, null, recipeIter.getId()); // alapanyag levonas staisztika
                        PreparedStatement prep = conn.prepareStatement("UPDATE ingredients set measure = ? where storage_id = ? AND name = ?");
                        prep.setDouble(1, maradek);
                        prep.setInt(2, storageId);
                        prep.setString(3, recipeIter.getName());

                        prep.executeUpdate();

                        System.out.println(prep.toString());

                        prep.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        addStatistic(1, userId, recipeId, null); // recept keszites statisztika
    }

    public void addIngredientToDatabase(String ingName, int ingGroup) {
        checkConnection();
        try {
            PreparedStatement prep = conn.prepareStatement("INSERT INTO pre_ingrediets(name, `group`) values(? ,?)");
            prep.setString(1,ingName);
            prep.setInt(2,ingGroup);

            prep.executeUpdate();

            prep.close();
        } catch (SQLException e) {
            System.out.println("Hiba a hozzavalo hozzaadasaban (addIngredientToDatabase)");
            e.printStackTrace();
        }
    }

    public boolean addCategory(String newCategory) {
        checkConnection();
        try {
            PreparedStatement prep = conn.prepareStatement("insert into categories(name) values (?)");
            prep.setString(1,newCategory);
            prep.executeUpdate();
            prep.close();
        } catch (SQLException e) {
            System.out.println("Hiba a kategoria beszurasa kozben!");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void deleteIngredientByRecipeId(int recipeId) {
        checkConnection();
        try {
            PreparedStatement prep = conn.prepareStatement("delete from ingredients where recipe_id=?");
            prep.setInt(1, recipeId);
            prep.executeUpdate();
            prep.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean deleteRecipeById(Integer recipeId, Integer allergies_id) {
        checkConnection();
        try {
            deleteIngredientByRecipeId(recipeId);
            deleteAllergiesById(allergies_id);
            Statement stmt = conn.createStatement();
            stmt.executeUpdate("delete from recipes where recipe_id = '" + recipeId + "'");
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void deleteAllergiesById(Integer allergies_id) {
        checkConnection();
        try {
            PreparedStatement prep = conn.prepareStatement("delete from allergies where allergie_id = ?");
            prep.setInt(1, allergies_id);
            prep.executeUpdate();
            prep.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateAllergies(HashMap<String, Boolean> allergies, int allergieId) {
        checkConnection();
        try {
            PreparedStatement prep = conn.prepareStatement("update allergies set cukor = ?, gluten = ?, hus = ?, laktoz = ?, tojas = ? where allergie_id = ?");
            prep.setBoolean(1, allergies.get("cukor"));
            prep.setBoolean(2, allergies.get("gluten"));
            prep.setBoolean(3, allergies.get("hus"));
            prep.setBoolean(4, allergies.get("laktoz"));
            prep.setBoolean(5, allergies.get("tojas"));
            prep.setInt(6, allergieId);

            prep.executeUpdate();

            prep.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateIngredients(ArrayList<Ingredient> ings, int recipeId) {
        checkConnection();
        try {
            PreparedStatement prep = conn.prepareStatement("delete from ingredients where recipe_id = ?");
            prep.setInt(1,recipeId);
            prep.executeUpdate();
            prep.close();

            for (Ingredient iter: ings) {
                addIngredientToDatabaseByRecipeId(recipeId, iter.getName(), iter.getMeasure());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean updateRecipeById(int id, Recipe toAdd) {
        checkConnection();
        try {
            updateAllergies(toAdd.getAllergies(), toAdd.getAllergies_id());
            updateIngredients(toAdd.getIngredients(), id);
            PreparedStatement prep = conn.prepareStatement("update recipes set name = ?, description = ?, category_id = ?, difficulty = ? where recipe_id = ?");
            prep.setString(1,toAdd.getName());
            prep.setString(2, toAdd.getDescription());
            prep.setInt(3, getCategoryIdByName(toAdd.getCategory()));
            prep.setInt(4,toAdd.getDifficulty());
            prep.setInt(5, id);

            prep.executeUpdate();

            prep.close();
        }catch (SQLException e) {
            System.out.println("A recept frissitese kozben hiba tortent");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void addStatistic(int type, Integer userId, Integer recipeId, Integer ingId) {
        checkConnection();
        try {
            PreparedStatement prep = conn.prepareStatement("insert into statistic (statistic_type_id, user_id, recipe_id, ingredient_id) values (?, ?, ?, ?)");
            prep.setInt(1, type);
            prep.setInt(2, userId);
            if (recipeId != null) {
                prep.setInt(3, recipeId);
            } else {
                prep.setNull(3, Types.NULL);
            }
            if (ingId != null) {
                prep.setInt(4, ingId);
            } else {
                prep.setNull(4, Types.NULL);
            }

            prep.executeUpdate();

            prep.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String topFiveRecipeStat(Integer userId) {
        checkConnection();
        StringBuilder sb = new StringBuilder();
        try {
            PreparedStatement prep = conn.prepareStatement("select r.name as  'name', COUNT(r.recipe_id) as 'count' from statistic s\n" +
                    "left join recipes r on r.recipe_id = s.recipe_id\n" +
                    "left join users u on u.user_id = s.user_id\n" +
                    "where statistic_type_id = 1\n" +
                    "and s.user_id = ?\n" +
                    " group by r.recipe_id order by count desc limit 5;");

            prep.setInt(1,userId);

            ResultSet rs = prep.executeQuery();

            while(rs.next()) {
                sb.append(" - ").append(rs.getString("name")).append("\n");
            }

            prep.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return sb.toString();
    }

    public String topFiveIngredientStat(Integer userId) {
        checkConnection();
        StringBuilder sb = new StringBuilder();
        try {
            PreparedStatement prep = conn.prepareStatement("select i.name as name, COUNT(i.ingredient_id) as count from statistic s\n" +
                    "left join users u on s.user_id = u.user_id\n" +
                    "left join ingredients i on i.ingredient_id = s.ingredient_id\n" +
                    "where statistic_type_id = 3 \n" +
                    "and u.user_id = ?\n" +
                    "group by i.ingredient_id limit 5;");
            prep.setInt(1, userId);

            ResultSet rs = prep.executeQuery();

            while (rs.next()) {
                sb.append(" - ").append(rs.getString("name")).append("\n");
            }
            prep.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return sb.toString();
    }
}
