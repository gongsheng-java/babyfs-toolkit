
package com.babyfs.tk.galaxy.codec;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.lang.reflect.Type;
import java.util.Collections;


public interface Encoder {


  Object encode(Object object, Type bodyType);


  class Default implements Encoder {


    private final ObjectMapper mapper;

    public Default() {
      this(Collections.<Module>emptyList());
    }

    public Default(Iterable<Module> modules) {
      this(new ObjectMapper()
              .setSerializationInclusion(JsonInclude.Include.NON_NULL)
              .configure(SerializationFeature.INDENT_OUTPUT, true)
              .registerModules(modules));
    }

    public Default(ObjectMapper mapper) {
      this.mapper = mapper;
    }

    @Override
    public Object encode(Object object, Type bodyType) {
      try {
        JavaType javaType = mapper.getTypeFactory().constructType(bodyType);
        return mapper.writerFor(javaType).writeValueAsString(object);
      } catch (JsonProcessingException e) {
        throw new EncodeException(e.getMessage(), e);
      }
    }
  }
}
