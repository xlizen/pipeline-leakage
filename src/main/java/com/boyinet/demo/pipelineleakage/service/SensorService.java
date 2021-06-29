package com.boyinet.demo.pipelineleakage.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.alibaba.excel.EasyExcel;
import com.boyinet.demo.pipelineleakage.bean.Sensor;
import com.boyinet.demo.pipelineleakage.excel.UploadDataListener;
import com.boyinet.demo.pipelineleakage.repository.SensorRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author lengchunyun
 */
@Service
public class SensorService {

    private final SensorRepository sensorRepository;

    public SensorService(SensorRepository sensorRepository) {
        this.sensorRepository = sensorRepository;
    }


    public void readFileAndSave(String fileName, Integer pipeLineId) {
        EasyExcel.read(fileName, Sensor.class,
                new UploadDataListener(sensorRepository, pipeLineId)).sheet().doRead();
    }

    public List<Sensor> batchListByIds(List<Integer> ids) {
        return sensorRepository.findSensorsByPipelineIdIn(ids);
    }

    public void deleteById(Integer no) {
        sensorRepository.deleteById(no);
    }

    public void updateById(Sensor sensor) {
        sensorRepository.findById(sensor.getNo()).ifPresent(old -> {
            BeanUtil.copyProperties(sensor, old, CopyOptions.create(Sensor.class, true));
            sensorRepository.save(old);
        });
    }

    public List<Sensor> findByPid(Integer id) {
        return sensorRepository.findSensorsByPipelineIdEquals(id);
    }
}
