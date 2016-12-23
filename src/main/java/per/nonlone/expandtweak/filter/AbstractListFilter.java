package per.nonlone.expandtweak.filter;

import java.util.List;

import per.nonlone.expandtweak.common.Filter;
import per.nonlone.expandtweak.filter.AbstractBaseFilter.FilterFlag;
import per.nonlone.expandtweak.filter.exception.FilterResultNullException;
import per.nonlone.expandtweak.filter.exception.UnKnownTypeException;

/**
 * 链式过滤器
 * @author leishy@corp.21cn.com
 *
 * @param <T>
 */
public abstract class AbstractListFilter<T> extends AbstractBaseFilter<T> {

	private List<Filter<T>> filterList;
	
	/**
	 *链式处理，由子类接口
	 * @author leishy@corp.21cn.com
	 *
	 * @param <T>
	 */
	protected interface ListFilterProcess<T>{
		FilterResult handle(List<Filter<T>> filterList,T entity);
	}
	
	

	/**
	 * 链式处理完全接口，传入对应链式过滤链，由子类实现对应处理方法
	 * @param lfExam
	 * @param filterPath
	 * @param listFilterProcessStrategy
	 * @return
	 * @throws FilterResultNullException 
	 * @throws UnKnownTypeException 
	 */
	protected List<FilterResult> processFilter(final ListFilterProcess<T> listFilterProcessStrategy) throws FilterResultNullException, UnKnownTypeException{
		//链式过滤器，完全由子类控制
		return super.processFilter(new FilterProcessStrategy<T>() {
			@Override
			public FilterResult handle(T entity) {
				return listFilterProcessStrategy.handle(filterList, entity);
			}
		});
	}
	
	/**
	 * 默认处理，按照链式顺序处理各个过滤器
	 * @param lfExam
	 * @param filterPath
	 * @return
	 * @throws FilterResultNullException 
	 * @throws UnKnownTypeException 
	 */
	protected List<FilterResult> processFilter() throws FilterResultNullException, UnKnownTypeException{
		return processFilter(new ListFilterProcess<T>() {
			
			@Override
			public FilterResult handle(List<Filter<T>> filterList, T entity) {
				boolean fResult = true;
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
					log.warn("op<filter> filterList empty");
				}
				return result;
			}
		});
	}
	

	public void setFilterList(List<Filter<T>> filterList) {
		this.filterList = filterList;
	}
	
	public List<Filter<T>> getFilterList() {
		return filterList;
	}
}
