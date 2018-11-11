package com.spring.springbook.user.service.sqlservice;

import java.util.Map;

public interface SqlService{
    String getSql(String key) throws SqlRetrievalFailureException;
    // 런타입 예외이므로 특별히 복구해야할 필요가 없다면 무시해도 된다.
    // 복구가 거의 불가능해서 런타임으로 ..

    // 조회 실패시 예외
    public class SqlRetrievalFailureException extends RuntimeException {
        public SqlRetrievalFailureException(String message) {
            super(message);
        }

        public SqlRetrievalFailureException(String message, Throwable cause) {
            super(message, cause);
            // Throwable = > SQL을 가져오는 데 실패한 근본 원인을 담을 수 있도록 중첩예외를 저장할 수 있는 생성자를 만들어둔다.
        }
    }
}
