package org.example;

import java.util.EnumSet;
import java.util.Set;
import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration.Dynamic;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;

public class ServletRequestHolderInitializer implements ServletContainerInitializer
{
    @Override
    public void onStartup(Set<Class<?>> set, ServletContext servletContext)
    {
        Dynamic registration = servletContext.addFilter("ServletRequestHolderFilter", ServletRequestHolderFilter.class);
        registration.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), false, "/*");
        registration.setAsyncSupported(true);
    }
}
