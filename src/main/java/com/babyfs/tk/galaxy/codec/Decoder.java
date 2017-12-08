
package com.babyfs.tk.galaxy.codec;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collections;


public interface Decoder {


  Object decode(String response, Type type) throws IOException;

  public class Default implements Decoder {

    private final ObjectMapper mapper;

    public Default() {
      this(Collections.<Module>emptyList());
    }

    public Default(Iterable<Module> modules) {
      this(new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
              .registerModules(modules));
    }

    public Default(ObjectMapper mapper) {
      this.mapper = mapper;
    }

    @Override
    public Object decode(String response, Type type) throws IOException {

      try {

        if(Strings.isNullOrEmpty(response)){
          return null;
        }
        Object obj = mapper.readValue(response, mapper.constructType(type));
        return obj;
      }catch (JsonProcessingException e){
        throw new EncodeException(e.getMessage(), e);
      }
    }
  }
}
