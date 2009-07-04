//  Scheduler.java

/*
 * Copyright (c) 2008, Gennady & Michael Kushnir
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 * 
 * 	•	Redistributions of source code must retain the above copyright notice, this
 * 		list of conditions and the following disclaimer.
 * 	•	Redistributions in binary form must reproduce the above copyright notice,
 * 		this list of conditions and the following disclaimer in the documentation
 * 		and/or other materials provided with the distribution.
 * 	•	Neither the name of the RUJEL nor the names of its contributors may be used
 * 		to endorse or promote products derived from this software without specific 
 * 		prior written permission.
 * 		
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.rujel.reusables;

import java.lang.reflect.Method;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.*;

@Deprecated
public class Scheduler {
	protected static Logger logger = Logger.getLogger("sheduler");
	//protected static final Preferences prefs = Preferences.systemNodeForPackage(Scheduler.class);
	protected SettingsReader settings;
	
	private static Scheduler sharedInstance;
	
	protected Timer timer;
	/*
	 protected Vector dailyTasks;
	 protected Vector weeklyTasks;
	 protected Vector monthlyTasks;*/
	
	protected Period[] periods;
	protected Vector[] periodTasks;
	
	public static final long DAY = 24*60*60*1000;
	public static final int DAILY = 365;
	public static final int WEEKLY = 52;
	public static final int MONTHLY = 12;
	public static final Object[] THIS_PERIOD = new Object[0];
	
	protected static String nodeName(int period) {
		switch (period) {
			case DAILY:
				return "DAILY";
			case WEEKLY:
				return "WEEKLY";
			case MONTHLY:
				return "MONTHLY";
			default:
				return "other";
		}
		//		return null;
	}
	
	
	protected Date schedulePeriodFirstRun (int period) {
		String nodeName = nodeName(period);
		Calendar today = new GregorianCalendar();
		SettingsReader node = settings.subreaderForPath(nodeName,true);
//		Preferences node = prefs.node(nodeName);
		Calendar startAt = (Calendar)today.clone();
		if(period != DAILY && period != WEEKLY && period != MONTHLY) {
			if(periods[period] == null) return null;
			Date end = periods[period].end();
			if(end == null) return null;
			/*Date now = new Date();
			while (now.compareTo(end) > 0) {
				periods[period] = periods[period].nextPeriod();
				if(periods[period] == null) return null;
				end = periods[period].end();
				if(end == null) return null;
			}*/
			startAt.setTime(end);
		}
		/*String onTime = (String)node.valueForKey("time");
		if(onTime == null) onTime = (String)settings.valueForKey("time");*/
		String onTime = node.get("time",settings.get("time",null));
		setTime(startAt,onTime);
		if(period < DAILY) {
			/*String dayT = (String)node.valueForKey("day");
			if(dayT == null) dayT = (String)settings.valueForKey("day");
			int onDay = (dayT == null)?0:Integer.parseInt(dayT);*/
			int onDay = node.getInt("day",settings.getInt("day",0));
			switch (period) {
				case WEEKLY:
					if(onDay == 0) {
						onDay = Calendar.MONDAY;
					} else if(onDay < 0) {
						if(onDay == -1)
							onDay = Calendar.SUNDAY;
						else
							onDay = 9 + onDay;
					}
					startAt.set(Calendar.DAY_OF_WEEK,onDay);
					break;
				case MONTHLY:
					if(onDay > 0)
						startAt.set(Calendar.DAY_OF_MONTH,onDay);
					else {
						startAt.set(Calendar.DAY_OF_MONTH,1);
						if(onDay < 0) {
							startAt.add(Calendar.MONTH,1);
							startAt.add(Calendar.DATE,onDay);
						}
					}
					break;
				default:
					startAt.add(Calendar.DAY_OF_YEAR,onDay);
					break;
			}
		}
		if(onTime == null) {
			startAt.add(Calendar.DATE,1);
		}
		if(startAt.compareTo(today) <= 0) {
			if(period == DAILY || period == WEEKLY || period == MONTHLY) {
				int field = Calendar.DATE;
				if(period == WEEKLY)
					field = Calendar.WEEK_OF_YEAR;
				else if (period == MONTHLY)
					field = Calendar.MONTH;
				startAt.add(field,1);
			} else {
				periods[period] = periods[period].nextPeriod();
				if(periods[period] == null) return null;
				return schedulePeriodFirstRun(period);
			}
		}
		return startAt.getTime();
	}
	
	protected Scheduler() {
		super();
		timer = new Timer(true);
		//try {
		
		//DAILY
		settings = SettingsReader.settingsForPath("schedule",false);
		if(settings == null) {
			settings = SettingsReader.DUMMY;
			logger.log(WOLogLevel.INFO,"No scheduling settings found, using default values.");
		}
		Object node = settings.valueForKeyPath("DAILY.tasks");
		/*Preferences node = prefs.node("DAILY");
		if(node.nodeExists("tasks"))
			registerNode(DAILY,node.node("tasks"));*/
		if(node != null && node instanceof SettingsReader)
			registerSettings(DAILY,(SettingsReader)node);
		Date firstRun = schedulePeriodFirstRun(DAILY);
		timer.scheduleAtFixedRate(new ScheduledTask(DAILY),firstRun,DAY);
		logger.logp(Level.CONFIG,"Scheduler","<init>","Scheduled to run DAILY since " + firstRun);
		
		//WEEKLY
		node =  settings.valueForKeyPath("WEEKLY.tasks");
		/*prefs.node("WEEKLY");
		if(node.nodeExists("tasks"))
			registerNode(WEEKLY,node.node("tasks"));*/
		if(node != null && node instanceof SettingsReader)
			registerSettings(WEEKLY,(SettingsReader)node);
		firstRun = schedulePeriodFirstRun(WEEKLY);
		timer.scheduleAtFixedRate(new ScheduledTask(WEEKLY),firstRun,DAY*7);
		logger.logp(Level.CONFIG,"Scheduler","<init>","Scheduled to run WEEKLY since " + firstRun);
		
		//MONTHLY
		node =  settings.valueForKeyPath("MONTHLY.tasks");
		/*prefs.node("MONTHLY");
		if(node.nodeExists("tasks"))
			registerNode(MONTHLY,node.node("tasks"));*/
		if(node != null && node instanceof SettingsReader)
			registerSettings(MONTHLY,(SettingsReader)node);
		firstRun = schedulePeriodFirstRun(MONTHLY);
		timer.schedule(new ScheduledTask(MONTHLY),firstRun);
		logger.logp(Level.CONFIG,"Scheduler","<init>","Scheduled to run MONTHLY since " + firstRun);
		/*} catch (BackingStoreException ex) {
			logger.logp(Level.WARNING,"Scheduler","<init>","Failed to read Scheduling preferences",ex);
		}*/
	}
	
	public static Scheduler sharedInstance() {
		if(sharedInstance == null) {
			sharedInstance = new Scheduler();
		} else {
			sharedInstance.settings.refresh();
		}
		return sharedInstance;
	}
	
	public static long millisFromTime(String time) {
		int minutes = 0;
		int colon = time.indexOf(':');
		if(colon > 0) {
			int hours = Integer.parseInt(time.substring(0,colon));
			minutes = Integer.parseInt(time.substring(colon + 1));
			minutes = minutes + hours*60;
		} else {
			minutes = Integer.parseInt(time);
		}
		return ((long)minutes)*60000;
	}
	
	protected static void setTime(Calendar cal,String time) {
		int minutes = 0;
		int hour = 0;
		if(time != null) {
			int colon = time.indexOf(':');
			if(colon > 0) {
				hour = Integer.parseInt(time.substring(0,colon));
				minutes = Integer.parseInt(time.substring(colon + 1));
			} else {
				hour = Integer.parseInt(time);
			}
		}
		cal.set(Calendar.HOUR_OF_DAY,hour);
		cal.set(Calendar.MINUTE,minutes);
		cal.set(Calendar.SECOND,0);
	}
	
	protected void initPeriods() {
		if(periods == null) {
			periods = new Period[366];
			Date today = new Date();
			periods[DAILY] = new Period.Day(today);
			periods[WEEKLY] = new Period.Week(today);
			periods[MONTHLY] = new Period.Month(today);
		}
	}
	
	public int registerPeriod(Period per) {
		int count = per.countInYear();
		try {
			registerPeriod(per,count);
			return count;
		} catch (Exception ex) {
			logger.logp(Level.CONFIG,"Scheduler","registerPeriod","Could not register period at default position [" + count + ']',ex);
		}
		for (int i = 364; i > 1; i--) {
			if(periods[i] == null) {
				periods[i] = per;
				Date firstRun = schedulePeriodFirstRun(i);
				if(firstRun != null) {
					timer.schedule(new ScheduledTask(i),firstRun);
					logger.logp(Level.CONFIG,"Scheduler","registerPeriod","Registered period [" + i + "] to first run at " + firstRun);
				} else {
					logger.logp(Level.WARNING,"Scheduler","registerPeriod","Could not schedule execution of period [" + i + ']',per);
				}
				return i;
			}
		}
		return -1;
	}
	
	public void registerPeriod(Period per, int perIndex) throws Exception {
		initPeriods();
		if(periods[perIndex] != null) {
			logger.logp(Level.CONFIG,"Scheduler","registerPeriod","Failed to register period, at position [" + perIndex + "] is occupied");
			throw new Exception("Specified position [" + perIndex + "] is already occupied");
		}
		periods[perIndex] = per;
		Date firstRun = schedulePeriodFirstRun(perIndex);
		if(firstRun != null) {
			timer.schedule(new ScheduledTask(perIndex),firstRun);
			logger.logp(Level.CONFIG,"Scheduler","registerPeriod","Registered period [" + perIndex + "] to first run at " + firstRun);
		} else {
			logger.logp(Level.WARNING,"Scheduler","registerPeriod","Could not schedule execution of period [" + perIndex + ']',per);
		}
	}
	
	protected void registerSettings(int period,SettingsReader node) {
		Enumeration enu = node.keyEnumerator();
		while (enu.hasMoreElements()) {
			String className = (String)enu.nextElement();
			String methodName = (String)node.valueForKey(className);
			try {
				Class cl = Class.forName(className);
				Method mt = cl.getMethod(methodName,(Class[])null);
				registerTask(period,mt,null,null,null);
			} catch (Throwable th) {
				String message = "Error registering " + methodName + " in " + className + " for " + nodeName(period) + " [" + period + ']';
				logger.logp(Level.WARNING,"Scheduler","registerNode",message,th);
			}			
		}
	}
	/*
	protected void registerMap(int period,Map map) throws BackingStoreException {
		String[] classes = (String[])map.keySet().toArray();//node.keys();
		if(classes != null && classes.length > 0) {
			logger.logp(Level.CONFIG,"Scheduler","registerNode","Registering tasks to run period " + nodeName(period) + " [" + period + ']');
			for (int i = 0; i < classes.length; i++) {
				String methodName = (String)map.get(classes[i]);
				try {
					Class cl = Class.forName(classes[i]);
					Method mt = cl.getMethod(methodName,(Class[])null);
//					mt.invoke(null,(Object[])null);
					//String id = node.get("id",null);
					registerTask(period,mt,null,null,null);
				} catch (Throwable th) {
					String message = "Error registering " + methodName + " in " + classes[i] + " for " + nodeName(period) + " [" + period + ']';
					logger.logp(Level.WARNING,"Scheduler","registerNode",message,th);
				}
			}
		}
	}
	*/
	public void unregisterPeriod(int period) {
		periodTasks[period] = null;
		periods[period] = null;
	}
	
	public int numForID(String id) {
		if(id == null) return -1;
		for (int i = 0; i < periods.length; i++) {
			if(periods[i] != null && id.equals(periods[i].typeID()))
				return i;
		}
		return -1;
	}
	
	public int numForPeriod(Period per) {
		if(per == null)
			return -1;
		if(per instanceof Period.Day) {
			return DAILY;
		} else if(per instanceof Period.Week) {
			return WEEKLY;
		} else if(per instanceof Period.Month) {
			return MONTHLY;
		} else {
			if(periods == null) return -1;
			if(periodsAreEqual(periods[per.countInYear()],per))
				return per.countInYear();
			
			for (int i = 364; i >= 0; i--) {
				if(periodsAreEqual(periods[i],per))
					return i;
			}
			return -1;
		}
	}
	
	protected boolean periodsAreEqual(Period per1, Period per2) {
		if(per1 == per2)
			return true;
		if(per1 == null || per2 == null)
			return false;
		if(!per1.getClass().equals(per2.getClass()))
			return false;
		//if(per1.countInYear() != per2.countInYear() || !per1.end().equals(per2.end()) || !per1.begin().equals(per2.begin()))
		if(!per2.typeID().equals(per2.typeID()))
		   return false;
		return true;
	}
	
	public Period periodForNum(int num) {
		if(periods != null)
			return periods[num];
		Date today = new Date();
		switch (num) {
			case DAILY:
				return new Period.Day(today);
			case WEEKLY:
				return new Period.Week(today);
			case MONTHLY:
				return new Period.Month(today);
			default:
				return null;
		}
	}
	
	public Period[] registeredPeriods() {
		if(periods == null)
			return null;
		return periods.clone();
	}
	
	public void registerTask(int period, Method task, Object obj, Object[] args, String id) {
		Hashtable taskDict = new Hashtable(3,1);
		taskDict.put("method",task);
		if(obj != null) taskDict.put("object",obj);
		if(args != null) taskDict.put("args",args);
		if(id != null) taskDict.put("id",id);
		
		if(periodTasks == null) {
			periodTasks = new Vector[366];
		}
		Vector tasks = periodTasks[period];
		if(tasks == null) {
			tasks =  new Vector(2,2);
			periodTasks[period] = tasks;
		}
		tasks.addElement(taskDict);
		/*
		 switch (period) {
			 case DAILY:
				 if(dailyTasks == null)
					 dailyTasks = new Vector(2,2);
				 dailyTasks.addElement(taskDict);
				 break;
			 case WEEKLY:
				 if(weeklyTasks == null)
					 weeklyTasks = new Vector(2,2);
				 weeklyTasks.addElement(taskDict);
				 break;
			 case MONTHLY:
				 if(monthlyTasks == null)
					 monthlyTasks = new Vector(2,2);
				 monthlyTasks.addElement(taskDict);
				 break;
			 default:
				 return;
		 }*/
		String message = "Registered " + task + " with id '" + id + "' to run on perod " + nodeName(period) + " [" + period + ']';
		logger.logp(Level.CONFIG,"Scheduler","registerNode",message);
	}
	
	public boolean cancelTask(int period, String id) {
		if(periodTasks == null || periodTasks[period] == null || periodTasks[period].size() == 0)
			return false;
		/*
		Enumeration enu = periodTasks[period].elements();
		while (enu.hasMoreElements())*/
		for (int i = 0; i < periodTasks[period].size(); i++) {
			Hashtable taskDict = (Hashtable)periodTasks[period].elementAt(i);//enu.nextElement();
			String currID = (String)taskDict.get("id");
			if(id.equals(currID)) {
				periodTasks[period].remove(i);
				return true;
			}
		}
		return false;
	}
	
	public void runPeriod(int period) {
		if(periodTasks == null) {
			logger.logp(Level.INFO,"Scheduler","runPeriod","Found no tasks defined when running scheduled for period " + nodeName(period) + " [" + period + ']');
			return;
		}

		Vector tasks = periodTasks[period];
		/*
		 switch (period) {
			 case DAILY:
				 tasks = dailyTasks;
				 break;
			 case WEEKLY:
				 tasks = weeklyTasks;
				 break;
			 case MONTHLY:
				 tasks = monthlyTasks;
				 break;
			 default:
				 logger.logp(Level.WARNING,"Scheduler","runPeriod","Unknown period " + period);
				 return;
		 }*/
		if(tasks == null || tasks.size() == 0) {
			logger.logp(Level.INFO,"Scheduler","runPeriod","Nothing to execute for " + nodeName(period) + " [" + period + ']');
			return;
		}
		String message = "Executing " + tasks.size() + " tasks for " + nodeName(period) + " [" + period + ']';
		logger.logp(Level.INFO,"Scheduler","runPeriod",message);
		Enumeration tasksEnum = tasks.elements();
		settings.refresh();
		SettingsReader disabled = settings.subreaderForPath("disabled", false);
		if(disabled==null)
			disabled = SettingsReader.DUMMY;
		while (tasksEnum.hasMoreElements()) {
			try {
				Hashtable taskDict = (Hashtable)tasksEnum.nextElement();
				String id = (String)taskDict.get("id");
				if(disabled.getBoolean(id, false))
					continue;
				Method method = (Method)taskDict.get("method");
				Object[] args = (Object[])taskDict.get("args");
				if(args == THIS_PERIOD) {
					initPeriods();
					args = new Object[] {periods[period]};
				}
				method.invoke(taskDict.get("object"),args);
			} catch (Exception ex) {
				message = "Failed to execute one task for " + nodeName(period) + " [" + period + ']';
				logger.logp(Level.WARNING,"Scheduler","runPeriod",message,ex);
			}
		}
		//ModulesInitialiser.initModules(null,periods[period]);
		if(periods != null) {
			periods[period] = periods[period].nextPeriod();
		}
		if(period != DAILY && period != WEEKLY){
			Date reRun = schedulePeriodFirstRun(period);
			if(reRun != null) {
				timer.schedule(new ScheduledTask(period),reRun);
				logger.logp(Level.CONFIG,"Scheduler","runPeriod","Rescheduled period [" + period + "] next run: " + reRun);
			}
		}		
	}
	/*
	 protected TimerTask scheduleTask(int period) {
		 switch (period) {
			 case DAILY:
				 return new TimerTask() {
					 public void run() {
						 runPeriod(DAILY);
					 }
				 };
			 case WEEKLY:
				 return new TimerTask() {
					 public void run() {
						 runPeriod(WEEKLY);
					 }
				 };
			 case MONTHLY:
				 return new TimerTask() {
					 public void run() {
						 runPeriod(MONTHLY);
					 }
				 };
			 default:
				 logger.logp(Level.WARNING,"Scheduler","scheduleTask","Unknown period " + period);
				 break;
		 }
		 return new TimerTask() {
			 public int per = period;
			 public void run() {
				 runPeriod(per);
			 }
		 };
	 }*/
	
	protected class ScheduledTask extends TimerTask {
		protected int per = 0;
		public ScheduledTask(int period) {
			per = period;
		}
		
		public void run() {
			runPeriod(per);
		}
	}
}