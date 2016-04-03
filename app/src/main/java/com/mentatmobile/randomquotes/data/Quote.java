package com.mentatmobile.randomquotes.data;

public class Quote {

    //private variables
    private int id;
    private String quote;
    private String author;
    private String genre;

    public Quote() {
    }

    public Quote(int id, String quote, String author, String genre) {
        this.id = id;
        this.quote = quote;
        this.author = author;
        this.genre = genre;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getQuote() {
        return quote;
    }

    public void setQuote(String quote) {
        this.quote = quote;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }
}