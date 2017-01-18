package org.recap.consumer;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.TransportConnector;
import org.junit.Test;
import org.recap.BaseTestCase;

import javax.jms.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.net.URI;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;

/**
 * Created by sudhishk on 12/1/17.
 */
public class TopicConsumerUT extends BaseTestCase {

    private String topicName = "PUL.RequestT";
    private String initialContextFactory = "org.apache.activemq.jndi.ActiveMQInitialContextFactory";
    //    private String connectionString = "tcp://localhost:61616";
    private String connectionString = "tcp://192.168.55.210:61616";

    private boolean messageReceived = false;


    public void subscribeWithTopicLookup() throws NamingException {

        Properties properties = new Properties();

        TopicConnection topicConnection = null;
        properties.put("java.naming.factory.initial", initialContextFactory);
//        properties.put("connectionfactory.QueueConnectionFactory",connectionString);
        properties.put("connectionfactory.TopicConnectionFactory", connectionString);
        properties.put("topic." + topicName, topicName);
        try {
            InitialContext ctx = new InitialContext(properties);
            TopicConnectionFactory topicConnectionFactory = (TopicConnectionFactory) ctx.lookup("TopicConnectionFactory");
            topicConnection = topicConnectionFactory.createTopicConnection();
            System.out.println("Create Topic Connection for Topic " + topicName);

            while (!messageReceived) {
                try {
                    TopicSession topicSession = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
                    Topic topic = (Topic) ctx.lookup(topicName);
                    // start the connection
                    topicConnection.start();
                    // create a topic subscriber
                    javax.jms.TopicSubscriber topicSubscriber = topicSession.createSubscriber(topic);
                    TestMessageListener messageListener = new TestMessageListener();
                    topicSubscriber.setMessageListener(messageListener);
                    Thread.sleep(5000);
                    topicSubscriber.close();
                    topicSession.close();
                } catch (JMSException e) {
                    e.printStackTrace();
                } catch (NamingException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        } catch (NamingException e) {
            throw new RuntimeException("Error in initial context lookup", e);
        } catch (JMSException e) {
            throw new RuntimeException("Error in JMS operations", e);
        } finally {
            if (topicConnection != null) {
                try {
                    topicConnection.close();
                } catch (JMSException e) {
                    throw new RuntimeException(
                            "Error in closing topic connection", e);
                }
            }
        }
    }

    public class TestMessageListener implements MessageListener {
        public void onMessage(Message message) {
            try {
                System.out.println("Got the Message : " + ((TextMessage) message).getText());
                messageReceived = true;
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testMQConsumption() throws JMSException, NamingException {
        subscribeWithTopicLookup();
    }

    @Test
    public void testMQBroker() throws JMSException, NamingException {

    }

    public static void main(String[] args) throws NamingException {
        TopicConsumerUT subscriber = new TopicConsumerUT();
        subscriber.subscribeWithTopicLookup();
    }
}
