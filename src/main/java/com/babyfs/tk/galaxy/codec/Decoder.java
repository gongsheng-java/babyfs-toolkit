
package com.babyfs.tk.galaxy.codec;


import com.babyfs.tk.commons.codec.util.HessianCodecUtil;

import java.io.IOException;
import java.lang.reflect.Type;


public interface Decoder {


    Object decode(byte[] response, Type type) throws IOException;

    public class Default implements Decoder {


        @Override
        public Object decode(byte[] response, Type type) throws IOException {

            return HessianCodecUtil.decode(response);
        }
    }
}
