package com.babyfs.tk.commons.enums;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 从0开始的可索引枚举接口
 * <p/>
 * 实现此接口的枚举各元素的index可以不连续<br/>
 * <b>建议:</b>
 * 此接口实现类的元素保持index的连续以节省空间
 * <p/>
 */
public interface IndexedEnum {
    /**
     * 取得该枚举元素的索引值
     *
     * @return 返回>=0的索引值
     */
    int getIndex();

    /**
     * 支持可索引枚举的工具类
     */
    final class Util {
        private static final Logger LOGGER = LoggerFactory.getLogger(Util.class);

        /**
         * 索引预警上限值，索引超过该上限可能存在空间浪费问题
         */
        private static final int WARNNING_MAX_INDEX = 1000;

        private Util() {

        }

        /**
         * 将枚举个元素放置ArrayList中，各元素在列表中的序列即为原则的index<br/>
         * 如果有不连续的index，则列表中空缺的位置由null填充
         *
         * @param enums 枚举元素数组
         * @param <E>   枚举泛型
         * @return 包含所有数组元素的List
         */
        public static <E extends IndexedEnum> List<E> toIndexes(E[] enums) {
            Preconditions.checkState((enums != null && enums.length > 0), "The Array of Enum cannot be null or empty.");

            int maxIndex = Integer.MIN_VALUE;
            int curIdx = 0;
            // 取得最大index的值，该值+1的结果就是目标容器的size
            for (E enm : enums) {
                curIdx = enm.getIndex();
                Preconditions.checkArgument(curIdx >= 0, "The index of Enum[%s] must be >= 0.", enm);
                if (curIdx > maxIndex) {
                    maxIndex = curIdx;
                }
            }

            if (maxIndex >= WARNNING_MAX_INDEX) {
                LOGGER.warn("The index of Enum[{}] exceed threshold:{}.There is wasting memory probably.",
                        enums.getClass().getComponentType().getName(), WARNNING_MAX_INDEX);
            }

            List<E> instances = new ArrayList<E>(maxIndex + 1);
            // 容器暂用null填充
            for (int i = 0; i < maxIndex + 1; i++) {
                instances.add(null);
            }
            for (E enm : enums) {
                curIdx = enm.getIndex();
                // 索引必须唯一
                Preconditions.checkState(instances.get(curIdx) == null,
                        "The index of Enum[%s] is not unique.", enums.getClass().getComponentType().getName());
                instances.set(curIdx, enm);
            }
            return instances;
        }

        /**
         * 根据指定序列从枚举列表中取得相应的枚举元素
         * <p/>
         * <b>注意：如果index参数小于0或者大于枚举元素最大索引值，返回null</b>
         *
         * @param values 枚举列表
         * @param index  目标序列
         * @param <E>    枚举泛型
         * @return
         */
        public static <E extends IndexedEnum> E valueOf(List<E> values, int index) {
            if (index < 0 || index >= values.size()) {
                return null;
            }
            return values.get(index);
        }

        /**
         * 检查是否有重复的index
         *
         * @param clazz
         * @param <E>
         * @throws IllegalStateException 有重复时抛出这个异常
         * @throws RuntimeException
         */
        public static <E extends IndexedEnum> void checkDuplicateIndexes(Class<E> clazz) {
            Field[] fields = clazz.getFields();
            if (fields == null || fields.length == 0) {
                return;
            }

            final Set<Integer> indexes = Sets.newHashSet();
            for (Field field : fields) {
                field.setAccessible(true);

                final int modifiers = field.getModifiers();
                if (!Modifier.isFinal(modifiers) || !Modifier.isStatic(modifiers)) {
                    continue;
                }

                if (!IndexedEnum.class.isAssignableFrom(field.getType())) {
                    continue;
                }

                try {
                    IndexedEnum in = (IndexedEnum) field.get(clazz);
                    Preconditions.checkState(indexes.add(in.getIndex()), "duplicate index %s in class %s", in.getIndex(), clazz.getName());
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    class Index implements IndexedEnum {
        private final int index;

        public Index(int index) {
            this.index = index;
        }

        @Override
        public int getIndex() {
            return index;
        }
    }
}
