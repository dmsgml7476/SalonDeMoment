package com.salon.constant;

public enum UploadType {
    DESIGNER("/desImg/"),
    SHOP("/shopImg/"),
    SHOP_SERVICE("/shopServiceImg/"),
    REVIEW("/reviewImg/"),
    BANNER("/bannerImg/"),
    ANNOUNCEMENT("/ancFile/"),
    CUSTOMER_SERVICE("/csFile/");

    private final String urlPath;

    UploadType(String urlPath) {
        this.urlPath = urlPath;
    }

    public String getUrlPath() {
        return urlPath;
    }
}

