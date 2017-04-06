package com.babyfs.tk.commons.name.impl.zookeeper;

import com.google.common.base.Function;
import com.babyfs.tk.commons.Constants;
import com.babyfs.tk.commons.name.model.gen.NamingServices;
import com.babyfs.tk.commons.utils.ListUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 */
public class ServerNodeCodecTest {
    private NamingServices.NSServer server;

    @Before
    public void before() {
        NamingServices.NSServer.Builder svr_1 = NamingServices.NSServer.newBuilder().setId("svr_1").setIp("127.0.0.1").setPort(9123);
        svr_1.setRegisterToken(UUID.randomUUID().toString());
        List<String> services = new ArrayList<String>(2);
        for (int i = 0; i < 2; i++) {
            services.add("service_ServerNodeCodecTest"+i);
        }
        svr_1.addAllServices(ListUtil.transform(services, new Function<String, String>() {
            @Override
            public String apply(@Nullable String input) {
                return input;
            }
        }));
        server = svr_1.build();

    }

    @Test
    public void testJsonEncode() throws Exception {
        ServerNodeJsonCodec codec = new ServerNodeJsonCodec();
        byte[] encode = codec.encode(server);
        System.out.println("json len:" + encode.length);
        System.out.println(new String(encode, Constants.DEFAULT_CHARSET));

        NamingServices.NSServer server = (NamingServices.NSServer) codec.decode(encode);
        Assert.assertNotNull(server);
    }

    @Test
    public void testProtoEncode() throws Exception {
        ServerNodeProtoCodec codec = new ServerNodeProtoCodec();
        byte[] encode = codec.encode(server);
        System.out.println("proto len:" + encode.length);

        NamingServices.NSServer server = (NamingServices.NSServer) codec.decode(encode);
        Assert.assertNotNull(server);
    }
}
