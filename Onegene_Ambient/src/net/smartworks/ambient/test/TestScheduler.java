/* 
 * $Id$
 * created by    : yukm
 * creation-date : 2014. 9. 17.
 * =========================================================
 * Copyright (c) 2014 ManinSoft, Inc. All rights reserved.
 */

package net.smartworks.ambient.test;

import java.util.Date;

import net.smartworks.ambient.test.job.TestJob;

import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleTrigger;
import org.quartz.impl.StdSchedulerFactory;

public class TestScheduler {

	public static void main(String[] args) throws Exception {
		new TestScheduler();
	}
	public TestScheduler()throws Exception{
		SchedulerFactory sf=new StdSchedulerFactory();
		Scheduler sched = sf.getScheduler();
		sched.start();
		JobDetail jd=new JobDetail("myjob",sched.DEFAULT_GROUP, TestJob.class);
		
		CronTrigger ct = new CronTrigger("mytrigger", sched.DEFAULT_GROUP, "0/5 * * * * ?");
		//SimpleTrigger st=new SimpleTrigger("mytrigger",sched.DEFAULT_GROUP,new Date(),null,SimpleTrigger.REPEAT_INDEFINITELY,60L*1000L);
		sched.scheduleJob(jd, ct);
	}
}
