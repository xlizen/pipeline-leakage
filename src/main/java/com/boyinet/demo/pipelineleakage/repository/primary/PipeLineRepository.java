package com.boyinet.demo.pipelineleakage.repository.primary;

import com.boyinet.demo.pipelineleakage.bean.primary.PipeLine;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

/**
 * @author lengchunyun
 */
public interface PipeLineRepository extends CrudRepository<PipeLine, Long>,
        JpaSpecificationExecutor<PipeLine> {
}
