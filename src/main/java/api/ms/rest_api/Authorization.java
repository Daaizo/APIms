package api.ms.rest_api;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.web.RoutingContext;

public class Authorization {
  private final JWTAuth provider;

  public Authorization(Vertx vertx) {
    this.provider = JWTAuth.create(vertx, new JWTAuthOptions()
      .addPubSecKey(new PubSecKeyOptions()
        .setAlgorithm("HS256")
        .setBuffer("some secret password")));
  }

  public String createToken(String login) {
    return provider.generateToken(
      new JsonObject().put("name", login));
  }

  public JWTAuth getProvider() {
    return provider;
  }

  public void failedAuthentication(RoutingContext context) {
    context
      .response()
      .putHeader("Content-type", "application/json; charset=utf-8")
      .setStatusCode(401)
      .setStatusMessage("You have not provided an authentication token, the one provided has expired, was revoked or is not authentic.")
      .end(context.failure().getMessage());
  }
}
