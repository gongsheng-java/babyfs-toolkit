package com.babyfs.tk.commons.name.impl.yaml;

import com.google.common.base.Function;
import com.babyfs.tk.commons.name.Server;
import org.junit.Assert;
import org.junit.Test;

import javax.annotation.Nullable;
import java.util.List;

/**
 */
public class YamlNameServiceImplTest {
    @Test
    public void testLoad() throws Exception {
        YamlNameServiceProvider ns = new YamlNameServiceProvider("services.yaml");
        List<Server> load = ns.init(new Function<List<Server>, List<Server>>() {
            @Override
            public List<Server> apply(@Nullable List<Server> input) {
                return input;
            }
        });
        Assert.assertNotNull(load);
    }
}
