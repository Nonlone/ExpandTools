package per.nonlone.expandtweak.mybatis;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
/**
 * 多数据池数据源
 * @author Nonlone
 * @email thunderbird.shun@gmail.com
 */
public class MultipleDataSource extends AbstractRoutingDataSource{

	private static final ThreadLocal<String> dataSourceKey = new InheritableThreadLocal<String>();
	
	public static String getDataSourceKey(){
		return dataSourceKey.get();
	}
	
	public static void setDataSourceKey(String dataSource) {
        dataSourceKey.set(dataSource);
    }
	
	@Override
	protected Object determineCurrentLookupKey() {
		return dataSourceKey.get();
	}

}
