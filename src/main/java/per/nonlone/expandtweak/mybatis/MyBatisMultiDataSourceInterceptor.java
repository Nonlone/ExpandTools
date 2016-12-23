package per.nonlone.expandtweak.mybatis;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.Properties;

import org.apache.ibatis.executor.statement.RoutingStatementHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.log4j.Logger;

import per.nonlone.expandtweak.mybatis.annotation.DataSource;

/**
 * MyBatis 自动分配数据库连接插件，需要注入多数据池数据源
 * @author Nonlone
 *  @email thunderbird.shun@gmail.com
 *
 */
@Intercepts({ @Signature(type = StatementHandler.class, method = "prepare", args = { Connection.class }) })
public class MyBatisMultiDataSourceInterceptor implements Interceptor {

	private static final Logger logger = Logger.getLogger(MyBatisMultiDataSourceInterceptor.class);
	
	private MultipleDataSource dataSourcePool;

	@Override
	public Object intercept(Invocation invocation) throws Throwable {
		if(dataSourcePool==null){
			logger.error("op<intercept> dataSourcePool is null");
			Object result = invocation.proceed();
			return result;
		}
		logger.debug("op<intercept> before Invocation.proceed()");
		RoutingStatementHandler handler = (RoutingStatementHandler) invocation.getTarget();
		StatementHandler delegate = (StatementHandler) ReflectUtil.getFieldValue(handler, "delegate");
		MappedStatement mappedStatement = (MappedStatement) ReflectUtil.getFieldValue(delegate, "mappedStatement");
		String mapperClassPath = mappedStatement.getId().substring(0, mappedStatement.getId().lastIndexOf("."));
		Class<?> mapperClass = Class.forName(mapperClassPath);
		DataSource dataSource = mapperClass.getAnnotation(DataSource.class);
		if (null != dataSource) {
			String dataSourceKey = dataSource.value().toString().toLowerCase();
			if(!dataSourceKey.equals(dataSourcePool.getDataSourceKey())){
				dataSourcePool.setDataSourceKey(dataSourceKey);
				Connection conn = dataSourcePool.getConnection();
				ReflectUtil.setFieldValue(invocation, "args", new Object[]{conn});
				logger.info("op<intercept> change to datasource key: "+dataSourceKey);
			}
		}
		Object result = invocation.proceed();
		logger.debug("op<intercept> after Invocation.proceed()");
		return result;
	}

	@Override
	public Object plugin(Object target) {
		return Plugin.wrap(target, this);
	}

	@Override
	public void setProperties(Properties properties) {

	}

	public MultipleDataSource getDataSourcePool() {
		return dataSourcePool;
	}

	public void setDataSourcePool(MultipleDataSource dataSourcePool) {
		this.dataSourcePool = dataSourcePool;
	}

	/**
	 * 利用反射进行操作的一个工具类
	 * 
	 */
	private static class ReflectUtil {
		/**
		 * 利用反射获取指定对象的指定属性
		 * 
		 * @param obj 目标对象
		 * @param fieldName 目标属性
		 * @return 目标属性的值
		 */
		public static Object getFieldValue(Object obj, String fieldName) {
			Object result = null;
			Field field = ReflectUtil.getField(obj, fieldName);
			if (field != null) {
				field.setAccessible(true);
				try {
					result = field.get(obj);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					logger.debug("op<getFieldValue> error<"+e.getMessage()+">",e);
				}
			}
			return result;
		}

		/**
		 * 利用反射获取指定对象里面的指定属性
		 * 
		 * @param obj 目标对象
		 * @param fieldName 目标属性
		 * @return 目标字段
		 */
		private static Field getField(Object obj, String fieldName) {
			Field field = null;
			for (Class<?> clazz = obj.getClass(); clazz != Object.class; clazz = clazz.getSuperclass()) {
				try {
					field = clazz.getDeclaredField(fieldName);
					break;
				} catch (NoSuchFieldException e) {
					// 这里不用做处理，子类没有该字段可能对应的父类有，都没有就返回null。
					logger.debug("op<getField> error<"+e.getMessage()+">",e);
				}
			}
			return field;
		}

		/**
		 * 利用反射设置指定对象的指定属性为指定的值
		 * 
		 * @param obj 目标对象
		 * @param fieldName 目标属性
		 * @param fieldValue 目标值
		 */
		public static void setFieldValue(Object obj, String fieldName, Object fieldValue) {
			Field field = ReflectUtil.getField(obj, fieldName);
			if (field != null) {
				try {
					field.setAccessible(true);
					field.set(obj, fieldValue);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					logger.error("op<setFieldValue> error<"+e.getMessage()+">",e);
				}
			}
		}
	}

}
