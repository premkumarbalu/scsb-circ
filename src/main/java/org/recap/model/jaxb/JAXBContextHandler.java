package org.recap.model.jaxb;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by pvsubrah on 6/21/16.
 */
public class JAXBContextHandler {

    private static JAXBContextHandler jaxbContextHandler;

    private JAXBContextHandler() {
    }

    public static JAXBContextHandler getInstance() {
        if(null == jaxbContextHandler){
            jaxbContextHandler = new JAXBContextHandler();
        }

        return jaxbContextHandler;
    }

    private Map<String, JAXBContext> contextMap;

    public JAXBContext getJAXBContextForClass(Class cl) throws JAXBException {
        if(getContextMap().containsKey(cl.getName())){
            return getContextMap().get(cl.getName());
        } else {
            JAXBContext newInstance = JAXBContext.newInstance(cl);
            getContextMap().put(cl.getName(), newInstance);
        }

        return getContextMap().get(cl.getName());
    }

    public Map<String, JAXBContext> getContextMap() {
        if (null == contextMap) {
            contextMap = new HashMap<>();
        }
        return contextMap;
    }

    public void setContextMap(Map<String, JAXBContext> contextMap) {
        this.contextMap = contextMap;
    }
}
