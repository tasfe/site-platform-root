package com.netfinworks.site.test.util;

import javax.annotation.Resource;

import org.junit.Test;
import org.mybatis.spring.SqlSessionTemplate;

import com.netfinworks.site.test.common.BaseTestCase;

/**
 * <p>DAO调用测试</p>
 * @author eric
 * @version $Id: DaoTest.java, v 0.1 2013-7-12 下午4:48:37  Exp $
 */
public class DaoTest extends BaseTestCase {
    @Resource(name = "sqlSession")
    public SqlSessionTemplate sqlSession;

    /**
     * 可用测试
     */
    @Test
    public void useable() {
        
    }
}
