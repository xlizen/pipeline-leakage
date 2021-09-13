package com.boyinet.demo.pipelineleakage.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchParam {
    private Long id;
    private Date start;
    private Date end;
    private Integer pi;
    private Integer ps;

    public SearchParam(Long id, Date start, Date end) {
        this.id = id;
        this.start = start;
        this.end = end;
    }
}
