package com.babyfs.tk.commons.trie;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 *
 */
public class TrieTest {
    @Test
    public void testAll() throws Exception {
        Trie trie = new Trie();
        for (int i = 0; i < 100000; i++) {
            String word = i + "word词典word词典";
            trie.insert(word);
        }
        for (int i = 0; i < 100000; i++) {
            String word = "word词典word词典" + i;
            trie.insert(word);
        }

        for (int i = 0; i < 100000; i++) {
            testMatch(trie, i, false);
        }
    }

    @Test
    @Ignore
    public void testBench() throws Exception {
        Trie trie = new Trie();
        for (int i = 0; i < 100000; i++) {
            String word = i + "word词典word词典";
            trie.insert(word);
        }
        for (int i = 0; i < 100000; i++) {
            String word = "word词典word词典" + i;
            trie.insert(word);
        }

        for (int i = 0; i < 100000; i++) {
            testMatch(trie, i, false);
        }

        System.gc();

        for (int c = 0; c < 50; c++) {
            System.gc();
            long start = System.nanoTime();
            long size = 0;
            for (int i = 0; i < 100000; i++) {
                size += testMatch(trie, i, true);
            }
            long end = System.nanoTime();
            long speed = (size * TimeUnit.SECONDS.toNanos(1)) / (end - start);
            System.out.println(speed / 1024 / 1024 + " Mchars/s");
            System.gc();
        }
    }

    private long testMatch(Trie trie, int i, boolean skipAssert) {
        String word = i + "word词典word词典";
        String text = " " + word;
        MatchResult matchResult = trie.trySearchNode(text, 0, null);
        if (matchResult == null) {
            throw new RuntimeException();
        }
        if (!skipAssert) {
            Assert.assertNotNull(matchResult);
            Assert.assertEquals(1, matchResult.getMatchStart());
            Assert.assertEquals(word.length(), matchResult.getMatchEnd());
            Assert.assertEquals(word, new String(matchResult.getWord()));
        }
        return text.length();
    }
}