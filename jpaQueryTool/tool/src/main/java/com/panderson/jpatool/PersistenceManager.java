package com.panderson.jpatool;

import java.sql.Connection;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.apache.commons.pool2.BaseObjectPool;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author paul.anderson
 */
public class PersistenceManager {
	private EntityManager em;// = 		Persistence.createEntityManagerFactory("plm-utilization_PU").createEntityManager();
	private EntityManagerFactory entityManagerFactory = null;
	
	public PersistenceManager(){
		System.out.println("");
		
	}
	
	protected void setEntityManagerFactory(EntityManagerFactory f){
		this.entityManagerFactory = f;
		 em = f.createEntityManager();
	}


	public int testUpdateQuery(String query){
		em.getTransaction().begin();
		try{
			int res = em.createQuery(query).executeUpdate();
			em.getTransaction().commit();
			return res;
		} catch(Throwable t){
			em.getTransaction().rollback();
			throw t;
		}
	}
	
	public List testQuery(String query, int maxResults){
		List result = (List)em.createQuery(query).setMaxResults(maxResults).getResultList();
		return result;
	}
	
	public static void xmain(String[] args){
		PersistenceManager pm = new PersistenceManager();
	}
}
