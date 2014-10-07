/* 
 * $Id$
 * created by    : yukm
 * creation-date : 2014. 9. 17.
 * =========================================================
 * Copyright (c) 2014 ManinSoft, Inc. All rights reserved.
 */

package net.smartworks.ambient.starter;

import java.util.Properties;

import net.smartworks.ambient.job.AmbientJob;
import net.smartworks.ambient.util.Constant;
import net.smartworks.ambient.util.PropertiesLoader;

import org.apache.log4j.Logger;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;

public class Starter {
	static Logger logger = Logger.getLogger(Starter.class);

	public static void main(String[] args) {
		try {
			logger.info("######## START AMBIENT SCHEDULER ########");
			new Starter();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e, e);
		}
	}
	public Starter() throws Exception{

		Properties prop = PropertiesLoader.loadProp(Constant.PROPERTIES_PATH);
		
		String cronExpress = prop.getProperty("scheduler.cron.express");
		
		SchedulerFactory sf = new StdSchedulerFactory();
		Scheduler sched = sf.getScheduler();
		sched.start();
		JobDetail jd = new JobDetail("ambientJob",sched.DEFAULT_GROUP, AmbientJob.class);
		
		CronTrigger ct = new CronTrigger("ambientCronTrigger", sched.DEFAULT_GROUP, cronExpress);
		//SimpleTrigger st=new SimpleTrigger("mytrigger",sched.DEFAULT_GROUP,new Date(),null,SimpleTrigger.REPEAT_INDEFINITELY,60L*1000L);
		sched.scheduleJob(jd, ct);
	}
}
