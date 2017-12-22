package com.babyfs.tk.galaxy.client;

import java.io.IOException;

public interface Client {

    public byte[] execute(String uri, byte[] body) throws IOException;
}
