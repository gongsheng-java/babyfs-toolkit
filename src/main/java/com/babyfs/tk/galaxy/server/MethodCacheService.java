package com.babyfs.tk.galaxy.server;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class MethodCacheService implements IMethodCacheService {

    private final Map<String,Method>  map = new HashMap();

    @Override
    public Method getMethodBySign(String sign) {

      if(map.containsKey(sign)){
          return map.get(sign);
      }
      return null;
    }

    public void  initMethodCache(){


    }
}