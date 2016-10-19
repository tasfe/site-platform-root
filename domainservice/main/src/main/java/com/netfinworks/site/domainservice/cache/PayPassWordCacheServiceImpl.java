package com.netfinworks.site.domainservice.cache;
import org.springframework.stereotype.Service;

import com.netfinworks.basis.inf.ucs.support.annotation.CacheResult;
import com.netfinworks.basis.inf.ucs.support.annotation.InvalidateCache;
import com.netfinworks.basis.inf.ucs.support.annotation.KeyGenerator;
import com.netfinworks.basis.inf.ucs.support.annotation.UpdateCache;
import com.netfinworks.site.core.common.constants.CommonConstant;
@Service("payPassWordCacheService")
public class PayPassWordCacheServiceImpl implements PayPassWordCacheService {

    @Override
    @CacheResult(namespace = CommonConstant.WALLET_NAMESPACE_CACHE)
    public String put(@KeyGenerator String key, String member) {
        return member;
    }
    
    @Override
    @CacheResult(namespace = CommonConstant.WALLET_NAMESPACE_CACHE)
    public String load(@KeyGenerator String key) {
        return null;
    }


    @Override
    @InvalidateCache(namespace = CommonConstant.WALLET_NAMESPACE_CACHE)
    public void clear(@KeyGenerator String key) {
    	
    }

	@Override
	@UpdateCache(namespace = CommonConstant.WALLET_NAMESPACE_CACHE,expireSecond=24*60*60)
	public String saveOrUpdate(@KeyGenerator String key, String member) {
		 return member;
		
	}
	
}
