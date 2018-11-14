package controllers;

import java.security.interfaces.RSAPublicKey;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import model.User;
import utils.Hashing;
import utils.Log;

public class UserController {

  String token = null;

  private static DatabaseController dbCon;

  public UserController() {
    dbCon = new DatabaseController();
  }

  public static User getUser(int id) {

    // Check for connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Build the query for DB
    String sql = "SELECT * FROM user where id=" + id;

    // Actually do the query
    ResultSet rs = dbCon.query(sql);
    User user = null;

    try {
      // Get first object, since we only have one
      if (rs.next()) {
        user =
            new User(
                rs.getInt("id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("password"),
                rs.getString("email"),
                rs.getLong("created_at"));


        // return the create object
        return user;
      } else {
        System.out.println("No user found");
      }
    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }

    // Return null
    return user;
  }

  /**
   * Get all users in database
   *
   * @return
   */
  public static ArrayList<User> getUsers() {

    // Check for DB connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Build SQL
    String sql = "SELECT * FROM user";

    // Do the query and initialyze an empty list for use if we don't get results
    ResultSet rs = dbCon.query(sql);
    ArrayList<User> users = new ArrayList<User>();

    try {
      // Loop through DB Data
      while (rs.next()) {
        User user =
            new User(
                rs.getInt("id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("password"),
                rs.getString("email"),
                rs.getLong("created_at"));

        // Add element to list
        users.add(user);
      }
    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }

    // Return the list of users
    return users;
  }

  public static User createUser(User user) {

    Hashing hashing = new Hashing();

    // Write in log that we've reach this step
    Log.writeLog(UserController.class.getName(), user, "Actually creating a user in DB", 0);

    // Set creation time for user.
    user.setCreatedTime(System.currentTimeMillis() / 1000L);

    // Check for DB Connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Insert the user in the DB
    // TODO: Hash the user password before saving it - FIXED
    int userID = dbCon.insert(
        "INSERT INTO user(first_name, last_name, password, email, created_at) VALUES('"
            + user.getFirstname()
            + "', '"
            + user.getLastname()
            + "', '"
            + Hashing.sha(user.getPassword()) //Oprettede bruger får nu hashet deres password, tjek med postman
            + "', '"
            + user.getEmail()
            + "', "
            + user.getCreatedTime()
            + ")");

    if (userID != 0) {
      //Update the userid of the user before returning
      user.setId(userID);
    } else{
      // Return null if user has not been inserted into database
      return null;
    }

    // Return user
    return user;
  }

  public static int deleteUser(int idToDelete) {

    int result = -2;
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    String sql = "DELETE FROM user WHERE id =" + idToDelete;

    result = dbCon.deleteUser(sql);

    return  result;
  }

  public String login(User user) {

    Log.writeLog(UserController.class.getName(), user, "Login", 0);

    // Check for connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Build the query for DB
    String sql = "SELECT * FROM user WHERE email= '" + user.getEmail() + "' AND password='" + Hashing.sha(user.getPassword())+"'";
    // select from user wnere id = 1;
    // select from user where email = 'test@example.com' AND

    ResultSet rs = dbCon.query(sql);
    User loginUser = null;


    try {
      // Get first object, since we only have one
      if (rs.next()) {
        loginUser = new User(
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("password"),
                        rs.getString("email"),
                        rs.getLong("created_at"));


            try {
              Algorithm algorithm = Algorithm.HMAC256("secret");
              token = JWT.create()
                      .withIssuer("auth0").withClaim("userId", loginUser.id)
                      .sign(algorithm);
            } catch (JWTCreationException exception){
              //Invalid Signing configuration / Couldn't convert Claims.
            }

            return token;
          } else {
        System.out.println("Wrong username or password");
      }


    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }

    return null;
  }

  public static String AuthUser(User userLogin) {
    ArrayList<User> allTheUsers = UserController.getUsers();

    for(User user : allTheUsers) {
      if (user.getEmail().equals(userLogin.getEmail())){

        //hashing.LoginHashWithSalt(String.valueOf(user.getCreatedTime()));

        String password = Hashing.sha(userLogin.getPassword());

        if(password.equals(user.getPassword())) {
          //hashing.setLoginSalt(String.valueOf(System.currentTimeMillis()/100L));

          String token = user.getFirstname()+user.getLastname()+user.getEmail();

          //token = hashing.LoginHashWithSalt(token);

          updateToken(user.id,token);

          return token;

        }

      }
    }

    return null;
  }

  private static void updateToken(int id, String token) {
    Log.writeLog(UserController.class.getName(), token, "Updating token in database", 0);

    if(dbCon == null) {
      dbCon = new DatabaseController();
    }

    String sql = "UPDATE dis.user SET token = '" + token + "' where id = " + id;

    dbCon.voidToDB(sql);

  }
/*
  public String delete(User user) {



    Log.writeLog(UserController.class.getName(), user, "Get loggin in user", 0);

    if (dbCon == null) {
      dbCon = new DatabaseController();
    }




    return null;
  }

public boolean delete(String token) {

  DecodedJWT jwt = null;
  try {
    DecodedJWT jwtx = JWT.decode(token);
  } catch (JWTDecodeException exception) {

  }


  //Log.writeLog(UserController.class.getName(), user, "Get loggin in user", 0);

  if (dbCon == null) {
    dbCon = new DatabaseController();
  }




  return null;
}
*/
}
