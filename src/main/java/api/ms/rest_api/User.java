package api.ms.rest_api;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.util.UUID;

public class User extends AbstractVerticle {
  String id;
  String login;
  String password;
  private Database database;

  public User(Database database) {
    this.database = database;
  }

  public User(String login, String password) {
    this.id = UUID.randomUUID().toString();
    this.login = login;
    this.password = password;
  }

  public User(JsonObject jsonObject) {
    this.id = UUID.randomUUID().toString();
    this.login = jsonObject.getString("login");
    this.password = Password.encryptPassword(jsonObject.getString("password"));
  }

  public JsonObject getUserAsJson() {
    return new JsonObject()
      .put("_id", this.id)
      .put("login", this.login)
      .put("password", this.password);
  }


  public void registerUser(RoutingContext context) {
    context
      .request()
      .bodyHandler(buffer -> {
        JsonObject object = buffer.toJsonObject();
        if (!isJsonValid(object)) {
          database.responseWithTextAndCode("please provide valid password and login", 422, context);
          return;
        }
        database.saveUser(object, context);

      });
  }

  private boolean isJsonValid(JsonObject jsonObject) {
    if (jsonObject.isEmpty() || !jsonObject.containsKey("login") || !jsonObject.containsKey("password")) {
      return false;
    }
    return Validate.isLoginValid(jsonObject.getString("login")) && Validate.isPasswordStrong(jsonObject.getString("password"));
  }


  public void getAllUsers(RoutingContext context) {
    database.getAllUsers(context);
  }

  public void handleUserLogin(RoutingContext context) {
    context
      .request()
      .bodyHandler(buffer -> {
        JsonObject object = buffer.toJsonObject();
        database.checkIfUsersIsInDatabaseAndResponseWithToken(object, context);
      });
  }
}
