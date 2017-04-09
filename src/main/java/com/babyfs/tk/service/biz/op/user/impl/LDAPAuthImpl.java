package com.babyfs.tk.service.biz.op.user.impl;

import com.babyfs.tk.service.biz.op.user.IAuth;
import com.babyfs.tk.service.biz.op.user.model.AccountType;
import com.babyfs.tk.service.biz.op.user.model.UserAccount;
import com.babyfs.tk.service.biz.op.user.model.IAccountType;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.babyfs.tk.commons.config.IConfigService;
import com.babyfs.tk.commons.utils.MapUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import javax.security.auth.login.LoginException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * LDAP认证
 */
public class LDAPAuthImpl implements IAuth {
    private static final Logger LOGGER = LoggerFactory.getLogger(LDAPAuthImpl.class);
    private static final String LDAP = "com.sun.jndi.ldap.LdapCtxFactory";
    protected static final String S_AMACCOUNT_NAME = "sAMAccountName";
    protected static final String MAIL = "mail";
    protected static final String SN = "sn";
    protected static final String GIVEN_NAME = "givenName";
    private static final String[] ATTRIBUTES = new String[]{S_AMACCOUNT_NAME, MAIL, SN, GIVEN_NAME};

    private static final long TIMEOUT_MILLIS = 5000;
    private static final String DN = "dn";
    private static final String SIMPLE = "simple";
    private static final String IGNORE = "ignore";
    private static final String DEFAULT_ACCOUNT_PATTERN = "(&(objectClass=user)(sAMAccountName={0}))";

    protected static final String SERVER = "ldap.server";
    protected static final String USERNAME = "ldap.username";
    protected static final String PASSWORD = "ldap.password";
    protected static final String ACCOUNT_BASE = "ldap.accountBase";
    protected static final String ACCOUNT_PATTERN = "ldap.accountPattern";

    private final String server;
    private final String bindUsername;
    private final String bindPassword;
    private final String accountBase;
    private final String accountPattern;

    /**
     * @param config
     */
    @Inject
    public LDAPAuthImpl(IConfigService config) {
        Preconditions.checkNotNull(config);
        this.server = Preconditions.checkNotNull(config.get(SERVER), SERVER);
        this.bindUsername = Preconditions.checkNotNull(config.get(USERNAME), USERNAME);
        this.bindPassword = Preconditions.checkNotNull(config.get(PASSWORD), PASSWORD);
        this.accountBase = Preconditions.checkNotNull(config.get(ACCOUNT_BASE), ACCOUNT_BASE);
        this.accountPattern = MapUtil.get(config, ACCOUNT_PATTERN, DEFAULT_ACCOUNT_PATTERN);
    }

    /**
     * 验证账户
     *
     * @param userAccount
     * @return ture, 验证通过;false,验证失败
     */
    @Override
    public boolean auth(UserAccount userAccount) {
        DirContext context = null;
        try {
            context = open();
            Map<String, String> account = findAccount(context, userAccount.getName());
            if (account == null || account.isEmpty()) {
                LOGGER.warn("Can't find user {}", userAccount.getName());
                return false;
            }
            String dn = account.get(DN);
            return authenticate(dn, userAccount.getPassword());
        } catch (Exception e) {
            LOGGER.error("auth user " + userAccount.getName() + " error.", e);
            return false;
        } finally {
            if (context != null) {
                try {
                    context.close();
                } catch (NamingException e) {
                    LOGGER.error("auth account error", e);
                }
            }
        }
    }

    /**
     * LDAP不支持密码校验
     *
     * @param inputPassord 输入的密码
     * @param passowrd     hash后的密码
     * @param salt         盐值
     * @return
     */
    @Override
    public boolean auth(String inputPassord, String passowrd, String salt) {
        return false;
    }

