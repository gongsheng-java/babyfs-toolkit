package com.babyfs.tk.apollo;

import com.babyfs.tk.apollo.annotation.ApolloScan;
import com.babyfs.tk.apollo.annotation.ConfigKey;

@ApolloScan

public class TestConfig {

    @ConfigKey("name")
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
