
package com.babyfs.tk.galaxy.codec;


import com.babyfs.tk.galaxy.client.Util;

public class EncodeException  extends RuntimeException {

  private static final long serialVersionUID = 1L;


  public EncodeException(String message) {
    super(Util.checkNotNull(message, "message"));
  }


  public EncodeException(String message, Throwable cause) {
    super(message, Util.checkNotNull(cause, "cause"));
  }
}
