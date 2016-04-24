package com.pa.messaging.connection;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Session;
import org.apache.activemq.ActiveMQConnectionFactory;

/**
 *
 * @author paul.anderson
 */
public abstract class ConnectionUtils {

	public static Connection connectToBroker(String brokerURL, String userName, String password)
					throws JMSException {
		ConnectionFactory cf = new ActiveMQConnectionFactory(brokerURL);
		((ActiveMQConnectionFactory)cf).setTransactedIndividualAck(true);
		
		return cf.createConnection(userName, password);
	}

	public static Session getSession(Connection conn)
		throws JMSException {
		return conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("1");
		Connection conn = connectToBroker("tcp://192.168.3.106:61616", "mquser", "pwd123");
		Session s = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
		s.close();
		conn.close();
	}
}
