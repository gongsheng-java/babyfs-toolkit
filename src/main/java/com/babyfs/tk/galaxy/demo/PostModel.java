package com.babyfs.tk.galaxy.demo;

import java.io.Serializable;


public class PostModel implements Serializable {

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "PostModel{" +
                "message='" + message + '\'' +
                '}';
    }

    private String message;
}
