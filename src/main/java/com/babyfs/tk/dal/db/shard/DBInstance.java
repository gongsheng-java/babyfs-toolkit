package com.babyfs.tk.dal.db.shard;

import com.google.common.base.Strings;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * 数据库实例
 */
public class DBInstance implements IDBObject {
    private final String id;
    private final String ip;
    private final String user;
    private final String password;
    private final int port;
    private final int hashCode;
    private final int seqId;

    /**
     * @param id       数据库实例的id,全局惟一
     * @param ip       数据库的ip
     * @param port     数据库的端口
     * @param user     用户名
     * @param password 密码
     */
    public DBInstance(@Nonnull String id, @Nonnull String ip, @Nonnegative int port, @Nonnull String user, @Nonnull String password) {
        checkArgument(!Strings.isNullOrEmpty(id), "id");
        checkArgument(!Strings.isNullOrEmpty(ip), "ip");
        checkArgument(port > 0, "port");
        checkArgument(!Strings.isNullOrEmpty(user), "user");
        checkArgument(password != null, "password");
        this.id = id;
        this.ip = ip;
        this.user = user;
        this.password = password;
        this.port = port;
        this.seqId = ShardUtil.SEQUENCE.incrementAndGet();
        this.hashCode = genHashCode();
    }

    public String getId() {

        return id;
    }


    public String getIp() {
        return ip;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public int getPort() {
        return port;
    }

    @Override
    public int getSeqId() {
        return this.seqId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DBInstance that = (DBInstance) o;

        if (port != that.port) {
            return false;
        }
        if (!id.equals(that.id)) {
            return false;
        }
        if (!ip.equals(that.ip)) {
            return false;
        }
        if (!password.equals(that.password)) {
            return false;
        }
        if (!user.equals(that.user)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("DBInstance");
        sb.append("{id='").append(id).append('\'');
        sb.append(", ip='").append(ip).append('\'');
        sb.append(", user='").append(user).append('\'');
        sb.append(", password='").append("******").append('\'');
        sb.append(", port=").append(port);
        sb.append(", seqId=").append(seqId);
        sb.append('}');
        return sb.toString();
    }

    private int genHashCode() {
        int result = id.hashCode();
        result = 31 * result + ip.hashCode();
        result = 31 * result + user.hashCode();
        result = 31 * result + password.hashCode();
        result = 31 * result + port;
        return result;
    }
}
