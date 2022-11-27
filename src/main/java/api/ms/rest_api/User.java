package api.ms.rest_api;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.util.UUID;

public class User extends AbstractVerticle {
  String id;
  String login;
  String password;
  private Database database;

  public User() {
    this.database = new Database(Vertx.vertx());
  }

  public User(String login, String password) {
    this.id = UUID.randomUUID().toString();
    this.login = login;
    this.password = password;
  }

  public User(JsonObject jsonObject) {
    this.id = jsonObject.getString("_id");
    this.login = jsonObject.getString("login");
    this.password = jsonObject.getString("password");

  }


  public void addUser(RoutingContext context) {
    context
      .request()
      .bodyHandler(buffer -> {
        JsonObject object = buffer.toJsonObject();
        if (!isJsonValid(object)) {
          userNotRegisteredResponse(context);
          return;
        }
        database.saveUser(object);
        userRegisteredResponse(context);

      });
  }

  private boolean isJsonValid(JsonObject jsonObject) {
    if (jsonObject.isEmpty() || !jsonObject.containsKey("login") || !jsonObject.containsKey("password")) {
      return false;
    }
    return Validate.isLoginValid(jsonObject.getString("login")) && Validate.isPasswordStrong(jsonObject.getString("password"));
  }

  private void userNotRegisteredResponse(RoutingContext context) {
    context
      .response()
      .setStatusCode(404)
      .setStatusMessage("Login or password invalid")
      .end("please provide valid password and login");

  }

  private void userRegisteredResponse(RoutingContext context) {
    context
      .response()
      .setStatusCode(204)
      .setStatusMessage("Registering successfull")
      .end();
  }

  public void getAllUsers(RoutingContext context) {
    database.getAllUsers(context);

  }

  JsonObject getUserAsJson() {
    return new JsonObject()
      .put("login", login)
      .put("password", password);
  }

}
