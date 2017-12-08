package com.babyfs.tk.galaxy.client;

import java.io.IOException;

public interface Client {


    public String execute(String uri, String body) throws IOException;
}
