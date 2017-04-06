package com.babyfs.tk.commons.enums;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * 测试可索引枚举接口
 * <p/>
 */
public class IndexedEnumTest {

    private static enum TestEnum implements IndexedEnum {

        T_Enum_0(0),
        T_Enum_1(1);

        private static final List<TestEnum> indexes = Util.toIndexes(TestEnum.values());

        private final int index;

        TestEnum(int index) {
            this.index = index;
        }

        @Override
        public int getIndex() {
            return index;
        }

        public static TestEnum indexOf(final int index) {
            return Util.valueOf(indexes, index);
        }
    }

    private static enum TestExceed implements IndexedEnum {

        T_EXCEED_0(0),
        T_EXCEED_1(1),
        T_EXCEED_1001(1001);

        private static final List<TestExceed> indexes = Util.toIndexes(TestExceed.values());

        private final int index;

        TestExceed(int index) {
            this.index = index;
        }

        @Override
        public int getIndex() {
            return index;
        }

        public static TestExceed indexOf(final int index) {
            return Util.valueOf(indexes, index);
        }
    }

    private static enum TestNegative implements IndexedEnum {

        TEST_NEGATIVE(-1);

        private static final List<TestNegative> indexes = Util.toIndexes(TestNegative.values());

        private final int index;

        TestNegative(int index) {
            this.index = index;
        }

        @Override
        public int getIndex() {
            return index;
        }

        public static TestNegative indexOf(final int index) {
            return Util.valueOf(indexes, index);
        }
    }

    private static enum TestDouble implements IndexedEnum {

        T_DOUBLE_0(0),
        T_DOUBLE_1(0);

        private static final List<TestDouble> indexes = Util.toIndexes(TestDouble.values());

        private final int index;

        TestDouble(int index) {
            this.index = index;
        }

        @Override
        public int getIndex() {
            return index;
        }

        public static TestDouble indexOf(final int index) {
            return Util.valueOf(indexes, index);
        }
    }

    private static enum TestNull_A implements IndexedEnum {

        NULL_ENUM(0);

        private static final List<TestNull_A> indexes = Util.toIndexes(TestNull_A.values());

        private final int index;

        TestNull_A(int index) {
            this.index = index;
        }

        @Override
        public int getIndex() {
            return index;
        }

        public static TestNull_A indexOf(final int index) {
            return Util.valueOf(null, index);
        }
    }

    private static enum TestNull_B implements IndexedEnum {

        ;

        private static final List<TestNull_B> indexes = Util.toIndexes(TestNull_B.values());

        private final int index;

        TestNull_B(int index) {
            this.index = index;
        }

        @Override
        public int getIndex() {
            return index;
        }

        public static TestNull_B indexOf(final int index) {
            return Util.valueOf(indexes, index);
        }
    }


    /**
     * 正常情况测试
     */
    @Test
    public void enumUtilTest() {
        Assert.assertSame(TestEnum.T_Enum_0, TestEnum.indexOf(0));
        Assert.assertSame(TestEnum.T_Enum_1, TestEnum.indexOf(1));
    }

    /**
     * 测试index超过预警
     */
    @Test
    public void exceedTest() {
        Assert.assertSame(TestExceed.T_EXCEED_1001, TestExceed.indexOf(1001));
    }

    /**
     * 测试枚举index为负数
     */
    @Test(expected = ExceptionInInitializerError.class)
    public void negativeTest() {
        TestNegative nega = IndexedEnumTest.TestNegative.TEST_NEGATIVE;
    }

    /**
     * 测试index取值为负数
     */
    //@Test(expected = IllegalArgumentException.class)
    public void indexTest() {
       // Assert.assertEquals(isNullException, true);

    }

    /**
     * 测试index超过最大长度
     */
    //@Test(expected = IllegalArgumentException.class)
    public void indexOverTest() {
        // Assert.assertEquals(true, isNullException);
    }


    /**
     * 测试index不唯一
     */
    @Test(expected = ExceptionInInitializerError.class)
    public void doubleTest() {
        TestDouble which = TestDouble.T_DOUBLE_0;
    }

    /**
     * 测试indexof参数为空
     */
    //@Test
    public void indexOfNullATest() {
         // Assert.assertEquals(isNullException, true);
    }

    /**
     * 测试enum元素空
     */
    @Test(expected = ExceptionInInitializerError.class)
    public void indexOfNullBTest() {
        Assert.assertSame(null, TestNull_B.indexOf(0));
    }


}
