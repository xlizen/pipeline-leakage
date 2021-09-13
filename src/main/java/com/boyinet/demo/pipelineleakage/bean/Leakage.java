package com.boyinet.demo.pipelineleakage.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author lengchunyun
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Leakage {
    private Long previous;
    private Long next;
    private String direction;
    private Double distance;
    private Byte status;
    private Double value;

    public Leakage(Long previous, Long next, Byte status) {
        this.previous = previous;
        this.next = next;
        this.status = status;
    }
}
