package com.babyfs.tk.dal.meta;

import com.babyfs.tk.dal.db.model.Friend;
import com.google.common.base.Joiner;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
public class SimpleEntityMetaTest {
    @Test
    public void paritalUpdateColumns() throws Exception {
        SimpleEntityMeta<Friend> meta = new SimpleEntityMeta<>(Friend.class);
        List<EntityField> update = meta.getUpdate();
        List<String> updateColumns = update.stream().map(meta::toUpdateColumn).collect(Collectors.toList());
        String updateColumnsSql = Joiner.on(",").join(updateColumns);
        System.out.println(updateColumnsSql);

        String partialColumnsSql = meta.paritalUpdateColumns(null, null);
        Assert.assertEquals(updateColumnsSql, partialColumnsSql);
        partialColumnsSql = meta.paritalUpdateColumns(new String[]{"height"}, null);
        System.out.println(partialColumnsSql);

        partialColumnsSql = meta.paritalUpdateColumns(new String[]{"weight","height","name"}, new String[]{"height"});
        System.out.println(partialColumnsSql);

        partialColumnsSql = meta.paritalUpdateColumns(new String[]{}, new String[]{"height","name"});
        System.out.println(partialColumnsSql);
    }

}