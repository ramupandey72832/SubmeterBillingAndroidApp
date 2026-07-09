package com.application.android_ui_templete1.templates.intro;

public class TutorialData {

    // image url is used to
    // store the url of image
    private String imgUrl;

    // Constructor method.
    public TutorialData(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    // Getter method
    public String getImgUrl() {
        return imgUrl;
    }

    // Setter method
    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }
}