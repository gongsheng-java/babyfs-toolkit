
package com.babyfs.tk.galaxy.codec;


import com.babyfs.tk.commons.codec.util.HessianCodecUtil;

import java.lang.reflect.Type;


public interface Encoder {


  byte[] encode(Object object, Type bodyType);


  class Default implements Encoder {


    @Override
    public byte[] encode(Object object, Type bodyType) {
      return HessianCodecUtil.encode(object);
    }
  }
}
