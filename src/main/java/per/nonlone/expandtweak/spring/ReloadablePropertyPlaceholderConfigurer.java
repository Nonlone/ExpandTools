package per.nonlone.expandtweak.spring;

import java.io.IOException;
import java.util.Properties;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.util.PropertyPlaceholderHelper;
import org.springframework.util.PropertyPlaceholderHelper.PlaceholderResolver;
import org.springframework.util.StringValueResolver;
/**
 * 可重新载入Properties的PropertiesPlaceholderConfigurer类，对Scope为Prototype的配置bean有效，用于动态更新properties
 * @author leishy@corp.21cn.com
 *
 */
public class ReloadablePropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer {
	
	private ReloadablePlaceholderResolvingStringValueResolver reloadableValueResolver;

	/**
	 * 重新导入注册在PropertiesPlaceholderConfigurer的Properties文件
	 * @throws IOException
	 */
	public void reloadProperties() throws IOException {
		Properties props = mergeProperties();
		this.reloadableValueResolver.refreshProperties(props);
	}
	
	/**
	 * 重写processProperties方法，方法在ApplicationContext构建时候执行，将StringValueResolver放入到BeanFactory中，
	 * StringValueResolver在获取Bean的时候，执行resolveStringValue方法去调用ReloadablePropertyPlaceholderConfigurerResolver
	 * 的resolvePlaceholder方法去替换properties
	 */
	@Override
	protected void processProperties(ConfigurableListableBeanFactory beanFactoryToProcess, Properties props)
			throws BeansException {
		this.reloadableValueResolver = new ReloadablePlaceholderResolvingStringValueResolver(props);
		StringValueResolver valueResolver = this.reloadableValueResolver;
		this.doProcessProperties(beanFactoryToProcess, valueResolver);
	}
	
	
	private class ReloadablePlaceholderResolvingStringValueResolver implements StringValueResolver {

		private final PropertyPlaceholderHelper helper;

		private final ReloadablePropertyPlaceholderConfigurerResolver resolver;

		public ReloadablePlaceholderResolvingStringValueResolver(Properties props) {
			this.helper = new PropertyPlaceholderHelper(placeholderPrefix, placeholderSuffix, valueSeparator, ignoreUnresolvablePlaceholders);
			this.resolver = new ReloadablePropertyPlaceholderConfigurerResolver(props);
		}

		@Override
		public String resolveStringValue(String strVal) throws BeansException {
			String value = this.helper.replacePlaceholders(strVal, this.resolver);
			return (value.equals(nullValue) ? null : value);
		}
		
		private void refreshProperties(Properties props){
			this.resolver.setProps(props);
		}
	}
	
	private class ReloadablePropertyPlaceholderConfigurerResolver implements PlaceholderResolver {

		private Properties props;

		private ReloadablePropertyPlaceholderConfigurerResolver(Properties props) {
			this.props = props;
		}

		@Override
		public String resolvePlaceholder(String placeholderName) {
			return ReloadablePropertyPlaceholderConfigurer.this.resolvePlaceholder(placeholderName, props, SYSTEM_PROPERTIES_MODE_FALLBACK);
		}

		public void setProps(Properties props) {
			this.props = props;
		}
	}
}
