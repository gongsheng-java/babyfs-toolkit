package com.babyfs.tk.dal.db;

import org.springframework.dao.DataAccessException;

/**
 */
public class DALException extends DataAccessException {
    public DALException(String msg) {
        super(msg);
    }

    public DALException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
