package com.mindforger.shiftsolver.shared.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mindforger.shiftsolver.shared.ShiftSolverLogger;

public class PeriodSolution implements Serializable {
	private static final long serialVersionUID = 7586400671035292788L;

	private String key;
	private String periodPreferencesKey;
	private int year;
	private int month;
	private Map<String, Job> employeeJobs;
	private List<DaySolution> days;
	private int solutionNumber;
	
	public PeriodSolution() {
		this.days=new ArrayList<DaySolution>();
		this.employeeJobs=new HashMap<String,Job>();
	}
	
	public PeriodSolution(int year, int month) {
		this();
		this.year=year;
		this.month=month;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public int getMonth() {
		return month;
	}

	public void setMonth(int month) {
		this.month = month;
	}

	public List<DaySolution> getDays() {
		return days;
	}

	public void setDays(List<DaySolution> days) {
		this.days = days;
	}
	
	public void addDaySolution(DaySolution daySolution) {
		this.days.add(daySolution);
	}
	
	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getDlouhanKey() {
		return periodPreferencesKey;
	}

	public void setDlouhanKey(String dlouhanKey) {
		this.periodPreferencesKey = dlouhanKey;
	}

	public String getPeriodPreferencesKey() {
		return periodPreferencesKey;
	}

	public void setPeriodPreferencesKey(String periodPreferencesKey) {
		this.periodPreferencesKey = periodPreferencesKey;
	}

	public Map<String, Job> getEmployeeJobs() {
		return employeeJobs;
	}

	public void setEmployeeJobs(Map<String, Job> employeeJobs) {
		this.employeeJobs = employeeJobs;
	}

	public void addEmployeeJob(String employeeKey, Job job) {
		this.employeeJobs.put(employeeKey, job);
	}

	public void setSolutionNumber(int solutionNumber) {
		this.solutionNumber=solutionNumber;
	}

	public int getSolutionNumber() {
		return solutionNumber;
	}

	public void printSchedule() {		
		List<DaySolution> days = getDays();
		for(DaySolution ds:days) {
			ShiftSolverLogger.debug((ds.isWorkday()?"Work":"Weekend") + " Day "+ ds.getDay() +":");
			if(ds.isWorkday()) {
				ShiftSolverLogger.debug("  Morning:");
				ShiftSolverLogger.debug("    E "+ds.getWorkdayMorningShift().editor.getFullName());
				ShiftSolverLogger.debug("    D "+ds.getWorkdayMorningShift().drone6am.getFullName());
				ShiftSolverLogger.debug("    D "+ds.getWorkdayMorningShift().drone7am.getFullName());
				ShiftSolverLogger.debug("    D "+ds.getWorkdayMorningShift().drone8am.getFullName());
				ShiftSolverLogger.debug("    E "+ds.getWorkdayMorningShift().sportak.getFullName());

				ShiftSolverLogger.debug("  Afternoon:");
				ShiftSolverLogger.debug("    E "+ds.getWorkdayAfternoonShift().editor.getFullName());
				ShiftSolverLogger.debug("    D "+ds.getWorkdayAfternoonShift().drones[0].getFullName());
				ShiftSolverLogger.debug("    D "+ds.getWorkdayAfternoonShift().drones[1].getFullName());
				ShiftSolverLogger.debug("    D "+ds.getWorkdayAfternoonShift().drones[2].getFullName());
				ShiftSolverLogger.debug("    D "+ds.getWorkdayAfternoonShift().drones[3].getFullName());
				ShiftSolverLogger.debug("    S "+ds.getWorkdayAfternoonShift().sportak.getFullName());

				ShiftSolverLogger.debug("  Night:");
				ShiftSolverLogger.debug("    D "+ds.getNightShift().drone.getFullName());
			} else {		
				ShiftSolverLogger.debug("  Morning:");
				ShiftSolverLogger.debug("    E "+ds.getWeekendMorningShift().editor.getFullName());
				ShiftSolverLogger.debug("    D "+ds.getWeekendMorningShift().drone6am.getFullName());
				ShiftSolverLogger.debug("    E "+ds.getWeekendMorningShift().sportak.getFullName());

				ShiftSolverLogger.debug("  Afternoon:");
				ShiftSolverLogger.debug("    E "+ds.getWeekendAfternoonShift().editor.getFullName());
				ShiftSolverLogger.debug("    D "+ds.getWeekendAfternoonShift().drone.getFullName());
				ShiftSolverLogger.debug("    S "+ds.getWeekendAfternoonShift().sportak.getFullName());

				ShiftSolverLogger.debug("  Night:");
				ShiftSolverLogger.debug("    D "+ds.getNightShift().drone.getFullName());
			}
		}
	}
}
