package com.spring.springbook.user.service.sqlservice;

import com.spring.springbook.user.dao.UserDao;
import com.spring.springbook.user.service.jaxb.SqlType;
import com.spring.springbook.user.service.jaxb.Sqlmap;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class XmlSqlService implements SqlService {

    private Map<String, String> sqlMap = new HashMap<String, String>(); // 읽어온 SQL을 저장해둘 Map

    private String sqlmapFile;

    public void setSqlmapFile(String sqlmapFile) {
        this.sqlmapFile = sqlmapFile;
    }

    @PostConstruct // loadSql()메소드를 빈의 초기화 메소드로 지정한다.
    public void loadSql() {
        String contextPath = Sqlmap.class.getPackage().getName();
        try {
            JAXBContext context = JAXBContext.newInstance(contextPath);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            // 프로퍼티로 설정을 통해 제공받은 파일 이름을 사용한다.
            InputStream is = UserDao.class.getResourceAsStream(this.sqlmapFile);
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
