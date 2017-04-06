package com.babyfs.tk.commons.guice;

import com.google.common.io.Closeables;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.grapher.graphviz.GraphvizGrapher;
import com.google.inject.grapher.graphviz.GraphvizModule;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

/**
 */
public final class GuiceGrapher {
    private GuiceGrapher() {

    }

    /**
     * 生成指定Injector的中对象关系图
     *
     * @param filename
     * @param injector
     * @throws IOException
     */
    public static void graph(@Nonnull String filename, @Nonnull Injector injector) throws IOException {
        PrintWriter out = null;
        try {
            out = new PrintWriter(new File(filename), "UTF-8");
            Injector graphInjector = Guice.createInjector(new GraphvizModule());
            GraphvizGrapher grapher = graphInjector.getInstance(GraphvizGrapher.class);
            grapher.setOut(out);
            grapher.setRankdir("TB");
            grapher.graph(injector);
        } finally {
            Closeables.close(out, true);
        }
    }
}
