/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mulesoft.services.jpasample.objects;

import java.io.Serializable;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
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
@Table(name = "NOTIFICATION_CLASS", catalog = "", schema = "DEMOUSER")
@XmlRootElement
@NamedQueries({
	@NamedQuery(name = "NotificationClass.findAll", query = "SELECT n FROM NotificationClass n"),
	@NamedQuery(name = "NotificationClass.findByClassification", query = "SELECT n FROM NotificationClass n WHERE n.classification = :classification")})
public class NotificationClass implements Serializable {

	private static final long serialVersionUID = 1L;
	@Id
  @Basic(optional = false)
  @NotNull
  @Size(min = 1, max = 40)
  @Column(name = "CLASSIFICATION")
	private String classification;
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "classification", fetch = FetchType.EAGER)
	private List<Notification> notificationList;

	public NotificationClass() {
	}

	public NotificationClass(String classification) {
		this.classification = classification;
	}

	public String getClassification() {
		return classification;
	}

	public void setClassification(String classification) {
		this.classification = classification;
	}

	@XmlTransient
  @JsonIgnore
	public List<Notification> getNotificationList() {
		return notificationList;
	}

	public void setNotificationList(List<Notification> notificationList) {
		this.notificationList = notificationList;
	}

	@Override
	public int hashCode() {
		int hash = 0;
		hash += (classification != null ? classification.hashCode() : 0);
		return hash;
	}

	@Override
	public boolean equals(Object object) {
		// TODO: Warning - this method won't work in the case the id fields are not set
		if (!(object instanceof NotificationClass)) {
			return false;
		}
		NotificationClass other = (NotificationClass) object;
		if ((this.classification == null && other.classification != null) || (this.classification != null && !this.classification.equals(other.classification))) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "com.mulesoft.services.jpasample.objects.NotificationClass[ classification=" + classification + " ]";
	}
	
}
