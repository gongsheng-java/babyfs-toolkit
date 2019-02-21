package com.babyfs.tk.apollo;

import com.babyfs.tk.service.basic.probe.Config;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.Resources;
import org.apache.commons.lang.StringUtils;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static com.babyfs.tk.apollo.EnvConstants.DEFAULT_NAMESPACE;
import static com.babyfs.tk.apollo.EnvConstants.KEY_SYSTEM_XML_NAMESPACES;
import static com.babyfs.tk.apollo.EnvConstants.OVERRIDE_PROPS_FILE;

public class ApolloUtil {

    private static Set<String> filteredSet = Sets.newHashSet();

    private final static String APP_PROPERTY_FILE_PATH = "META-INF/app.properties";
    private final static String APP_ID_KEY = "app.id";

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

    public static String getAppId(){
        String property = System.getProperty(APP_ID_KEY);
        if(!Strings.isNullOrEmpty(property)){
            return property;
        }
        try{
            Properties properties = new Properties();
            try(InputStream is = Resources.asByteSource(Resources.getResource(APP_PROPERTY_FILE_PATH)).openStream()){
                properties.load(is);
                if(properties.size() > 0){
                    property = properties.getProperty(APP_ID_KEY);
                    if(!Strings.isNullOrEmpty(property)){
                        return property;
                    }
                }
            }

        }catch (Exception e){
//
        }
        return "";
    }
}
