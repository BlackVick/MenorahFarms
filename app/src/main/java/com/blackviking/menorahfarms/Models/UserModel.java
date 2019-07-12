package com.blackviking.menorahfarms.Models;

public class UserModel {

    private String email;
    private String firstName;
    private String lastName;
    private String profilePicture;
    private String profilePictureThumb;
    private String signUpMode;
    private String facebook;
    private String instagram;
    private String twitter;
    private String linkedIn;
    private String userType;
    private String userPackage;
    private String phone;
    private String birthday;
    private String gender;
    private String nationality;
    private String address;
    private String city;
    private String state;
    private String bank;
    private String accountName;
    private String accountNumber;
    private String kinName;
    private String kinEmail;
    private String kinRelationship;
    private String kinPhone;
    private String kinAddress;

    public UserModel() {
    }

    public UserModel(String email, String firstName, String lastName, String profilePicture, String profilePictureThumb, String signUpMode, String facebook, String instagram, String twitter) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.profilePicture = profilePicture;
        this.profilePictureThumb = profilePictureThumb;
        this.signUpMode = signUpMode;
        this.facebook = facebook;
        this.instagram = instagram;
        this.twitter = twitter;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public String getProfilePictureThumb() {
        return profilePictureThumb;
    }

    public void setProfilePictureThumb(String profilePictureThumb) {
        this.profilePictureThumb = profilePictureThumb;
    }

    public String getSignUpMode() {
        return signUpMode;
    }

    public void setSignUpMode(String signUpMode) {
        this.signUpMode = signUpMode;
    }

    public String getFacebook() {
        return facebook;
    }

    public void setFacebook(String facebook) {
        this.facebook = facebook;
    }

    public String getInstagram() {
        return instagram;
    }

    public void setInstagram(String instagram) {
        this.instagram = instagram;
    }

    public String getTwitter() {
        return twitter;
    }

    public void setTwitter(String twitter) {
        this.twitter = twitter;
    }
}
