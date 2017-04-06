package com.babyfs.tk.service.biz.service.backend.user.model.bean;

import com.babyfs.tk.service.biz.service.backend.user.Status;

/**
 * 用户账户
 */
public class UserAccount {
    private long id;
    private String name;

    private String email;

    private String phone;

    private String password;

    private String salt;

    private String displayName;

    private Status stat;

    private IAccountType type;

    public UserAccount() {

    }

    public UserAccount(String name, String displayName, String mail) {
        this.name = name;
        this.displayName = displayName;
        this.email = mail;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Status getStat() {
        return stat;
    }

    public void setStat(Status stat) {
        this.stat = stat;
    }

    public IAccountType getType() {
        return type;
    }

    public void setType(IAccountType type) {
        this.type = type;
    }
}
