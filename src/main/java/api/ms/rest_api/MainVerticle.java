package api.ms.rest_api;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;

public class MainVerticle extends AbstractVerticle {
  //to start from main instead of console
  public static void main(String[] args) {
    Vertx vertx1 = Vertx.vertx();
    vertx1.deployVerticle(new MainVerticle());
  }

  private static void testRequest(HttpServerRequest request) {
    request
      .response()
      .putHeader("content-type", "text/plain")
      .end("Server up");
  }

  @Override
  public void start(Promise<Void> startPromise) {
    HttpServer httpServer = vertx.createHttpServer();
    httpServer
      .requestHandler(MainVerticle::testRequest)
      .listen(3000,httpServerAsyncResult -> {
          if(httpServerAsyncResult.succeeded()){
            System.out.println("HTTP server started on port " + httpServer.actualPort());
            startPromise.complete();
          }else{
            startPromise.fail(httpServerAsyncResult.cause());
          }
      });



  }
}
