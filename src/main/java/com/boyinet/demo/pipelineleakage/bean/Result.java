package com.boyinet.demo.pipelineleakage.bean;

import com.boyinet.demo.pipelineleakage.bean.primary.Flow;
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
        private Long no;
        private Double standard;
        private Double current;
    }
}
