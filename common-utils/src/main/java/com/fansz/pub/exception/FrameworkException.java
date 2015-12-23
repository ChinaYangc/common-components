package com.fansz.pub.exception;

/**
 * Created by allan on 15/12/16.
 */
public class FrameworkException extends RuntimeException {
    private static final long serialVersionUID = -3662399480857267255L;

    public FrameworkException(Exception e) {
        super(e);
    }

    public FrameworkException(String message) {
        super(message);
    }
}
