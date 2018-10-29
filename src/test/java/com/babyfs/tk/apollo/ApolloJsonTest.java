package com.babyfs.tk.apollo;

import com.babyfs.tk.apollo.annotation.ApolloScan;
import com.babyfs.tk.apollo.annotation.ConfigKey;

@ApolloScan
@ConfigKey("json")
public class ApolloJsonTest {
    private String name;
    private String path;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
