package api.ms.rest_api;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.util.UUID;

public class User extends AbstractVerticle {
  UUID id;
  String login;
  String password;

  public User() {
  }

  public User(String login, String password) {
    this.id = UUID.randomUUID();
    this.login = login;
    this.password = password;
  }

  public static void addUser(RoutingContext context) {
    context.request().bodyHandler(buffer -> {
      JsonObject object = buffer.toJsonObject();
      System.out.println("object = " + object);
      Database database = new Database(Vertx.vertx());
      database.saveUser(object);
    });
    context
      .response()
      .setStatusCode(204)
      .putHeader("content-type", "application/json; charset=utf-8")
      .setStatusMessage("Registering successfull")
      .end();
  }

  JsonObject getUserAsJson() {
    return new JsonObject()
      .put("login", login)
      .put("password", password);
  }

}
