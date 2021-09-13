package com.boyinet.demo.pipelineleakage.service;

import cn.hutool.core.util.StrUtil;
import com.boyinet.demo.pipelineleakage.bean.primary.PipeLine;
import com.boyinet.demo.pipelineleakage.bean.primary.Sensor;
import com.boyinet.demo.pipelineleakage.bean.primary.Zero;
import com.boyinet.demo.pipelineleakage.bean.primary.ZeroKey;
import com.boyinet.demo.pipelineleakage.repository.primary.ZeroRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author lengchunyun
 */
@Service
public class ZeroService {

    private final ZeroRepository zeroRepository;

    private final SensorService sensorService;

    private final PipeLineService pipeLineService;

    public ZeroService(ZeroRepository zeroRepository, SensorService sensorService, PipeLineService pipeLineService) {
        this.zeroRepository = zeroRepository;
        this.sensorService = sensorService;
        this.pipeLineService = pipeLineService;
    }

    Map<Long, Zero> listMapByIds(List<Long> ids) {
        List<Zero> zeros = zeroRepository.findZerosByNoIn(ids);
        return zeros.stream().collect(Collectors.toMap(Zero::getNo, item -> item));
    }

    Map<ZeroKey, Zero> listZeroMapByIds(List<Long> ids) {
        List<Zero> zeros = zeroRepository.findZerosByNoIn(ids);
        return zeros.stream().collect(Collectors.toMap(item -> new ZeroKey(item.getNo(), item.getTarget()), item -> item));
    }

    public void recordZero(Map<Long, Sensor> map) {
        Collection<Sensor> sensors = map.values();
        if (sensors.size() < 2) {
            return;
        }
        List<Zero> zeros = new ArrayList<>(sensors.size());
        Zero h = new Zero(1L, map.get(1).getNo(), BigDecimal.ZERO, map.get(1).getCurrentValue());
        zeros.add(h);

        //获取所有头结点的列表
        List<Sensor> headList = sensors.stream().filter(sensor -> sensor.getType().equals((byte) 0)).collect(Collectors.toList());

        //遍历头结点
        headList.forEach(head -> {
            //获取以该为头的路径列表
            List<Sensor> subList = sensors.stream().filter(sensor -> sensor.getHead().equals(head.getNo())).collect(Collectors.toList());
            //遍历设置 Zero 支路和头结点的差值
            subList.forEach(sub -> {
                Zero zero = new Zero();
                zero.setDiffValue(head.getCurrentValue().subtract(sub.getCurrentValue()));
                zero.setValue(sub.getCurrentValue());
                zeros.add(zero);
            });
        });

/*        for (Sensor sensor : sensors) {
            Zero zero = new Zero();
            zero.setNo(sensor.getNo());
            zero.setDiffValue(standard.getCurrentValue().subtract(sensor.getCurrentValue()).multiply(BigDecimal.valueOf(100)));
            zeros.add(zero);
        }*/
        zeroRepository.saveAll(zeros);
    }

    public void recordZero() {
        List<PipeLine> pipeLines = pipeLineService.findAll();
        pipeLines.forEach(pipeLine -> {
            recordZero(pipeLine.getId());
        });
    }

    public void recordZero(Long pipelineId) {
        PipeLine pipeLine = pipeLineService.load(pipelineId);
        if (pipeLine != null) {
            List<Sensor> sensors = pipeLine.getSensorList();
            Set<Long> headSet = sensors.stream().filter(s -> s.getType() == 4 || s.getType() == 0).map(Sensor::getNo).collect(Collectors.toSet());
            String referencePoint = pipeLine.getReferencePoint();
            if (StrUtil.isNotBlank(referencePoint)) {
                headSet.addAll(Arrays.stream(referencePoint.split(",")).map(Long::parseLong).collect(Collectors.toList()));
            }
            headSet.forEach(head -> recordZero(sensors, head));
        }
    }

    public void recordZero(Long pipelineId, Long targetId) {
        List<Sensor> sensors = sensorService.findByPid(pipelineId);
        recordZero(sensors, targetId);
    }

    public void recordZero(Long pipelineId, Sensor target) {
        List<Sensor> sensors = sensorService.findByPid(pipelineId);
        recordZero(sensors, target);
    }

    public void recordZero(List<Sensor> sensors, Sensor target) {
        List<Zero> zeros = sensors.stream().filter(s -> !s.getNo().equals(target.getNo()))
                .map(s -> new Zero(s.getNo(), target.getNo(), target.getAverageValue()
                        .subtract(s.getAverageValue()), s.getCurrentValue())).collect(Collectors.toList());
        zeroRepository.saveAll(zeros);
    }

    public void recordZero(List<Sensor> sensors, Long targetId) {
        Optional<Sensor> sensor = sensors.stream().filter(sen -> sen.getNo()
                .equals(targetId)).findFirst();
        sensor.ifPresent(sens -> recordZero(sensors, sens));
    }
}
