package com.babyfs.tk.service.basic.guice.constants;

import com.google.inject.Key;
import com.google.inject.name.Names;
import com.babyfs.tk.commons.guice.GuiceKeys;
import com.babyfs.tk.service.basic.xml.mongodb.MongodbConfigs;

import java.util.Map;

/**
 * <p/>
 */
public final class NamesConstants {
    // mongo
    public static final String NAME_MONGODB_SERVICES = "name.mongodb.services";
    public static final String NAME_MONGODB_CLUSTERS_CONFIG = "name.mongodb.clusters";
    public static final Key<Map<String, MongodbConfigs.ClusterElement>> KEY_MONGODB_CLUSTERS_CONFIG = GuiceKeys.getKey(Map.class, Names.named(NamesConstants.NAME_MONGODB_CLUSTERS_CONFIG), String.class, MongodbConfigs.ClusterElement.class);
    public static final String NAME_MONGODB_SERVICES_CONFIG = "name.mongodb.services";
    public static final Key<Map<String, MongodbConfigs.ServiceElement>> KEY_MONGODB_SERVICES_CONFIG = GuiceKeys.getKey(Map.class, Names.named(NamesConstants.NAME_MONGODB_SERVICES_CONFIG), String.class, MongodbConfigs.ServiceElement.class);

    private NamesConstants() {

    }

}
