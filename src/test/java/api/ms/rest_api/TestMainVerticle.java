package api.ms.rest_api;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.*;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class TestMainVerticle {

  private  Vertx vertx;

  @Before
  public void startServer(TestContext context) {
    vertx = Vertx.vertx();
    vertx.deployVerticle(
      MainVerticle.class.getName(),
      context.asyncAssertSuccess());
  }
  @After
  public void stopServer(TestContext context) {
    vertx.close(context.asyncAssertSuccess());
  }

  @Test
  public void testMyApplication(TestContext context) {
    final Async async = context.async();
    HttpClientOptions httpClientOptions = new HttpClientOptions().setDefaultPort(3000);
    HttpClient client = vertx.createHttpClient(httpClientOptions);
    
    Future<HttpClientRequest> request = client.request(HttpMethod.GET, "/");
    request.compose(req -> req.send().compose(HttpClientResponse::body));
    context.assertEquals("abc","abc");
  }
}
