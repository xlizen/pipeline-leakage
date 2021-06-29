package com.boyinet.demo.pipelineleakage.service;

import com.boyinet.demo.pipelineleakage.bean.*;
import com.boyinet.demo.pipelineleakage.common.R;
import com.boyinet.demo.pipelineleakage.config.StandardConfig;
import com.boyinet.demo.pipelineleakage.repository.SensorRepository;
import com.boyinet.demo.pipelineleakage.repository.ZeroRepository;
import com.boyinet.demo.pipelineleakage.util.LeakUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author lengchunyun
 */
@Service
@Slf4j
public class LeakageService {


    private final ZeroRepository zeroRepository;

    private final SensorRepository sensorRepository;

    private final StandardConfig standardConfig;

    private final WebSocketService webSocketService;

    private final HistoryService historyService;

    private final ZeroService zeroService;

    private final SimpMessagingTemplate simpMessagingTemplate;

    public LeakageService(ZeroRepository zeroRepository, SensorRepository sensorRepository, StandardConfig standardConfig,
                          WebSocketService webSocketService, HistoryService historyService, ZeroService zeroService,
                          SimpMessagingTemplate simpMessagingTemplate) {
        this.zeroRepository = zeroRepository;
        this.sensorRepository = sensorRepository;
        this.standardConfig = standardConfig;
        this.webSocketService = webSocketService;
        this.historyService = historyService;
        this.zeroService = zeroService;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    public R<Result> findLeak(Date date) {

        R<Result> r = R.nok(200, "");
        Result result = new Result();
        List<Sensor> sensors = new ArrayList<>();
        sensorRepository.findAll().forEach(sensors::add);
        Map<Integer, Sensor> sensorMap = historyService.listByTime(date, sensors);
        Map<Integer, Zero> zeroMap = zeroService.listMapByTime(date);
        Map<Integer, Flow> flowMap = new HashMap<>(sensors.size() / 3 * 4 + 1);

        //开始计算当前传感器处的流量
        List<Flow> flows = new ArrayList<>();

        List<Leakage> leakages = new ArrayList<>();

        Result.SysInfo sysInfo = new Result.SysInfo(1, zeroMap.get(1).getValue().doubleValue(),
                sensorMap.get(1).getCurrentValue().doubleValue());

        //获取所有头结点的列表
        List<Sensor> headList = sensors.stream().filter(sensor -> sensor.getType().equals((byte) 0)).collect(Collectors.toList());

        final boolean[] hasFind = {false};
        //遍历头结点
        headList.forEach(head -> {
            //获取以该为头的路径列表
            List<Sensor> subList = sensors.stream().filter(sensor ->
                    sensor.getHead().equals(head.getNo())).collect(Collectors.toList());
            //遍历设置 Zero 支路和头结点的差值
            double lastValue = 0.0;
            subList.forEach(sub -> {
                Integer no = sub.getNo();
                Sensor sensor = sensorMap.get(no);
                BigDecimal currentValue = sensor.getCurrentValue();
                BigDecimal diffValue = zeroMap.get(no).getDiffValue();
                BigDecimal subtract = head.getCurrentValue().subtract(currentValue.add(diffValue));
                BigDecimal flow = LeakUtil.calcMain(sensor.getK(), subtract.multiply(BigDecimal.valueOf(100)), head.getR(), sensor.getL());
                Flow f = new Flow(no, subtract.multiply(BigDecimal.valueOf(100)).doubleValue(), flow.doubleValue());
                flows.add(f);
                flowMap.put(no, f);
                if (flow.doubleValue() < lastValue && !hasFind[0]) {
                    leakages.add(new Leakage(sensor.getPre(), sensor.getNo(), (byte) 1));
                    hasFind[0] = true;
                }
            });
        });
        if (hasFind[0]) {
            leakages.forEach(leakage -> {
                Integer next = sensorMap.get(leakage.getNext()).getNext();
                if (next == null) {
                    leakage.setValue(flowMap.get(leakage.getPrevious()).getFlow());
                } else {
                    leakage.setValue(flowMap.get(leakage.getPrevious()).getFlow() - flowMap.get(next).getFlow());
                }
            });
        }
        System.out.println(flows);
        result.setFlow(flows);
        result.setLeakage(leakages);
        result.setSysInfo(sysInfo);
        r.setData(result);
        webSocketService.sendMsg(r);
        return r;
    }

    public R<Result> demo(List<Sensor> pressure) {

        R<Result> r = R.nok(200, "");
        Result result = new Result();

        List<Integer> leakResult = new ArrayList<>(pressure.size());

        log.info("数据长度 = {}，当前压力数据详情 {}", pressure.size(), pressure.toString());

        Integer firstNo = pressure.get(0).getNo();

        log.info("firstNo = {}，", firstNo);

        Page<Zero> zeroPage = zeroRepository.listZero(firstNo, PageRequest.of(0, pressure.size()));
        List<Zero> zeros = zeroPage.getContent();

        log.info("数据长度 = {}，零点数据详情 {}", zeros.size(), zeros.toString());

        List<BigDecimal> diffpressure = new ArrayList<>(pressure.size());


        //开始计算当前传感器处的流量
        List<Flow> flows = new ArrayList<>();

        diffpressure.add(new BigDecimal("0.0"));
        flows.add(new Flow(1, pressure.get(0).getCurrentValue().doubleValue(), 0.0));
        pressure.get(0).setDiffValue(diffpressure.get(0));

        for (int i = 1; i < pressure.size(); i++) {
            if (i == 6) {
                diffpressure.add((pressure.get(3).getCurrentValue().add(zeros.get(3).getDiffValue())
                        .subtract((pressure.get(i).getCurrentValue().add(zeros.get(i).getDiffValue())))));
                pressure.get(i).setDiffValue(diffpressure.get(i));
                //flows.add(new Flow(i + 1, pressure.get(i).getCurrentValue().doubleValue(), LeakUtil.calcBranch(diffpressure.get(i)).doubleValue()));
            } else {
                diffpressure.add((pressure.get(i - 1).getCurrentValue().add(zeros.get(i - 1).getDiffValue())
                        .subtract((pressure.get(i).getCurrentValue().add(zeros.get(i).getDiffValue())))));
                pressure.get(i).setDiffValue(diffpressure.get(i));
                if (i == 5 || i == 7) {
                    continue;
                }
                //flows.add(new Flow(i + 1, pressure.get(i).getCurrentValue().doubleValue(), LeakUtil.calcMain(diffpressure.get(i)).doubleValue()));
            }
        }
        log.info("数据长度 = {}，差压数据详情 {}", diffpressure.size(), diffpressure.toString());
        sensorRepository.saveAll(pressure);
        result.setFlow(flows);

        log.info("流量情况为：{}", flows);

        //∆P2≤a时系统无泄漏
        if (diffpressure.get(1).compareTo(standardConfig.getStandardDiffPressure()) <= 0) {
            result.setLeakage(Collections.emptyList());
            r.setData(result);
            return r;
        }

        List<Leakage> leakages = new ArrayList<>();

        //∆P2>a且∆P4≤a时只有q泄1泄漏
        if (diffpressure.get(1).compareTo(standardConfig.getStandardDiffPressure()) > 0
                && diffpressure.get(3).compareTo(standardConfig.getStandardDiffPressure()) <= 0) {
            leakages.add(new Leakage(2, 3, (byte) 2));
        }

        //∆P2>a且∆P4>a时不能确定q泄1泄漏
        if (diffpressure.get(1).compareTo(standardConfig.getStandardDiffPressure()) > 0
                && diffpressure.get(3).compareTo(standardConfig.getStandardDiffPressure()) > 0) {
            leakages.add(new Leakage(2, 3, (byte) 1));
        }

        //∆P4>a且∆P6≤a、∆P8≤a时则q泄5有泄漏
        if (diffpressure.get(3).compareTo(standardConfig.getStandardDiffPressure()) > 0
                && diffpressure.get(5).compareTo(standardConfig.getStandardDiffPressure()) > 0
                && diffpressure.get(7).compareTo(standardConfig.getStandardDiffPressure()) > 0) {
            leakages.add(new Leakage(4, 7, (byte) 2));
        }

        //∆P4>a且∆P6>a、∆P8>a时不能确定q泄5有泄漏
        if (diffpressure.get(3).compareTo(standardConfig.getStandardDiffPressure()) > 0
                && (diffpressure.get(5).compareTo(standardConfig.getStandardDiffPressure()) > 0
                || diffpressure.get(7).compareTo(standardConfig.getStandardDiffPressure()) > 0)) {
            leakages.add(new Leakage(4, 7, (byte) 1));
        }

        //∆P6>a则q泄2、q泄3、q泄4有泄漏
        if (diffpressure.get(5).compareTo(standardConfig.getStandardDiffPressure()) > 0) {
            leakages.add(new Leakage(5, 6, (byte) 2));
        }

        //∆P8>a则q泄6有泄漏
        if (diffpressure.get(7).compareTo(standardConfig.getStandardDiffPressure()) > 0) {
            leakages.add(new Leakage(7, 8, (byte) 2));
        }

        result.setLeakage(leakages);
        r.setData(result);
        log.info("结果为 {}", r);
        webSocketService.sendMsg(r);
        return r;
    }

    public void load(Integer id) {
        //Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = null;
        try {
            date = dateFormat.parse("2021/04/30 17:05:00");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        R<Result> r = R.nok(200, "");
        Result result = new Result();
        List<Sensor> sensors = sensorRepository.findSensorsByPipelineIdEquals(id);
        Map<Integer, Sensor> sensorMap = historyService.listByTime(date, sensors);
        Map<Integer, Zero> zeroMap = zeroService.listMapByTime(date);
        Map<Integer, Flow> flowMap = new HashMap<>(sensors.size() / 3 * 4 + 1);

        //开始计算当前传感器处的流量
        List<Flow> flows = new ArrayList<>();

        List<Leakage> leakages = new ArrayList<>();

        Result.SysInfo sysInfo = new Result.SysInfo(1, zeroMap.get(1).getValue().doubleValue(),
                sensorMap.get(1).getCurrentValue().doubleValue());

        //获取所有头结点的列表
        List<Sensor> headList = sensors.stream().filter(sensor -> sensor.getType().equals((byte) 0)).collect(Collectors.toList());

        final boolean[] hasFind = {false};
        //遍历头结点
        headList.forEach(head -> {
            //获取以该为头的路径列表
            List<Sensor> subList = sensors.stream().filter(sensor ->
                    sensor.getHead().equals(head.getNo())).collect(Collectors.toList());
            //遍历设置 Zero 支路和头结点的差值
            double lastValue = 0.0;
            subList.forEach(sub -> {
                Integer no = sub.getNo();
                Sensor sensor = sensorMap.get(no);
                BigDecimal currentValue = sensor.getCurrentValue();
                BigDecimal diffValue = zeroMap.get(no).getDiffValue();
                BigDecimal subtract = head.getCurrentValue().subtract(currentValue.add(diffValue));
                BigDecimal flow = LeakUtil.calcMain(sensor.getK(), subtract.multiply(BigDecimal.valueOf(100)), head.getR(), sensor.getL());
                Flow f = new Flow(no, subtract.multiply(BigDecimal.valueOf(100)).doubleValue(), flow.doubleValue());
                flows.add(f);
                flowMap.put(no, f);
                if (flow.doubleValue() < lastValue && !hasFind[0]) {
                    leakages.add(new Leakage(sensor.getPre(), sensor.getNo(), (byte) 1));
                    hasFind[0] = true;
                }
            });
        });
        if (hasFind[0]) {
            leakages.forEach(leakage -> {
                Integer next = sensorMap.get(leakage.getNext()).getNext();
                if (next == null) {
                    leakage.setValue(flowMap.get(leakage.getPrevious()).getFlow());
                } else {
                    leakage.setValue(flowMap.get(leakage.getPrevious()).getFlow() - flowMap.get(next).getFlow());
                }
            });
        }
        System.out.println(flows);
        result.setFlow(flows);
        result.setLeakage(leakages);
        result.setSysInfo(sysInfo);
        r.setData(result);
        String destination = "/topic/";
        simpMessagingTemplate.convertAndSend(destination, r);
    }
}
