package com.boyinet.demo.pipelineleakage.repository.primary;

import com.boyinet.demo.pipelineleakage.bean.primary.SchedulerJob;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

public interface SchedulerJobRepository extends CrudRepository<SchedulerJob, Long>, JpaSpecificationExecutor<SchedulerJob> {


    SchedulerJob findSchedulerJobByGroupAndName(String group, String name);
}
