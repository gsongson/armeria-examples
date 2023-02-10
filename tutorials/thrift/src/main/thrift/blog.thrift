namespace java example.armeria.server.blog.thrift

struct BlogPost {
  1: required i32 id;
  2: required string title;
  3: required string content;
  4: required i64 createdAt;
  5: required i64 modifiedAt;
}

struct CreateBlogPostRequest {
  1: required string title;
  2: required string content;
}

struct GetBlogPostRequest {
  1: required i32 id;
}

struct ListBlogPostsRequest {
  1: required bool descending;
}

struct ListBlogPostsResponse {
  1: required list<BlogPost> blogs;
}

struct UpdateBlogPostRequest {
  1: required i32 id;
  2: required string title;
  3: required string content;
}

struct DeleteBlogPostRequest {
  1: required i32 id;
}

struct DeleteBlogPostResponse {
  1: required string message;
}

service BlogService {
  BlogPost createBlogPost(1:CreateBlogPostRequest request)
  BlogPost getBlogPost(1:GetBlogPostRequest request)
  ListBlogPostsResponse listBlogPosts(1:ListBlogPostsRequest request)
  BlogPost updateBlogPost (1:UpdateBlogPostRequest request)
  DeleteBlogPostResponse deleteBlogPost(1:DeleteBlogPostRequest request)
}