package org.recap.consumer;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.recap.ReCAPConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import javax.jms.*;

/**
 * Created by sudhishk on 17/1/17.
 */
public class JMSConsumer implements MessageListener{

    private final static Logger logger = LoggerFactory.getLogger(JMSConsumer.class);

    private static String topicName = "PUL.RequestT";

    private static String devBrokerURL = "tcp://192.168.55.198:61616";
    private static String tstBrokerURL = "tcp://tst-recap.htcinc.com:61616";
    private static String tstSSLBrokerURL = "ssl://tst-recap.htcinc.com:61616?trace=false";
    private static String uatBrokerURL = "tcp://uat-recap.htcinc.com:61616";

    public static void main(String[] args) throws Exception{

        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(tstSSLBrokerURL);
        Connection connection = connectionFactory.createConnection();
        connection.start();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        Topic topic =session.createTopic(topicName);
        MessageConsumer messageConsumer = session.createConsumer(topic);
        messageConsumer.setMessageListener(new JMSConsumer());
        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));

        logger.info("Press enter to quit application");

        stdin.readLine();
        connection.close();
    }

    @Override
    public void onMessage(Message message) {
        try {
            TextMessage textMessage =(TextMessage)message;
            logger.info("Message is " + textMessage.getText());
        } catch (JMSException e) {
            logger.error(ReCAPConstants.REQUEST_EXCEPTION,e);
        }
    }

}
