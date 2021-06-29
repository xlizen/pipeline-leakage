package com.boyinet.demo.pipelineleakage.controller;

import cn.hutool.core.util.StrUtil;
import com.boyinet.demo.pipelineleakage.bean.PipeLine;
import com.boyinet.demo.pipelineleakage.common.R;
import com.boyinet.demo.pipelineleakage.common.Tree;
import com.boyinet.demo.pipelineleakage.service.LeakageService;
import com.boyinet.demo.pipelineleakage.service.PipeLineService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;


/**
 * @author lengchunyun
 */
@RestController
@RequestMapping("pipeline")
public class PipeLineController {

    private final PipeLineService pipeLineService;

    private final LeakageService leakageService;

    public PipeLineController(PipeLineService pipeLineService, LeakageService leakageService) {
        this.pipeLineService = pipeLineService;
        this.leakageService = leakageService;
    }

    @PostMapping("insert")
    R<Map<String, Object>> insert(PipeLine pipeLine) {
        if (StrUtil.isNotBlank(pipeLine.getXlsFileName())) {
            pipeLineService.save(pipeLine);
        } else {
            return R.nok();
        }
        return R.ok();
    }

    @GetMapping("list")
    R<List<PipeLine>> list(Integer page, Integer limit, String searchParams) {
        Page<PipeLine> pipeLinePage = pipeLineService.list(page, limit, searchParams);
        R<List<PipeLine>> ok = R.ok();
        ok.setData(pipeLinePage.getContent());
        ok.setTotal(pipeLinePage.getTotalElements());
        ok.setPage(pipeLinePage.getPageable());
        return ok;
    }

    @PostMapping("update")
    R<Map<String, Object>> update(PipeLine pipeLine) {
        pipeLineService.updateById(pipeLine);
        return R.ok();
    }

    @GetMapping("delete")
    R<Map<String, Object>> delete(Integer id) {
        pipeLineService.deleteById(id);
        return R.ok();
    }

    @PostMapping("tree")
    R<List<Tree>> tree() {
        return R.ok(pipeLineService.tree());
    }

    @GetMapping("load")
    public R<PipeLine> load(Integer id) {
        PipeLine pipeLine = pipeLineService.load(id);
        return R.ok(pipeLine);
    }

    @GetMapping("loadLeakage")
    public R<Void> loadLeakage(Integer id) {
        leakageService.load(id);
        return R.ok();
    }
}
