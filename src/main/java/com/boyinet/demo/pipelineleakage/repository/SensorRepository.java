package com.boyinet.demo.pipelineleakage.repository;

import com.boyinet.demo.pipelineleakage.bean.Sensor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author lengchunyun
 */
@Repository
public interface SensorRepository extends CrudRepository<Sensor, Integer> {

    /**
     * 根据传入的pipeLineId列表查询Sensor列表
     *
     * @param ids pipeline id 列表
     * @return Sensor 列表
     */
    List<Sensor> findSensorsByPipelineIdIn(List<Integer> ids);

    /**
     * 根据传入的pipeLineId询Sensor列表
     *
     * @param id pipeline id 列表
     * @return Sensor 列表
     */
    List<Sensor> findSensorsByPipelineIdEquals(Integer id);
}
