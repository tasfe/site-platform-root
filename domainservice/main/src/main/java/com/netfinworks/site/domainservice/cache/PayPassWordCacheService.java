/**
 * 
 */
package com.netfinworks.site.domainservice.cache;

/**
 * <p>注释</p>
 * @author Guan Xiaoxu
 * @version $Id: PayPassWordCacheService.java, v 0.1 2013-11-29 下午5:29:33 Guanxiaoxu Exp $
 */
public interface PayPassWordCacheService {
    /**
     * 钱包缓存
     * @param key
     * @param member
     * @return
     */
    public String put(String key,String member);
    public String load(String key);

    /**
     * 缓存销毁
     * @param key
     */
    public void clear(String key);
    
    
    public String saveOrUpdate(String key,String member);
   
   

}
