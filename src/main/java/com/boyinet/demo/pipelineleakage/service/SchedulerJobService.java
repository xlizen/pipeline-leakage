package com.boyinet.demo.pipelineleakage.service;

import com.boyinet.demo.pipelineleakage.bean.primary.SchedulerJob;
import com.boyinet.demo.pipelineleakage.common.R;
import com.boyinet.demo.pipelineleakage.repository.primary.SchedulerJobRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SchedulerJobService {

    private final SchedulerJobRepository schedulerJobRepository;

    public SchedulerJobService(SchedulerJobRepository schedulerJobRepository) {
        this.schedulerJobRepository = schedulerJobRepository;
    }

    public R<List<SchedulerJob>> findAll(PageRequest pageRequest) {
        List<SchedulerJob> list = new ArrayList<>(pageRequest.getPageSize());
        long count = schedulerJobRepository.count();
        Iterable<SchedulerJob> all = schedulerJobRepository.findAll(null, pageRequest);
        all.forEach(list::add);
        R<List<SchedulerJob>> ok = R.ok(list);
        ok.setTotal(count);
        ok.setPage(pageRequest);
        return ok;
    }
}
