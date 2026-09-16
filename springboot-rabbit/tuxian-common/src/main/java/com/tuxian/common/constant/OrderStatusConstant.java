package com.tuxian.common.constant;

/**
 * 订单状态常量。
 * <p>
 * 与前端约定一致：orderState 为数字 0~6，0 表示“全部”（仅用于查询过滤），
 * 1~6 分别对应 待付款 / 待发货 / 待收货 / 待评价 / 已完成 / 已取消。
 */
public class OrderStatusConstant {

    /** 全部（查询过滤用） */
    public static final int ALL = 0;
    /** 待付款 */
    public static final int UNPAY = 1;
    /** 待发货 */
    public static final int WAIT_DELIVER = 2;
    /** 待收货 */
    public static final int WAIT_RECEIVE = 3;
    /** 待评价 */
    public static final int WAIT_COMMENT = 4;
    /** 已完成 */
    public static final int COMPLETED = 5;
    /** 已取消 */
    public static final int CANCELED = 6;
}