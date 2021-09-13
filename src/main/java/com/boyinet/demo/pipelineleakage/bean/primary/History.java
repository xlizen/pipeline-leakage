package com.boyinet.demo.pipelineleakage.bean.primary;

import com.alibaba.excel.annotation.format.DateTimeFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author lengchunyun
 */
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class History {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private BigDecimal value;
    @DateTimeFormat(value = "yyyy-MM-dd HH:mm:ss")
    private Date time;
    private Long no;

    public History(BigDecimal value, Date time, Long no) {
        this.value = value;
        this.time = time;
        this.no = no;
    }
}
