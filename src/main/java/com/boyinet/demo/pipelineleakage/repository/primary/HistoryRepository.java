package com.boyinet.demo.pipelineleakage.repository.primary;

import com.boyinet.demo.pipelineleakage.bean.primary.History;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author lengchunyun
 */
public interface HistoryRepository extends CrudRepository<History, Long> {

    /**
     * 根据日期和传感器no获取当前值
     *
     * @param no   传感器序号
     * @param date 日期
     * @return Histories
     */
    List<History> findHistoriesByNoEqualsAndTimeEquals(Long no, Date date);

    /**
     * 根据日期和传感器no获取该区间内的平均值
     *
     * @param no    传感器序号
     * @param start 起始时间
     * @param end   结束时间
     * @return 均值
     */
    @Query(nativeQuery = true, value = "SELECT AVG(value) FROM History WHERE no = :no AND time BETWEEN :start AND :end")
    BigDecimal avgValueByNoEqualsAndTimeBetween(Long no, Date start, Date end);

    /**
     * 根据时间获取所有当前时间下的历史数据
     *
     * @param time 时间
     * @return 历史数据
     */
    List<History> findHistoriesByTimeEquals(Date time);

}
