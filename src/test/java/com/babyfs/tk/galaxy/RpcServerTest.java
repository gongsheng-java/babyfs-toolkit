package com.babyfs.tk.galaxy;

import com.babyfs.tk.commons.codec.ICodec;
import com.babyfs.tk.commons.model.ServiceResponse;
import com.babyfs.tk.commons.service.ServiceModule;
import com.babyfs.tk.galaxy.constant.RpcConstant;
import com.babyfs.tk.galaxy.demo.BadService;
import com.babyfs.tk.galaxy.demo.BadServiceImpl;
import com.babyfs.tk.galaxy.demo.Health;
import com.babyfs.tk.galaxy.demo.HealthImpl;
import com.babyfs.tk.galaxy.guice.RpcServerServiceModule;
import com.babyfs.tk.galaxy.guice.RpcServerSupportModule;
import com.babyfs.tk.galaxy.guice.RpcSupportModule;
import com.babyfs.tk.galaxy.guice.ZkServiceRegisterModule;
import com.babyfs.tk.galaxy.server.IServer;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Module;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

@Ignore
public class RpcServerTest extends BaseTest {
    @Inject
    @RpcCodec
    private ICodec codec;


    @BeforeClass
    public static void setUp() throws Exception {
        System.setProperty(RpcConstant.REGISTER_MODE,RpcConstant.REGISTER_MODE_ZK);
        BaseTest.setUp(getSubModules());

    }


    @Test
    public void test() throws IOException {
        IServer server = injector.getInstance(IServer.class);
        System.out.println(server);

        {
            ServicePoint point = new ServicePoint(Health.class, null);
            Utils.parseMethods(point.getType(), meta -> {
                RpcRequest request = new RpcRequest();
                request.setInterfaceName(point.getInterfaceName());
                request.setMethodSign(meta.getSig());
                request.setParameters(new Object[]{null});

                {
                    ServiceResponse<Object> handle = server.handle(request);
                    printResponseMsg(handle);
                    if (handle.isSuccess()) {
                        ServiceResponse data = (ServiceResponse) handle.getData();
                        Assert.assertTrue(data.isSuccess());
                        System.out.println("call " + request + " result:" + data.getData());
                    }
                }

                {
                    ServiceResponse<byte[]> handle1 = server.handle(codec.encode(request));
                    printResponseMsg(handle1);
                    if (handle1.isSuccess()) {
                        ServiceResponse data = (ServiceResponse) codec.decode(handle1.getData());
                        Assert.assertTrue(data.isSuccess());
                        System.out.println("call " + request + " result:" + data.getData());
                    }
                }
                return null;
            });
        }

        {
            ServicePoint point = new ServicePoint(BadService.class, null);
            Utils.parseMethods(point.getType(), meta -> {
                RpcRequest request = new RpcRequest();
                request.setInterfaceName(point.getInterfaceName());
                request.setMethodSign(meta.getSig());
                request.setParameters(new Object[]{null});
                ServiceResponse<Object> handle = server.handle(request);
                printResponseMsg(handle);
                if (handle.isSuccess()) {
                    ServiceResponse data = (ServiceResponse) handle.getData();
                    Assert.assertNull(data);
                } else {
                    Assert.assertEquals(500, handle.getCode());
                    System.out.println(handle.getMsg());
                }
                return null;
            });
        }

        System.out.println("wait finish");

        System.in.read();
    }

    protected static List<Module> getSubModules() {
        List<Module> modules = Lists.newArrayList();
        modules.add(new RpcSupportModule());
        modules.add(new RpcServerSupportModule());

        //注册业务
        modules.add(new ServiceModule() {
            @Override
            protected void configure() {
                bindService(Health.class, HealthImpl.class);
                bindService(BadService.class, BadServiceImpl.class);
            }
        });

        modules.add(new RpcServerServiceModule() {
            @Override
            protected void configure() {
                exposeRPCService(Health.class);
                exposeRPCService(BadService.class);
            }
        });
        return modules;
    }
}
