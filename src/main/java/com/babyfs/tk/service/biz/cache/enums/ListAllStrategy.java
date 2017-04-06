package com.babyfs.tk.service.biz.cache.enums;

/**
 * 当需要查询列表类型数据的所有数据时提供的两种策略
 * <p/>
 * <p/>
 * 策略 1：按照分页模式，不断的获取下一页，直到不再取到数据
 * <p/>
 * 优点：可以很好的命中基于cursor的分页缓存
 * 缺点：对缓存操作次数过多，对数据库操作次数过多
 * <p/>
 * 策略 2：先从缓存中获取所有数据，再从数据库中获取剩余所有数据
 * <p/>
 * 优点：查询次数少，缓存查询一次，数据库查询一次
 * 缺点：基于cursor的缓存很难命中，如果数据超大，单次查询的数量过大，时间长
 * <p/>
 * 综合考虑，目前默认使用第二种策略，因为cursor的缓存时间可能会设的很短
 * 可根据实际业务情况进行选择
 * <p/>
 */
public enum ListAllStrategy {

    /** 根据cursor不断查询下一页 */
    CURSOR_BY_CURSOR,
    /** 一次性查询所有缓存和数据库 */
    ALL_IN_ONE,

}
