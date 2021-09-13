package com.boyinet.demo.pipelineleakage.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.boyinet.demo.pipelineleakage.bean.primary.History;
import com.boyinet.demo.pipelineleakage.bean.primary.PipeLine;
import com.boyinet.demo.pipelineleakage.bean.primary.Sensor;
import com.boyinet.demo.pipelineleakage.dto.PressureValue;
import com.boyinet.demo.pipelineleakage.dto.PressureValueResponse;
import com.boyinet.demo.pipelineleakage.dto.SearchParam;
import com.boyinet.demo.pipelineleakage.repository.primary.HistoryRepository;
import com.boyinet.demo.pipelineleakage.util.HttpUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author lengchunyun
 */
@Service
public class HistoryService {

    private final HistoryRepository historyRepository;

    private final SensorService sensorService;

    private final ZeroService zeroService;

    private final PipeLineService pipeLineService;

    private final HttpUtil httpUtil;

    public static final int PAGE_SIZE = 300;

    @Value("${remote.pull.data.enable}")
    Boolean remotePull;


    public HistoryService(HistoryRepository historyRepository, SensorService sensorService,
                          ZeroService zeroService, PipeLineService pipeLineService, HttpUtil httpUtil) {
        this.historyRepository = historyRepository;
        this.sensorService = sensorService;
        this.zeroService = zeroService;
        this.pipeLineService = pipeLineService;
        this.httpUtil = httpUtil;
    }


    public BigDecimal calc5minuteAvg(Date date, Long no) {
        long time = date.getTime();
        long before = time - 1000 * 60 * 5;
        Date start = new Date(before);
        BigDecimal bigDecimal = Optional.ofNullable(historyRepository.avgValueByNoEqualsAndTimeBetween(no, start, date))
                .orElse(BigDecimal.ZERO);
        return bigDecimal.setScale(8, RoundingMode.HALF_UP);
    }

    public History getValueByTime(Date date, Long no) {
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
    public Map<Long, Sensor> listByTime(Date time, List<Sensor> sensors) {
        Map<Long, Sensor> sensorMap = sensors.stream().collect(Collectors.toMap(Sensor::getNo, item -> item));
        sensorMap.keySet().forEach(key -> {
            Sensor sensor = sensorMap.get(key);
            sensor.setCreateTime(time);

            //本地计算5分钟均值
            BigDecimal value = this.calc5minuteAvg(time, key);
            sensor.setCurrentValue(value);

            //TODO 远程计算5分钟均值
            long milliseconds = time.getTime();
            long before = milliseconds - 1000 * 60 * 5;
            Date start = new Date(before);
            BigDecimal average = calcRemoteAverage(start, time, key);
            sensor.setCurrentValue(average);
        });
        return sensorMap;
    }

    public void recordStableValueByPipelineId(Date startTime, Date endTime, Long no) {
        List<Long> pipelineIds = new ArrayList<>();
        if (no == null) {
            pipelineIds = pipeLineService.findAll().stream().map(PipeLine::getId).collect(Collectors.toList());
        } else {
            pipelineIds.add(no);
        }
        pipelineIds.forEach(id -> {
            PipeLine pipeLine = pipeLineService.load(id);
            if (pipeLine != null) {
                List<Sensor> sensors = pipeLine.getSensorList();
                sensors.forEach(s -> recordStableValueBySensorId(startTime, endTime, s));
                sensorService.saveAll(sensors);
                Set<Long> headSet = sensors.stream().filter(s -> s.getHead() == -1).map(Sensor::getNo).collect(Collectors.toSet());
                String referencePoint = pipeLine.getReferencePoint();
                if (StrUtil.isNotBlank(referencePoint)) {
                    headSet.addAll(Arrays.stream(referencePoint.split(",")).map(Long::parseLong).collect(Collectors.toList()));
                }
                headSet.forEach(head -> zeroService.recordZero(sensors, head));
            }
        });
    }

    public void recordStableValueBySensorId(Date startTime, Date endTime, Sensor sensor) {
        //远程获取计算
        if (remotePull) {
            sensor.setAverageValue(calcRemoteAverage(startTime, endTime, sensor.getNo()));
        } else {
            //本地获取计算
            BigDecimal bigDecimal = historyRepository.avgValueByNoEqualsAndTimeBetween(sensor.getNo(), startTime, endTime);
            sensor.setAverageValue(bigDecimal.setScale(8, RoundingMode.HALF_UP));
        }
    }

    private BigDecimal calcRemoteAverage(Date startTime, Date endTime, Long no) {
        List<Double> avgList = new ArrayList<>();
        SearchParam searchParam = new SearchParam(no, startTime, endTime);
        searchParam.setPi(1);
        searchParam.setPs(PAGE_SIZE);
        PressureValueResponse response = httpUtil.getValue(searchParam);
        Long total = response.getTotal();
        double average = response.getData().stream().mapToDouble(PressureValue::getValue).average().orElse(0.0);
        if (total <= PAGE_SIZE) {
            return BigDecimal.valueOf(average);
        } else {
            avgList.add(average);
            long page = (total % PAGE_SIZE == 0) ? total / PAGE_SIZE : total / PAGE_SIZE + 1;
            for (int i = 2; i <= page; i++) {
                searchParam.setPi(i);
                response = httpUtil.getValue(searchParam);
                average = response.getData().stream().mapToDouble(PressureValue::getValue).average().orElse(0.0);
                avgList.add(average);
            }
            double result = avgList.stream().mapToDouble(s -> s).average().orElse(0.0);
            return BigDecimal.valueOf(result);
        }
    }
}
