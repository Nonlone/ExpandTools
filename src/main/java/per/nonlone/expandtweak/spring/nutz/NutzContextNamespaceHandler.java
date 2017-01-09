package per.nonlone.expandtweak.spring.nutz;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class NutzContextNamespaceHandler extends NamespaceHandlerSupport{

	public void init() {
		registerBeanDefinitionParser("component-scan", new NutzComponentScanBeanDefinitionParser());
	}

}
