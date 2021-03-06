package com.spring.springbook.learningtest.jdk.jaxb;

import com.spring.springbook.user.service.jaxb.SqlType;
import com.spring.springbook.user.service.jaxb.Sqlmap;
import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class JaxbTest {
    @Test
    public void readSqlmap() throws JAXBException, IOException {
        String contextPath = Sqlmap.class.getPackage().getName();
        JAXBContext context = JAXBContext.newInstance(contextPath);
        // contextPath = 바인딩용 클래스들 위치를 가지고 JAXB컨텍스트를 만든다

        Unmarshaller unmarshaller = context.createUnmarshaller(); // 언마샬러 생성
        System.out.println(getClass().getResourceAsStream("sqlmap.xml"));
        Sqlmap sqlmap = (Sqlmap) unmarshaller.unmarshal(
            // 언마샬을 하면 매핑된 오브젝트 트리의 루트인 Sqlmap을 돌려준다.
            getClass().getResourceAsStream("sqlmap.xml"));
            // 테스트 클래스와 같은 폴더에 있는 XML 파일을 사용한다.

            List<SqlType> sqlList = sqlmap.getSql();

            assertThat(sqlList.size(), is(3));
            assertThat(sqlList.get(0).getKey(), is("add"));
            assertThat(sqlList.get(0).getValue(), is("insert"));
            assertThat(sqlList.get(1).getKey(), is("get"));
            assertThat(sqlList.get(1).getValue(), is("select"));
            assertThat(sqlList.get(2).getKey(), is("delete"));
            assertThat(sqlList.get(2).getValue(), is("delete"));
            // List에 담겨있는 Sql 오브젝트를 가져와 XML문서와 같은 정보를 갖고 있는지 확인~~~
    }
}
