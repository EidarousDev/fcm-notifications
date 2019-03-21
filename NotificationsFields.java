package com.eidarousdev.clashblog.notifications;

import java.util.Date;

/**
 * Created by E on 4/11/2018.
 */

public class NotificationsFields {

    public String commentUID, post_id, comment_text;
    private Date comment_time;

    public NotificationsFields() {
        this.comment_time = comment_time;
    }

    public NotificationsFields(String commentUID, String post_id, String comment_text, Date comment_time) {
        this.commentUID = commentUID;
        this.post_id = post_id;
        this.comment_text = comment_text;
        this.comment_time = comment_time;
    }

    public String getCommentUID() {
        return commentUID;
    }

    public void setCommentUID(String commentUID) {
        this.commentUID = commentUID;
    }

    public String getPost_id() {
        return post_id;
    }

    public void setPost_id(String post_id) {
        this.post_id = post_id;
    }

    public String getComment_text() {
        return comment_text;
    }

    public void setComment_text(String comment_text) {
        this.comment_text = comment_text;
    }

    public Date getComment_time() {
        return comment_time;
    }

    public void setComment_time(Date comment_time) {
        this.comment_time = comment_time;
    }
}
