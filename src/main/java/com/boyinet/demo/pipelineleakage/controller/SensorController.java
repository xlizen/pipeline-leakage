package com.boyinet.demo.pipelineleakage.controller;

import com.alibaba.excel.EasyExcel;
import com.boyinet.demo.pipelineleakage.bean.History;
import com.boyinet.demo.pipelineleakage.bean.Sensor;
import com.boyinet.demo.pipelineleakage.common.R;
import com.boyinet.demo.pipelineleakage.repository.SensorRepository;
import com.boyinet.demo.pipelineleakage.excel.UploadDataListener;
import com.boyinet.demo.pipelineleakage.service.HistoryService;
import com.boyinet.demo.pipelineleakage.service.SensorService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author lengchunyun
 */
@RestController
@RequestMapping("sensor")
public class SensorController {

    private final SensorRepository sensorRepository;

    private final HistoryService historyService;

    private final SensorService sensorService;

    public SensorController(SensorRepository sensorRepository, HistoryService historyService,
                            SensorService sensorService) {
        this.sensorRepository = sensorRepository;
        this.historyService = historyService;
        this.sensorService = sensorService;
    }

    @PostMapping("upload")
    @ResponseBody
    public String upload(MultipartFile file) throws IOException {
        EasyExcel.read(file.getInputStream(), Sensor.class,
                new UploadDataListener(sensorRepository, 1)).sheet().doRead();
        return "success";
    }

    @GetMapping("history")
    @ResponseBody
    public History history(Date date) {
        return historyService.getValueByTime(date, 1);
    }

    @GetMapping("pipeline")
    public R<List<Sensor>> listByPipeLine(Integer pipeLineId) {
        List<Sensor> sensorList = sensorRepository.findSensorsByPipelineIdEquals(pipeLineId);
        R<List<Sensor>> ok = R.ok(sensorList);
        ok.setTotal(sensorList.size());
        return ok;
    }

    @PostMapping("update")
    R<Map<String, Object>> update(Sensor sensor) {
        sensorService.updateById(sensor);
        return R.ok();
    }

    @GetMapping("delete")
    R<Map<String, Object>> delete(Integer id) {
        sensorService.deleteById(id);
        return R.ok();
    }
}
