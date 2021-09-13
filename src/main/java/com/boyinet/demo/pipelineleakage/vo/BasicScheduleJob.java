package com.boyinet.demo.pipelineleakage.vo;

import org.quartz.Trigger.TriggerState;

public class BasicScheduleJob {

    private String jobName;

    private String jobGroup;

    private String cronExpression;

    private TriggerState state;

    public BasicScheduleJob() {
    }

    public BasicScheduleJob(String jobName, String jobGroup) {
        this.jobName = jobName;
        this.jobGroup = jobGroup;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getJobGroup() {
        return jobGroup;
    }

    public void setJobGroup(String jobGroup) {
        this.jobGroup = jobGroup;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public TriggerState getState() {
        return state;
    }

    public void setState(TriggerState state) {
        this.state = state;
    }
}