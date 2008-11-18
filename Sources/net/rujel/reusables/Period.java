//  Period.java

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

import java.util.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;

public interface Period {
	
	public String typeID();

	public Date begin();
	
	public Date end();
	
	public int countInYear();
	
	public boolean contains(Date date);/* {
		return (date.compareTo(begin()) >= 0 && date.compareTo(end()) <= 0);
	}*/
	
	public Period nextPeriod();
	
	//Implementations
	
	public static class ByDates implements Period {
		private Date begin;
		private Date end;
		
		public ByDates(Date begin, Date end) {
			this.begin = begin;
			this.end = end;
		}
		
		private static final String typeID = "Period.ByDates";
		public String typeID() {
			return typeID;
		}
		
		public Date begin() {
			return begin;
		}
		public Date end() {
			return end;
		}
		public boolean contains(Date date) {
			return (date.compareTo(begin()) >= 0 && date.compareTo(end()) <= 0);
		}
		
		public int countInYear() {
			return 0;
		}
		public Period nextPeriod() {
			return null;
		}
	}
	
	public static class Day implements Period {
		private Calendar today;
		
		private Day() {
			;
		}
		
		public Day (Date date) {
			today = new GregorianCalendar();
			today.setTime(date);
		}
		
		private static final String typeID = "Period.Day";
		public String typeID() {
			return typeID;
		}

		public Date begin() {
			Calendar begin = (Calendar)today.clone();
			begin.set(Calendar.HOUR,0);
			begin.set(Calendar.MINUTE,0);
			begin.set(Calendar.SECOND,0);
			return begin.getTime();
		}
		
		public Date end() {
			Calendar end = (Calendar)today.clone();
			end.set(Calendar.HOUR,23);
			end.set(Calendar.MINUTE,59);
			end.set(Calendar.SECOND,59);
			return end.getTime();
		}
		
		public int countInYear() {
			return 365;
		}
		
		public boolean contains(Date date) {
			Calendar check = new GregorianCalendar();
			check.setTime(date);
			return ((today.get(Calendar.YEAR) == check.get(Calendar.YEAR)) && (today.get(Calendar.DAY_OF_YEAR) == check.get(Calendar.DAY_OF_YEAR)));
		}
			
		public Day nextPeriod() {
			Calendar newCal = (Calendar)today.clone();
			newCal.add(Calendar.DATE,1);
			Day newDay = new Day();
			newDay.today = newCal;
			return newDay;
		}
	}
	
	public static class Week implements Period {
		private Calendar firstday;
		
		private Week() {
			;
		}
		
		public Week (Date date) {
			firstday = new GregorianCalendar();
			firstday.setTime(date);
			firstday.set(Calendar.DAY_OF_WEEK,1);
			firstday.set(Calendar.HOUR,0);
			firstday.set(Calendar.MINUTE,0);
			firstday.set(Calendar.SECOND,0);
		}
		
		private static final String typeID = "Period.Week";
		public String typeID() {
			return typeID;
		}

		public Date begin() {
			return firstday.getTime();
		}
		
		public Date end() {
			Calendar end = (Calendar)firstday.clone();
			end.set(Calendar.DAY_OF_WEEK,7);
			end.set(Calendar.HOUR,23);
			end.set(Calendar.MINUTE,59);
			end.set(Calendar.SECOND,59);
			return end.getTime();
		}
		
		public int countInYear() {
			return 52;
		}
		
		public boolean contains(Date date) {
			Calendar check = new GregorianCalendar();
			check.setTime(date);
			return ((firstday.get(Calendar.YEAR) == check.get(Calendar.YEAR)) && (firstday.get(Calendar.WEEK_OF_YEAR) == check.get(Calendar.WEEK_OF_YEAR)));
		}
		
		public Week nextPeriod() {
			Calendar newCal = (Calendar)firstday.clone();
			newCal.add(Calendar.WEEK_OF_YEAR,1);
			Week newWeek = new Week();
			newWeek.firstday = newCal;
			return newWeek;
		}
	}

	public static class Month implements Period {
		private Calendar firstday;
		
		private Month() {
			;
		}
		
		public Month (Date date) {
			firstday = new GregorianCalendar();
			firstday.setTime(date);
			firstday.set(Calendar.DAY_OF_WEEK,1);
			firstday.set(Calendar.HOUR,0);
			firstday.set(Calendar.MINUTE,0);
			firstday.set(Calendar.SECOND,0);
		}
		
		private static final String typeID = "Period.Month";
		public String typeID() {
			return typeID;
		}

		public Date begin() {
			return firstday.getTime();
		}
		
		public Date end() {
			Calendar end = (Calendar)firstday.clone();
			end.add(Calendar.MONTH,1);
			end.add(Calendar.SECOND,-1);
			return end.getTime();
		}
		
		public int countInYear() {
			return 12;
		}
		
		public boolean contains(Date date) {
			Calendar check = new GregorianCalendar();
			check.setTime(date);
			return ((firstday.get(Calendar.YEAR) == check.get(Calendar.YEAR)) && (firstday.get(Calendar.MONTH) == check.get(Calendar.MONTH)));
		}
		
		public Month nextPeriod() {
			Calendar newCal = (Calendar)firstday.clone();
			newCal.add(Calendar.MONTH,1);
			Month newMonth = new Month();
			newMonth.firstday = newCal;
			return newMonth;
		}
	}
	
}
