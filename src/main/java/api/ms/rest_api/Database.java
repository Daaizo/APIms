package api.ms.rest_api;

import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.IndexOptions;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.RoutingContext;

import java.util.List;
import java.util.UUID;

public class Database {

  private final MongoClient client;
  private final Authorization authorization;
  private final String collectionName = "users";

  public Database(Authorization authorization, Vertx vertx) {
    this.authorization = authorization;
    client = createDatabaseClient(vertx);
    createIndexIfDatabaseIsNotCreated();
  }

  private MongoClient createDatabaseClient(Vertx vertx) {
    String dbName = "test";
    JsonObject config = new JsonObject()
      .put("db_name", dbName)
      .put("connection_string", "mongodb://localhost:27017");
    return MongoClient.create(vertx, config);
  }

  private void createIndexIfDatabaseIsNotCreated() {
    client.getCollections(listAsyncResult -> {
      if (listAsyncResult.succeeded() && listAsyncResult.result().stream().noneMatch(s -> s.equals(collectionName))) {
        client.createIndexWithOptions(collectionName, new JsonObject().put("login", 1), new IndexOptions().unique(true));
      }
    });
  }

  public void saveUser(JsonObject jsonObject, RoutingContext context) {
    JsonObject query = new JsonObject().put("login", jsonObject.getValue("login"));
    client.find(collectionName, query, listAsyncResult -> {
      if (listAsyncResult.succeeded() && listAsyncResult.result().isEmpty()) {
        jsonObject.put("_id", UUID.randomUUID().toString());
        client.save(collectionName, jsonObject);
        this.responseWithTextAndCode("Registering successfull", 204, context);
      } else this.responseWithTextAndCode("User with this login exists", 409, context);
    });
  }


  public void getAllUsers(RoutingContext context) {
    JsonObject entries = new JsonObject();
    client.find(collectionName, entries, res -> {
      if (res.succeeded()) {
        List<JsonObject> jsonObjectList = res.result();
        this.responseWithTextAndCode(Json.encodePrettily(jsonObjectList), 200, context);
      }
    });
  }

  public void checkIfUsersIsInDatabase(JsonObject dataFromLogin, RoutingContext context) {
    JsonObject query = new JsonObject().put("login", dataFromLogin.getValue("login"));
    client.find(collectionName, query, listAsyncResult -> {
      if (listAsyncResult.succeeded() && !listAsyncResult.result().isEmpty()) {
        JsonObject dataFromDb = listAsyncResult.result().get(0);
        if (verifyUserIdentity(dataFromLogin, dataFromDb)) {
          String token = authorization.createToken(dataFromDb.getString("login"));
          authorization.checkToken(token);
          JsonObject jsonObject = new JsonObject().put("token", token);
          this.responseWithJsonAndCode(jsonObject, 200, context);
        } else this.responseWithTextAndCode("Login or password is incorrect", 409, context);
      } else this.responseWithTextAndCode("Login or password is incorrect", 409, context);

    });
  }


  public void addItemForUser(JsonObject item, String userLogin, RoutingContext context) {
    JsonObject query = new JsonObject().put("login", userLogin);
    client.find(collectionName, query, listAsyncResult -> {
      if (listAsyncResult.succeeded()) {
        JsonObject user = listAsyncResult.result().get(0);
        String userId = user.getString("_id");
        System.out.println("userId = " + userId);
        System.out.println("item " + item.encodePrettily());
        JsonObject itemJson = new JsonObject(); // TODO wywoloac item z konstruktora klasy Item lub z metody jakiejs
        itemJson
          .put("_id", UUID.randomUUID().toString())
          .put("owner", userId)
          .put("name", item.getValue("title"));
        client.save(collectionName, itemJson);
        this.responseWithTextAndCode("Item created successfull", 204, context);
      }
    });
  }


  public boolean verifyUserIdentity(JsonObject dataFromLogin, JsonObject dataFromDb) {
    return dataFromLogin.getString("password").equals(dataFromDb.getString("password"));
  }

  public void responseWithJsonAndCode(JsonObject jsonObject, int statusCode, RoutingContext context) {
    context
      .response()
      .setStatusCode(statusCode)
      .putHeader("content-type", "application/json; charset=utf-8")
      .end(Json.encodePrettily(jsonObject));
  }

  public void responseWithTextAndCode(String text, int statusCode, RoutingContext context) {
    context
      .response()
      .setStatusCode(statusCode)
      .putHeader("content-type", "text/html; charset=UTF-8")
      .end(text);
  }


}
