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
        this.responseWithTextAndCode("Registering successfull", 200, context);
      } else this.responseWithTextAndCode("User with this login exists", 404, context);
    });
  }


  public void getAllUsers(RoutingContext context) {
    JsonObject entries = new JsonObject();
    client.find(collectionName, entries, res -> {
      if (res.succeeded()) {
        List<JsonObject> jsonObjectList = res.result();
        context.response().end(Json.encodePrettily(jsonObjectList));
        for (JsonObject jsonObject : res.result()) {
          System.out.println("jsonObject = " + jsonObject);
        }
      }
    });
  }

  public void checkIfUsersIsInDatabase(JsonObject dataFromLogin, RoutingContext context) {
    JsonObject query = new JsonObject().put("login", dataFromLogin.getValue("login"));

//    Future<List<JsonObject>> listFuture = client.find(collectionName, query);
//    System.out.println("listFuture = " + listFuture.succeeded());
    // not workign ??????????????????????

    client.find(collectionName, query, listAsyncResult -> {
      if (listAsyncResult.succeeded() && !listAsyncResult.result().isEmpty()) {
        JsonObject dataFromDb = listAsyncResult.result().get(0);
        if (verifyUserIdentity(dataFromLogin, dataFromDb)) {
          context
            .response()
            .setStatusCode(200)
            .putHeader("content-type", "text/html; charset=UTF-8")
            .end(authorization.createToken(dataFromDb.getString("login")));
        } else {
          context
            .response()
            .setStatusCode(401)
            .putHeader("content-type", "text/html; charset=UTF-8")
            .end("Login or password is incorrect");
        }
      } else {
        context
          .response()
          .setStatusCode(401)
          .putHeader("content-type", "text/html; charset=UTF-8")
          .end("Login or password is incorrect");
      }
    });
    //working ?????????? this is the SAMEEE

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

}
