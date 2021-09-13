package com.boyinet.demo.pipelineleakage.repository.second;

import com.boyinet.demo.pipelineleakage.bean.second.DataHistory;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author lengchunyun
 */
@Repository
public interface DataHistoryRepository extends CrudRepository<DataHistory, Long> {

    /**
     * 根据日期和传感器no获取当前值
     *
     * @param no   传感器序号
     * @param date 日期
     * @return Histories
     */
    List<DataHistory> findHistoriesByEquipmentIdEqualsAndGmtCreateEquals(String no, Long date);

    /**
     * 根据日期和传感器no获取该区间内的平均值
     *
     * @param no    传感器序号
     * @param start 起始时间
     * @param end   结束时间
     * @return 均值
     */
    @Query(nativeQuery = true, value = "SELECT AVG(value) FROM DataHistory WHERE equipment_id = :no AND gmt_create BETWEEN :start AND :end")
    String avgValueByNoEqualsAndTimeBetween(String no, Long start, Long end);

    /**
     * 根据时间获取所有当前时间下的历史数据
     *
     * @param time 时间
     * @return 历史数据
     */
    List<DataHistory> findHistoriesByGmtCreateEquals(Long time);
}
