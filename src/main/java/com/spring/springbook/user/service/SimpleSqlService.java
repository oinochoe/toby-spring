package com.spring.springbook.user.service;

import java.util.Map;

public class SimpleSqlService implements SqlService {
    private Map<String, String> sqlMap;

    public void setSqlMap(Map<String, String> sqlMap) {
        this.sqlMap = sqlMap;
        // 설정 파일에 <Map> 으로 정의된 SQL 정보를 가져오도록 프로퍼티로 등록
    }

    public String getSql(String key) throws SqlRetrievalFailureException {
        String sql = sqlMap.get(key); // 내부 sqlMap에서 SQL을 가져온다.
        if (sql == null) {
            throw new SqlRetrievalFailureException(key + "에 대한 SQL을 찾을 수 없습니다.");
            // 인터페이스에 정의된 규약대로 SQL을 가져오는 데 실패하면 예외
        } else {
            return sql;
        }
    }
}
