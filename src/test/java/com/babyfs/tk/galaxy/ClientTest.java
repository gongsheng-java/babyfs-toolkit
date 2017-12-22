package com.babyfs.tk.galaxy;

import com.babyfs.tk.galaxy.client.Galaxy;
import com.babyfs.tk.galaxy.demo.Health;
import com.babyfs.tk.galaxy.demo.model.PostModel;
import com.babyfs.tk.galaxy.demo.DemoDiscoveryProperties;
import com.babyfs.tk.galaxy.register.LoadBalance;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class ClientTest {

    @Test
    public void test(){

        LoadBalance loadBalance = LoadBalance.builder().discoveryProperties(new DemoDiscoveryProperties()).build();
        Health health = Galaxy.builder().loadBalance(loadBalance).target(Health.class,"api");
        PostModel postModel = health.notJsonTest(1l);
        Assert.assertTrue(postModel.getMessage()!=null);
    }
}
