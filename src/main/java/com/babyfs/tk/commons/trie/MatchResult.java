package com.babyfs.tk.commons.trie;

import com.google.common.base.Preconditions;

/**
 * 匹配的结果
 */
public class MatchResult {
    /**
     * 匹配开始的索引
     */
    private final int matchStart;
    /**
     * 匹配结束的索引
     */
    private final int matchEnd;
    /**
     * 匹配的词
     */
    private final char[] word;
    /**
     * 类型mask
     */
    private final int typeMask;

    public MatchResult(int matchStart, int matchEnd, char[] word, int typeMask) {
        Preconditions.checkArgument(matchEnd >= matchStart, "matchEnd must >= matchStart");
        this.word = Preconditions.checkNotNull(word);
        this.matchStart = matchStart;
        this.matchEnd = matchEnd;
        this.typeMask = typeMask;
    }

    public int getMatchStart() {
        return matchStart;
    }

    public int getMatchEnd() {
        return matchEnd;
    }

    public char[] getWord() {
        return word;
    }

    public int getTypeMask() {
        return typeMask;
    }

    @Override
    public String toString() {
        return "MatchResult{" +
                "matchStart=" + matchStart +
                ", matchEnd=" + matchEnd +
                ", word=" + (word != null ? String.valueOf(word) : "null") +
                '}';
    }
}
