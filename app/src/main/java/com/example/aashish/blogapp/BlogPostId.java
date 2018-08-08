package com.example.aashish.blogapp;

public class BlogPostId
{
    // This is an extendible class , we pass an id to this class , it simply returns the id where ever we are using it.

    public String BlogPostId;

    public <T extends  BlogPostId> T withId(final String id)
    {
    this.BlogPostId=id;
    return (T) this;
    }
}
