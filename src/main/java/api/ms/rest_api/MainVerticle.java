package api.ms.rest_api;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.JWTAuthHandler;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MainVerticle extends AbstractVerticle {
  //to start from main instead of console
  private static final Logger logger = LoggerFactory.getLogger(MainVerticle.class);

  public static void main(String[] args) {
    MainVerticle mainVerticle = new MainVerticle();
    mainVerticle.configureLogger();

    Vertx vertx1 = Vertx.vertx();
    Router router = Router.router(vertx1);

    mainVerticle.startServer(vertx1, router);
    mainVerticle.setEndpoints(vertx1, router);

  }

  private void setEndpoints(Vertx vertx1, Router router) {
    Authorization authorization = new Authorization(vertx1);
    Database db = new Database(authorization, vertx1);
    Item item = new Item(db);
    User user = new User(db);
    router
      .post("/register")
      .handler(user::registerUser);

    router
      .get("/all")
      .handler(user::getAllUsers);

    router
      .post("/login")
      .handler(user::handleUserLogin);

    router
      .route("/items/*")
      .handler(JWTAuthHandler.create(authorization.getProvider()));

    router
      .post("/items")
      .handler(item::addItem);


    router
      .get("/items")
      .handler(context1 -> {
        context1.response().end("some user items");
      });

  }

  private void startServer(Vertx vertx1, Router router) {
    HttpServer server = vertx1.createHttpServer();
    server
      .requestHandler(router)
      .listen(3000)
      .onSuccess(server1 -> logger.info("server listening on port " + server1.actualPort()))
      .onFailure(throwable -> System.out.println("throwable = " + throwable.getCause()));
  }

  private void configureLogger() {
    BasicConfigurator.configure();
    LogManager.getLogger("org.mongodb.driver.cluster").setLevel(org.apache.log4j.Level.OFF);
    LogManager.getLogger("io.netty.buffer.PooledByteBufAllocator").setLevel(org.apache.log4j.Level.OFF);
    LogManager.getLogger("org.mongodb.driver.protocol.command").setLevel(org.apache.log4j.Level.OFF);
  }
}
