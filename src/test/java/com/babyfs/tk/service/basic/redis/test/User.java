package com.babyfs.tk.service.basic.redis.test;


import com.babyfs.tk.dal.orm.AutoIdEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

/**
 */

@Entity
@Table(name = "gsns_user")
public class User  extends AutoIdEntity implements Serializable{

    private static final long serialVersionUID = 1896394648488390533L;

    /** 用户状态常量：正常 */
    public static final int USER_STATUS_NORMAL = 0;
    /** 用户状态常量：删除 */
    public static final int USER_STATUS_DELETE = 1;
    /** 用户状态常量：锁定 */
    public static final int USER_STATUS_LOCK = 2;

    /** 用户激活状态常量：未激活 */
    public static final int USER_ACTIVE_NO = 0;
    /** 用户激活状态常量：激活 */
    public static final int USER_ACTIVE_YES = 1;

    /** 用户类型常量：普通用户 */
    public static final int USER_TYPE_USER = 0;
    /** 用户类型常量：游戏 */
    public static final int USER_TYPE_GAME = 1;

    /** 用户登录账号(邮箱) */
    private String email;

    /** 用户登录密码(md5加密) */
    private String password;

    /** 用户真实姓名 */
    private String userName;

    /** 用户昵称 */
    private String nickName;

    /** 用户头像链接 */
    private String icon;

    /** 用户状态(0:正常;1:已删除; 2:被锁定; */
    private byte stat;

    /** 用户状态(0:未激活;1:激活; */
    private int active;

    /** 用户类型(0:普通用户; 1:游戏[后台添加]) */
    private int type;

    /** 用户注册时间(自1970.1.1到注册时间的毫秒数) */
    private long registerAt;

    /** 用户注册所用IP地址*/
    private String registerIp;

    @Column(name = "email")
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Column(name = "password")
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Column(name = "userName")
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Column(name = "nickName")
    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    @Column(name = "icon")
    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    @Column(name = "stat")
    public byte getStat() {
        return stat;
    }

    public void setStat(byte stat) {
        this.stat = stat;
    }

    @Column(name = "active")
    public int getActive() {
        return active;
    }

    public void setActive(int active) {
        this.active = active;
    }

    @Column(name = "type")
    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Column(name = "registerAt")
    public long getRegisterAt() {
        return registerAt;
    }

    public void setRegisterAt(long registerAt) {
        this.registerAt = registerAt;
    }

    @Column(name = "registerIp")
    public String getRegisterIp() {
        return registerIp;
    }

    public void setRegisterIp(String registerIp) {
        this.registerIp = registerIp;
    }

}
