package example.armeria.server.blog.thrift;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linecorp.armeria.server.Server;
import com.linecorp.armeria.server.ServerBuilder;
import com.linecorp.armeria.server.docs.DocService;
import com.linecorp.armeria.server.thrift.THttpService;

import example.armeria.server.blog.thrift.BlogPost;

public final class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        final Server server = newServer(8080);

        server.closeOnJvmShutdown().thenRun(() -> {
          logger.info("Server has been stopped.");
        });

        server.start().join();

        logger.info("Server has been started. Serving DocService at http://127.0.0.1:{}/docs",
                    server.activeLocalPort());
    }

    static Server newServer(int httpPort) throws Exception {
      final THttpService thriftService =
      THttpService.builder()
                  .addService(new BlogServiceImpl())
                  .build();

      final BlogPost exampleRequest = new BlogPost();
      exampleRequest.setTitle("My first blog");
      exampleRequest.setContent("Hello Armeria!");

      return Server.builder()
                  .http(httpPort)
                  .service("/", thriftService)
                  .service("/second", thriftService)
                  // You can access the documentation service at http://127.0.0.1:8080/docs.
                  // See https://armeria.dev/docs/server-docservice for more information.
                  .serviceUnder("/docs",
                                DocService.builder()
                                          .exampleRequests("example.armeria.blog.thrift.BlogService",
                                          "CreateBlogPost", exampleRequest)
                                          .build())
                  .build();
    }

    private Main() {}
}
