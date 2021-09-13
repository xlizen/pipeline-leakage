package com.boyinet.demo.pipelineleakage.bean.primary;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.IdClass;
import java.math.BigDecimal;


/**
 * @author lengchunyun
 */
@Data
@Entity
@ToString
@IdClass(ZeroKey.class)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Zero extends ZeroKey {

    private BigDecimal diffValue;
    private BigDecimal value;

    public Zero(Long no, Long target, BigDecimal diffValue, BigDecimal value) {
        super(no, target);
        this.diffValue = diffValue;
        this.value = value;
    }
}
