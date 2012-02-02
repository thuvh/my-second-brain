package org.brain2.test.scheduling;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.tiling.scheduling.Scheduler;
import org.tiling.scheduling.SchedulerTask;
import org.tiling.scheduling.examples.iterators.DailyIterator;

import com.vnexpress.parser.VneMapiParser;

public class AlarmClock {
	
	
	private SchedulerTask theTask = new SchedulerTask() {
        public void run() {
            soundAlarm();
        }
        private void soundAlarm() {
        	//parseArticleFromAPI(1002243731);
    		long s = System.currentTimeMillis();
    		int n = VneMapiParser.notifyParserAllCategories();
    		System.out.println("notifyParserAllCategories: " + n);
    		long diff = System.currentTimeMillis() - s;
    		System.out.println("diff: " + diff);
        }
    };

    private final Scheduler scheduler = new Scheduler();
    private final SimpleDateFormat dateFormat =
        new SimpleDateFormat("dd MMM yyyy HH:mm:ss.SSS");
    private final int hourOfDay, minute, second;

    public AlarmClock(int hourOfDay, int minute, int second) {
        this.hourOfDay = hourOfDay;
        this.minute = minute;
        this.second = second;
    }

    public void start() {
        scheduler.schedule(theTask, new DailyIterator(hourOfDay, minute, second));
    }
    
    

    public static void main(String[] args) {
    	long delay = 10000;   // delay for 5 sec.
    	long period = 1000;  // repeat every sec.
    	Timer timer = new Timer();

    	timer.scheduleAtFixedRate(new TimerTask() {			
			@Override
			public void run() {
			    SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss.SSS");
				System.out.println("#notifyParserAllCategories up! " +  "It's " + dateFormat.format(new Date()));		
				
				//parseArticleFromAPI(1002243731);
	    		long s = System.currentTimeMillis();
	    		int n = VneMapiParser.notifyParserAllCategories();
	    		System.out.println("notifyParserAllCategories: " + n);
	    		long diff = System.currentTimeMillis() - s;
	    		System.out.println("diff: " + diff);
			}
		}, delay, period*180);//3 minutes

    }
}