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

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public ApolloJsonTest getJsonTest() {
        return jsonTest;
    }

    public void setJsonTest(ApolloJsonTest jsonTest) {
        this.jsonTest = jsonTest;
    }
}
