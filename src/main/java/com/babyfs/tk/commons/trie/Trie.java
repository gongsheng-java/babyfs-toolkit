package com.babyfs.tk.commons.trie;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import gnu.trove.list.array.TCharArrayList;

import java.util.Map;

/**
 * Trie树的实现
 * 注:该实现不是线程安全的
 */
public class Trie {
    public static final int NOT_MATCH = -1;
    private final TrieNode root;
    private final boolean caseSensitive;

    public Trie() {
        this(false);
    }

    public Trie(boolean caseSensitive) {
        root = new TrieNode();
        this.caseSensitive = caseSensitive;
    }

    /**
     * 插入一个词
     *
     * @param word 词 not null
     * @param word
     */
    public void insert(String word) {
        insert(word, 0);
    }

    /**
     * 插入一个词
     *
     * @param word     词 not null
     * @param typeMask 类型的mask,与{@link TrieNode#typeMask}进行或的操作
     */
    public void insert(String word, int typeMask) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(word));

        Map<Character, TrieNode> children = root.children;
        final int wordLength = word.length();

        for (int i = 0; i < wordLength; i++) {
            final char c = chooseChar(word.charAt(i));

            final TrieNode t;
            if (children.containsKey(c)) {
                t = children.get(c);
            } else {
                t = new TrieNode(c);
                children.put(c, t);
            }

            children = t.children;

            if (i == wordLength - 1) {
                t.end = true;
                t.typeMask |= typeMask;
            }
        }
    }

    /**
     * 查询一个词是否在树中
     *
     * @param word
     * @return true, 存在;false,不存在
     */
    public boolean search(String word) {
        if (Strings.isNullOrEmpty(word)) {
            return false;
        }

        TrieNode t = searchNode(word);
        return t != null && t.end;
    }

    /**
     * 查询树中是否存在前缀prefix
     *
     * @param prefix
     * @return
     */
    public boolean startsWith(String prefix) {
        return !Strings.isNullOrEmpty(prefix) && searchNode(prefix) != null;

    }

    /**
     * 查询词节点
     *
     * @param word
     * @return
     */
    public TrieNode searchNode(String word) {
        Map<Character, TrieNode> children = root.children;
        TrieNode t = null;

        final int length = word.length();
        for (int i = 0; i < length; i++) {
            char c = chooseChar(word.charAt(i));
            if (children.containsKey(c)) {
                t = children.get(c);
                children = t.children;
            } else {
                return null;
            }
        }
        return t;
    }


    /**
     * 从text start开始的位置在trie中查找第一个匹配的word
     *
     * @param text  搜索的文本
     * @param start 搜索的起始位置,从0开始
     * @return 返回匹配的结果
     */
    public MatchResult trySearchNode(final String text, final int start, final Function<Character, Boolean> stopFunc) {
        Preconditions.checkArgument(start >= 0, "start must >=0");
        if (Strings.isNullOrEmpty(text)) {
            return null;
        }

        Map<Character, TrieNode> children = root.children;
        TrieNode t = null;

        int matchStart = NOT_MATCH;
        int matchEnd = NOT_MATCH;

        final TCharArrayList chars = new TCharArrayList(10);
        final int length = text.length();
        for (int i = start; i < length; ) {
            final char c = chooseChar(text.charAt(i));
            if (stopFunc != null) {
                //判断是否需要跳过
                Boolean stop = stopFunc.apply(c);
                if (stop != null && stop) {
                    i++;
                    continue;
                }
            }

            if ((t = children.get(c)) != null) {
                if (matchStart == NOT_MATCH) {
                    //新匹配开始
                    matchStart = i;
                    chars.resetQuick();
                }
                chars.add(c);

                if (t.end) {
                    //匹配到word
                    matchEnd = i;
                    break;
                }
                children = t.children;
                i++;
            } else {
                //未匹配到,重置后继续查找
                children = root.children;
                t = null;
                if (matchStart > NOT_MATCH) {
                    //如果之前曾经匹配过,从上一次匹配开始的下一个位置开始查找
                    i = matchStart + 1;
                } else {
                    i++;
                }
                matchStart = NOT_MATCH;
                matchEnd = NOT_MATCH;
                chars.resetQuick();
            }
        }

        if (t != null && matchStart > NOT_MATCH && matchEnd > NOT_MATCH) {
            return new MatchResult(matchStart, matchEnd, chars.toArray(), t.typeMask);
        }
        return null;
    }

    public char chooseChar(char c) {
        return this.caseSensitive ? c : Character.toLowerCase(c);
    }
}
