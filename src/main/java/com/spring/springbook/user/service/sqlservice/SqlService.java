package com.spring.springbook.user.service.sqlservice;

public interface SqlService{
    String getSql(String key) throws SqlRetrievalFailureException;
}
