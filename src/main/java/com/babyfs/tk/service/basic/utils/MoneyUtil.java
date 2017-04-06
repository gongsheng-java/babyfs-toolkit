package com.babyfs.tk.service.basic.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 和钱相关的工具类
 */
public final class MoneyUtil {
    /**
     * 默认的除法运算精度
     */
    public static final int DEFAULT_DIV_SCALE = 10;

    private MoneyUtil() {

    }

    /**
     * @param value
     * @return
     */
    public static BigDecimal toBigDecimal(int value) {
        return new BigDecimal(value);
    }

    /**
     * @param value
     * @return
     */
    public static BigDecimal toBigDecimal(long value) {
        return new BigDecimal(value);
    }

    /**
     * @param value
     * @return
     */
    public static BigDecimal toBigDecimal(float value) {
        return new BigDecimal(Float.toString(value));
    }

    /**
     * @param value
     * @return
     */
    public static BigDecimal toBigDecimal(double value) {
        return new BigDecimal(Double.toString(value));
    }

    /**
     * @param value
     * @return
     */
    public static BigDecimal toBigDecimal(Number value) {
        return new BigDecimal(value.toString());
    }

    /**
     * @param value
     * @return
     */
    public static BigDecimal toBigDecimal(String value) {
        return new BigDecimal(value);
    }


    /**
     * 将以元为单位表示的数字转换为以分为单位表示的数字(yuan * 100)
     *
     * @param yuan
     * @return
     * @throws ArithmeticException 如果不能完整地转换为整数,即乘100后还有小数，会抛出这个异常
     */
    public static long yuanToFen(double yuan) {
        return yuanToFen(Double.toString(yuan));
    }

    /**
     * 将以元为单位表示的数字转换为以分为单位表示的整数(yuan * 100)
     *
     * @param yuan 不能为空,如果是小数表示的格式，则最多可以有两位小数
     * @return
     * @throws ArithmeticException 如果不能完整地转换为整数,即乘100后还有小数，会抛出这个异常
     */
    public static long yuanToFen(String yuan) {
        BigDecimal bigYuan = new BigDecimal(yuan);
        return bigYuan.multiply(toBigDecimal(100)).longValueExact();
    }

    /**
     * 将以分为单位表示的数字转换为以元为单位表示的数字(yuan / 100)
     *
     * @param value
     * @return
     */
    public static BigDecimal fenToYuan(long value) {
        return toBigDecimal(value).divide(toBigDecimal(100));
    }

    /**
     * 将以分为单位表示的数字转换为以元为单位表示的数字(yuan / 100)
     *
     * @param value
     * @return
     */
    public static String fenToYuanStr(long value) {
        return fenToYuan(value).toPlainString();
    }

    /**
     * 两个double相加
     *
     * @param v1
     * @param v2
     * @return
     */
    public static BigDecimal add(double v1, double v2) {
        return toBigDecimal(v1).add(toBigDecimal(v2));
    }

    /**
     * 两个double相减
     *
     * @param v1
     * @param v2
     * @return
     */
    public static BigDecimal sub(double v1, double v2) {
        return toBigDecimal(v1).subtract(toBigDecimal(v2));
    }

    /**
     * 两个double相乘
     *
     * @param v1
     * @param v2
     * @return
     */
    public static BigDecimal multi(double v1, double v2) {
        return toBigDecimal(v1).multiply(toBigDecimal(v2));
    }

    /**
     * 两个double相除,使用默认的运算精度{@link #DEFAULT_DIV_SCALE},结果四舍五入
     *
     * @param v1
     * @param v2
     * @return
     */
    public static BigDecimal div(double v1, double v2) {
        return div(v1, v2, DEFAULT_DIV_SCALE);
    }

    /**
     * 两个double相除,四舍五入
     *
     * @param v1    除数
     * @param v2    被除数
     * @param scale 指定结果的小数点后的位数
     * @return
     */
    public static BigDecimal div(double v1, double v2, int scale) {
        return div(v1, v2, scale, RoundingMode.HALF_UP);
    }

    /**
     * 两个double相除
     *
     * @param v1           除数
     * @param v2           被除数
     * @param scale        指定结果的小数点后的位数
     * @param roundingMode 指定的RoundingMode
     * @return
     */
    public static BigDecimal div(double v1, double v2, int scale, RoundingMode roundingMode) {
        return toBigDecimal(v1).divide(toBigDecimal(v2), scale, roundingMode);
    }
}
