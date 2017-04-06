package com.babyfs.tk.commons.utils;

import com.google.common.collect.ImmutableList;
import com.babyfs.tk.commons.base.Pair;
import org.junit.Test;

public class GitVersionUtilTest {
    @Test
    public void testGetAllVersions() throws Exception {
        ImmutableList<Pair<String, String>> allVersions = GitVersionUtil.getAllVersions();
        for(Pair<String,String> pair:allVersions){
            System.out.println(pair);
        }
    }
}