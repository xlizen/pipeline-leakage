package com.boyinet.demo.pipelineleakage.util;

import com.boyinet.demo.pipelineleakage.corn.CalcJob;
import com.boyinet.demo.pipelineleakage.vo.BasicScheduleJob;
import org.quartz.*;
import org.quartz.Trigger.TriggerState;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.quartz.JobKey.jobKey;
import static org.quartz.TriggerKey.triggerKey;

public abstract class SchedulerUtils {

    private static final Logger log = LoggerFactory.getLogger(SchedulerUtils.class);

    private static Scheduler scheduler;

    public static void setScheduler(Scheduler scheduler) {
        SchedulerUtils.scheduler = scheduler;
    }

    public List<BasicScheduleJob> getAllJob() throws SchedulerException {
        List<BasicScheduleJob> jobs = new ArrayList();

        for (String group : scheduler.getJobGroupNames()) {

            for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.groupEquals(group))) {
                BasicScheduleJob job = new BasicScheduleJob(jobKey.getName(), jobKey.getGroup());

                List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
                Trigger trigger = triggers.iterator().next();

                TriggerState triggerState = scheduler.getTriggerState(trigger.getKey());
                job.setState(triggerState);
                if (trigger instanceof CronTrigger) {
                    String cronExpression = ((CronTrigger) trigger).getCronExpression();
                    job.setCronExpression(cronExpression);
                }
                jobs.add(job);
            }
        }
        return jobs;
    }

    public List<BasicScheduleJob> getRunningJob() throws SchedulerException {
        List<JobExecutionContext> executingJobs = scheduler.getCurrentlyExecutingJobs();
        List<BasicScheduleJob> jobs = new ArrayList(executingJobs.size());

        for (JobExecutionContext executingJob : executingJobs) {
            JobKey jobKey = executingJob.getJobDetail().getKey();
            BasicScheduleJob job = new BasicScheduleJob(jobKey.getName(), jobKey.getGroup());

            Trigger trigger = executingJob.getTrigger();

            TriggerState triggerState = scheduler.getTriggerState(trigger.getKey());
            job.setState(triggerState);
            if (trigger instanceof CronTrigger) {
                String cronExpression = ((CronTrigger) trigger).getCronExpression();
                job.setCronExpression(cronExpression);
            }
            jobs.add(job);
        }
        return jobs;
    }

    public static void fillJobInfo(BasicScheduleJob job) throws SchedulerException {
        TriggerKey triggerKey = triggerKey(job.getJobName(), job.getJobGroup());

        TriggerState triggerState = scheduler.getTriggerState(triggerKey);
        job.setState(triggerState);

        Trigger trigger = scheduler.getTrigger(triggerKey);

        if (trigger != null && trigger instanceof CronTrigger) {
            String cronExpression = ((CronTrigger) trigger).getCronExpression();
            job.setCronExpression(cronExpression);
        }
    }

    public static void createJob(String name, String group, String cronExpression) throws SchedulerException {
        TriggerKey triggerKey = triggerKey(name, group);
        if (scheduler.checkExists(triggerKey))
            return;

        Class<? extends Job> jobClass = CalcJob.class;

        JobDetail jobDetail = JobBuilder.newJob(jobClass).withIdentity(jobKey(name, group)).build();

//        withMisfireHandlingInstructionDoNothing：不触发立即执行，等待下次调度；
//        withMisfireHandlingInstructionIgnoreMisfires：以错过的第一个频率时间立刻开始执行；
//        withMisfireHandlingInstructionFireAndProceed：以当前时间为触发频率立刻触发一次执行；
        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cronExpression).withMisfireHandlingInstructionDoNothing();
        CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity(triggerKey).withSchedule(scheduleBuilder).build();

        scheduler.scheduleJob(jobDetail, trigger);
    }

    public static void createJob(BasicScheduleJob scheduleJob) throws SchedulerException {
        createJob(scheduleJob.getJobName(), scheduleJob.getJobGroup(), scheduleJob.getCronExpression());
    }

    public static void updateCron(String name, String group, String cronExpression) throws SchedulerException {
        TriggerKey triggerKey = triggerKey(name, group);
        if (!scheduler.checkExists(triggerKey))
            return;

        CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);
        if (trigger != null) {

            if (trigger.getCronExpression().equals(cronExpression))
                return;

            CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cronExpression).withMisfireHandlingInstructionDoNothing();
            trigger = trigger.getTriggerBuilder().withIdentity(triggerKey).withSchedule(scheduleBuilder).build();

            scheduler.rescheduleJob(triggerKey, trigger);
        } else {

            CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cronExpression).withMisfireHandlingInstructionDoNothing();
            trigger = TriggerBuilder.newTrigger().withIdentity(triggerKey).withSchedule(scheduleBuilder).build();

            JobDetail jobDetail = scheduler.getJobDetail(jobKey(name, group));

            HashSet<Trigger> triggerSet = new HashSet(1);
            triggerSet.add(trigger);

            scheduler.scheduleJob(jobDetail, triggerSet, true);
        }
    }

    public static void updateCron(BasicScheduleJob scheduleJob) throws SchedulerException {
        updateCron(scheduleJob.getJobName(), scheduleJob.getJobGroup(), scheduleJob.getCronExpression());
    }

    public static boolean delete(String name, String group) {
        TriggerKey triggerKey = triggerKey(name, group);
        try {
            if (scheduler.checkExists(triggerKey))
                scheduler.unscheduleJob(triggerKey);
            return true;
        } catch (SchedulerException e) {
            log.warn("删除任务失败", e);
            return false;
        }
    }

    public static boolean runOnce(String name, String group) {
        try {
            if (!scheduler.checkExists(triggerKey(name, group)))
                return false;
            scheduler.triggerJob(jobKey(name, group));
            return true;
        } catch (SchedulerException e) {
            log.warn("运行任务失败", e);
            return false;
        }
    }

    public static boolean pauseJob(String name, String group) {
        try {
            if (scheduler.checkExists(triggerKey(name, group)))
                scheduler.pauseJob(jobKey(name, group));
            return true;
        } catch (SchedulerException e) {
            log.warn("暂停任务失败", e);
            return false;
        }
    }

    public static boolean resumeJob(String name, String group) {
        try {
            if (!scheduler.checkExists(triggerKey(name, group)))
                return false;
            scheduler.resumeJob(jobKey(name, group));
            return true;
        } catch (SchedulerException e) {
            log.warn("恢复任务失败", e);
            return false;
        }
    }

    public static boolean checkExists(String name, String group) throws SchedulerException {
        return scheduler.checkExists(triggerKey(name, group));
    }
}