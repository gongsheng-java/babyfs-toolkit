package com.babyfs.tk.apollo;

import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.Set;

import static com.babyfs.tk.apollo.EnvConstants.*;

public class ApolloUtil {

    private static Set<String> filteredSet = Sets.newHashSet();

    static {
        String config = ConfigLoader.getConfig(KEY_SYSTEM_XML_NAMESPACES);
        String[] split = config.split(",");
        filteredSet.addAll(Arrays.asList(split));
    }


    public static String getNamespace(String fileName, String pendfix){
        if(StringUtils.isEmpty(fileName) || !filteredSet.contains(fileName)){
            return DEFAULT_NAMESPACE;
        }
        return String.format("%s.%s", DEFAULT_DEPARTNAME, fileName.replace(String.format(".%s", pendfix), ""));
    }
}
