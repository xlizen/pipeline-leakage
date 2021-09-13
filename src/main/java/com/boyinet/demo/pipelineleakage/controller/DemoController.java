package com.boyinet.demo.pipelineleakage.controller;


import com.boyinet.demo.pipelineleakage.bean.Result;
import com.boyinet.demo.pipelineleakage.bean.primary.Sensor;
import com.boyinet.demo.pipelineleakage.common.R;
import com.boyinet.demo.pipelineleakage.repository.primary.SensorRepository;
import com.boyinet.demo.pipelineleakage.service.*;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author lengchunyun
 */
@RestController
@RequestMapping("demo")
public class DemoController {

    private final WebSocketService webSocketService;

    private final LeakageService leakageService;

    private final HistoryService historyService;

    private final FileLoadService fileLoadService;

    private final SensorRepository sensorRepository;

    private final ZeroService zeroService;

    public DemoController(WebSocketService webSocketService, LeakageService leakageService, HistoryService historyService,
                          FileLoadService fileLoadService, SensorRepository sensorRepository, ZeroService zeroService) {
        this.webSocketService = webSocketService;
        this.leakageService = leakageService;
        this.historyService = historyService;
        this.fileLoadService = fileLoadService;
        this.sensorRepository = sensorRepository;
        this.zeroService = zeroService;
    }

    @RequestMapping("setZero")
    void setZero(@RequestParam Date time) {
        List<Sensor> sensors = new ArrayList<>();
        sensorRepository.findAll().forEach(sensors::add);
        Map<Long, Sensor> map = historyService.listByTime(time, sensors);
        zeroService.recordZero(map);
    }

    @RequestMapping("demo")
    void demo(@RequestBody List<Sensor> sensor) {
        leakageService.demo(sensor);
    }

    @RequestMapping("show")
    void show(@RequestBody R<Result> r) {
        webSocketService.sendMsg(r);
    }

    @GetMapping("src")
    void loadFileToDataBase(String src) {
        fileLoadService.loadFileToDataBase(src);
    }

    @GetMapping("avg")
    BigDecimal avg(@RequestParam String date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            return historyService.calc5minuteAvg(dateFormat.parse(date), 1L);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new BigDecimal("0.0");
    }

    @PostMapping("build")
    void buildMap(@RequestBody List<Sensor> sensors) {
        sensorRepository.saveAll(sensors);
    }

    @RequestMapping("findLeak")
    R<Result> findLeak(@RequestParam Date time) {
        return leakageService.findLeak(time);
    }

    @PostMapping("updateK")
    void updateK(List<Sensor> sensors) {
        sensorRepository.saveAll(sensors);
    }
}
