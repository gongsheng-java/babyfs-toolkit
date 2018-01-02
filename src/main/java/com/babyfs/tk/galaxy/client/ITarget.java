
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
    String name();

    String url();

    /**
     * ITarget接口的默认实现
     */
    class HardCodedTarget<T> implements ITarget<T> {

        private final Class<T> type;
        private final String name;
        private final String url;

        public HardCodedTarget(Class<T> type, String appName) {
            this(type, appName, null);
        }

        public HardCodedTarget(Class<T> type, String name, String url) {
            this.type = checkNotNull(type, "type");
            this.name = checkNotNull(emptyToNull(name), "name");
            this.url = url;
        }

        @Override
        public Class<T> type() {
            return type;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public String url() {
            return url;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof HardCodedTarget) {
                HardCodedTarget<?> other = (HardCodedTarget) obj;
                return type.equals(other.type)
                        && name.equals(other.name)
                        && url.equals(other.url);
            }
            return false;
        }

        @Override
        public int hashCode() {
            int result = 17;
            result = 31 * result + type.hashCode();
            result = 31 * result + name.hashCode();
            result = 31 * result + url.hashCode();
            return result;
        }

        @Override
        public String toString() {
            if (name.equals(url)) {
                return "HardCodedTarget(type=" + type.getSimpleName() + ", url=" + url + ")";
            }
            return "HardCodedTarget(type=" + type.getSimpleName() + ", name=" + name + ", url=" + url
                    + ")";
        }
    }

}
