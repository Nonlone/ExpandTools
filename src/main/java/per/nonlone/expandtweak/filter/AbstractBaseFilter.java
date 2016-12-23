package per.nonlone.expandtweak.filter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import per.nonlone.expandtweak.filter.exception.FilterResultNullException;
import per.nonlone.expandtweak.filter.exception.UnKnownTypeException;

/**
 * 泛型处理过滤结果，处理过滤基本流程
 * 
 * @author leishy@corp.21cn.com
 *
 * @param <T>
 */
public abstract class AbstractBaseFilter<E> {

	protected final Logger log = Logger.getLogger(getClass());

	/**
	 * 过滤处理状态
	 * 
	 * @author leishy@corp.21cn.com
	 *
	 */
	protected enum FilterFlag {
		SUCCUESS, // 过滤成功
		FAIL, // 过滤失败
		ASYNCCONTROL, // 异步控制
		NOTMATCH// 不匹配
	}

	/**
	 * 过滤结果
	 * 
	 * @author leishy@corp.21cn.com
	 *
	 */
	protected static class FilterResult {

		public FilterResult() {
		};

		public FilterResult(FilterFlag result) {
			this.result = result;
		}
		
		public FilterResult(Integer mapSize){
			this.filterResultData = new HashMap<String,Object>(mapSize);
		}

		private FilterFlag result;
		private String filterErrorMessage;
		private Map<String, Object> filterResultData = new HashMap<String,Object>();

		public FilterFlag getResult() {
			return result;
		}

		public void setResult(FilterFlag result) {
			this.result = result;
		}

		public String getFilterErrorMessage() {
			return filterErrorMessage;
		}

		public void setFilterErrorMessage(String filterErrorMessage) {
			this.filterErrorMessage = filterErrorMessage;
		}

		public Map<String, Object> getFilterResultData() {
			return filterResultData;
		}

		public void setFilterResultData(Map<String, Object> filterResultData) {
			this.filterResultData = filterResultData;
		}
	}

	protected interface FilterProcessStrategy<E>  {
		FilterResult handle(E entity) throws UnKnownTypeException;
	}

	/**
	 * 
	 * 过滤日志处理器
	 * 
	 * @param lfExam
	 *            日志提取条件
	 * @param filterPath
	 *            过滤输出路径
	 * @param filterProcessStrategy
	 *            处理策略
	 * @return 过滤处理行数
	 * @throws FilterResultNullException
	 */
	protected List<FilterResult> processFilter(FilterProcessStrategy<E> filterProcessStrategy) 
			throws UnKnownTypeException,FilterResultNullException {
		List<E> entityList = getEntityList();
		List<FilterResult> resultList = new ArrayList<FilterResult>(entityList.size());
		if (null != entityList && !entityList.isEmpty()) {
			for (E entity : entityList) {
				log.info(String.format("op<processFilter> entity<%s>", entity.toString()));
				// 日志结果处理
				if ( null != entity && judgeEntity(entity)) {
					// 过滤数据处理
					FilterResult filterResult = filterProcessStrategy.handle(entity);
					resultList.add(filterResult);
					if (filterResult != null) {
						log.info(String.format("op<processFilter> type<%s> entity<%s> filterResult<%s>",entity,filterResult.getResult().toString()));
						switch (filterResult.getResult()) {
						case SUCCUESS:
							successFilterResult(entity, filterResult);
							break;
						case FAIL:
							failFilterResult(entity, filterResult);
							log.warn(String.format("op<processFilter> type<%s> entity<%s> filterErrorMessage<%s> ", entity.toString(), filterResult.getFilterErrorMessage()));
							break;
						case NOTMATCH:
							// 不匹配，恢复状态
							notMatchFilterResult( entity, filterResult);
							break;
						case ASYNCCONTROL:
							// 异步控制
							asyncControlFilterResult(entity, filterResult);
							break;
						}
					} else {
						// 返回结果为空，抛出异常
						throw new FilterResultNullException(entity.toString());
					}
				} else {
					log.warn(String.format("op<processFilter> validate fail type<%s> entity<%s>", entity));
					validateFail(entity);
				}
				afterFilter(entity);
				log.info(String.format("op<processFilter> finish type<%s> entity<%s>", entity));
			}
		} else {
			log.info("op<processFilter> lflist is empty ");
		}
		return resultList;
	}

	protected abstract List<E> getEntityList();

	protected abstract boolean judgeEntity(E entity);

	protected abstract void successFilterResult(E entity, FilterResult filterResult);

	protected abstract void failFilterResult(E entity, FilterResult filterResult);

	protected abstract void notMatchFilterResult(E entity, FilterResult filterResult);

	protected abstract void asyncControlFilterResult(E entity, FilterResult filterResult);

	protected abstract void validateFail(E entity);

	protected abstract void afterFilter(E entity);
}
