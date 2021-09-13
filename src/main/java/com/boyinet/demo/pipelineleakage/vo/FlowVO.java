package com.boyinet.demo.pipelineleakage.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlowVO {

    private String pipelineId;
    private String sensorId;
    private Date startTime;
    private Date endTime;
    private Integer page;
    private Integer limit;
}
