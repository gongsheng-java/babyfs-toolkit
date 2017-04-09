package com.babyfs.tk.commons.trie;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class TrieNode {
    /**
     * 字符值
     */
    char c;
    /**
     * 当前节点是否是完整的word
     */
    boolean end;
    /**
     *
     */
    Map<Character, TrieNode> children = new HashMap<>();
    /**
     * 类型Mask
     */
    int typeMask;

    public TrieNode() {
    }

    public TrieNode(char c) {
        this.c = c;
    }

    public char getC() {
        return c;
    }

    public boolean isEnd() {
        return end;
    }

    public Map<Character, TrieNode> getChildren() {
        return Collections.unmodifiableMap(children);
    }

    public int getTypeMask() {
        return typeMask;
    }
}
