package example.armeria.server.blog.thrift;

import java.util.concurrent.TimeUnit;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.thrift.TException;
import org.apache.thrift.TApplicationException;
import org.apache.thrift.async.AsyncMethodCallback;

import com.linecorp.armeria.server.ServiceRequestContext;

class BlogServiceImpl implements BlogService.AsyncIface {

    private final AtomicInteger idGenerator = new AtomicInteger();
    private final Map<Integer, BlogPost> blogPosts = new ConcurrentHashMap<>();

    @Override
    public void createBlogPost(CreateBlogPostRequest request, AsyncMethodCallback<BlogPost> resultHandler) throws TException {
        final int id = idGenerator.getAndIncrement();
        final Instant now = Instant.now();
        final BlogPost updated = new BlogPost(id 
                                            ,request.getTitle()
                                            ,request.getContent()
                                            ,now.toEpochMilli()
                                            ,now.toEpochMilli());
        blogPosts.put(id, updated);
        final BlogPost stored = updated;
        resultHandler.onComplete(stored);
    }
    
    @Override
    public void getBlogPost(GetBlogPostRequest request, AsyncMethodCallback<BlogPost> resultHandler) throws TException  {
        final BlogPost blogPost = blogPosts.get(request.getId());
        if (blogPost == null) {
            throw new TApplicationException(TApplicationException.MISSING_RESULT, "The blog post does not exist. ID: " + request.getId());
        } else {
            resultHandler.onComplete(blogPost);
        }
    }


    @Override
    public void listBlogPosts(ListBlogPostsRequest request, AsyncMethodCallback<ListBlogPostsResponse> resultHandler) throws TException  {
        final Collection<BlogPost> blogPosts;
        if (request.isDescending()) {
            blogPosts = this.blogPosts.entrySet()
                                      .stream()
                                      .sorted(Collections.reverseOrder(Comparator.comparingInt(Entry::getKey)))
                                      .map(Entry::getValue).collect(Collectors.toList());
        } else {
            blogPosts = this.blogPosts.values();
        }
        resultHandler.onComplete(new ListBlogPostsResponse(blogPosts.stream().collect(Collectors.toList())));
    }

    @Override
    public void updateBlogPost (UpdateBlogPostRequest request, AsyncMethodCallback<BlogPost> resultHandler) throws TException {
        final BlogPost oldBlogPost = blogPosts.get(request.getId());
        if (oldBlogPost == null) {
            resultHandler.onError(
                new TApplicationException(TApplicationException.MISSING_RESULT, "The blog post does not exist. ID: " + request.getId()));
        } else {
            final BlogPost newBlogPost = oldBlogPost.setTitle(request.getTitle())
                                                    .setContent(request.getContent())
                                                    .setModifiedAt(Instant.now().toEpochMilli());
            blogPosts.put(request.getId(), newBlogPost);
            resultHandler.onComplete(newBlogPost);
        }
    }

    @Override
    public void deleteBlogPost(DeleteBlogPostRequest request, AsyncMethodCallback<DeleteBlogPostResponse> resultHandler) {
        final BlogPost removed = blogPosts.remove(request.getId());
        if (removed == null) {
            resultHandler.onError(
                new TApplicationException(TApplicationException.MISSING_RESULT, "The blog post does not exist. ID: " + request.getId()));
        } else {
            resultHandler.onComplete(new DeleteBlogPostResponse("The blog post has been deleted. ID: " + request.getId()));
        }
    }
}
