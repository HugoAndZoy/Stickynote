package com.dan.stickynote;

public class Fruit {

    private String name;
    private int imageId;

    public Fruit(String name, int imageId) {
        this.name = name;
        this.imageId = imageId;
    }

    public String getName() {
        return name;

    }

    public int getImageId() {
        return imageId;
    }

    public void setName(String s) {
        this.name = s;
    }

    public void setImageId(int id) {
        this.imageId = id;
    }
}
