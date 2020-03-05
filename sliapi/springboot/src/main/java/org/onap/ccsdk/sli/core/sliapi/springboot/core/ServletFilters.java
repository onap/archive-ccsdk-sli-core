package org.onap.ccsdk.sli.core.sliapi.springboot.core;

import javax.servlet.ServletException;

import org.onap.logging.filter.base.PayloadLoggingServletFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServletFilters {

	@Bean
	public FilterRegistrationBean<PayloadLoggingServletFilter> payloadFilterRegistration() throws ServletException {
		FilterRegistrationBean<PayloadLoggingServletFilter> registration = new FilterRegistrationBean<PayloadLoggingServletFilter>();
		registration.setFilter(new PayloadLoggingServletFilter());
		registration.addUrlPatterns("/*");
		registration.setName("payloadFilter");
		registration.setOrder(0);
		return registration;
	}
}