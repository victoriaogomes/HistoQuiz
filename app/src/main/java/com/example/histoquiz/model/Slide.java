package com.example.histoquiz.model;

import java.util.ArrayList;

public class Slide {

    protected String  name;
    protected int code;
    protected ArrayList<String> images;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public ArrayList<String> getImages() {
        return images;
    }

    public void setImages(ArrayList<String> images) {
        this.images = images;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
