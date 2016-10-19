package com.netfinworks.site.core.common.convert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
/**
 * 对象转换类
 * 提供单个对象转换,集合对象转换
 * 例子： 创建静态匿名类转换(也可以直接继承 ObjectConvert 这里指提供 静态匿名类转换例子)
 *  
	public static final ObjectConvert<Source, Map<String, Object>> convert = new ObjectConvert<Source, Map<String, Object>>(){
		@Override
		public Map<String, Object> convert(Source source) {
			Map<String, Object> map = new HashMap<String, Object>(); 
			map.put("id", source.getXX());
			return map;
		}
	};
	// 单个对象转换
	Source source = new Source();
	Map<String, Object> map = convert.convert(source);
	System.out.println(map);
	
	// 转换成 List
	List<Source> list = new ArrayList<Source>();
	list.add(source);
	List<Map<String, Object>> targetList = convert.convertToList(list);
	System.out.println(targetList);
	
 * @author tangL
 * @date 2015-02-05
 * 
 * @param <Source> 转换源对象
 * @param <Target> 转换后的对象
 */
public abstract class ObjectConvert<Source, Target> {
	/**
	 * 转换接口 由具体继承的类来实现
	 * @param source 转换源对象
	 * @return target 转换后的对象
	 */
	public abstract Target convert(Source source);
	public List<Target> convertToList(Collection<Source> list) {
		return  (List<Target>) this.toCollections(list, new ArrayList<Target>());
	}
	public List<Target> convertToList(Collection<Source> list, ConvertFilter<Source, Target> filter) {
		return  (List<Target>) this.toCollections(list, new ArrayList<Target>(), filter);
	}
	public Set<Target> convertToSet(Collection<Source> list) {
		return  (Set<Target>) this.toCollections(list, new HashSet<Target>());
	}
	public Set<Target> convertToSet(Collection<Source> list, ConvertFilter<Source, Target> filter) {
		return (Set<Target>) this.toCollections(list, new HashSet<Target>(), filter);
	}
	/**
	 * 从源集合转换成目标集合
	 * @param sourceColl 源集合
	 * @param targetColl 目标集合
	 * @return
	 */
	protected Collection<Target> toCollections(Collection<Source> sourceColl, Collection<Target> targetColl) {
		if (sourceColl != null && sourceColl.size() > 0) {
			for (Source source : sourceColl) {
				Target target = this.convert(source);
				targetColl.add(target);
			}
		}
		return targetColl;
	}
	/**
	 * 从源集合转换成目标集合,增加过滤器
	 * @param sourceColl
	 * @param targetColl
	 * @param filter
	 * @return
	 */
	protected Collection<Target> toCollections(Collection<Source> sourceColl, Collection<Target> targetColl, ConvertFilter<Source, Target> filter) {
		if (sourceColl != null && sourceColl.size() > 0) {
			for (Source source : sourceColl) {
				Target target = this.convert(source);
				if(target != null) {
					filter.filter(source, target);
					targetColl.add(target);
				}
			}
		}
		return targetColl;
	}
	
	/**
	 * 转换过滤器
	 * 
	 * @param <Source>
	 * @param <Target>
	 */
	public static interface ConvertFilter<Source, Target> {
		public void filter(Source source, Target target);
	}
}
