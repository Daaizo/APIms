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
  private final String collectionWithUsers = "users";
  private final String collectionWithItems = "items";

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
      if (listAsyncResult.succeeded() && listAsyncResult.result().stream().noneMatch(s -> s.equals(collectionWithUsers))) {
        client.createIndexWithOptions(collectionWithUsers, new JsonObject().put("login", 1), new IndexOptions().unique(true));
      }
    });
  }

  public void saveUser(JsonObject jsonObject, RoutingContext context) {
    JsonObject query = new JsonObject().put("login", jsonObject.getValue("login"));

    client.find(collectionWithUsers, query, listAsyncResult -> {
      if (listAsyncResult.succeeded() && listAsyncResult.result().isEmpty()) {
        jsonObject.put("_id", UUID.randomUUID().toString());
        client.save(collectionWithUsers, jsonObject);
        this.responseWithStatusDescriptionAndCode("Registering successfull", 204, context);
      } else this.responseWithStatusDescriptionAndCode("User with this login exists", 409, context);
    });
  }


  public void getAllUsers(RoutingContext context) {
    client.find(collectionWithUsers, new JsonObject(), res -> {
      if (res.succeeded()) {
        List<JsonObject> jsonObjectList = res.result();
        this.responseWithTextAndCode(Json.encodePrettily(jsonObjectList), 200, context);
      }
    });
  }

  public void checkIfUsersIsInDatabase(JsonObject dataFromLogin, RoutingContext context) {
    // JsonObject query = new JsonObject().put("login", dataFromLogin.getValue("login"));
    client.find(collectionWithUsers, dataFromLogin, listAsyncResult -> {
      if (listAsyncResult.succeeded() && !listAsyncResult.result().isEmpty()) {
        JsonObject dataFromDb = listAsyncResult.result().get(0);
        if (verifyUserIdentity(dataFromLogin, dataFromDb)) {
          String token = authorization.createToken(dataFromDb.getString("login"));
          JsonObject jsonObject = new JsonObject().put("token", token);
          this.responseWithJsonAndCode(jsonObject, 200, context);
        } else this.responseWithStatusDescriptionAndCode("Login or password is incorrect", 409, context);
      } else this.responseWithStatusDescriptionAndCode("Login or password is incorrect", 409, context);

    });
  }

  public void saveItem(JsonObject user, JsonObject itemFromRequestBody) {
    String userId = user.getString("_id");
    Item item = new Item(userId, itemFromRequestBody.getValue("title").toString());
    client.save(collectionWithItems, item.getItemAsJson());
  }

  public void addItemForUser(JsonObject itemFromRequestBody, String userLogin, RoutingContext context) {
    JsonObject query = new JsonObject().put("login", userLogin);
    client.find(collectionWithUsers, query, listAsyncResult -> {
      if (listAsyncResult.succeeded()) {
        saveItem(listAsyncResult.result().get(0), itemFromRequestBody);
        responseWithStatusDescriptionAndCode("Item created successfull", 204, context);
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

  public void responseWithStatusDescriptionAndCode(String text, int statusCode, RoutingContext context) {
    context
      .response()
      .setStatusCode(statusCode)
      .putHeader("content-type", "text/html; charset=UTF-8")
      .setStatusMessage(text)
      .end();
  }


}
