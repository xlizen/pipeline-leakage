package com.boyinet.demo.pipelineleakage.service;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.boyinet.demo.pipelineleakage.bean.Leakage;
import com.boyinet.demo.pipelineleakage.bean.Result;
import com.boyinet.demo.pipelineleakage.bean.primary.*;
import com.boyinet.demo.pipelineleakage.common.R;
import com.boyinet.demo.pipelineleakage.config.StandardConfig;
import com.boyinet.demo.pipelineleakage.corn.PostTaskConsumer;
import com.boyinet.demo.pipelineleakage.repository.primary.SensorRepository;
import com.boyinet.demo.pipelineleakage.repository.primary.ZeroRepository;
import com.boyinet.demo.pipelineleakage.util.HttpUtil;
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
import java.util.concurrent.TimeUnit;
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

    private final PipeLineService pipeLineService;

    private final HttpUtil httpUtil;

    public LeakageService(ZeroRepository zeroRepository, SensorRepository sensorRepository, StandardConfig standardConfig,
                          WebSocketService webSocketService, HistoryService historyService, ZeroService zeroService,
                          SimpMessagingTemplate simpMessagingTemplate, PipeLineService pipeLineService, HttpUtil httpUtil) {
        this.zeroRepository = zeroRepository;
        this.sensorRepository = sensorRepository;
        this.standardConfig = standardConfig;
        this.webSocketService = webSocketService;
        this.historyService = historyService;
        this.zeroService = zeroService;
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.pipeLineService = pipeLineService;
        this.httpUtil = httpUtil;
    }

    public R<Result> findLeak(Date date) {

        R<Result> r = R.nok(200, "");
        Result result = new Result();
        List<Sensor> sensors = sensorRepository.findSensorsByPipelineIdEquals(1L);
        Map<Long, Sensor> sensorMap = historyService.listByTime(date, sensors);
        List<Long> ids = sensors.stream().map(Sensor::getNo).collect(Collectors.toList());
        Map<Long, Zero> zeroMap = zeroService.listMapByIds(ids);
        Map<Long, Flow> flowMap = new HashMap<>(sensors.size() / 3 * 4 + 1);

        //???????????????????????????????????????
        List<Flow> flows = new ArrayList<>();

        List<Leakage> leakages = new ArrayList<>();

        Sensor first = sensors.stream().filter(sensor -> sensor.getType().equals((byte) 4)).findFirst().get();

        Result.SysInfo sysInfo = new Result.SysInfo(first.getNo(), zeroMap.get(first.getNo()).getValue().doubleValue(),
                sensorMap.get(first.getNo()).getCurrentValue().doubleValue());

        //??????????????????????????????
        List<Sensor> headList = sensors.stream().filter(sensor -> sensor.getType().equals((byte) 0)
                || sensor.getType().equals((byte) 4)).collect(Collectors.toList());


        final boolean[] hasFind = {false};
        //???????????????
        headList.forEach(head -> {
            //?????????????????????????????????
            List<Sensor> subList = sensors.stream().filter(sensor ->
                    sensor.getHead().equals(head.getNo())).collect(Collectors.toList());
            //???????????? Zero ???????????????????????????
            double lastValue = 0.0;
            subList.forEach(sub -> {
                Long no = sub.getNo();
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
                Long next = sensorMap.get(leakage.getNext()).getNext();
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

        List<Long> leakResult = new ArrayList<>(pressure.size());

        log.info("???????????? = {}??????????????????????????? {}", pressure.size(), pressure.toString());

        Long firstNo = pressure.get(0).getNo();

        log.info("firstNo = {}???", firstNo);

        Page<Zero> zeroPage = zeroRepository.listZero(firstNo, PageRequest.of(0, pressure.size()));
        List<Zero> zeros = zeroPage.getContent();

        log.info("???????????? = {}????????????????????? {}", zeros.size(), zeros.toString());

        List<BigDecimal> diffpressure = new ArrayList<>(pressure.size());


        //???????????????????????????????????????
        List<Flow> flows = new ArrayList<>();

        diffpressure.add(new BigDecimal("0.0"));
        flows.add(new Flow(1L, pressure.get(0).getCurrentValue().doubleValue(), 0.0));
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
        log.info("???????????? = {}????????????????????? {}", diffpressure.size(), diffpressure.toString());
        sensorRepository.saveAll(pressure);
        result.setFlow(flows);

        log.info("??????????????????{}", flows);

        //???P2???a??????????????????
        if (diffpressure.get(1).compareTo(standardConfig.getStandardDiffPressure()) <= 0) {
            result.setLeakage(Collections.emptyList());
            r.setData(result);
            return r;
        }

        List<Leakage> leakages = new ArrayList<>();

        //???P2>a??????P4???a?????????q???1??????
        if (diffpressure.get(1).compareTo(standardConfig.getStandardDiffPressure()) > 0
                && diffpressure.get(3).compareTo(standardConfig.getStandardDiffPressure()) <= 0) {
            leakages.add(new Leakage(2L, 3L, (byte) 2));
        }

        //???P2>a??????P4>a???????????????q???1??????
        if (diffpressure.get(1).compareTo(standardConfig.getStandardDiffPressure()) > 0
                && diffpressure.get(3).compareTo(standardConfig.getStandardDiffPressure()) > 0) {
            leakages.add(new Leakage(2L, 3L, (byte) 1));
        }

        //???P4>a??????P6???a??????P8???a??????q???5?????????
        if (diffpressure.get(3).compareTo(standardConfig.getStandardDiffPressure()) > 0
                && diffpressure.get(5).compareTo(standardConfig.getStandardDiffPressure()) > 0
                && diffpressure.get(7).compareTo(standardConfig.getStandardDiffPressure()) > 0) {
            leakages.add(new Leakage(4L, 7L, (byte) 2));
        }

        //???P4>a??????P6>a??????P8>a???????????????q???5?????????
        if (diffpressure.get(3).compareTo(standardConfig.getStandardDiffPressure()) > 0
                && (diffpressure.get(5).compareTo(standardConfig.getStandardDiffPressure()) > 0
                || diffpressure.get(7).compareTo(standardConfig.getStandardDiffPressure()) > 0)) {
            leakages.add(new Leakage(4L, 7L, (byte) 1));
        }

        //???P6>a???q???2???q???3???q???4?????????
        if (diffpressure.get(5).compareTo(standardConfig.getStandardDiffPressure()) > 0) {
            leakages.add(new Leakage(5L, 6L, (byte) 2));
        }

        //???P8>a???q???6?????????
        if (diffpressure.get(7).compareTo(standardConfig.getStandardDiffPressure()) > 0) {
            leakages.add(new Leakage(7L, 8L, (byte) 2));
        }

        result.setLeakage(leakages);
        r.setData(result);
        log.info("????????? {}", r);
        webSocketService.sendMsg(r);
        return r;
    }

    /**
     * ???????????????????????????
     *
     * @param id
     */
    public void load(Long id, String time) {
        if (StrUtil.isBlank(time)) {
            time = "2021/04/30 17:20:00";
        }
        //????????????
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = null;
        try {
            date = dateFormat.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        PipeLine pipeLine = pipeLineService.load(id);
        String referencePoint = pipeLine.getReferencePoint();

        List<Sensor> sensors = sensorRepository.findSensorsByPipelineIdEquals(id);

        //?????????ID ??? INFO??????
        Map<Long, Sensor> sensorMap = historyService.listByTime(date, sensors);

        //?????????NO ?????? ??????
        Map<Long, Flow> flowMap = new HashMap<>(sensors.size() / 3 * 4 + 1);

        //????????????
        List<Leakage> leakages = new ArrayList<>();

        //??????????????? ?????? ????????????
        List<Sensor> referencePoints = new ArrayList<>();
        List<Sensor> branchHeadList = sensors.stream().filter(s -> s.getType() == 0 || s.getType() == 4).collect(Collectors.toList());
        final Set<Long> headSet = new HashSet<>();
        if (StrUtil.isNotBlank(referencePoint)) {
            referencePoints.addAll(Arrays.stream(referencePoint.split(",")).map(Long::parseLong).map(sensorMap::get).collect(Collectors.toList()));
            headSet.addAll(referencePoints.stream().map(Sensor::getHead).collect(Collectors.toSet()));
        }
        branchHeadList.forEach(sensor -> {
            if (!referencePoints.contains(sensor) || !(headSet.contains(sensor.getNo()))) {
                referencePoints.add(sensor);
            }
        });

        //1???????????????????????????????????????????????????????????? ????????????????????????
        List<Long> ids = sensors.stream().map(Sensor::getNo).collect(Collectors.toList());
        Map<ZeroKey, Zero> zeroMap = zeroService.listZeroMapByIds(ids);
        referencePoints.forEach(ref -> {
            flowMap.put(ref.getNo(), new Flow(ref.getNo(), 0.0, 0.0));
            buildFlow(sensorMap, zeroMap, flowMap, leakages, ref);
        });

        R<Result> resultR = R.nok(200, "");
        Result result = new Result();
        result.setLeakage(leakages);
        result.setFlow(new ArrayList<>(flowMap.values()));
        resultR.setData(result);
        log.info(JSON.toJSONString(resultR, true));
        boolean sendStatus = httpUtil.postLeak(resultR);
        if (!sendStatus) {
            log.info("[????????????] ????????????:false,5S?????????");
            resultR.setTime(5, TimeUnit.SECONDS);
            PostTaskConsumer.DELAY_QUEUE.put(resultR);
        }
        String destination = "/topic/";
        simpMessagingTemplate.convertAndSend(destination, resultR);
    }

    /**
     * ????????????ID?????????ID ?????????????????????
     *
     * @param id     ??????ID
     * @param target ??????ID
     */
    public void load(Long id, Long target) {
        //Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = null;
        try {
            date = dateFormat.parse("2021/04/30 17:05:00");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        R<Result> resultR = R.nok(200, "");
        Result result = new Result();
        //????????????????????????????????????
        List<Sensor> sensors = sensorRepository.findSensorsByPipelineIdEquals(id);
        //???????????????no:???????????????
        Map<Long, Sensor> sensorMap = historyService.listByTime(date, sensors);
        List<Long> ids = sensors.stream().map(Sensor::getNo).collect(Collectors.toList());
        //?????????????????????
        Map<ZeroKey, Zero> zeroMap = zeroService.listZeroMapByIds(ids);

        //?????????NO ?????? ??????
        Map<Long, Flow> flowMap = new HashMap<>(sensors.size() / 3 * 4 + 1);

        //????????????
        List<Leakage> leakages = new ArrayList<>();

        //???????????????????????????????????????
        //???????????????
        sensors.stream().filter(sensor -> sensor.getNo().equals(target)).findFirst().ifPresent(first -> {

            ZeroKey key = new ZeroKey(first.getNo(), first.getNo());
            Result.SysInfo sysInfo = new Result.SysInfo(first.getNo(), Optional.ofNullable(zeroMap.get(key))
                    .map(Zero::getValue).orElse(BigDecimal.ZERO).doubleValue(), Optional.ofNullable(sensorMap.get(key))
                    .map(Sensor::getCurrentValue).orElse(BigDecimal.ZERO).doubleValue());

            buildFlow(sensorMap, zeroMap, flowMap, leakages, first);
            System.out.println(flowMap.values());
            result.setFlow(new ArrayList<>(flowMap.values()));
            result.setLeakage(leakages);
            result.setSysInfo(sysInfo);
        });
        resultR.setData(result);
        String destination = "/topic/";
        simpMessagingTemplate.convertAndSend(destination, resultR);
    }

    private void buildFlow(Map<Long, Sensor> sensorMap, Map<ZeroKey, Zero> zeroMap, Map<Long, Flow> flowMap,
                           List<Leakage> leakages, Sensor first) {
        //1?????????????????????
        Sensor l = first;
        Sensor r = first;
        //1.1 ???????????????
        while (l.getPre() != null) {
            Sensor pre = sensorMap.get(l.getPre());
            calcFlow(zeroMap, flowMap, first, pre);
            calcLeakage(pre.getNo(), pre.getNext(), flowMap, leakages);
            if (1 == pre.getType()) {
                break;
            }
            l = pre;
        }
        //1.2 ???????????????
        while (r.getNext() != null) {
            Sensor next = sensorMap.get(r.getNext());
            calcFlow(zeroMap, flowMap, first, next);
            calcLeakage(next.getPre(), next.getNo(), flowMap, leakages);
            r = next;
        }
    }

    private void calcFlow(Map<ZeroKey, Zero> zeroMap, Map<Long, Flow> flowMap, Sensor first, Sensor cur) {
        ZeroKey zeroKey = new ZeroKey(cur.getNo(), first.getNo());
        BigDecimal currentValue = Optional.ofNullable(cur.getCurrentValue()).orElse(BigDecimal.ZERO);
        BigDecimal diffValue = Optional.ofNullable(zeroMap.get(zeroKey)).map(Zero::getDiffValue).orElse(BigDecimal.ZERO);
        BigDecimal subtract = Optional.ofNullable(first.getCurrentValue()).orElse(BigDecimal.ZERO).subtract(currentValue.add(diffValue));
        BigDecimal flow = LeakUtil.calcMain(cur.getK(), subtract.multiply(BigDecimal.valueOf(100)), cur.getCalcR(), cur.getCalcL().subtract(first.getL()));
        Flow f = new Flow(first.getPipelineId(), cur.getNo(), first.getNo(), subtract.multiply(BigDecimal.valueOf(100)).doubleValue(), flow.doubleValue(), new Date());
        flowMap.put(cur.getNo(), f);
    }

    private void calcLeakage(Long pre, Long next, Map<Long, Flow> flowMap, List<Leakage> leakages) {
        Double preFlow = flowMap.get(pre).getFlow();
        Double nextFlow = flowMap.get(next).getFlow();
        if (preFlow - nextFlow > 5) {
            Leakage leakage = new Leakage();
            leakage.setPrevious(pre);
            leakage.setNext(next);
            leakage.setValue(preFlow - nextFlow);
            leakages.add(leakage);
        }
    }

    private void buildPreDistance(Map<Long, BigDecimal> distanceMap, Sensor first, Sensor pre) {
        if (pre.getNext().equals(first.getNo())) {
            distanceMap.put(pre.getNo(), pre.getK());
        } else {
            distanceMap.put(pre.getNo(), distanceMap.get(pre.getNext()));
        }
    }

    private void buildNextDistance(Map<Long, BigDecimal> distanceMap, Sensor first, Sensor next) {
        if (next.getPre().equals(first.getNo())) {
            distanceMap.put(next.getNo(), next.getK());
        } else {
            distanceMap.put(next.getNo(), distanceMap.get(next.getPre()));
        }
    }
}
