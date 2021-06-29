package com.boyinet.demo.pipelineleakage.service;

import com.boyinet.demo.pipelineleakage.bean.Sensor;
import com.boyinet.demo.pipelineleakage.bean.Zero;
import com.boyinet.demo.pipelineleakage.repository.ZeroRepository;
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

    public ZeroService(ZeroRepository zeroRepository) {
        this.zeroRepository = zeroRepository;
    }

    Map<Integer, Zero> listMapByTime(Date time) {
        List<Zero> zeros = zeroRepository.findAll();
        return zeros.stream().collect(Collectors.toMap(Zero::getNo, item -> item));
    }

    public void recordZero(Map<Integer, Sensor> map) {
        Collection<Sensor> sensors = map.values();
        if (sensors.size() < 2) {
            return;
        }
        List<Zero> zeros = new ArrayList<>(sensors.size());
        Zero h = new Zero(1, BigDecimal.ZERO, map.get(1).getCurrentValue());
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
                zero.setNo(sub.getNo());
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
}
