package com.example.ar8ee.Classes;

public class postClass {
    private String userName;
    private String userId;
    private String postId;
    private String userImage;
    private String body;
    private String postImage;
    private String likes;

    public postClass(String userName, String userImage, String body, String postImage, String userId, String likes) {
        this.userName = userName;
        this.userImage = userImage;
        this.body = body;
        this.postImage = postImage;
        this.userId = userId;
    }

    public postClass() {
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserImage() {
        return userImage;
    }

    public void setUserImage(String userImage) {
        this.userImage = userImage;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getPostImage() {
        return postImage;
    }

    public void setPostImage(String postImage) {
        this.postImage = postImage;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getLikes() {
        return likes;
    }

    public void setLikes(String likes) {
        this.likes = likes;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }
}
