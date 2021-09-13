package com.boyinet.demo.pipelineleakage.dto;

import lombok.Data;

import java.util.Date;

@Data
public class PressureValue {
    private Long id;
    private Double value;
    private Date time;
}
