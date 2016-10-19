package com.netfinworks.site.core.dal.util;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>仓储抽象基类</p>
 * @author eric
 * @version $Id: BaseRepository.java, v 0.1 2013-7-17 上午10:37:16  Exp $
 */
public abstract class BaseRepository {
    @Resource(name = "sqlSession")
    protected SqlSessionTemplate  sqlSession;

    /**
     * 获取下个序列�?
     * @param name
     * @return
     */
    @Transactional
    protected long getNextValue(String name) {
        Long currentValue = sqlSession.selectOne("Common.selectSequence", name);
        long nextValue = currentValue + 1;
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("name", name);
        param.put("nextValue", nextValue);
        sqlSession.update("Common.updateSequence", param);

        return nextValue;
    }
}
