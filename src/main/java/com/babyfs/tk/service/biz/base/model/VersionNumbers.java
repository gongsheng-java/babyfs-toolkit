package com.babyfs.tk.service.biz.base.model;

import com.google.common.base.Strings;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 版本号
 */
public class VersionNumbers {
    private static final Pattern VERSION_PATTERN = Pattern.compile("^(\\d+)\\.(\\d)\\.(\\d).*");
    /**
     * 主版本号
     */
    private int main;
    /**
     * 次版本号
     */
    private int sub;
    /**
     * 小版本号
     */
    private int minor;

    public VersionNumbers() {

    }

    public VersionNumbers(int main, int sub, int minor) {
        this.main = main;
        this.sub = sub;
        this.minor = minor;
    }

    public int getMain() {
        return main;
    }

    public void setMain(int main) {
        this.main = main;
    }

    public int getSub() {
        return sub;
    }

    public void setSub(int sub) {
        this.sub = sub;
    }

    public int getMinor() {
        return minor;
    }

    public void setMinor(int minor) {
        this.minor = minor;
    }


    /**
     * 解析版本号,版本号的格式为1.2.3,分别对应主版本号,次版本号,小版本号
     *
     * @param version
     * @return
     */
    public static VersionNumbers of(String version) {
        if (Strings.isNullOrEmpty(version)) {
            return null;
        }

        Matcher matcher = VERSION_PATTERN.matcher(version);
        if (!matcher.matches()) {
            return null;
        }

        return new VersionNumbers(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)), Integer.parseInt(matcher.group(3)));
    }
}
