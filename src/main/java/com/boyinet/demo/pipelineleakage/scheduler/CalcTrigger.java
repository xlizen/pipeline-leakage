package com.boyinet.demo.pipelineleakage.scheduler;

import com.boyinet.demo.pipelineleakage.bean.primary.SchedulerJob;
import com.boyinet.demo.pipelineleakage.repository.primary.SchedulerJobRepository;
import com.boyinet.demo.pipelineleakage.service.LeakageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class CalcTrigger {

    @Autowired
    LeakageService leakageService;

    @Autowired
    SchedulerJobRepository schedulerJobRepository;

    public void trigger(String group, String name) {
        SchedulerJob job = schedulerJobRepository.findSchedulerJobByGroupAndName(group, name);
        if (null != job) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            leakageService.load(job.getPipeLineId(), dateFormat.format(new Date()));
        }
    }
}
