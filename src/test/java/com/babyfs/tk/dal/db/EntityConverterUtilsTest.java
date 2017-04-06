package com.babyfs.tk.dal.db;

import com.babyfs.tk.orm.AssignIdEntity;
import com.babyfs.tk.orm.Shard;
import com.babyfs.tk.dal.orm.SimpleEntityMeta;
import javassist.CannotCompileException;
import javassist.CtClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;

/**
 */
public class EntityConverterUtilsTest {
    @Entity
    @Table(name = "user")
    public static class User extends AssignIdEntity {
        private String name;
        private int i;
        private Integer iB;
        private short s;
        private Short sB;
        private byte b;
        private Byte bB;
        private boolean bool;
        private Boolean boolB;
        private long height;
        private Long heightL;
        private double d;
        private Double dB;
        private float f;
        private Float fB;
        private Date date;
        private Time time;
        private Timestamp timestamp;
        private byte[] data;

        @Column(name = "heightL")
        public Long getHeightL() {
            return heightL;
        }

        public void setHeightL(Long heightL) {
            this.heightL = heightL;
        }

        @Column(name = "data")
        public byte[] getData() {
            return data;
        }

        public void setData(byte[] data) {
            this.data = data;
        }

        @Column(name = "name")
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Column(name = "height_int")
        public long getHeight() {
            return height;
        }

        public void setHeight(long height) {
            this.height = height;
        }

        @Column(name = "i")
        public int getI() {
            return i;
        }

        public void setI(int i) {
            this.i = i;
        }

        @Column(name = "ib")
        public Integer getIB() {
            return iB;
        }

        public void setIB(Integer iB) {
            this.iB = iB;
        }

        @Column(name = "s")
        public short getS() {
            return s;
        }

        public void setS(short s) {
            this.s = s;
        }

        @Column(name = "sb")
        public Short getSB() {
            return sB;
        }

        public void setSB(Short sB) {
            this.sB = sB;
        }

        @Column(name = "b")
        public byte getB() {
            return b;
        }

        public void setB(byte b) {
            this.b = b;
        }

        @Column(name = "bb")
        public Byte getBB() {
            return bB;
        }

        public void setBB(Byte bB) {
            this.bB = bB;
        }

        @Column(name = "bool")
        public boolean getBool() {
            return bool;
        }

        public void setBool(boolean bool) {
            this.bool = bool;
        }

        @Column(name = "boolB")
        public Boolean getBoolB() {
            return boolB;
        }

        public void setBoolB(Boolean boolB) {
            this.boolB = boolB;
        }

        @Column(name = "d")
        public double getD() {
            return d;
        }

        public void setD(double d) {
            this.d = d;
        }

        @Column(name = "db")
        public Double getDB() {
            return dB;
        }

        public void setDB(Double dB) {
            this.dB = dB;
        }

        @Column(name = "f")
        public float getF() {
            return f;
        }

        public void setF(float f) {
            this.f = f;
        }

        @Column(name = "fb")
        public Float getFB() {
            return fB;
        }

        public void setFB(Float fB) {
            this.fB = fB;
        }

        @Column(name = "date")
        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        @Column(name = "time")
        public Time getTime() {
            return time;
        }

        public void setTime(Time time) {
            this.time = time;
        }

        @Column(name = "timeStamp")
        public Timestamp getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Timestamp timestamp) {
            this.timestamp = timestamp;
        }
    }

    @Entity
    @Table(name = "address")
    public static class Address extends AssignIdEntity {
        private String name;
        private long code;

        @Column(name = "name")
        @Shard(name = "name")
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Column(name = "code")
        public long getCode() {
            return code;
        }

        public void setCode(long code) {
            this.code = code;
        }
    }

    private static File codeGen = new File("target/class_gen");

    @BeforeClass
    public static void setUp() {
        if (!codeGen.exists()) {
            Assert.assertTrue(codeGen.mkdirs());
        }
    }

    @Test
    public void testGenerateClass() throws Exception {
        SimpleEntityMeta<User> userSimpleEntityMeta = new SimpleEntityMeta<User>(User.class);
        CtClass a = EntityHelperGenerator.generateConverterClass(userSimpleEntityMeta,User.class.getName()+"$Helper");
        File out = new File(codeGen, "Test.class");
        DataOutputStream stream = new DataOutputStream(new FileOutputStream(out));
        a.toBytecode(stream);
        stream.close();
        System.out.println("Write to " + out.getAbsolutePath());
        Class aClass = a.toClass();
        System.out.println(aClass);

        Object o = aClass.newInstance();
        System.out.println(o);
        IEntityHelper converterISql = (IEntityHelper) o;
        User user = new User();
        user.setId(20);
        user.setName("dong");
        user.setHeight(3939);
        SqlParameterSource map = converterISql.toSource(user);
        System.out.println(map);
        byte[] ba = new byte[0];
        System.out.println(ba.getClass());
    }

    @Test
    public void testResultSet() throws CannotCompileException, IllegalAccessException, InstantiationException, SQLException, IOException {
        SimpleEntityMeta<Address> addressSimpleEntityMeta = new SimpleEntityMeta<Address>(Address.class);
        CtClass a = EntityHelperGenerator.generateConverterClass(addressSimpleEntityMeta,Address.class.getName()+"$Helper");
        File out = new File(codeGen, "Test2.class");
        DataOutputStream stream = new DataOutputStream(new FileOutputStream(out));
        a.toBytecode(stream);
        stream.close();
        System.out.println("Write to " + out.getAbsolutePath());
        Class aClass = a.toClass();
        IEntityHelper helper = (IEntityHelper) aClass.newInstance();
        ResultSet resultSet = Mockito.mock(ResultSet.class);
        Mockito.when(resultSet.getString("name")).thenReturn("dong");
        Mockito.when(resultSet.getLong("code")).thenReturn(5L);
        Mockito.when(resultSet.getLong("id")).thenReturn(1L);

        Address address = (Address) helper.toEntity(resultSet);
        Assert.assertNotNull(address);
        Assert.assertEquals("dong", address.getName());
        Assert.assertEquals(5, address.getCode());
        Assert.assertEquals(1, address.getId());

        SqlParameterSource map = helper.toSource(address);
        System.out.println(map);
        Assert.assertTrue("dong".equals(map.getValue("name")));
        Assert.assertTrue(map.getValue("code").equals(5L));
        Assert.assertTrue(map.getValue("id").equals(1L));
    }

}
