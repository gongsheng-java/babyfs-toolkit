package com.babyfs.tk.service.basic.es.guice;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import com.babyfs.tk.service.basic.guice.BasicServiceModule;
import org.elasticsearch.client.Client;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 */
public class ESClientProviderTest {
    @Test
    @Ignore
    public void test_client() {
        BasicServiceModule module = new BasicServiceModule() {
            @Override
            protected void configure() {
                bindServiceWithConf(Client.class, "es-client.xml", ESClientProvider.class);
            }
        };
        Injector injector = Guice.createInjector(module);
        Client client = injector.getInstance(Client.class);
        Assert.assertNotNull(client);
    }

    @Test
    @Ignore
    public void test_client_withname() {
        BasicServiceModule nodeClientModule = new BasicServiceModule() {
            @Override
            protected void configure() {
                bindServiceWithConf(Client.class, "es-client.xml", ESClientProvider.class);
            }
        };
        BasicServiceModule transportClientModule = new BasicServiceModule() {
            @Override
            protected void configure() {
                bindNamedServiceWithConf(Client.class, "transportclient", "es-client-transport.xml", ESClientProvider.class);
            }
        };
        Injector injector = Guice.createInjector(nodeClientModule, transportClientModule);
        Client nodeClient = injector.getInstance(Client.class);
        System.out.println(nodeClient);
        Assert.assertNotNull(nodeClient);

        Key<Client> transportclientKey = Key.get(Client.class, Names.named("transportclient"));
        Client transportClient = injector.getInstance(transportclientKey);
        System.out.println(transportClient);
        Assert.assertNotNull(transportClient);
    }
}
