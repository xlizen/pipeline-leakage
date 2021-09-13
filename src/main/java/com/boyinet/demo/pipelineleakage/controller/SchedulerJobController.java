package com.boyinet.demo.pipelineleakage.controller;

import com.boyinet.demo.pipelineleakage.bean.primary.SchedulerJob;
import com.boyinet.demo.pipelineleakage.common.R;
import com.boyinet.demo.pipelineleakage.service.SchedulerJobService;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("schedulerJob")
public class SchedulerJobController {

    private final SchedulerJobService schedulerJobService;

    public SchedulerJobController(SchedulerJobService schedulerJobService) {
        this.schedulerJobService = schedulerJobService;
    }

    @RequestMapping("list")
    R<List<SchedulerJob>> list(Integer page, Integer limit) {
        PageRequest pageRequest = PageRequest.of(page - 1, limit);
        return schedulerJobService.findAll(pageRequest);
    }


}
