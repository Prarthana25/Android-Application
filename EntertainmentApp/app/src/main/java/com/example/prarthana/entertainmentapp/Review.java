package com.example.prarthana.entertainmentapp;

import java.util.Comparator;
import java.util.Date;

public class Review {
    int id;
    String author_name;
    String author_url;
    String profile_photo_url;
    float rating;
    String text;
    Date date;
    public Review(int id,String author_name, String author_url, String profile_photo_url, float rating, String text,Date date) {
        this.id=id;
        this.author_name = author_name;
        this.author_url = author_url;
        this.profile_photo_url = profile_photo_url;
        this.rating = rating;
        this.text = text;
        this.date=date;
    }

    static Comparator<Review> defaultComparator() {
        return new Comparator<Review>() {
            @Override
            public int compare(Review o1, Review o2) {
                return o1.id-o2.id;
            }
        };
    }

    static Comparator<Review> highRatingComparator() {
        return new Comparator<Review>() {
            @Override
            public int compare(Review o1, Review o2) {
                if(o1.rating<o2.rating) return 1;
                if(o2.rating<o1.rating) return -1;
                return 0;
            }
        };
    }

    static Comparator<Review> lowRatingComparator() {
        return new Comparator<Review>() {
            @Override
            public int compare(Review o1, Review o2) {
                if(o1.rating<o2.rating) return -1;
                if(o2.rating<o1.rating) return 1;
                return 0;
            }
        };
    }

    static Comparator<Review> mostRecentComparator() {
        return new Comparator<Review>() {
            @Override
            public int compare(Review o1, Review o2) {
                return o2.date.compareTo(o1.date);
            }
        };
    }

    static Comparator<Review> leastRecentComparator() {
        return new Comparator<Review>() {
            @Override
            public int compare(Review o1, Review o2) {
                return o1.date.compareTo(o2.date);
            }
        };
    }
}
