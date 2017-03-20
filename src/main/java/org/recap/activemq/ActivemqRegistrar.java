package org.recap.activemq;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.camel.CamelContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;


/**
 * Created by premkb on 12/10/16.
 */
@Component
public class ActivemqRegistrar {

    /**
     * Register ActiveMQ queue service, with Camel context
     * @param camelContext
     * @param defaultBrokerURL
     * @throws JMSException
     */
    @Autowired
    public ActivemqRegistrar(CamelContext camelContext , @Value("${activemq.broker.url}") String defaultBrokerURL) throws JMSException {
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(defaultBrokerURL);
        ActiveMQComponent activeMQComponent = new ActiveMQComponent();
        activeMQComponent.setConnectionFactory(connectionFactory);
        activeMQComponent.setTrustAllPackages(true);
        camelContext.addComponent("scsbactivemq", activeMQComponent);
    }
}
