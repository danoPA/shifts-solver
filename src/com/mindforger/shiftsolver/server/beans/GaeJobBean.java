package com.mindforger.shiftsolver.server.beans;

import java.io.Serializable;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class GaeJobBean implements Serializable, GaeBean {
	private static final long serialVersionUID = 1733290552721580460L;

	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)	
	private Key key;

	@Persistent
	private GaePeriodPreferencesBean periodPreferences;
	
	@Persistent
	String employeeKey;
	@Persistent
	int shiftsLimit;
	
	public GaeJobBean() {		
	}

	public Key getKey() {
		return key;
	}

	public void setKey(Key key) {
		this.key = key;
	}

	public int getShiftsLimit() {
		return shiftsLimit;
	}

	public void setShiftsLimit(int shiftsLimit) {
		this.shiftsLimit = shiftsLimit;
	}

	public void setEmployeeKey(String k) {
		this.employeeKey=k;
	}
	
	public String getEmployeeKey() {
		return employeeKey;
	}

	public GaePeriodPreferencesBean getPeriodPreferences() {
		return periodPreferences;
	}

	public void setPeriodPreferences(GaePeriodPreferencesBean periodPreferences) {
		this.periodPreferences = periodPreferences;
	}
}
