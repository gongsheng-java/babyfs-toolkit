package com.babyfs.tk.probe.metrics;

import com.google.common.base.Strings;

/**
 * Metrics的格式
 */
public enum MetricsFormat {
    /**
     * 普通的
     */
    NORMAL(".", "=") {
        public String formatName(String name, String itemName) {
            if (Strings.isNullOrEmpty(itemName)) {
                return name;
            }
            return name + this.nameSep + itemName;
        }
    },
    /**
     * 普罗米修斯
     */
    PROMETHEUS("_", " ") {
        public String formatName(String name, String itemName) {
            if (Strings.isNullOrEmpty(itemName)) {
                return name.replace(".", "_");
            }
            return (name + this.nameSep + itemName).replace(".", "_").replace("-","_");
        }

    };

    final String nameSep;
    final String valSep;

    MetricsFormat(String nameSep, String valSep) {
        this.nameSep = nameSep;
        this.valSep = valSep;
    }

    public abstract String formatName(String name, String itemName);

    public String formatIntValue(String name, long val) {
        return String.format("%s%s%d", name, valSep, val);
    }

    public String formatFloatValue(String name, double val) {
        return String.format("%s%s%f", name, valSep, val);
    }

    public String formatObjectValue(String name, Object val) {
        if (val instanceof Integer || val instanceof Long) {
            return formatIntValue(name, ((Number) val).longValue());
        } else if (val instanceof Float || val instanceof Double) {
            return formatFloatValue(name, ((Number) val).doubleValue());
        }
        return String.format("%s%s%s", name, valSep, val.toString());
    }
}
