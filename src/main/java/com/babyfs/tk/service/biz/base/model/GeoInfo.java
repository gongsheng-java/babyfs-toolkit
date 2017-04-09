package com.babyfs.tk.service.biz.base.model;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * 地理位置
 */
public class GeoInfo {
    /**
     * 具体地址
     */
    private String address;
    /**
     * 经纬度
     */
    private LonLat lonLat;
    /**
     * 四级地址库信息
     */
    private Lvl4Addr lvl4Addr;

    @JSONField(name = "address")
    public String getAddress() {
        return address;
    }

    @JSONField(name = "address")
    public void setAddress(String addr) {
        this.address = addr;
    }

    @JSONField(name = "lonlat")
    public LonLat getLonLat() {
        return lonLat;
    }

    @JSONField(name = "lonlat")
    public void setLonLat(LonLat lonLat) {
        this.lonLat = lonLat;
    }

    @JSONField(name = "lvl4_addr")
    public Lvl4Addr getLvl4Addr() {
        return lvl4Addr;
    }

    @JSONField(name = "lvl4_addr")
    public void setLvl4Addr(Lvl4Addr lvl4Addr) {
        this.lvl4Addr = lvl4Addr;
    }

    /**
     * 四级地址库
     */
    public static class Lvl4Addr {
        /**
         * 零级地址:国家
         */
        private Partition country;
        /**
         * 一级地址:省份、直辖市
         */
        private Partition province;
        /**
         * 二级地址:城市
         */
        private Partition city;
        /**
         * 三级地址:区县
         */
        private Partition district;
        /**
         * 四级地址:镇区(县以下的行政区)
         */
        private Partition township;

        @JSONField(name = "country")
        public Partition getCountry() {
            return country;
        }

        @JSONField(name = "country")
        public void setCountry(Partition country) {
            this.country = country;
        }

        @JSONField(name = "province")
        public Partition getProvince() {
            return province;
        }

        @JSONField(name = "province")
        public void setProvince(Partition province) {
            this.province = province;
        }

        @JSONField(name = "city")
        public Partition getCity() {
            return city;
        }

        @JSONField(name = "city")
        public void setCity(Partition city) {
            this.city = city;
        }

        @JSONField(name = "district")
        public Partition getDistrict() {
            return district;
        }

        @JSONField(name = "district")
        public void setDistrict(Partition district) {
            this.district = district;
        }

        @JSONField(name = "township")
        public Partition getTownship() {
            return township;
        }

        @JSONField(name = "township")
        public void setTownship(Partition township) {
            this.township = township;
        }
    }

    /**
     * 行政区域的划分
     */
    public static class Partition {
        /**
         * 名字:如"北京市通州区"
         */
        private String name;
        /**
         * 编码:如"110223"
         */
        private String code;

        public Partition() {

        }

        public Partition(String name, String code) {
            this.name = name;
            this.code = code;
        }

        @JSONField(name = "name")
        public String getName() {
            return name;
        }

        @JSONField(name = "name")
        public void setName(String name) {
            this.name = name;
        }

        @JSONField(name = "code")
        public String getCode() {
            return code;
        }

        @JSONField(name = "code")
        public void setCode(String code) {
            this.code = code;
        }
    }

    /**
     * 经纬度坐标
     */
    public static class LonLat {
        /**
         * 经度
         */
        private String lon;
        /**
         * 纬度
         */
        private String lat;

        public LonLat() {
        }

        public LonLat(String lon, String lat) {
            this.lon = lon;
            this.lat = lat;
        }

        @JSONField(name = "lon")
        public String getLon() {
            return lon;
        }

        @JSONField(name = "lon")
        public void setLon(String lon) {
            this.lon = lon;
        }

        @JSONField(name = "lat")
        public String getLat() {
            return lat;
        }

        @JSONField(name = "lat")
        public void setLat(String lat) {
            this.lat = lat;
        }
    }

}
