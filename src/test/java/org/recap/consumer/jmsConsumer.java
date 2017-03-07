package org.recap.consumer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import javax.jms.*;

import org.apache.activemq.ActiveMQConnectionFactory;

/**
 * Created by sudhishk on 17/1/17.
 */
public class jmsConsumer implements MessageListener{

    private static String topicName = "PUL.RequestT";

    private static String dev_BrokerURL = "tcp://192.168.55.198:61616";
    private static String tst_BrokerURL = "tcp://tst-recap.htcinc.com:61616";
    private static String tst_ssl_BrokerURL = "ssl://tst-recap.htcinc.com:61616?trace=false";
    private static String uat_BrokerURL = "tcp://uat-recap.htcinc.com:61616";

    public static void main(String[] args) throws Exception{

        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(tst_ssl_BrokerURL);
        Connection connection = connectionFactory.createConnection();
        connection.start();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        Topic topic =session.createTopic(topicName);
        MessageConsumer messageConsumer = session.createConsumer(topic);
        messageConsumer.setMessageListener(new jmsConsumer());
        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("Press enter to quit application");

        stdin.readLine();
        connection.close();
    }

    @Override
    public void onMessage(Message message) {
        try {
            TextMessage textMessage =(TextMessage)message;
            System.out.println("Message is " + textMessage.getText());
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

}
