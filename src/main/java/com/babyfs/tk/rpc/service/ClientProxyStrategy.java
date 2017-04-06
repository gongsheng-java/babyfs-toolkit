package com.babyfs.tk.rpc.service;

import com.babyfs.tk.rpc.NameContext;
import com.babyfs.tk.rpc.guice.ClientServiceModule;

/**
 * Client Proxy选择Server的的策略,以下分别介绍各种策略的用法<br />
 * {@link #AUTO}:
 * AUTO在使用的时候没有特殊的要求,RPC Server的选择是随机的
 * <br/>
 * {@link #STICKY}:
 * STICKY为RPC调用提供一定的粘性机制,如为用户A处理UserService的请求时,A用户的
 * 所有请求都尽可能地发往同一个RPC Server.这种情况的代码写法如下:
 * <pre>
 *     String rpcServerId = user.getBindServerId();
 *     try{
 *       if(rpcServerId != null){
 *           NameContext.setCurServerId(rpcServerId); //设置rpc调用的上下文
 *       }
 *       userService.find(user.getId);
 *     }finally{
 *     //首先通过NameContext取得新的serverId,当rpcServerId指向的服务器无法找到时,会自动查找下一个可用的sever,
 *     //调用者可以通过NameContext.getAndCleantNewServerId()取得该值,并保存起来
 *      String newServerId = NameContext.getAndCleantNewServerId();
 *      user.setBindServerId(newServerId);
 *      //清除上下文中的当前serverId
 *      NameContext.cleanCurServerId();
 *     }
 * </pre>
 * <br/>
 * {@link #RESTRICT}:
 * RESTRICT严格指定RPC Server的ID,每次调用前都要指定目标Server的ID,例如:
 * <pre>
 *     String rpcServerId = user.getBindServerId();
 *     try{
 *       NameContext.setCurServerId(rpcServerId); //设置rpc调用的上下文,如果rpcServerId为空,此处会抛出异常
 *       userService.find(user.getId);
 *     }finally{
 *      //清除上下文中的当前serverId
 *      NameContext.cleanCurServerId();
 *     }
 *
 * </pre>
 * <br/>
 * 特别需要注意的是,不能将NameContext用在嵌套调用中.
 *
 * @see {@link NameContext}
 * @see {@link ClientServiceModule#bindProxyService(Class, ClientProxyStrategy)}
 */
public enum ClientProxyStrategy {
    /**
     * 自动选择server
     */
    AUTO,
    /**
     * 粘性选择server
     */
    STICKY,
    /**
     * 严格选择server
     */
    RESTRICT;
}
