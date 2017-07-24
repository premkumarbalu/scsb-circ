package org.recap.spring;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Created by sheiks on 23/01/17.
 */
@Component
public class ApplicationContextProvider implements ApplicationContextAware {

    private static ApplicationContext context;

    private static ApplicationContextProvider applicationContextProvider;

    private ApplicationContextProvider(){
    }

    /**
     * Get instance of ApplicationContextProvider.
     *
     * @return the application context provider
     */
    public static ApplicationContextProvider getInstance(){
        if(null == applicationContextProvider){
            applicationContextProvider = new ApplicationContextProvider();
        }
        return applicationContextProvider;
    }

    /**
     * Gets application context.
     *
     * @return the application context
     */
    public ApplicationContext getApplicationContext() {
        return context;
    }

    public void setApplicationContext(ApplicationContext ac)
            throws BeansException {
        context = ac;
    }
}
