package com.boyinet.demo.pipelineleakage.dto;

import lombok.Data;

@Data
public class CommonResponse {
    private Integer code;
    private String msg;
    private Long total;
}
