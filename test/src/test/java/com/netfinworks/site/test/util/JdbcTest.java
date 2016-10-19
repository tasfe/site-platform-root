package com.netfinworks.site.test.util;

import org.junit.Assert;
import org.junit.Test;

import com.netfinworks.site.test.common.BaseTestCase;

/**
 * <p>jdbc连接测试</p>
 * @author eric
 * @version $Id: JdbcTest.java, v 0.1 2013-7-12 下午1:54:26  Exp $
 */
public class JdbcTest extends BaseTestCase {

    /**
     * 测试可用
     */
    @Test
    public void useable() {
        Integer count = jdbcTemplate.queryForObject("select LAST_INSERT_ID() from seq_member_id", Integer.class);
        Assert.assertTrue(count >= 0);
    }
}
