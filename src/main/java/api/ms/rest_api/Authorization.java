package api.ms.rest_api;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;

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
      new JsonObject().put("name", login), new JWTOptions().setExpiresInMinutes(10));
  }
}
