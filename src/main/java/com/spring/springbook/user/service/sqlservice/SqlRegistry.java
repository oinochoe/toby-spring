package com.spring.springbook.user.service.sqlservice;

public interface SqlRegistry {
    // SqlReader는 읽어들인 SQL을 이 메소드를 이용해서 레지스트리에 저장.
    // Sql을 키와함께 등록
    void registerSql(String key, String sql);
    String findSql(String key) throws SqlNotFoundException;
    // 키로 sql을 검색이 실패하면 예외

}
