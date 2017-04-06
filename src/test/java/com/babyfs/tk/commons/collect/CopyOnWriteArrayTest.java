package com.babyfs.tk.commons.collect;

import org.junit.Assert;
import org.junit.Test;

/**
 */
public class CopyOnWriteArrayTest {
    @Test
    public void testCopnOnWriteArray() {
        CopyOnWriteArray array = new CopyOnWriteArray();
        for (int i = 0; i < 10; i++) {
            array.add(i);
        }
        Object[] array1 = array.getArray();
        Assert.assertEquals(10, array1.length);
        for (int i = 0; i < 10; i++) {
            Assert.assertEquals(i, array1[i]);
        }
        int[] toDel = new int[]{1, 3, 8, 5};
        for (int i = 0; i < toDel.length; i++) {
            array.remove(toDel[i]);
        }
        array1 = array.getArray();
        Assert.assertEquals(6, array1.length);
        Assert.assertArrayEquals(new Object[]{0, 2, 4, 6, 7, 9}, array1);
        for (int i = 0; i < toDel.length; i++) {
            array.remove(toDel[i]);
        }
        array1 = array.getArray();
        Assert.assertEquals(6, array1.length);
        Assert.assertArrayEquals(new Object[]{0, 2, 4, 6, 7, 9}, array1);
    }
}
