package com.babyfs.tk.galaxy;

import com.babyfs.tk.galaxy.register.LoadBalanceTest;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class ClientTest {

    @Test
    public void test() {
//        LoadBalanceImpl loadBalance = LoadBalanceImpl.builder().rpcConfig(new DemoApiDiscoveryProperties()).build("127.0.0.1:2181",20000,20000);
//        Health health = ClientProxyBuilder.builder().loadBalance(loadBalance).target(Health.class,"api");
//        ServiceResponse<PostModel> serviceResponse = health.notJsonTest(1l);
//        Assert.assertTrue(serviceResponse.getData().getMessage()!=null);
        System.out.println(LoadBalanceTest.class.getSimpleName());
        System.out.println(LoadBalanceTest.class.getName());
    }
}
