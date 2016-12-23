package per.nonlone.expandtweak.common;


public interface Filter <T> {

	/**
	 * 日志过滤器
	 * @param record
	 * @param exportPath
	 * @return boolean false为过滤错误，标记文件无法处理
	 */
	boolean filter(T t);
	
	/**
	 * 返回过滤器名字
	 * @return
	 */
	String getName();
	
	/**
	 * 返回过滤结果
	 * @return
	 */
	Object getFilterResult();
}
