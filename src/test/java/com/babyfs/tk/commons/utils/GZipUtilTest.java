package com.babyfs.tk.commons.utils;

import org.apache.commons.codec.binary.Base64;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * {@link GZipUtil} 单元测试
 * <p/>
 */
public class GZipUtilTest {
    @Test
    public void testRoundtrip() throws IOException {
        String text = "The quick brown fox jumps over the lazy dog";

        byte[] gzip = GZipUtil.compress(text.getBytes());

        byte[] origin = GZipUtil.decompress(gzip);

        assertEquals(text, new String(origin));
    }

    @Test
    public void test1() throws IOException {
        byte[] gzip = Base64.decodeBase64("H4sIAAAAAAAAAIXRy0rDQBQG4Fcpsy7zAO4kKxdeMIgLcXEyGZMhk5mQTBCUQhEqlYqoVbwhUgRd6CYI1VofJ2matzBoEUci2Z5/vh/OmY1dFADxwKFL4FM0hxaEoqGgahVsJjFwjpoIgmCWph+X2fHzV4Zazb8WhB1KZmti/nvWyF/G+fiuAhHfwoGlmUnvJjvfL/rD6eCwSghsgYhcKhwnBgyMuCAcrSHvJtlZUm29eFtixYQTubGGis7rpHtS7peOHqqo9LGp4nL1ZbEIYYQNc0WGCixOtRrDbPzMq1tmd8KGFAqIikxJGPB1ZjtU6ed+a+d7o6J9XVO0JnZYoMnp43121KthwCH0CZfE089/MSxOK7/ql7XKJA6IFFss9DX+/6uaRh5TJaVy9UX6t/nBVdZJ0vcn1Nr8BKpgznGwAgAA");
        System.out.println(new String(GZipUtil.decompress(gzip)));
    }

}
