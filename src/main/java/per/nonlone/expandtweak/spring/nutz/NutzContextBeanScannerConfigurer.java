package per.nonlone.expandtweak.spring.nutz;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class NutzContextBeanScannerConfigurer implements BeanFactoryPostProcessor,ApplicationContextAware{
	
	private ApplicationContext applicationContext;

	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		System.out.println("test postProcessBeanFactory:"+beanFactory.toString());
		BeanDefinitionRegistry beanDefintionRegistry = (BeanDefinitionRegistry) beanFactory;
	}

}
