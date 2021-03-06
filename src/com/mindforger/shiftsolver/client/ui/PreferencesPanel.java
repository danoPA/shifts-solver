package com.mindforger.shiftsolver.client.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ListBox;
import com.mindforger.shiftsolver.client.RiaContext;
import com.mindforger.shiftsolver.client.RiaMessages;
import com.mindforger.shiftsolver.client.Utils;
import com.mindforger.shiftsolver.client.solver.EmployeeAllocation;
import com.mindforger.shiftsolver.client.solver.PublicHolidays;
import com.mindforger.shiftsolver.client.solver.ShiftSolverException;
import com.mindforger.shiftsolver.client.ui.buttons.EmployeesTableToEmployeeButton;
import com.mindforger.shiftsolver.client.ui.buttons.YesNoDontcareButton;
import com.mindforger.shiftsolver.client.ui.buttons.YesNoDontcareDofcaButton;
import com.mindforger.shiftsolver.client.ui.text.ShiftsLimitTextBox;
import com.mindforger.shiftsolver.shared.model.DayPreference;
import com.mindforger.shiftsolver.shared.model.Employee;
import com.mindforger.shiftsolver.shared.model.EmployeePreferences;
import com.mindforger.shiftsolver.shared.model.PeriodPreferences;
import com.mindforger.shiftsolver.shared.model.PeriodSolution;

public class PreferencesPanel extends FlexTable {

	private static final int YEAR=2015;
	
	private RiaMessages i18n;
	private RiaContext ctx;

	private TableSortCriteria sortCriteria;
	private boolean sortIsAscending;
	
	private ListBox yearListBox;
	private ListBox monthListBox;
	private ListBox lastMonthEditorListBox;
	private FlexTable preferencesTable;
	private Map<String,List<YesNoDontcareButton>> preferenceButtons;
	private Map<String,ShiftsLimitTextBox> shiftLimitTextBoxes;
	
	public PeriodPreferences preferences;

	private PublicHolidays publicHolidays;

	private static final int CHECK_DAY=1;
	private static final int CHECK_MORNING_6=2;
	private static final int CHECK_MORNING_7=3;
	private static final int CHECK_MORNING_8=4;
	private static final int CHECK_AFTERNOON=5;
	private static final int CHECK_NIGHT=6;

	private FlowPanel buttonPanel;
	
	public PreferencesPanel(final RiaContext ctx) {
		this.ctx=ctx;
		this.i18n=ctx.getI18n();
		this.publicHolidays=new PublicHolidays();
		this.preferenceButtons=new HashMap<String,List<YesNoDontcareButton>>();
		this.shiftLimitTextBoxes=new HashMap<String,ShiftsLimitTextBox>();
		
		buttonPanel = newButtonPanel(ctx);
		setWidget(0, 0, buttonPanel);
		
		FlowPanel datePanel = newDatePanel();
		setWidget(1, 0, datePanel);
		
		preferencesTable = newPreferencesTable();
		setWidget(2, 0, preferencesTable);
	}

