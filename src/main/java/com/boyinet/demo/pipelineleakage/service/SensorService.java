package com.boyinet.demo.pipelineleakage.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.alibaba.excel.EasyExcel;
import com.boyinet.demo.pipelineleakage.bean.primary.Sensor;
import com.boyinet.demo.pipelineleakage.excel.UploadDataListener;
import com.boyinet.demo.pipelineleakage.repository.primary.SensorRepository;
import com.boyinet.demo.pipelineleakage.util.LeakUtil;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author lengchunyun
 */
@Service
public class SensorService {

    private final SensorRepository sensorRepository;

    public SensorService(SensorRepository sensorRepository) {
        this.sensorRepository = sensorRepository;
    }


    public void readFileAndSave(String fileName, Long pipeLineId) {
        EasyExcel.read(fileName, Sensor.class,
                new UploadDataListener(sensorRepository, pipeLineId)).sheet().doRead();
        calcLandR(pipeLineId);
    }

    private void calcLandR(Long pipeLineId) {
        List<Sensor> sensors = sensorRepository.findSensorsByHeadEqualsAndPipelineIdEquals(-1L, pipeLineId);
        System.out.println("calcLandR running...");
        //去重所有头结点
        Set<Long> headSet = sensors.stream().map(Sensor::getNo).collect(Collectors.toSet());
        List<Sensor> sensorByHeadIn = sensorRepository.findSensorByHeadIn(headSet);
        //映射头结点NO 和 信息
        Map<Long, Sensor> sensorMap = sensorByHeadIn.stream().collect(Collectors.toMap(Sensor::getNo, i -> i));
        List<Sensor> result = new ArrayList<>();
        sensors.forEach(sensor -> {
            BigDecimal lastR = BigDecimal.ZERO;
            BigDecimal lastL = BigDecimal.ZERO;
            while (sensor.getNext() != null) {
                Sensor next = sensorMap.get(sensor.getNext());
                next.setCalcL(sensor.getCalcL().add(next.getL()));
                next.setCalcR(LeakUtil.calcR(lastR, lastL, next.getR(), next.getCalcL()));
                if (next.getType() == 3) {
                    lastL = next.getCalcL();
                    lastR = next.getCalcR();
                } else {
                    lastL = BigDecimal.ZERO;
                    lastR = BigDecimal.ZERO;
                }
                result.add(next);
                sensor = next;
            }
        });
        sensorRepository.saveAll(result);
    }

    public List<Sensor> batchListByIds(List<Long> ids) {
        return sensorRepository.findSensorsByPipelineIdIn(ids);
    }

    public void deleteById(Long no) {
        sensorRepository.deleteById(no);
    }

    public void updateById(Sensor sensor) {
        sensorRepository.findById(sensor.getNo()).ifPresent(old -> {
            BeanUtil.copyProperties(sensor, old, CopyOptions.create(Sensor.class, true));
            sensorRepository.save(old);
        });
    }

    public List<Sensor> findByPid(Long id) {
        return sensorRepository.findSensorsByPipelineIdEquals(id);
    }

    public Sensor findById(Long targetId) {
        return sensorRepository.findById(targetId).get();
    }

    public void saveAll(List<Sensor> sensors) {
        sensorRepository.saveAll(sensors);
    }
}
