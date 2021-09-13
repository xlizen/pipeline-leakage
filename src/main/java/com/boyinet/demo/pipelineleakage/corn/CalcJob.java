package com.boyinet.demo.pipelineleakage.corn;

import com.boyinet.demo.pipelineleakage.scheduler.CalcTrigger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;

public class CalcJob implements Job {

    private static CalcTrigger calcTrigger;

    public static void setCalcTrigger(CalcTrigger calcTrigger) {
        CalcJob.calcTrigger = calcTrigger;
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        JobKey jobKey = jobExecutionContext.getTrigger().getJobKey();
        calcTrigger.trigger(jobKey.getGroup(),jobKey.getName());
    }
}
