package com.spring.springbook.user.service.sqlservice;

// 조회 실패시 예외
public class SqlRetrievalFailureException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public SqlRetrievalFailureException(String message) {
        super(message);
    }

    public SqlRetrievalFailureException(String message, Throwable cause) {
        super(message, cause);
    }

    public SqlRetrievalFailureException(Throwable cause) {
        super(cause);
    }
}