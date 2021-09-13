package com.boyinet.demo.pipelineleakage.bean.primary;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * @author lengchunyun
 */
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Setting {

    @Id
    private Long id;
    private String name;
    private String value;
}
