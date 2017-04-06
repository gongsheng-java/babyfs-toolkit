package com.babyfs.tk.commons.utils;


import com.babyfs.tk.commons.base.Pair;
import com.babyfs.tk.commons.Constants;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.ByteSource;
import com.google.common.io.CharSource;
import com.google.common.io.LineProcessor;
import com.google.common.io.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 取得jar中git的Version
 */
public final class GitVersionUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(GitVersionUtil.class);

    public static final String GIT_PROPERTIES = "git.properties";
    public static final String GIT_COMMIT_ID = "git.commit.id";
    public static final String GIT_BRANCH = "git.branch";
    public static final String GIT_COMMIT_TIME = "git.commit.time";
    public static final String GIT_BUILD_TIME = "git.build.time";
    public static final String GIT_COMMIT_USER_NAME = "git.commit.user.name";

    private static final String[] GIT_PROP_KEYS = new String[]{GIT_COMMIT_ID, GIT_BRANCH, GIT_COMMIT_USER_NAME, GIT_COMMIT_TIME, GIT_BUILD_TIME};

    private GitVersionUtil() {
    }

    /**
     * 取得classpath中所有jar的版本好
     */
    public static ImmutableList<Pair<String, String>> getAllVersions() {
        ImmutableList.Builder<Pair<String, String>> gitVersionList = new ImmutableList.Builder();
        try {
            ClassPathRecources classPath = ClassPathRecources.from(Thread.currentThread().getContextClassLoader());
            ImmutableList<ClassPathRecources.ResourceInfo> resources = classPath.getResources();
            for (ClassPathRecources.ResourceInfo resourceInfo : resources) {
                String resourceName = resourceInfo.getResourceName();
                URL url = resourceInfo.getUrl();
                if (resourceName == null || url == null) {
                    continue;
                }
                if (!resourceName.contains(GIT_PROPERTIES)) {
                    continue;
                }
                String from = url.getFile();
                String lowerCaseFile = url.getFile().toLowerCase();
                if (lowerCaseFile.endsWith(".jar") || lowerCaseFile.endsWith(".zip")) {
                    String splitSep = "/";
                    if ("file".equalsIgnoreCase(url.getProtocol())) {
                        splitSep = Pattern.quote(File.separator);
                    }
                    String[] split = url.getFile().split(splitSep);
                    if (split.length > 0) {
                        from = split[split.length - 1];
                    }
                    /*
                    jar url格式:
                    jar:http://www.example.com/bar/baz.jar!/path/to/file
                     */
                    url = new URL("jar:" + url.toString() + "!/" + resourceName);
                }
                ByteSource byteSource = Resources.asByteSource(url);
                CharSource source = byteSource.asCharSource(Constants.DEFAULT_CHARSET_OBJ);
                add(gitVersionList, from, source);
            }
        } catch (Exception e) {
            LOGGER.error("get all versions fail", e);
        }
        return gitVersionList.build();
    }

    private static void add(ImmutableList.Builder<Pair<String, String>> gitVersionList, String from, CharSource source) throws IOException {
        Map<String, String> map = source.readLines(new MapLineProcessor());
        List<String> propList = Lists.newArrayListWithCapacity(GIT_PROP_KEYS.length);
        for (String key : GIT_PROP_KEYS) {
            String value = map.get(key);
            if (value == null) {
                value = "";
            }
            propList.add(key + "=" + value);
        }
        String versionDesc = Joiner.on(",").join(propList);
        gitVersionList.add(Pair.of(from, versionDesc));
    }

    private static class MapLineProcessor implements LineProcessor<Map<String, String>> {
        private Map<String, String> properites = Maps.newHashMap();

        @Override
        public boolean processLine(String line) throws IOException {
            if (!line.startsWith("#")) {
                List<String> strings = Splitter.on("=").limit(2).splitToList(line);
                if (strings.size() == 2) {
                    properites.put(strings.get(0), strings.get(1));
                }
            }
            return true;
        }

        @Override
        public Map<String, String> getResult() {
            return properites;
        }
    }
}
