package com.boyinet.demo.pipelineleakage.bean;


import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author lengchunyun
 */
@Data
@Entity
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Sensor {

    @Id()
    private Integer no;

    @ExcelIgnore
    private BigDecimal currentValue;

    @ExcelIgnore
    private BigDecimal diffValue;

    /**
     * 系数k
     */
    private BigDecimal k;


    private Integer pre;

    private Integer next;

    /**
     * 距离标准的距离
     */
    private BigDecimal l;

    /**
     * 管路半径 ，只需在头结点处标明
     */
    private BigDecimal r;

    /**
     * 0 为头结点 1 为非头
     */
    private Byte type;

    private Integer head;

    @ExcelIgnore
    private Integer pipelineId;

    private String point;


    @ExcelIgnore
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(value = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
}
