package com.boyinet.demo.pipelineleakage.bean.primary;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Date;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Flow {

    @Id()
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Long no;
    private Long target;
    private Long pipelineId;
    private Double pressure;
    private Date time;
    private Double flow;

    public Flow(Long id, Double pressure, Double flow) {
        this.no = id;
        this.pressure = pressure;
        this.flow = flow;
    }

    public Flow(Long pipelineId, Long no, Long target, Double pressure, Double flow, Date time) {
        this.pipelineId = pipelineId;
        this.no = no;
        this.target = target;
        this.pressure = pressure;
        this.flow = flow;
        this.time = time;
    }
}
