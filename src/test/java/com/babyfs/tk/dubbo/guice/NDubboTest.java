package com.babyfs.tk.dubbo.guice;

import com.babyfs.noteservice.api.NoteService;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.Test;
import org.springframework.util.Assert;


public class NDubboTest {

    @Test
    public void testDubbo(){
        DubboClientModule test = new DubboClientModule("test", "babyfs-dubbo.xml");
        Injector injector = Guice.createInjector(test);
//        NoteService instance = injector.getInstance(NoteService.class);
//        Assert.notNull(instance);
    }
}
