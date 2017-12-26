
package com.babyfs.tk.galaxy.client;

import java.io.*;
import java.lang.reflect.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.util.*;

import static java.lang.String.format;

/**
 * 工具类
 */
public class Util {


    public static final Charset UTF_8 = Charset.forName("UTF-8");

    private static final int BUF_SIZE = 0x800;


    private Util() {
    }

    public static void checkArgument(boolean expression,
                                     String errorMessageTemplate,
                                     Object... errorMessageArgs) {
        if (!expression) {
            throw new IllegalArgumentException(
                    format(errorMessageTemplate, errorMessageArgs));
        }
    }


    public static <T> T checkNotNull(T reference,
                                     String errorMessageTemplate,
                                     Object... errorMessageArgs) {
        if (reference == null) {
            throw new NullPointerException(
                    format(errorMessageTemplate, errorMessageArgs));
        }
        return reference;
    }

    public static void checkState(boolean expression,
                                  String errorMessageTemplate,
                                  Object... errorMessageArgs) {
        if (!expression) {
            throw new IllegalStateException(
                    format(errorMessageTemplate, errorMessageArgs));
        }
    }


    public static boolean isDefault(Method method) {

        final int SYNTHETIC = 0x00001000;
        return ((method.getModifiers() & (Modifier.ABSTRACT | Modifier.PUBLIC | Modifier.STATIC | SYNTHETIC)) ==
                Modifier.PUBLIC) && method.getDeclaringClass().isInterface();
    }


    public static String emptyToNull(String string) {
        return string == null || string.isEmpty() ? null : string;
    }


    public static void ensureClosed(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ignored) {
            }
        }
    }


    private static final Map<Class<?>, Object> EMPTIES;

    static {
        Map<Class<?>, Object> empties = new LinkedHashMap<Class<?>, Object>();
        empties.put(boolean.class, false);
        empties.put(Boolean.class, false);
        empties.put(byte[].class, new byte[0]);
        empties.put(Collection.class, Collections.emptyList());
        empties.put(Iterator.class, new Iterator<Object>() { // Collections.emptyIterator is a 1.7 api
            public boolean hasNext() {
                return false;
            }

            public Object next() {
                throw new NoSuchElementException();
            }

            public void remove() {
                throw new IllegalStateException();
            }
        });
        empties.put(List.class, Collections.emptyList());
        empties.put(Map.class, Collections.emptyMap());
        empties.put(Set.class, Collections.emptySet());
        EMPTIES = Collections.unmodifiableMap(empties);
    }


    public static String toString(Reader reader) throws IOException {
        if (reader == null) {
            return null;
        }
        try {
            StringBuilder to = new StringBuilder();
            CharBuffer buf = CharBuffer.allocate(BUF_SIZE);
            while (reader.read(buf) != -1) {
                buf.flip();
                to.append(buf);
                buf.clear();
            }
            return to.toString();
        } finally {
            ensureClosed(reader);
        }
    }

    public static byte[] toByteArray(InputStream in) throws IOException {
        checkNotNull(in, "in");
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            copy(in, out);
            return out.toByteArray();
        } finally {
            ensureClosed(in);
        }
    }


    private static long copy(InputStream from, OutputStream to)
            throws IOException {
        checkNotNull(from, "from");
        checkNotNull(to, "to");
        byte[] buf = new byte[BUF_SIZE];
        long total = 0;
        while (true) {
            int r = from.read(buf);
            if (r == -1) {
                break;
            }
            to.write(buf, 0, r);
            total += r;
        }
        return total;
    }
}
