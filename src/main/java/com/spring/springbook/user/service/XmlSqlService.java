package com.spring.springbook.user.service;

import com.spring.springbook.user.dao.UserDao;
import com.spring.springbook.user.service.jaxb.SqlType;
import com.spring.springbook.user.service.jaxb.Sqlmap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class XmlSqlService implements SqlService {
    private Map<String, String> sqlMap = new HashMap<String, String>(); // 읽어온 SQL을 저장해둘 Map

    public XmlSqlService() { // 스프링이 오브젝트를 만드는 시점에서 SQL을 읽어오도록 생성자를 이용한다.
        String contextPath = Sqlmap.class.getPackage().getName(); // JAXB API를 이용해 XML 문서를 오브젝트 트리로 읽어온다.
        try {
            JAXBContext context = JAXBContext.newInstance(contextPath);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            // UserDao와 같은 클래스패스의 sqlMap.xml파일을 변환한다.
            InputStream is = UserDao.class.getResourceAsStream("sqlmap.xml");
            Sqlmap sqlmap = (Sqlmap)unmarshaller.unmarshal(is);

            for (SqlType sql : sqlmap.getSql()) {
                sqlMap.put(sql.getKey(), sql.getValue());
                // 읽어온 SQL 맵으로 저장해둔다.
            }
        } catch (JAXBException e) {
            throw new RuntimeException(e);
            // JAXBException은 복구 불가능한 예외다. 불필요한 throw를 피하도록 런타임 예외로 포장해서 던진다.
        }
    }

    public String getSql(String key) throws SqlRetrievalFailureException {
        String sql = sqlMap.get(key);
        if (sql == null) {
            throw new SqlRetrievalFailureException(key + "를 이용해서 SQL을 찾을 수 없습니다.");
        } else {
            return sql;
        }

    }
}
