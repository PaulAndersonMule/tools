package com.pa.messaging.data;

import com.pa.messaging.connection.ConnectionUtils;
import java.io.Serializable;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author paul.anderson
 */
@Entity
@Table(name = "JMS_CONNECTION", catalog = "", schema = "JMSUSER")
@XmlRootElement
@NamedQueries({
	@NamedQuery(name = "JMSConnection.findAll", query = "SELECT j FROM JMSConnection j ORDER BY j.connectionName asc"),
	@NamedQuery(name = "JMSConnection.findByConnectionName", query = "SELECT j FROM JMSConnection j WHERE j.connectionName = :connectionName"),
	@NamedQuery(name = "JMSConnection.findByBrokerUrl", query = "SELECT j FROM JMSConnection j WHERE j.brokerUrl = :brokerUrl"),
	@NamedQuery(name = "JMSConnection.findByUserName", query = "SELECT j FROM JMSConnection j WHERE j.userName = :userName"),
	@NamedQuery(name = "JMSConnection.findByPassword", query = "SELECT j FROM JMSConnection j WHERE j.password = :password")})
public class JMSConnection implements Serializable {

	private static final long serialVersionUID = 1L;
	@Id
  @Basic(optional = false)
  @Column(name = "CONNECTION_NAME")
	private String connectionName;
	@Basic(optional = false)
  @Column(name = "BROKER_URL")
	private String brokerUrl;
	@Basic(optional = false)
  @Column(name = "USER_NAME")
	private String userName;
	@Basic(optional = false)
  @Column(name = "PASSWORD")
	private String password;

	public JMSConnection() {
	}

	public JMSConnection(String connectionName) {
		this.connectionName = connectionName;
	}

	public JMSConnection(String connectionName, String brokerUrl, String userName, String password) {
		this.connectionName = connectionName;
		this.brokerUrl = brokerUrl;
		this.userName = userName;
		this.password = password;
	}

	public String getConnectionName() {
		return connectionName;
	}

	public void setConnectionName(String connectionName) {
		this.connectionName = connectionName;
	}

	public String getBrokerUrl() {
		return brokerUrl;
	}

	public void setBrokerUrl(String brokerUrl) {
		this.brokerUrl = brokerUrl;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

		public Connection connect()
		throws JMSException {
		return ConnectionUtils.connectToBroker(brokerUrl, userName, password);
	}
	@Override
	public int hashCode() {
		int hash = 0;
		hash += (connectionName != null ? connectionName.hashCode() : 0);
		return hash;
	}

	@Override
	public boolean equals(Object object) {
		// TODO: Warning - this method won't work in the case the id fields are not set
		if (!(object instanceof JMSConnection)) {
			return false;
		}
		JMSConnection other = (JMSConnection) object;
		if ((this.connectionName == null && other.connectionName != null) || (this.connectionName != null && !this.connectionName.equals(other.connectionName))) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return String.format("name: %s, user: %s", connectionName, userName);
	}
	
}
