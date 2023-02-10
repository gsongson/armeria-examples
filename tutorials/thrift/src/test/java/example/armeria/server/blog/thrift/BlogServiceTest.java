package example.armeria.server.blog.thrift;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.RegisterExtension;

import org.apache.thrift.TException;
import org.apache.thrift.TApplicationException;

import com.linecorp.armeria.common.util.CompletionActions;
import com.linecorp.armeria.client.thrift.ThriftClients;
import com.linecorp.armeria.server.thrift.THttpService;
import com.linecorp.armeria.server.ServerBuilder;
import com.linecorp.armeria.testing.junit5.server.ServerExtension;

import example.armeria.server.blog.thrift.BlogPost;

@TestMethodOrder(OrderAnnotation.class)
class BlogServiceTest {

    @RegisterExtension
    static final ServerExtension server = new ServerExtension() {
        @Override
        protected void configure(ServerBuilder sb) throws Exception {
            sb.service("/", THttpService.builder()
                                    .addService(new BlogServiceImpl())
                                    .build());
        }
    };

    static BlogService.Iface client;

    @BeforeAll
    static void beforeAll() {
        client = ThriftClients.newClient(server.httpUri(), BlogService.Iface.class);
    }

    @Test
    @Order(1)
    void createBlogPost() throws TException {
        final CreateBlogPostRequest request = new CreateBlogPostRequest("My first blog", "Hello Armeria!");
        final BlogPost response = client.createBlogPost(request);

        assertThat(response.getTitle()).isEqualTo(request.getTitle());
        assertThat(response.getContent()).isEqualTo(request.getContent());
    }

    @Test
    @Order(2)
    void getBlogPost() throws TException {
        final BlogPost blogPost = client.getBlogPost(new GetBlogPostRequest(0));

        assertThat(blogPost.getTitle()).isEqualTo("My first blog");
        assertThat(blogPost.getContent()).isEqualTo("Hello Armeria!");
    }

    
    @Test
    @Order(2)
    void getInvalidBlogPost() throws TException {
        final Throwable exception = catchThrowable(() -> {
            final GetBlogPostRequest request = new GetBlogPostRequest(Integer.MAX_VALUE);
            client.getBlogPost(request);
        });
        final TApplicationException tApplicationException = (TApplicationException) exception;
        assertThat(tApplicationException.getType() == TApplicationException.MISSING_RESULT);
        assertThat(tApplicationException).hasMessageContaining("The blog post does not exist. ID: " + Integer.MAX_VALUE);
    }

    @Test
    @Order(3)
    void listBlogPosts() throws TException {
        client.createBlogPost( new CreateBlogPostRequest("My second blog", "Armeria is awesome!"));

        final ListBlogPostsResponse response = client.listBlogPosts(new ListBlogPostsRequest());

        final List<BlogPost> blogs = response.getBlogs();
        assertThat(blogs).hasSize(2);

        final BlogPost firstBlog = blogs.get(0);
        assertThat(firstBlog.getTitle()).isEqualTo("My first blog");
        assertThat(firstBlog.getContent()).isEqualTo("Hello Armeria!");

        final BlogPost secondBlog = blogs.get(1);
        assertThat(secondBlog.getTitle()).isEqualTo("My second blog");
        assertThat(secondBlog.getContent()).isEqualTo("Armeria is awesome!");
    }

    @Test
    @Order(4)
    void updateBlogPost() throws TException {
        final BlogPost updated = client.updateBlogPost(new UpdateBlogPostRequest(0, "My first blog", "Hello awesome Armeria!"));
        assertThat(updated.getId()).isZero();
        assertThat(updated.getTitle()).isEqualTo("My first blog");
        assertThat(updated.getContent()).isEqualTo("Hello awesome Armeria!");
    }

    @Test
    @Order(5)
    void deleteBlogPost() throws TException {
        final DeleteBlogPostResponse response = client.deleteBlogPost(new DeleteBlogPostRequest(0));
        assertThat(response.getMessage()).isEqualTo("The blog post has been deleted. ID: 0");
    }

    @Test
    @Order(5)
    void badRequestExceptionHandlerWhenTryingDeleteMissingBlogPost() throws TException {
        final Throwable exception = catchThrowable(() -> {
            client.deleteBlogPost(new DeleteBlogPostRequest(Integer.MAX_VALUE));
        });
        final TApplicationException tApplicationException = (TApplicationException) exception;
        assertThat(tApplicationException.getType() == TApplicationException.MISSING_RESULT);
        assertThat(tApplicationException).hasMessageContaining("The blog post does not exist. ID: " + Integer.MAX_VALUE);
    }
}


