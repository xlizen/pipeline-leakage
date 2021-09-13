package com.boyinet.demo.pipelineleakage.repository.primary;

import com.boyinet.demo.pipelineleakage.bean.primary.Sensor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

/**
 * @author lengchunyun
 */
@Repository
public interface SensorRepository extends CrudRepository<Sensor, Long> {

    /**
     * 根据传入的pipeLineId列表查询Sensor列表
     *
     * @param ids pipeline id 列表
     * @return Sensor 列表
     */
    List<Sensor> findSensorsByPipelineIdIn(List<Long> ids);

    /**
     * 根据传入的pipeLineId询Sensor列表
     *
     * @param id pipeline id 列表
     * @return Sensor 列表
     */
    List<Sensor> findSensorsByPipelineIdEquals(Long id);


    /**
     * 根据传入的pipeLineId和节点状态询Sensor
     *
     * @param id   pipeline id
     * @param type 0
     * @return Sensor
     */
    Sensor findSensorByPipelineIdEqualsAndTypeEquals(Long id, Byte type);

    /**
     * 查询指定状态的节点列表
     *
     * @param type 类型
     * @param id   管道ID
     * @return
     */
    List<Sensor> findSensorByTypeEqualsAndPipelineIdEquals(Byte type, Long id);

    /**
     * 根据头ID查询列表
     *
     * @param heads
     * @return
     */
    List<Sensor> findSensorByHeadIn(Set<Long> heads);

    /**
     * 批量查询
     *
     * @param heads
     * @return
     */
    List<Sensor> findSensorByNoIn(Set<Long> heads);

    /**
     * 根据头结点和管道ID查询传感器列表
     *
     * @param head       -1
     * @param pipelineId pipelineId
     * @return
     */
    List<Sensor> findSensorsByHeadEqualsAndPipelineIdEquals(Long head, Long pipelineId);
}
