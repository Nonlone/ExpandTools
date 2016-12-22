package per.nonlone.expandtweak.common;

public interface Observable<T> {

	/**
	 * 通知状态切换
	 * @param t
	 */
	void notifiedChange(T t);
}
