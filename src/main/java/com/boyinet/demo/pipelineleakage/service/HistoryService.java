package com.boyinet.demo.pipelineleakage.service;

import cn.hutool.core.collection.CollectionUtil;
import com.boyinet.demo.pipelineleakage.bean.History;
import com.boyinet.demo.pipelineleakage.bean.Sensor;
import com.boyinet.demo.pipelineleakage.repository.HistoryRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author lengchunyun
 */
@Service
public class HistoryService {

    private final HistoryRepository historyRepository;

    public HistoryService(HistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }


    public BigDecimal calc5minuteAvg(Date date, Integer no) {
        long time = date.getTime();
        long before = time - 1000 * 60 * 5;
        Date start = new Date(before);
        return historyRepository.avgValueByNoEqualsAndTimeBetween(no, start, date)
                .setScale(8, RoundingMode.HALF_UP);
    }

    public History getValueByTime(Date date, Integer no) {
        List<History> histories = historyRepository.findHistoriesByNoEqualsAndTimeEquals(no, date);
        if (CollectionUtil.isEmpty(histories)) {
            long time = date.getTime();
            long before = time - 1000;
            date = new Date(before);
            return getValueByTime(date, no);
        } else {
            return histories.get(0);
        }
    }

    /**
     * 根据时间获取列表
     *
     * @param time    时间
     * @param sensors 传感器列表
     * @return map key-no value-senor
     */
    public Map<Integer, Sensor> listByTime(Date time, List<Sensor> sensors) {
        Map<Integer, Sensor> sensorMap = sensors.stream().collect(Collectors.toMap(Sensor::getNo, item -> item));
        sensorMap.keySet().forEach(key -> {
            BigDecimal value = this.calc5minuteAvg(time, key);
            Sensor sensor = sensorMap.get(key);
            sensor.setCreateTime(time);
            sensor.setCurrentValue(value);
        });
        System.out.println(sensorMap.values());
        return sensorMap;
    }
}
