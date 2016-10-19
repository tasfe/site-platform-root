package com.netfinworks.site.test.common;

import java.util.HashMap;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * <p>测试基类</p>
 * @author eric
 * @version $Id: BaseTestCase.java, v 0.1 2013-7-12 下午1:48:16  Exp $
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/META-INF/spring/common-test.xml",
                                   "/META-INF/spring/persistence.xml" })
public class BaseTestCase extends AbstractJUnit4SpringContextTests {
    protected static final Logger logger = LoggerFactory.getLogger(BaseTestCase.class);

    /** JDBC模板 */
    protected JdbcTemplate        jdbcTemplate;

    @Resource(name = "dataSource")
    public void setDataSource(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }
    
    
    public static void main(String[] args) throws Exception{
        HashMap<String, String> map = new HashMap<String, String>();
        
    }
}
