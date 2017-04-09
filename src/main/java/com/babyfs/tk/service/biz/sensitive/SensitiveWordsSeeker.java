package com.babyfs.tk.service.biz.sensitive;

import com.babyfs.tk.commons.model.ServiceResponse;
import com.babyfs.tk.commons.trie.MatchResult;
import com.babyfs.tk.commons.trie.Trie;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import gnu.trove.set.TCharSet;
import gnu.trove.set.hash.TCharHashSet;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

import static com.babyfs.tk.commons.model.ServiceResponse.createFailResponse;
import static com.babyfs.tk.commons.model.ServiceResponse.createSuccessResponse;

/**
 *
 */
public class SensitiveWordsSeeker {

    private static final TCharSet STOP_CHARS = new TCharHashSet();

    static {
        STOP_CHARS.add('　');
    }

    /**
     * 检查是否有敏感词
     *
     * @param trie
     * @param text
     * @return
     */
    public MatchResult findOne(Trie trie, String text) {
        Preconditions.checkNotNull(trie);
        if (Strings.isNullOrEmpty(text)) {
            return null;
        }

        MatchResult matchResult = trie.trySearchNode(text, 0, this::isStop);
        if (matchResult == null) {
            return null;
        } else {
            return matchResult;
        }
    }

    /**
     * 查询所有的敏感词
     *
     * @param trie
     * @param text
     * @return
     */
    public List<MatchResult> findAll(Trie trie, String text) {
        Preconditions.checkNotNull(trie);
        if (Strings.isNullOrEmpty(text)) {
            return null;
        }

        List<MatchResult> results = Lists.newArrayList();

        Set<String> foundWords = Sets.newHashSet();

        int start = 0;
        while (true) {
            MatchResult matchResult = trie.trySearchNode(text, start, this::isStop);
            if (matchResult == null) {
                break;
            } else {
                if (foundWords.add(String.valueOf(matchResult.getWord()))) {
                    results.add(matchResult);
                }
                start = matchResult.getMatchEnd() + 1;
            }
        }
        return results;
    }

    /**
     * 过滤敏感词
     *
     * @param trie              Trie树
     * @param text              文本
     * @param forbiddenTypeMask 禁止发布的类型mask
     * @return
     */
    public ServiceResponse<String> filter(Trie trie, String text, Function<String, String> sensitiveWordHandler, int forbiddenTypeMask) {
        Preconditions.checkNotNull(trie);
        if (Strings.isNullOrEmpty(text)) {
            return createSuccessResponse(text);
        }

        int start = 0;
        StringBuilder filterd = null;

        while (true) {
            MatchResult matchResult = trie.trySearchNode(text, start, this::isStop);
            if (matchResult == null) {
                break;
            } else {
                if (filterd == null) {
                    filterd = new StringBuilder(text.length());
                }

                final int matchStart = matchResult.getMatchStart();
                final int matchEnd = matchResult.getMatchEnd();
                if (forbiddenTypeMask > 0 && (matchResult.getTypeMask() & forbiddenTypeMask) > 0) {
                    return createFailResponse("包含禁词");
                }

                String foundWord = text.substring(matchStart, matchEnd + 1);
                filterd.append(text.substring(start, matchStart));
                filterd.append(sensitiveWordHandler.apply(foundWord));

                start = matchEnd + 1;
            }
        }

        if (filterd != null && start < text.length()) {
            filterd.append(text.substring(start));
        }

        if (filterd != null) {
            return createSuccessResponse(filterd.toString());
        } else {
            return createSuccessResponse(text);
        }
    }

    boolean isStop(Character c) {
        if (c == null) {
            return true;
        }
        return Character.isWhitespace(c) || STOP_CHARS.contains(c);
    }

    public static final Function<String, String> IGNORE_ONE_HANDLER = new Function<String, String>() {
        @Nullable
        @Override
        public String apply(@Nullable String input) {
            if (input == null) {
                return "";
            }
            return "*";
        }
    };

    public static final Function<String, String> IGNORE_HANDLER = new Function<String, String>() {
        @Nullable
        @Override
        public String apply(@Nullable String input) {
            if (input == null) {
                return "";
            }
            return Strings.repeat("*", input.length());
        }
    };
}
