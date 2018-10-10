package com.babyfs.tk.dubbo.guice;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.Test;


public class NDubboTest {

    @Test
    public void testDubbo(){
        DubboClientModule test = new DubboClientModule("test", "babyfs-dubbo.xml");
        Injector injector = Guice.createInjector(test);
//        NoteService instance = injector.getInstance(NoteService.class);
//        Assert.notNull(instance);
    }
}
