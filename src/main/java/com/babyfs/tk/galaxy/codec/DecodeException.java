
package com.babyfs.tk.galaxy.codec;


import com.babyfs.tk.galaxy.client.Util;

public class DecodeException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  /**
   * @param message the reason for the failure.
   */
  public DecodeException(String message) {
    super(Util.checkNotNull(message, "message"));
  }

  /**
   * @param message possibly null reason for the failure.
   * @param cause   the cause of the error.
   */
  public DecodeException(String message, Throwable cause) {
    super(message, Util.checkNotNull(cause, "cause"));
  }
}
