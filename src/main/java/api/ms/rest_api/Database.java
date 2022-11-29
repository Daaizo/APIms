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

  public void checkIfUsersIsInDatabaseAndResponseWithToken(JsonObject dataFromLogin, RoutingContext context) {
    client.find(collectionWithUsers, dataFromLogin, listAsyncResult -> {
      if (listAsyncResult.succeeded() && !listAsyncResult.result().isEmpty()) {
        JsonObject dataFromDb = listAsyncResult.result().get(0);
        JsonObject token = authorization.getToken(dataFromDb, dataFromLogin);
        if (token != null) this.responseWithJsonAndCode(token, context);
        else responseWithStatusDescriptionAndCode("Login or password is incorrect", 409, context);
      } else responseWithStatusDescriptionAndCode("Login or password is incorrect", 409, context);
    });
  }

  public void saveItem(JsonObject user, JsonObject itemFromRequestBody) {
    String userId = user.getString("_id");
    Item item = new Item(userId, itemFromRequestBody.getValue("title").toString());
    client.save(collectionWithItems, item.getItemAsJson());
  }

  public void addItemForUser(JsonObject itemFromRequestBody, RoutingContext context) {
    String userId = authorization.getTokenOwnersId(context);
    JsonObject query = new JsonObject().put("_id", userId);
    client.find(collectionWithUsers, query, listAsyncResult -> {
      if (listAsyncResult.succeeded()) {
        saveItem(listAsyncResult.result().get(0), itemFromRequestBody);
        responseWithStatusDescriptionAndCode("Item created successfull", 204, context);
      }
    });
  }


  public void getUsersItems(RoutingContext context) {
    String tokenOwnersId = authorization.getTokenOwnersId(context);
    JsonObject query = new JsonObject().put("owner", tokenOwnersId);
    client.find(collectionWithItems, query, listAsyncResult -> {
      if (listAsyncResult.succeeded()) {
        List<JsonObject> jsonObjectList = listAsyncResult.result();
        jsonObjectList.forEach(jsonObject -> jsonObject.remove("owner"));
        this.responseWithTextAndCode(Json.encodePrettily(jsonObjectList), 200, context);
      }
    });
  }

  private void responseWithJsonAndCode(JsonObject jsonObject, RoutingContext context) {
    int statusCode = 200;
    context
      .response()
      .setStatusCode(statusCode)
      .putHeader("content-type", "application/json; charset=utf-8")
      .end(Json.encodePrettily(jsonObject));
  }

  void responseWithTextAndCode(String text, int statusCode, RoutingContext context) {
    context
      .response()
      .setStatusCode(statusCode)
      .putHeader("content-type", "text/html; charset=UTF-8")
      .end(text);
  }

  private void responseWithStatusDescriptionAndCode(String text, int statusCode, RoutingContext context) {
    context
      .response()
      .setStatusCode(statusCode)
      .putHeader("content-type", "text/html; charset=UTF-8")
      .setStatusMessage(text)
      .end();
  }


}
