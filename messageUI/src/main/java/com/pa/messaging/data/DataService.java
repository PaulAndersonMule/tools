package com.pa.messaging.data;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.PersistenceUnit;

/**
 *
 * @author paul.anderson
 */
public class DataService {
	@PersistenceUnit(unitName = "messageUI_PU")
	private static final EntityManager entityManager = Persistence.createEntityManagerFactory("messageUI_PU").createEntityManager();
	
	public static JMSConnection editConnection(String connectionName, String brokerURL, String userName, String password){
		JMSConnection connx = entityManager.find(JMSConnection.class, connectionName);
		if (connx == null){
			throw new RuntimeException("connection-names cannot be edited");
		}
		connx.setPassword(password);
		connx.setUserName(userName);
		connx.setBrokerUrl(brokerURL);
		EntityTransaction tx = entityManager.getTransaction();
		tx.begin();
		entityManager.merge(connx);
		tx.commit();
		return connx;
	}
	
	public static void storeNewConnection(String connectioName, String brokerURL, String userName, String password){
		JMSConnection connx = new JMSConnection(connectioName, brokerURL, userName, password);
		EntityTransaction tx = entityManager.getTransaction();
		tx.begin();
		try{
			entityManager.persist(connx);
			tx.commit();
		} catch (Exception x){
			tx.rollback();
		}
	}
	
	public static List<JMSConnection> readConnections(){
		return entityManager.createNamedQuery("JMSConnection.findAll").getResultList();
	}
	
	public static void deleteConnection(JMSConnection connection){
		JMSConnection persisted = entityManager.find(JMSConnection.class, connection.getConnectionName());
		entityManager.getTransaction().begin();
		entityManager.remove(persisted);
		entityManager.getTransaction().commit();
	}
	
	public static void main(String[] args){
		System.out.println(entityManager);
	}
}