	private FlowPanel newButtonPanel(final RiaContext ctx) {
		FlowPanel buttonPanel=new FlowPanel();

		Button saveButton=new Button(i18n.save());
		saveButton.setStyleName("mf-button");
		saveButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if(preferences!=null) {
		    		ctx.getStatusLine().showProgress(ctx.getI18n().savingPeriodPreferences());
		    		riaToObject();
		      		ctx.getRia().savePeriodPreferences(preferences);
		      		ctx.getStatusLine().showInfo(i18n.periodPreferencesSaved());
				}
			}
		});		
		buttonPanel.add(saveButton);
		
		Button solveButton=new Button(i18n.solve());
		solveButton.setStyleName("mf-button");
		solveButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				handleSolve(ctx, false, false);
			}
		});		
		buttonPanel.add(solveButton);

		Button solvePartiallyButton=new Button(i18n.solvePartially());
		solvePartiallyButton.setTitle(i18n.solveWhatCanBeSolvedAndSkipTheRest());
		solvePartiallyButton.setStyleName("mf-buttonLooser");
		solvePartiallyButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				handleSolve(ctx, false, true);
			}
		});		
		buttonPanel.add(solvePartiallyButton);
		
		/*
		Button shuffleAndSolveButton=new Button(i18n.shuffleAndSolve());
		shuffleAndSolveButton.setStyleName("mf-buttonLooser");
		shuffleAndSolveButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				handleSolve(ctx, true, false);
			}
		});		
		buttonPanel.add(shuffleAndSolveButton);
		*/
		
		Button newSolutionButton=new Button(i18n.newEmptySolution());
		newSolutionButton.setStyleName("mf-buttonLooser");
		newSolutionButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if(preferences!=null) {
					ctx.getMenu().createNewSolution(preferences);					
				} else {
					ctx.getStatusLine().showError(i18n.savePrefsFirstToCreateSolution());
				}
			}
		});		
		buttonPanel.add(newSolutionButton);
		
		Button deleteButton=new Button(i18n.delete());
		deleteButton.setStyleName("mf-button");
		deleteButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if(preferences!=null) {
		    		ctx.getStatusLine().showProgress(i18n.deletingPreferences());
		      		ctx.getRia().deletePeriodPreferences(preferences);
		      		ctx.getStatusLine().showInfo(i18n.periodPreferencesDeleted());
				}
			}
		});		
		buttonPanel.add(deleteButton);
		
		Button cancelButton=new Button(i18n.cancel());
		cancelButton.setStyleName("mf-buttonLooser");
		cancelButton.setTitle(i18n.discardChanges());
		cancelButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if(preferences!=null) {
					ctx.getPeriodPreferencesTable().refresh(ctx.getState().getPeriodPreferencesArray());
		      		ctx.getRia().showPeriodPreferencesTable();
				}
			}
		});		
		buttonPanel.add(cancelButton);
		
		return buttonPanel;
	}

	private FlowPanel newDatePanel() {
		FlowPanel flowPanel=new FlowPanel();
		
		yearListBox = new ListBox(false);
		for(int i=0; i<10; i++) {
			yearListBox.addItem(""+(YEAR+i));			
		}
		yearListBox.setTitle(i18n.year());
		yearListBox.addChangeHandler(new ChangeHandler() {			
			@Override
			public void onChange(ChangeEvent event) {
				preferences.setYear(Integer.parseInt(yearListBox.getValue(yearListBox.getSelectedIndex())));
				handleDateListboxChange();				
			}
		});
		flowPanel.add(yearListBox);
		
		monthListBox = new ListBox(false);
		for(int i=1; i<=12; i++) {
			monthListBox.addItem(""+i);			
		}
		monthListBox.setTitle(i18n.month());
		monthListBox.addChangeHandler(new ChangeHandler() {			
			@Override
			public void onChange(ChangeEvent event) {
				preferences.setMonth(Integer.parseInt(monthListBox.getValue(monthListBox.getSelectedIndex())));
				handleDateListboxChange();				
			}
		});
		flowPanel.add(monthListBox);

		lastMonthEditorListBox = new ListBox(false);
		lastMonthEditorListBox.setTitle(i18n.lastMonthEditor());
		lastMonthEditorListBox.addChangeHandler(new ChangeHandler() {			
			@Override
			public void onChange(ChangeEvent event) {
				int idx = lastMonthEditorListBox.getSelectedIndex();		
				preferences.setLastMonthEditor(lastMonthEditorListBox.getValue(idx));
			}
		});
		flowPanel.add(lastMonthEditorListBox);
		
		return flowPanel;
	}
	
	private FlexTable newPreferencesTable() {
		FlexTable table = new FlexTable();
		table.setStyleName("mf-growsTable");

		table.removeAllRows();
				
		return table;		
	}

	private void refreshPreferencesTable(FlexTable table) {
		ctx.getStatusLine().showInfo(i18n.buildingPeriodPreferences());

		table.removeAllRows();
		preferenceButtons.clear();
		shiftLimitTextBoxes.clear();

		// TODO change these to button
		HTML html = new HTML("&nbsp;"+i18n.employee()+"&nbsp;");
		html.setStyleName("s2-tableHeadColumnButton");
		table.setWidget(0, 0, html);
		html = new HTML("&nbsp;"+i18n.periodPreferences());
		html.setStyleName("s2-tableHeadColumnButton");
		table.setWidget(0, 1, html);

		EmployeePreferences employeePreferences;
		int monthDays = preferences.getMonthDays();
		for(Employee employee:ctx.getState().getEmployees()) {
			if(preferences!=null && preferences.getEmployeeToPreferences()!=null && preferences.getEmployeeToPreferences().size()>0) {
				employeePreferences = preferences.getEmployeeToPreferences().get(employee.getKey());
			} else {
				employeePreferences = null;
			}
			addEmployeeRow(preferencesTable, employee, employeePreferences, monthDays);
		}
		
		ctx.getStatusLine().showInfo(i18n.periodPreferencesBuilt());
	}
		
	public void addEmployeeRow(
			FlexTable table, 
			Employee employee, 
			EmployeePreferences employeePreferences, 
			int monthDays) 
	{
		int numRows = table.getRowCount();
		
		FlexTable employeeAndJobsTable=new FlexTable();		
		employeeAndJobsTable.setStyleName("s2-preferencesEmployeeAndJobsTable");
		EmployeesTableToEmployeeButton button = new EmployeesTableToEmployeeButton(
				employee.getKey(),
				employee.getFullName(),
				"mf-growsTableGoalButton", 
				ctx);
		employeeAndJobsTable.setWidget(0, 0, button);		
		ShiftsLimitTextBox jobsTextBox=new ShiftsLimitTextBox(ctx, employeePreferences);
		shiftLimitTextBoxes.put(employee.getKey(),jobsTextBox);
		employeeAndJobsTable.setWidget(1, 0, jobsTextBox);
		table.setWidget(numRows, 0, employeeAndJobsTable);
		
		FlexTable employeePrefsTable=new FlexTable();
		
		for (int i = 1; i<=monthDays; i++) {
			HTML html = new HTML(""+i+Utils.getDayLetter(i, preferences.getStartWeekDay(), ctx.getI18n()));
			employeePrefsTable.setWidget(0, i, html);				
			if(Utils.isWeekend(i, preferences.getStartWeekDay())
					|| publicHolidays.isHolidays(
							preferences.getYear(), 
							preferences.getMonth(), 
							i)) 
			{
				html.addStyleName("s2-weekendDay");
			}
		}

		employeePrefsTable.setWidget(CHECK_DAY, 0, new HTML(i18n.day()));
		employeePrefsTable.setWidget(CHECK_MORNING_6, 0, new HTML(i18n.morning()+"&nbsp;6am"));
		employeePrefsTable.setWidget(CHECK_MORNING_7, 0, new HTML(i18n.morning()+"&nbsp;7am"));
		employeePrefsTable.setWidget(CHECK_MORNING_8, 0, new HTML(i18n.morning()+"&nbsp;8am"));
		employeePrefsTable.setWidget(CHECK_AFTERNOON, 0, new HTML(i18n.afternoon()));
		employeePrefsTable.setWidget(CHECK_NIGHT, 0, new HTML(i18n.night()));
		
		List<YesNoDontcareButton> employeeButtons=new ArrayList<YesNoDontcareButton>();
		DayPreference dayPreference;
		for(int c=1; c<=monthDays; c++) {
			if(employeePreferences!=null) {
				dayPreference = employeePreferences.getPreferencesForDay(c);				
			} else {
				dayPreference = null;
			}
			for(int r=1; r<=6; r++) {
				YesNoDontcareButton yesNoDontcare;
				if(r==CHECK_DAY) {
					yesNoDontcare = new YesNoDontcareDofcaButton();
				} else {
					yesNoDontcare = new YesNoDontcareButton();					
				}
				if(dayPreference!=null) {
					yesNoDontcare.setKey(dayPreference.getKey());
					switch(r) {
					case CHECK_DAY:
						if(dayPreference.isHoliDay()) {
							yesNoDontcare.setYesNoValue(3);
							yesNoDontcare.setStylePrimaryName("s2-3stateDofca");							
						} else {
							if(dayPreference.isNoDay()) {
								yesNoDontcare.setYesNoValue(1);
								yesNoDontcare.setStylePrimaryName("s2-3stateNo");
							}
							if(dayPreference.isYesDay()) {
								yesNoDontcare.setYesNoValue(2);
								yesNoDontcare.setStylePrimaryName("s2-3stateYes");
							}							
						}
						break;
					case CHECK_MORNING_6:
						if(dayPreference.isNoMorning6()) {
							yesNoDontcare.setYesNoValue(1);
							yesNoDontcare.setStylePrimaryName("s2-3stateNo");
						}
						if(dayPreference.isYesMorning6()) {
							yesNoDontcare.setYesNoValue(2);
							yesNoDontcare.setStylePrimaryName("s2-3stateYes");
						}
						break;
					case CHECK_MORNING_7:
						if(dayPreference.isNoMorning7()) {
							yesNoDontcare.setYesNoValue(1);
							yesNoDontcare.setStylePrimaryName("s2-3stateNo");
						}
						if(dayPreference.isYesMorning7()) {
							yesNoDontcare.setYesNoValue(2);
							yesNoDontcare.setStylePrimaryName("s2-3stateYes");
						}
						break;
					case CHECK_MORNING_8:
						if(dayPreference.isNoMorning8()) {
							yesNoDontcare.setYesNoValue(1);
							yesNoDontcare.setStylePrimaryName("s2-3stateNo");
						}
						if(dayPreference.isYesMorning8()) {
							yesNoDontcare.setYesNoValue(2);
							yesNoDontcare.setStylePrimaryName("s2-3stateYes");
						}
						break;
					case CHECK_AFTERNOON:
						if(dayPreference.isNoAfternoon()) {
							yesNoDontcare.setYesNoValue(1);
							yesNoDontcare.setStylePrimaryName("s2-3stateNo");
						}
						if(dayPreference.isYesAfternoon()) {
							yesNoDontcare.setYesNoValue(2);
							yesNoDontcare.setStylePrimaryName("s2-3stateYes");
						}
						break;
					case CHECK_NIGHT:
						if(dayPreference.isNoNight()) {
							yesNoDontcare.setYesNoValue(1);
							yesNoDontcare.setStylePrimaryName("s2-3stateNo");
						}
						if(dayPreference.isYesNight()) {
							yesNoDontcare.setYesNoValue(2);
							yesNoDontcare.setStylePrimaryName("s2-3stateYes");
						}
						break;
					}
				}
				employeePrefsTable.setWidget(r, c, yesNoDontcare);
				employeeButtons.add(yesNoDontcare);
			}
		}
		preferenceButtons.put(employee.getKey(), employeeButtons);
		
		table.setWidget(numRows, 1, employeePrefsTable);		
	}

	public void refresh(PeriodPreferences result) {
		if(result==null) {
			setVisible(false);
			return;
		} else {
			setVisible(true);
		}
		
		objectToRia(result);
	}
	
	public void setSortingCriteria(TableSortCriteria criteria, boolean sortIsAscending) {
		this.sortCriteria=criteria;
		this.sortIsAscending=sortIsAscending;
	}

	public TableSortCriteria getSortingCriteria() {
		return sortCriteria;
	}

	public boolean isSortAscending() {
		return sortIsAscending;
	}
	
	private void objectToRia(PeriodPreferences periodPreferences) {
		this.preferences=periodPreferences;
		
		yearListBox.setSelectedIndex(periodPreferences.getYear()-YEAR);
		monthListBox.setSelectedIndex(periodPreferences.getMonth()-1);
		lastMonthEditorListBox.clear();
		int idx=-1;
		for(Employee ee:ctx.getState().getEmployees()) {
			if(ee.isEditor()) {
				lastMonthEditorListBox.addItem(""+ee.getFullName(),ee.getKey());		
			}
			if(ee.getKey().equals(periodPreferences.getLastMonthEditor())) {
				idx=lastMonthEditorListBox.getItemCount()-1;
			}
		}
		if(idx>=0) {
			lastMonthEditorListBox.setSelectedIndex(idx);			
		}
		
		refreshPreferencesTable(preferencesTable);
	}

	private void riaToObject() {
		if(preferences!=null) {
			preferences.setYear(Integer.parseInt(yearListBox.getValue(yearListBox.getSelectedIndex())));
			preferences.setMonth(Integer.parseInt(monthListBox.getValue(monthListBox.getSelectedIndex())));
		}

		if(preferences.getEmployeeToPreferences()!=null) {
			preferences.getEmployeeToPreferences().clear();			
		} else {
			preferences.setEmployeeToPreferences(new HashMap<String,EmployeePreferences>());
		}
		for(Employee e:ctx.getState().getEmployees()) {
			EmployeePreferences ep=new EmployeePreferences();
			ep.setShiftsLimit(shiftLimitTextBoxes.get(e.getKey()).getShiftLimit());
			List<DayPreference> dps=new ArrayList<DayPreference>();
			ep.setPreferences(dps);
			
			List<YesNoDontcareButton> employeeButtons=preferenceButtons.get(e.getKey());
			for(int c=1; c<=preferences.getMonthDays(); c++) {
				DayPreference dayPreference=null;
				for(int r=1; r<=6; r++) {
					YesNoDontcareButton yesNoDontcareButton = employeeButtons.get((c-1)*6+(r-1));
					if(yesNoDontcareButton.getYesNoValue()>0) {
						if(dayPreference==null) {
							dayPreference=new DayPreference();
							dayPreference.setKey(yesNoDontcareButton.getKey());
							dayPreference.setYear(preferences.getYear());
							dayPreference.setMonth(preferences.getMonth());
							dayPreference.setDay(c);
						}
						switch(r) {
						case CHECK_DAY:
							switch(yesNoDontcareButton.getYesNoValue()) {
							case 3:
								dayPreference.setNoDay(true);
								dayPreference.setHoliDay(true);
								break;
							case 2:
								dayPreference.setYesDay(true);
								break;
							case 1:
								dayPreference.setNoDay(true);
								break;
							default:
								break;
							}
							break;
						case CHECK_MORNING_6:
							switch(yesNoDontcareButton.getYesNoValue()) {
							case 2:
								dayPreference.setYesMorning6(true);
								break;
							case 1:
								dayPreference.setNoMorning6(true);
								break;
							default:
								break;
							}
							break;
						case CHECK_MORNING_7:
							switch(yesNoDontcareButton.getYesNoValue()) {
							case 2:
								dayPreference.setYesMorning7(true);
								break;
							case 1:
								dayPreference.setNoMorning7(true);
								break;
							default:
								break;
							}
							break;
						case CHECK_MORNING_8:
							switch(yesNoDontcareButton.getYesNoValue()) {
							case 2:
								dayPreference.setYesMorning8(true);
								break;
							case 1:
								dayPreference.setNoMorning8(true);
								break;
							default:
								break;
							}
							break;
						case CHECK_AFTERNOON:
							switch(yesNoDontcareButton.getYesNoValue()) {
							case 2:
								dayPreference.setYesAfternoon(true);
								break;
							case 1:
								dayPreference.setNoAfternoon(true);
								break;
							default:
								break;
							}
							break;
						case CHECK_NIGHT:
							switch(yesNoDontcareButton.getYesNoValue()) {
							case 2:
								dayPreference.setYesNight(true);
								break;
							case 1:
								dayPreference.setNoNight(true);
								break;
							default:
								break;
							}
							break;
						}
					}
				}
				if(dayPreference!=null) {
					dps.add(dayPreference);
				}
			}			
			preferences.getEmployeeToPreferences().put(e.getKey(), ep);			
		}
	}

	public void handleSolve(final RiaContext ctx, final boolean shuffle, final boolean partialSolution) {
		if(preferences!=null) {
			ctx.getStatusLine().showProgress(ctx.getI18n().solvingShifts());
			PeriodSolution solution;
			try {
				Employee[] employees = ctx.getState().getEmployees();
				if(shuffle) {
					Utils.shuffleArray(employees);						
				}
				solution = ctx.getSolver().solve(Arrays.asList(employees), preferences, partialSolution);							
				if(solution!=null) {
					ctx.getStatusLine().showInfo(i18n.solutionFound());
					ctx.getSolutionPanel().refresh(solution);
					ctx.getRia().showSolutionViewPanel();  			
				} else {
					ctx.getStatusLine().showError(i18n.noSolutionExists());
					ctx.getSolverNoSolutionPanel().refresh(
							ctx.getSolver().getFailedWithEmployeeAllocations(),
							1,
							preferences);
					ctx.getRia().showSolverNoSolutionPanel();
				}		    			
			} catch(ShiftSolverException e) {
				ctx.getStatusLine().showError(e.getMessage());
				ctx.getSolverNoSolutionPanel().refresh(
						e.getFailedOnEmloyeeAllocations(),
						e.getFailedOnDay(),
						preferences);
				ctx.getRia().showSolverNoSolutionPanel();
			} catch(RuntimeException e) {
				ctx.getStatusLine().showError(i18n.solverFailed()+": "+e.getMessage());
				ctx.getRia().showPeriodPreferencesEditPanel();
			}
		}
	}

	private void handleDateListboxChange() {
		int y=yearListBox.getSelectedIndex()+YEAR;
		int m=monthListBox.getSelectedIndex()+1;
		PeriodPreferences p=new PeriodPreferences(y,m);
		ctx.getService().setDaysWorkdaysStartDay(p, new AsyncCallback<PeriodPreferences>() {					
			@Override
			public void onSuccess(PeriodPreferences result) {
				preferences.setMonthDays(result.getMonthDays());
				preferences.setStartWeekDay(result.getStartWeekDay());
				preferences.setMonthWorkDays(result.getMonthWorkDays());
				for(String ek:result.getEmployeeToPreferences().keySet()) {
					result.getEmployeeToPreferences()
						.get(ek)
							.setShiftsLimit(
									EmployeeAllocation.calculateShiftToGet(ctx.getState().getEmployee(ek),result));
				}
				refresh(preferences);
			}
			@Override
			public void onFailure(Throwable caught) {
				ctx.getStatusLine().showError(i18n.unableToDetermineMonthProperties());
			}
		});
	}

	public void print(boolean visible) {
		buttonPanel.setVisible(visible);
	}	
}
