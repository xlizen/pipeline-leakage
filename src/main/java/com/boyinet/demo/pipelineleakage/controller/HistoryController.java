package com.boyinet.demo.pipelineleakage.controller;

import com.boyinet.demo.pipelineleakage.bean.primary.Flow;
import com.boyinet.demo.pipelineleakage.common.R;
import com.boyinet.demo.pipelineleakage.service.FlowService;
import com.boyinet.demo.pipelineleakage.service.HistoryService;
import com.boyinet.demo.pipelineleakage.vo.FlowVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


@RestController
@Slf4j
@RequestMapping("flow")
public class HistoryController {

    public static final String DATE_TIME_FORMAT = "yyyy/MM/dd HH:mm:ss";

    private final FlowService flowService;

    private final HistoryService historyService;

    public HistoryController(FlowService flowService, HistoryService historyService) {
        this.flowService = flowService;
        this.historyService = historyService;
    }

    @RequestMapping("list")
    public R<List<Flow>> listFlow(FlowVO vo) {
        return flowService.list(vo);
    }


    @RequestMapping("recordStand")
    public R<Void> recordStand(String startTime, String endTime, Long pipelineId) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_TIME_FORMAT);
        Date start, end;
        try {
            start = simpleDateFormat.parse(startTime);
            end = simpleDateFormat.parse(endTime);
            historyService.recordStableValueByPipelineId(start, end, pipelineId);
            return R.ok("记录完成");
        } catch (ParseException e) {
            log.warn(e.getMessage(), e);
            return R.nok("记录失败，请重试");
        }
    }

}
