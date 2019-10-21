package com.blackviking.menorahfarms.Models;

public class FarmUpdateModel {

    private String video_title;
    private String video_url;
    private String video_embed;

    public FarmUpdateModel() {
    }

    public FarmUpdateModel(String video_title, String video_url, String video_embed) {
        this.video_title = video_title;
        this.video_url = video_url;
        this.video_embed = video_embed;
    }

    public String getVideo_title() {
        return video_title;
    }

    public void setVideo_title(String video_title) {
        this.video_title = video_title;
    }

    public String getVideo_url() {
        return video_url;
    }

    public void setVideo_url(String video_url) {
        this.video_url = video_url;
    }

    public String getVideo_embed() {
        return video_embed;
    }

    public void setVideo_embed(String video_embed) {
        this.video_embed = video_embed;
    }
}
