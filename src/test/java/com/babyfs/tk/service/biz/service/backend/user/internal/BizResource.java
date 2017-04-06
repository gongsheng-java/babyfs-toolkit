package com.babyfs.tk.service.biz.service.backend.user.internal;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.babyfs.tk.service.biz.service.backend.user.Util;
import com.babyfs.tk.service.biz.service.backend.user.model.IBizResource;
import com.babyfs.tk.service.biz.service.backend.user.model.ResourceType;

import java.util.Map;

/**
 * 业务模块资源
 */
public enum BizResource implements IBizResource {
    /*根*/
    ROOT(null, "r", null),

    /* 首页模块 */
    HOME("首页", "h", ROOT),
    HOME_PROFILE("个人信息", "profile", HOME),

    /* 商品模块 */
    PRODUCT("商品", "p", ROOT),
    PRODUCT_PROP("属性管理", "prop", PRODUCT),
    PRODUCT_CATE("类目管理", "cate", PRODUCT),
    PRODUCT_SPU("商品管理", "spu", PRODUCT),

    /* 运营 */
    OPER("运营", "o", ROOT),
    OPER_USER("账号管理", "user", OPER),
    OPER_ROLE("角色管理", "role", OPER),
    OPER_MAIL("邮件组管理", "mailgroup", OPER),

    /* 订单 */
    ORDER("订单", "od", ROOT),
    ORDER_MANAGE("订单管理", "order", ORDER),

    /* 财务 */
    FINANCE("财务", "f", ROOT),
    FINANCE_INCOME("收入管理", "income", FINANCE),

    /* 财务 */
    AFTER_SALE("售后", "as", ROOT),
    AFTER_SALE_MANAGE("售后申请管理", "manage", AFTER_SALE),

    /* 库存 */
    STOCK("库存", "s", ROOT),
    STOCK_SELLABLE("可售库存管理", "sas", STOCK),

    //进销存
    SCM("进销存", "scm", ROOT),
    SCM_CATEGORY("类别管理", "cate", SCM),
    SCM_SUPPIZER("供应商管理", "sup", SCM),
    SCM_SELLER("销售商管理", "sell", SCM),
    SCM_CUSTOMER("客户管理", "cust", SCM),
    SCM_WAREHOUSE("仓库管理", "ware", SCM),
    SCM_PUR_ORDER("购货订单管理", "pur_order", SCM),
    SCM_PUR_FORM("购货单管理", "pur_form", SCM),
    SCM_SELL_FORM("销货单管理", "sell_form", SCM),
    SCM_ADJUST_FORM("调货单管理", "adjust_form", SCM),
    SCM_GOAL("目标管理", "goal", SCM),
    SCM_GROUP("分组管理", "group", SCM),

    SCM_REPORT("进销存销售报表", "scm_repo", ROOT),
    SCM_SELL_REPORT("销售报表", "seller", SCM_REPORT),
    SCM_WAREHOUSE_REPORT("库存报表", "stock", SCM_REPORT),
    SCM_DELIVER_DETAIL_REPORT("商品收发明细报表", "deliver", SCM_REPORT),

    /**
     * 暂时所有进销存单统一权限，以后有需要，可以根据不同的单据分开权限
     */
    SCM_FORM_AUDIT("进销存单审核", "formaudit", SCM);


    /**
     * 资源的类型
     */
    public static final int TYPE = ResourceType.BIZ_RESOURCE.getValue();

    /**
     * 资源的id直接对应{@link BizResource}
     */
    private static final Map<String, ? extends IBizResource> FLAT_MAP = Util.buildBizFlatMap(Lists.<IBizResource>newArrayList(BizResource.values()));

    private final String name;
    private final String id;
    private final IBizResource parent;

    /**
     * @param name
     * @param id
     * @param parent
     */
    BizResource(String name, String id, BizResource parent) {
        Preconditions.checkArgument(id != null, "id");
        this.name = name;
        this.parent = parent;
        this.id = Util.buildBizId(id, parent);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public int getType() {
        return TYPE;
    }

    public IBizResource getParent() {
        return parent;
    }

    /**
     * 根据id查找对应的资源
     *
     * @param id
     * @return
     */
    public static IBizResource getResById(String id) {
        return FLAT_MAP.get(id);
    }

}
