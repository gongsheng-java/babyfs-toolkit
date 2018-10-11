package com.babyfs.tk.apollo;

import com.babyfs.tk.apollo.annotation.ApolloScan;
import com.babyfs.tk.apollo.annotation.ConfigKey;

@ApolloScan
public class CompConfig {

    @ConfigKey
    private Node node;

    @ConfigKey("json")
    private ApolloJsonTest jsonTest;

    public static class Node{
        @ConfigKey("name")
        private String name;
    }
}
