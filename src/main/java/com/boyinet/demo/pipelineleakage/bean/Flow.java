package com.boyinet.demo.pipelineleakage.bean;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author lengchunyun
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Flow {

    private Integer id;
    private Double pressure;
    private Double flow;
}
