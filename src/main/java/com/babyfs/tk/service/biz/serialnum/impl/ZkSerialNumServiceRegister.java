package com.babyfs.tk.service.biz.serialnum.impl;

import com.alibaba.fastjson.JSON;
import com.babyfs.tk.service.biz.serialnum.ISerialNumServiceRegister;
import com.babyfs.tk.service.biz.serialnum.NetUtils;
import com.babyfs.tk.commons.service.LifeServiceSupport;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class ZkSerialNumServiceRegister extends LifeServiceSupport implements ISerialNumServiceRegister {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZkSerialNumServiceRegister.class);

    /**
     * 本机IP
     */
    private final String ip;

    /**
     * zk 注册根目录
     */
    private final String zkRoot;

    /**
     * zk 注册目录
     */
    private final String zkNode;

    private final CuratorFramework curator;

    private volatile boolean running;

    private Map<String, Integer> clients = Maps.newHashMap();

    private int number = -1;

    private String zkPath;

    public ZkSerialNumServiceRegister(String zkRoot, String zkNode, CuratorFramework curator) {
        this.zkRoot = Preconditions.checkNotNull(zkRoot);
        this.zkNode = Preconditions.checkNotNull(zkNode);
        this.curator = Preconditions.checkNotNull(curator);
        this.ip = Preconditions.checkNotNull(NetUtils.getLocalAddress().getHostAddress());
        this.zkPath = this.zkRoot + this.zkNode;
        this.running = false;
    }

    @Override
    protected void execStart() {
        if (running) {
            return;
        }

        try {
            Stat state = curator.checkExists().forPath(zkRoot);
            if (null == state) {
                curator.create().withMode(CreateMode.PERSISTENT).forPath(zkRoot);
                LOGGER.info("create persistent zkRoot : {}", zkRoot);
            }
            if (state.getNumChildren() > 0) {
                List<String> childs = curator.getChildren().forPath(zkRoot);
                for (String node : childs) {
                    byte[] data = curator.getData().forPath(zkRoot + "/" + node);
                    RegisterEntity entity = JSON.parseObject(data, RegisterEntity.class);
                    if (null != entity) {
                        int id = getNumberFromNode(node);
                        if (id != -1) {
                            clients.put(entity.ip, id);
                        }
                    }
                }
            }

            if (state.getNumChildren() == 0 || !clients.containsKey(ip)) {
                String path = curator.create().withMode(CreateMode.PERSISTENT_SEQUENTIAL).forPath(zkPath);
                LOGGER.info("create persistent sequential zknode : {}", path);
                byte[] data = JSON.toJSONBytes(new RegisterEntity(this.ip, System.currentTimeMillis()));
                curator.setData().forPath(zkPath, data);
                int id = getNumberFromNode(path);
                if (id != -1) {
                    clients.put(this.ip, id);
                }
            }

            if (!clients.containsKey(ip)) {
                throw new RuntimeException("register serial num client fail. can not find ip: " + ip + " in zk");
            }

            number = clients.get(ip);
            running = true;

        } catch (Exception e) {
            LOGGER.error("serial num client start fail", e);
            throw new RuntimeException(e);
        } finally {
            curator.close();
        }
    }

    @Override
    public int getSerialNum() {
        return this.number;
    }

    private static class RegisterEntity implements Serializable {
        private static final long serialVersionUID = 4128176388229686153L;
        private String ip;
        private long time;

        public RegisterEntity() {
        }

        public RegisterEntity(String ip, long time) {
            this.ip = ip;
            this.time = time;
        }

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public long getTime() {
            return time;
        }

        public void setTime(long time) {
            this.time = time;
        }

    }

    private int getNumberFromNode(String node) {
        List<String> result = Splitter.on(zkNode).splitToList(node);
        if (result.size() > 1) {
            return Integer.valueOf(result.get(result.size() - 1));
        }

        return -1;
    }
}



























