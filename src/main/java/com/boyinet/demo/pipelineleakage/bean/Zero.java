package com.boyinet.demo.pipelineleakage.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.math.BigDecimal;

/**
 * @author lengchunyun
 */
@Data
@Entity
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Zero {

    @Id
    private Integer no;
    private BigDecimal diffValue;
    private BigDecimal value;
}
