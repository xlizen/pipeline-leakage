package com.boyinet.demo.pipelineleakage.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author lengchunyun
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Tree {

    private String title;
    private Integer id;
    private Boolean spread;
    private List<Tree> children;

    public Tree(String title, Integer id, Boolean spread) {
        this.title = title;
        this.id = id;
        this.spread = spread;
    }
}
