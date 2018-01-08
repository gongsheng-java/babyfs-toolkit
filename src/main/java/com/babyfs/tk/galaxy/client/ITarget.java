
package com.babyfs.tk.galaxy.client;


import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.emptyToNull;

/**
 * rpc代理对象接口
 * HardCodedTarget为ITarget的默认实现
 */
public interface ITarget<T> {

    /**
     * 获取被代理对象的类型
     *
     * @return
     */
    Class<T> type();

    /**
     * 获取调用服务的名称
     *
     * @return
     */
    String appName();

    /**
     * ITarget接口的默认实现
     */
    class HardCodedTarget<T> implements ITarget<T> {

        private final Class<T> type;
        private final String appName;


        public HardCodedTarget(Class<T> type, String appName) {
            this.type = checkNotNull(type, "type");
            this.appName = checkNotNull(emptyToNull(appName), "appName");
        }

        @Override
        public Class<T> type() {
            return type;
        }

        @Override
        public String appName() {
            return appName;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof HardCodedTarget) {
                HardCodedTarget<?> other = (HardCodedTarget) obj;
                return type.equals(other.type)
                        && appName.equals(other.appName);
            }
            return false;
        }

        @Override
        public int hashCode() {
            int result = 17;
            result = 31 * result + type.hashCode();
            result = 31 * result + appName.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "HardCodedTarget(type=" + type.getSimpleName() + ", appName=" + appName + ")";
        }
    }

}
