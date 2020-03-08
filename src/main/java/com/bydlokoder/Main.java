package com.bydlokoder;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Main {
    private static OkHttpClient client = new OkHttpClient();
    private static final String APP_STORE_TARGET_URL = "https://itunes.apple.com/WebObjects/MZStore.woa/wa/customerReviews?displayable-kind=11&id=%s&page=%d&sort=4";
    private static DateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
    private static DateFormat formatterUK = new SimpleDateFormat("dd MMMM yyyy", Locale.UK);
    private static DateFormat formatterUSA = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
    private static final long[] apps = {1377291789};

    private static final int UK_ID = 143444;
    private static final int USA_ID = 143441;
    private static final int Venezuela_ID = 143502;

    public static void main(String[] args) {
        List<Report> reportList = new ArrayList<>();
        for (long app : apps) {
            reportList.add(getReport(app));
        }
        ExcelExporter.export(reportList);
    }

    private static Report getReport(long appId) {
        List<Review> reviews = new ArrayList<>();
        Countries[] countries = Countries.values();
        for (Countries country : countries) {
            reviews.addAll(getAllReviewsForCountry(appId, country));
        }

        System.out.println("Total review: " + reviews.size());

        return new Report(appId, reviews);
    }

    private static List<Review> getAllReviewsForCountry(long appId, Countries country) {
        List<Review> reviewList = new ArrayList<Review>();
        Request request = buildRequest(appId, country, 1);
        try {
            Response response = client.newCall(request).execute();
            InputStream is = response.body().byteStream();
            Document doc = Jsoup.parse(is, null, request.urlString());
            Elements pageNumbers = doc.getElementsByAttribute("total-number-of-pages");
            if (pageNumbers.size() != 0) { // no reviews
                int totalNumOfPages = Integer.valueOf(pageNumbers.get(0).attr("total-number-of-pages"));
                for (int i = 1; i <= totalNumOfPages; i++) {
                    reviewList.addAll(getReviewsFromPage(appId, country, i));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Reviews for country " + country + ": " + reviewList.size());

        return reviewList;
    }

    private static List<Review> getReviewsFromPage(long appId, Countries country, int page) {
        List<Review> reviewList = new ArrayList<Review>();
        boolean successful = false;
        do {
            Request request = buildRequest(appId, country, page);
            try {
                Response response = client.newCall(request).execute();
                InputStream is = response.body().byteStream();
                Document doc = Jsoup.parse(is, null, request.urlString());
                Elements titles = doc.getElementsByClass("customerReviewTitle");
                Elements reviews = doc.getElementsByClass("content");
                Elements users = doc.getElementsByClass("user-info");
                System.out.println("Reviews: " + reviews.size());
                successful = reviews.size() > 0;
                for (int i = 0; i < reviews.size(); i++) {
                    Review review = getReview(appId, country, titles.get(i), reviews.get(i), users.get(i));
                    if (!reviewList.contains(review)) {
                        reviewList.add(review);
                    }
                }
            } catch (IOException|ParseException e) {
                e.printStackTrace();
            }
        } while (!successful);

        System.out.println("Reviews for page " + page + ": " + reviewList.size());

        return reviewList;
    }

    private static Review getReview(long appId, Countries country, Element titleElement, Element review, Element user) throws ParseException {
        String starsString = titleElement.nextElementSibling().attr("aria-label"); //string that contains number of stars e.g. 5 stars
        String title = titleElement.text(); // string contains a title
        String reviewBody = review.text(); // review itself
        String userInfo = user.text(); // string contains nickname, version of the app and date that can be splitted by dash
//        double rate = Double.parseDouble(starsString.substring(0, 1));
        String[] info = userInfo.split(" - ");
        String version = info[info.length - 2].trim().split(" ")[1];
//        Date date;
        String dateString = info[info.length - 1].trim();
//        switch (country.getId()) {
//            case UK_ID:
//                date = formatterUK.parse(dateString);
//                break;
//            case USA_ID:
//                date = formatterUSA.parse(dateString);
//                break;
//            case Venezuela_ID:
//                date = formatterUSA.parse(dateString);
//                break;
//            default:
//                date = formatter.parse(dateString);
//                break;
//        }
        return new Review(appId, country, starsString, title, reviewBody, dateString, version);
    }

    private static Request buildRequest(long appId, Countries country, int page) {
        String url = String.format(APP_STORE_TARGET_URL, appId, page);
        return new Request.Builder()
                .url(url)
                .header("User-Agent", "iTunes/10.3.1 (Macintosh; Intel Mac OS X 10.6.8) AppleWebKit/533.21.1")
                .header("X-Apple-Store-Front", String.format("%s,12", country.getId()))
                .header("Accept-Language", "en-us, en;q=0.50")
                .build();
    }
}
