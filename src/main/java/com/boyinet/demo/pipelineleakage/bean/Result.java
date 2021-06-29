package com.boyinet.demo.pipelineleakage.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author lengchunyun
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result {

    private List<Leakage> leakage;

    private List<Flow> flow;

    private SysInfo sysInfo;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SysInfo {
        private Integer no;
        private Double standard;
        private Double current;
    }
}
