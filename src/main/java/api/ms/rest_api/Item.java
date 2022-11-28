package api.ms.rest_api;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class Item {
  private final Database database;

  public Item(Database database) {
    this.database = database;
  }


  public void addItem(RoutingContext context) {
    // String token = context.request().getHeader("Authorization");
    // String tokenValue = token.substring("Bearer".length());
    String tokenOwnersLogin = context.user().principal().getString("name");
    context
      .request()
      .bodyHandler(buffer -> {
        JsonObject item = buffer.toJsonObject();
        database.addItemForUser(item, tokenOwnersLogin, context);
      });
  }
}
