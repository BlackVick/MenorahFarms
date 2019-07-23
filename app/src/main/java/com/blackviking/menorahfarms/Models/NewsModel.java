package com.blackviking.menorahfarms.Models;

public class NewsModel {

    private String newsTopic;
    private String newsImage;
    private String newsImageThumb;
    private String newsContent;
    private String newsCreator;
    private long newsTime;
    private String newsLink;

    public NewsModel() {
    }

    public NewsModel(String newsTopic, String newsImage, String newsImageThumb, String newsContent, String newsCreator, long newsTime, String newsLink) {
        this.newsTopic = newsTopic;
        this.newsImage = newsImage;
        this.newsImageThumb = newsImageThumb;
        this.newsContent = newsContent;
        this.newsCreator = newsCreator;
        this.newsTime = newsTime;
        this.newsLink = newsLink;
    }

    public String getNewsTopic() {
        return newsTopic;
    }

    public void setNewsTopic(String newsTopic) {
        this.newsTopic = newsTopic;
    }

    public String getNewsImage() {
        return newsImage;
    }

    public void setNewsImage(String newsImage) {
        this.newsImage = newsImage;
    }

    public String getNewsImageThumb() {
        return newsImageThumb;
    }

    public void setNewsImageThumb(String newsImageThumb) {
        this.newsImageThumb = newsImageThumb;
    }

    public String getNewsContent() {
        return newsContent;
    }

    public void setNewsContent(String newsContent) {
        this.newsContent = newsContent;
    }

    public String getNewsCreator() {
        return newsCreator;
    }

    public void setNewsCreator(String newsCreator) {
        this.newsCreator = newsCreator;
    }

    public long getNewsTime() {
        return newsTime;
    }

    public void setNewsTime(long newsTime) {
        this.newsTime = newsTime;
    }

    public String getNewsLink() {
        return newsLink;
    }

    public void setNewsLink(String newsLink) {
        this.newsLink = newsLink;
    }
}
