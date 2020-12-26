/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mulesoft.services.jpasample.objects;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 *
 * @author paul.anderson
 */
@Embeddable
public class NotificationUserPK implements Serializable {

	@Basic(optional = false)
  @NotNull
  @Column(name = "NOTIFICATION_ID")
	private int notificationId;
	@Basic(optional = false)
  @NotNull
  @Size(min = 1, max = 10)
  @Column(name = "USER_ID")
	private String userId;

	public NotificationUserPK() {
	}

	public NotificationUserPK(int notificationId, String userId) {
		this.notificationId = notificationId;
		this.userId = userId;
	}

	public int getNotificationId() {
		return notificationId;
	}

	public void setNotificationId(int notificationId) {
		this.notificationId = notificationId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	@Override
	public int hashCode() {
		int hash = 0;
		hash += (int) notificationId;
		hash += (userId != null ? userId.hashCode() : 0);
		return hash;
	}

	@Override
	public boolean equals(Object object) {
		// TODO: Warning - this method won't work in the case the id fields are not set
		if (!(object instanceof NotificationUserPK)) {
			return false;
		}
		NotificationUserPK other = (NotificationUserPK) object;
		if (this.notificationId != other.notificationId) {
			return false;
		}
		if ((this.userId == null && other.userId != null) || (this.userId != null && !this.userId.equals(other.userId))) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "com.mulesoft.services.jpasample.objects.NotificationUserPK[ notificationId=" + notificationId + ", userId=" + userId + " ]";
	}
	
}
