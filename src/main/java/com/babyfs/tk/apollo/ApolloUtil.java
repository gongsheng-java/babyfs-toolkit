package com.babyfs.tk.apollo;

import com.babyfs.tk.service.basic.probe.Config;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.Set;

import static com.babyfs.tk.apollo.EnvConstants.DEFAULT_NAMESPACE;
import static com.babyfs.tk.apollo.EnvConstants.KEY_SYSTEM_XML_NAMESPACES;

public class ApolloUtil {

    private static Set<String> filteredSet = Sets.newHashSet();

    static {
        if(ConfigLoader.isApolloReady()){
            String config = ConfigLoader.getConfig(KEY_SYSTEM_XML_NAMESPACES);
            if(config != null){
                String[] split = config.split(",");
                filteredSet.addAll(Arrays.asList(split));
            }
        }
    }


    /**
     * 去掉后缀名
     * @param fileName
     * @param pendfix
     * @return
     */
    public static String getNamespace(String fileName, String pendfix){
        if(!ConfigLoader.isApolloReady()) return "";
        if(StringUtils.isEmpty(fileName) || !filteredSet.contains(fileName)){
            return DEFAULT_NAMESPACE;
        }
        return getNamespace(fileName.replace(String.format(".%s", pendfix), ""));
    }

    /**
     * 增加部门前缀
     * @param rawName
     * @return
     */
    public static String getNamespace(String rawName){
        if(!ConfigLoader.isApolloReady()) return "";
        return String.format("%s.%s", ConfigLoader.DEPART_NAME, rawName);
    }
}
