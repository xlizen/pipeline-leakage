package com.boyinet.demo.pipelineleakage.dto;

import lombok.Data;

import java.util.List;

@Data
public class PressureValueResponse extends CommonResponse {

    private List<PressureValue> data;

}
