package com.babyfs.tk.apollo;

import com.babyfs.tk.apollo.annotation.ApolloScan;
import com.babyfs.tk.apollo.annotation.ConfigKey;

@ApolloScan

public class TestConfig {

    @ConfigKey("name")
    private String name;

    @ConfigKey("tel")
    private String tel;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }
}
