package com.boyinet.demo.pipelineleakage.repository;

import com.boyinet.demo.pipelineleakage.bean.PipeLine;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

/**
 * @author lengchunyun
 */
public interface PipeLineRepository extends CrudRepository<PipeLine, Integer>,
        JpaSpecificationExecutor<PipeLine> {
}
