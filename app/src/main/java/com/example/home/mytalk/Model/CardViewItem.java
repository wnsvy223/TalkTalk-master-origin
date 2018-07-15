package com.example.home.mytalk.Model;



public class CardViewItem {

    private String name;
    private String image;
    private String type;
    private String director;
    private String link;

    public CardViewItem(String name, String image, String type, String director, String link) {
        this.name = name;
        this.image = image;
        this.type = type;
        this.director = director;
        this.link = link;
    }

    @Override
    public String toString() {
        return name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}
