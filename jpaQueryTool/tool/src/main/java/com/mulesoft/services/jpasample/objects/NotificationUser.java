/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mulesoft.services.jpasample.objects;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author paul.anderson
 */
@Entity
@Table(name = "NOTIFICATION_USER", catalog = "", schema = "DEMOUSER")
@XmlRootElement
@NamedQueries({
  @NamedQuery(name = "NotificationUser.findByNotificationIdAndUserId", query = "SELECT n from NotificationUser n WHERE n.notificationUserPK.notificationId = :notificationId and n.notificationUserPK.userId = :userId"),
  @NamedQuery(name = "NotificationUser.findAll", query = "SELECT n FROM NotificationUser n"),
  @NamedQuery(name = "NotificationUser.findByNotificationId", query = "SELECT n FROM NotificationUser n WHERE n.notificationUserPK.notificationId = :notificationId"),
  @NamedQuery(name = "NotificationUser.findByUserID", query = "SELECT n.notification FROM NotificationUser n WHERE n.notificationUserPK.userId = :userId"),
  @NamedQuery(name = "NotificationUser.findUnacknowledgedByIDAndUserID", query = "SELECT n FROM NotificationUser n WHERE n.notification.notificationId = :notificationID and n.notificationUserPK.userId = :userId and n.acknowledged = 0"),
  @NamedQuery(name = "NotificationUser.findUnacknowledgedByUserID", query = "SELECT n FROM NotificationUser n WHERE n.notificationUserPK.userId = :userId and n.acknowledged = 0"),
  @NamedQuery(name = "NotificationUser.findUnacknowledgedWithDetail", query = "SELECT n.notification FROM NotificationUser n WHERE n.notificationUserPK.userId = :userId and n.acknowledged = 0"),
  @NamedQuery(name = "NotificationUser.findByAcknowledged", query = "SELECT n FROM NotificationUser n WHERE n.acknowledged = :acknowledged"),
  @NamedQuery(name = "NotificationUser.findByAckTime", query = "SELECT n FROM NotificationUser n WHERE n.ackTime = :ackTime")
})

public class NotificationUser implements Serializable {

	private static final long serialVersionUID = 1L;
	@EmbeddedId
	protected NotificationUserPK notificationUserPK;
	@Basic(optional = false)
  @NotNull
  @Column(name = "ACKNOWLEDGED")
	private short acknowledged;
	@Column(name = "ACK_TIME")
  @Temporal(TemporalType.TIMESTAMP)
	private Date ackTime;
	@JoinColumn(name = "NOTIFICATION_ID", referencedColumnName = "NOTIFICATION_ID", insertable = false, updatable = false)
  @ManyToOne(optional = false, fetch = FetchType.EAGER)
	private Notification notification;

	public NotificationUser() {
	}

	public NotificationUser(NotificationUserPK notificationUserPK) {
		this.notificationUserPK = notificationUserPK;
	}

	public NotificationUser(NotificationUserPK notificationUserPK, short acknowledged) {
		this.notificationUserPK = notificationUserPK;
		this.acknowledged = acknowledged;
	}

	public NotificationUser(int notificationId, String userId) {
		this.notificationUserPK = new NotificationUserPK(notificationId, userId);
	}

	public NotificationUserPK getNotificationUserPK() {
		return notificationUserPK;
	}

	public void setNotificationUserPK(NotificationUserPK notificationUserPK) {
		this.notificationUserPK = notificationUserPK;
	}

	public short getAcknowledged() {
		return acknowledged;
	}

	public void setAcknowledged(short acknowledged) {
		this.acknowledged = acknowledged;
	}

	public Date getAckTime() {
		return ackTime;
	}

	public void setAckTime(Date ackTime) {
		this.ackTime = ackTime;
	}

	public Notification getNotification() {
		return notification;
	}

	public void setNotification(Notification notification) {
		this.notification = notification;
	}

	@Override
	public int hashCode() {
		int hash = 0;
		hash += (notificationUserPK != null ? notificationUserPK.hashCode() : 0);
		return hash;
	}

	@Override
	public boolean equals(Object object) {
		// TODO: Warning - this method won't work in the case the id fields are not set
		if (!(object instanceof NotificationUser)) {
			return false;
		}
		NotificationUser other = (NotificationUser) object;
		if ((this.notificationUserPK == null && other.notificationUserPK != null) || (this.notificationUserPK != null && !this.notificationUserPK.equals(other.notificationUserPK))) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "com.mulesoft.services.jpasample.objects.NotificationUser[ notificationUserPK=" + notificationUserPK + " ]";
	}
	
}