    /**
     * 查找一个用户,并返回相关的属性
     *
     * @param userAccount
     * @return
     */
    @Override
    public UserAccount createToAddUserAccount(UserAccount userAccount) {
        DirContext context = null;
        String name = userAccount.getName();
        try {
            context = open();
            Map<String, String> account = findAccount(context, name);
            if (account == null || account.isEmpty()) {
                return null;
            }
            String ldapName = Preconditions.checkNotNull(account.get(S_AMACCOUNT_NAME));
            String ldapDisplayName = Joiner.on("").skipNulls().join(new String[]{account.get(SN), account.get(GIVEN_NAME)});
            String ldapMail = Preconditions.checkNotNull(account.get(MAIL));
            return new UserAccount(ldapName, ldapDisplayName, ldapMail);
        } catch (Exception e) {
            LOGGER.error("find user " + name + "error.", e);
            return null;
        } finally {
            if (context != null) {
                try {
                    context.close();
                } catch (NamingException e) {
                    LOGGER.error("find account error", e);
                }
            }
        }
    }

    @Override
    public IAccountType getAccountType() {
        return AccountType.LDAP;
    }

    @Override
    public boolean supportChangePassword() {
        return false;
    }

    /**
     * @return
     */
    private Properties createContextProperties() {
        final Properties env = new Properties();
        env.put(Context.INITIAL_CONTEXT_FACTORY, LDAP);
        env.put(Context.PROVIDER_URL, server);
        env.put("com.sun.jndi.ldap.read.timeout", Long.toString(TIMEOUT_MILLIS));
        env.put("com.sun.jndi.ldap.connect.timeout", Long.toString(TIMEOUT_MILLIS));
        return env;
    }


    /**
     * 建立初始的LDAP链接
     *
     * @return
     * @throws NamingException
     * @throws LoginException
     */
    DirContext open() throws NamingException, LoginException {
        final Properties env = createContextProperties();
        env.put(Context.SECURITY_AUTHENTICATION, SIMPLE);
        env.put(Context.REFERRAL, IGNORE);
        env.put(Context.SECURITY_PRINCIPAL, bindUsername);
        env.put(Context.SECURITY_CREDENTIALS, bindPassword);
        return new InitialDirContext(env);
    }

    Map<String, String> findAccount(final DirContext ctx, final String username) throws NamingException {
        final NamingEnumeration<SearchResult> res;
        final SearchControls sc = new SearchControls();
        final Map<String, String> ret = Maps.newHashMap();

        sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
        sc.setReturningAttributes(ATTRIBUTES);
        res = ctx.search(accountBase, accountPattern, new Object[]{username}, sc);
        List<SearchResult> resultList = Lists.newArrayList();
        while (res.hasMore()) {
            resultList.add(res.next());
        }
        switch (resultList.size()) {
            case 0:
                return null;
            case 1:
                SearchResult searchResult = resultList.get(0);
                Attributes searchAttrs = searchResult.getAttributes();
                for (String attr : ATTRIBUTES) {
                    Attribute attribute = searchAttrs.get(attr);
                    if (attribute != null) {
                        ret.put(attr, String.valueOf(attribute.get(0)));
                    }
                }
                ret.put(DN, searchResult.getNameInNamespace());
                return ret;
            default:
                throw new IllegalStateException("Duplicate users: " + username);
        }
    }

    /**
     * 验证用用户名和密码
     *
     * @param dn
     * @param password
     * @return
     */
    boolean authenticate(String dn, String password) {
        dn = StringUtils.trimToNull(dn);
        password = StringUtils.trimToNull(password);
        if (Strings.isNullOrEmpty(dn) || Strings.isNullOrEmpty(password)) {
            //dn和password不能为空
            return false;
        }
        final Properties env = createContextProperties();
        env.put(Context.SECURITY_AUTHENTICATION, SIMPLE);
        env.put(Context.SECURITY_PRINCIPAL, dn);
        env.put(Context.SECURITY_CREDENTIALS, password);
        env.put(Context.REFERRAL, IGNORE);
        DirContext context = null;
        try {
            context = new InitialDirContext(env);
            return true;
        } catch (NamingException e) {
            throw new RuntimeException("Incorrect username or password", e);
        } finally {
            if (context != null) {
                try {
                    context.close();
                } catch (NamingException e) {
                    LOGGER.error("auth account error", e);
                }
            }
        }
    }
}
