package api.ms.rest_api;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import org.apache.log4j.BasicConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MainVerticle extends AbstractVerticle {
  //to start from main instead of console
  private static final Logger logger = LoggerFactory.getLogger(MainVerticle.class);

  public static void main(String[] args) {
    BasicConfigurator.configure();
    Vertx vertx1 = Vertx.vertx();
    Router router = Router.router(vertx1);
    HttpServer server = vertx1.createHttpServer();
    server
      .requestHandler(router)
      .listen(3000)
      .onSuccess(server1 -> logger.info("server listening on port " + server1.actualPort()))
      .onFailure(throwable -> System.out.println("throwable = " + throwable.getCause()));

    //router - controller
    router
      .post("/add")
      .handler(User::addUser);


//
//    User u1 = new User("daniel", "12");
//    Route deafultRoute =
//      router.get().handler(context1 -> {
//        context1
//          .response()
//          .setStatusCode(201)
//          .end(u1.getUserAsJson().toString());
//      });


  }

}
