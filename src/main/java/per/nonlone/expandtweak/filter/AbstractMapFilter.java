package per.nonlone.expandtweak.filter;

import java.util.List;
import java.util.Map;

import per.nonlone.expandtweak.common.Filter;
import per.nonlone.expandtweak.filter.exception.FilterResultNullException;
import per.nonlone.expandtweak.filter.exception.UnKnownTypeException;
/**
 * Map式过滤器，按照Map匹配T（logType）类型进行过滤
 * @author leishy@corp.21cn.com
 *
 * @param <T>
 */
public abstract class AbstractMapFilter<T,E> extends AbstractListFilter<T>{

	
	private Map<T,List<Filter<T>>> filterMap;
	
	
	/**
	 * Map式过滤器，有子类实现接口
	 * @author leishy@corp.21cn.com
	 *
	 * @param <T>
	 */
	protected interface MapFilterProcess<T>{
		FilterResult handle(Map<T,List<Filter<T>>> filterMap,T entity);
	}
	
	/**
	 * Map式处理完全接口，传入对应Map式过滤链，由子类实现对应处理方法
	 * @param lfExam
	 * @param filterPath
	 * @param mapFilterProcess
	 * @return
	 * @throws FilterResultNullException 
	 * @throws UnKnownTypeException 
	 */
	protected List<FilterResult> processFilter(final MapFilterProcess<T> mapFilterProcess) 
			throws FilterResultNullException, UnKnownTypeException{
		//链式过滤器，完全由子类控制
		return processFilter(new FilterProcessStrategy<T>() {
			@Override
			public FilterResult handle(T entity) {
				return mapFilterProcess.handle(filterMap,entity);
			}
		});
	}
	
	/**
	 * 重写链式处理方法，传入链式处理接口为匹配Map之后的控制接口
	 * @throws UnKnownTypeException 
	 */
	@Override
	protected List<FilterResult> processFilter(final ListFilterProcess<T> listFilterProces) 
			throws FilterResultNullException, UnKnownTypeException{
		//链式过滤器，完全由子类控制
		return processFilter(new FilterProcessStrategy<T>() {
			@Override
			public FilterResult handle(T entity) throws UnKnownTypeException {
				FilterResult result = new FilterResult(FilterFlag.NOTMATCH);
				if(filterMap==null||!filterMap.isEmpty()){
					E type = getType(entity);
					if(null!=type){
						List<Filter<T>> filterList = filterMap.get(type);
						log.warn(String.format("op<processFilter> filterMap key<%s> filterList size<%d>",type.toString(),filterList.size()));
						if(filterList!=null&&!filterList.isEmpty()){
							return listFilterProces.handle(filterList,entity);
						}
					}else{
						throw new UnKnownTypeException(entity.toString());
					}
				}else{
					log.warn("op<processFilter> filterMap is empty");
				}
				return result;
			}
		});
	}
	
	/**
	 * 重写默认处理，匹配对应Map之后按顺序处理链式过滤器
	 * @throws UnKnownTypeException 
	 */
	@Override
	protected List<FilterResult> processFilter() throws FilterResultNullException, UnKnownTypeException{
		return processFilter(new ListFilterProcess<T>() {
			/**
			 * 默认Map处理，映射后按照List顺序处理
			 */
			@Override
			public FilterResult handle(List<Filter<T>> filterList, T entity) {
				boolean fResult  = true;
				FilterResult result = new FilterResult(filterList.size());
				result.setResult(FilterFlag.NOTMATCH);
				if(filterList!=null&&!filterList.isEmpty()){
					for(Filter<T> filter:filterList){
						String filterName = filter.getName();
		            	log.info(String.format("op<filter> entity<%s> filterName<%s>  begin",entity.toString(),filterName));
		            	fResult &= filter.filter(entity);
		            	log.info(String.format("op<filter> entity<%s> filterName<%s>  result<> finish",entity.toString(),filterName,fResult));
		            	Object filterResult  = filter.getFilterResult();
		            	result.getFilterResultData().put(filterName, filterResult);
		            	if(!fResult) 
		            		result.setFilterErrorMessage(result.getFilterErrorMessage()+filter.getName()+",");
		            }
					if(fResult){
						result.setResult(FilterFlag.SUCCUESS);
					}else{
						result.setResult(FilterFlag.FAIL);
					}
				}else{
					result.setResult(FilterFlag.NOTMATCH);
					log.warn("op<logfilter> filterList of filterMap is empty");
				}
				return result;
			}
		});
	}

	public Map<T, List<Filter<T>>> getFilterMap() {
		return filterMap;
	}

	public void setFilterMap(Map<T, List<Filter<T>>> filterMap) {
		this.filterMap = filterMap;
	}
	
	public abstract E getType(T entity);
}
