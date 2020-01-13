package com.erlyvideo.sample.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LoginUser {

    @SerializedName("is_admin")
    @Expose
    private Boolean isAdmin;

    @SerializedName("notification_email")
    @Expose
    private String notificationEmail;


    @SerializedName("session")
    @Expose
    private String session;


    @SerializedName("login")
    @Expose
    private String login;

    public Boolean getAdmin() {
        return isAdmin;
    }

    public void setAdmin(Boolean admin) {
        isAdmin = admin;
    }

    public String getNotificationEmail() {
        return notificationEmail;
    }

    public void setNotificationEmail(String notificationEmail) {
        this.notificationEmail = notificationEmail;
    }

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }
}
