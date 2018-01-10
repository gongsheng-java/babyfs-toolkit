package com.babyfs.tk.galaxy;

import com.babyfs.tk.commons.model.ServiceResponse;
import com.babyfs.tk.galaxy.client.ClientProxyBuilder;
import com.babyfs.tk.galaxy.client.IClientProxy;
import com.babyfs.tk.galaxy.demo.Health;
import com.babyfs.tk.galaxy.demo.PostModel;
import com.babyfs.tk.galaxy.demo.DemoDiscoveryProperties;
import com.babyfs.tk.galaxy.register.LoadBalanceImpl;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class ClientTest {

    @Test
    public void test(){

        LoadBalanceImpl loadBalance = LoadBalanceImpl.builder().discoveryProperties(new DemoDiscoveryProperties()).build("127.0.0.1:2181",20000,20000);
        Health health = ClientProxyBuilder.builder().loadBalance(loadBalance).target(Health.class,"api");
        ServiceResponse<PostModel> serviceResponse = health.notJsonTest(1l);
        Assert.assertTrue(serviceResponse.getData().getMessage()!=null);
    }
}
