/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mulesoft.services.jpasample.objects;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.codehaus.jackson.annotate.JsonIgnore;

/**
 *
 * @author paul.anderson
 */
@Entity
@Table(name = "NOTIFICATION", catalog = "", schema = "DEMOUSER")
@XmlRootElement
@NamedQueries({
	@NamedQuery(name = "Notification.findAll", query = "SELECT n FROM Notification n"),
	@NamedQuery(name = "Notification.findByNotificationId", query = "SELECT n FROM Notification n WHERE n.notificationId = :notificationId"),
	@NamedQuery(name = "Notification.findBySummaryInfo", query = "SELECT n FROM Notification n WHERE n.summaryInfo = :summaryInfo"),
	@NamedQuery(name = "Notification.findBySender", query = "SELECT n FROM Notification n WHERE n.sender = :sender"),
	@NamedQuery(name = "Notification.findBySentTime", query = "SELECT n FROM Notification n WHERE n.sentTime = :sentTime")})
public class Notification implements Serializable {

	private static final long serialVersionUID = 1L;
	@Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Basic(optional = false)
  @Column(name = "NOTIFICATION_ID")
	private Integer notificationId;
	@Size(max = 40)
  @Column(name = "SUMMARY_INFO")
	private String summaryInfo;
	@Basic(optional = false)
  @NotNull
  @Size(min = 1, max = 10)
  @Column(name = "SENDER")
	private String sender;
	@Basic(optional = false)
  @NotNull
  @Column(name = "SENT_TIME")
  @Temporal(TemporalType.TIMESTAMP)
	private Date sentTime;
	@Basic(optional = false)
  @NotNull
  @Lob
  @Column(name = "CONTENT")
	private String content;
	@JoinColumn(name = "CLASSIFICATION", referencedColumnName = "CLASSIFICATION")
  @ManyToOne(optional = false, fetch = FetchType.EAGER)
	private NotificationClass classification;
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "notification", fetch = FetchType.EAGER)
	private List<NotificationUser> notificationUserList;

	public Notification() {
	}

	public Notification(Integer notificationId) {
		this.notificationId = notificationId;
	}

	public Notification(Integer notificationId, String sender, Date sentTime, String content) {
		this.notificationId = notificationId;
		this.sender = sender;
		this.sentTime = sentTime;
		this.content = content;
	}

	public Integer getNotificationId() {
		return notificationId;
	}

	public void setNotificationId(Integer notificationId) {
		this.notificationId = notificationId;
	}

	public String getSummaryInfo() {
		return summaryInfo;
	}

	public void setSummaryInfo(String summaryInfo) {
		this.summaryInfo = summaryInfo;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public Date getSentTime() {
		return sentTime;
	}

	public void setSentTime(Date sentTime) {
		this.sentTime = sentTime;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public NotificationClass getClassification() {
		return classification;
	}

	public void setClassification(NotificationClass classification) {
		this.classification = classification;
	}

	@XmlTransient
  @JsonIgnore
	public List<NotificationUser> getNotificationUserList() {
		return notificationUserList;
	}

	public void setNotificationUserList(List<NotificationUser> notificationUserList) {
		this.notificationUserList = notificationUserList;
	}

	@Override
	public int hashCode() {
		int hash = 0;
		hash += (notificationId != null ? notificationId.hashCode() : 0);
		return hash;
	}

	@Override
	public boolean equals(Object object) {
		// TODO: Warning - this method won't work in the case the id fields are not set
		if (!(object instanceof Notification)) {
			return false;
		}
		Notification other = (Notification) object;
		if ((this.notificationId == null && other.notificationId != null) || (this.notificationId != null && !this.notificationId.equals(other.notificationId))) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "com.mulesoft.services.jpasample.objects.Notification[ notificationId=" + notificationId + " ]";
	}
	
}
