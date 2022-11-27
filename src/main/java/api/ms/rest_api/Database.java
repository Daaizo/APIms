package api.ms.rest_api;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

public class Database extends AbstractVerticle {

  private final MongoClient client;
  private final Vertx vertx;

  public Database(Vertx vertx) {
    this.vertx = vertx;
    JsonObject config = new JsonObject()
      .put("db_name", "test")
      .put("connection_string",
        "mongodb://localhost:27017");
    client = MongoClient.create(vertx, config);
  }

  public void saveUser(JsonObject jsonObject) {
    client.save("users", jsonObject, res -> {
      if (res.succeeded()) {
        String id = res.result();
        System.out.println("Saved user with id " + id);
      } else {
        res.cause().printStackTrace();
      }
    });
  }
}
