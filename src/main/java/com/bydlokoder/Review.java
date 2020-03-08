package com.bydlokoder;

import java.util.Date;

public class Review implements Comparable<Review> {
    private long appId;
    private String rate;
    private String title;
    private String body;
    private String date;
    private String version;
    private Countries country;

    public Review(long appId, Countries country, String rate, String title, String body, String date, String version) {
        this.appId = appId;
        this.country = country;
        this.rate = rate;
        this.title = title;
        this.body = body;
        this.date = date;
        this.version = version;
    }

    public String getRate() {
        return rate;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public String getDate() {
        return date;
    }


    public String getVersion() {
        return version;
    }

    public long getAppId() {
        return appId;
    }

    public Countries getCountry() {
        return country;
    }

    public int compareTo(Review review) {
        if (review.date == null) {
            return -1;
        } else {
            return review.date.compareTo(date);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Review review = (Review) o;

        if (appId != review.appId) return false;
        if (rate != null ? !rate.equals(review.rate) : review.rate != null) return false;
        if (title != null ? !title.equals(review.title) : review.title != null) return false;
        if (body != null ? !body.equals(review.body) : review.body != null) return false;
        if (date != null ? !date.equals(review.date) : review.date != null) return false;
        return !(version != null ? !version.equals(review.version) : review.version != null);

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = (int) (appId ^ (appId >>> 32));
        temp = (rate != null ? rate.hashCode() : 0);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (body != null ? body.hashCode() : 0);
        result = 31 * result + (date != null ? date.hashCode() : 0);
        result = 31 * result + (version != null ? version.hashCode() : 0);
        return result;
    }
}
