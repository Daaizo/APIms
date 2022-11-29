package api.ms.rest_api;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.util.UUID;

public class Item {
  private UUID id;
  private String ownerId;
  private String name;
  private Database database;

  public Item(Database database) {
    this.database = database;
  }

  public Item(String ownerId, String name) {
    this.id = UUID.randomUUID();
    this.ownerId = ownerId;
    this.name = name;
  }

  public void addItem(RoutingContext context) {
    // String token = context.request().getHeader("Authorization");
    // String tokenValue = token.substring("Bearer".length());
    try {
      String tokenOwnersLogin = context.user().principal().getString("name");
      context
        .request()
        .bodyHandler(buffer -> {
          JsonObject item = buffer.toJsonObject();
          database.addItemForUser(item, tokenOwnersLogin, context);
        });
    } catch (RuntimeException e) {
      System.out.println("Unauthorized");
    }
  }

  public JsonObject getItemAsJson() {
    return
      new JsonObject()
        .put("_id", this.id.toString())
        .put("owner", this.ownerId)
        .put("name", this.name);
  }
}
