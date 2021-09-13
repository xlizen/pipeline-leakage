package com.boyinet.demo.pipelineleakage.service;

import com.boyinet.demo.pipelineleakage.bean.primary.Flow;
import com.boyinet.demo.pipelineleakage.common.R;
import com.boyinet.demo.pipelineleakage.repository.primary.FlowRepository;
import com.boyinet.demo.pipelineleakage.vo.FlowVO;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FlowService {

    private final FlowRepository flowRepository;

    public FlowService(FlowRepository flowRepository) {
        this.flowRepository = flowRepository;
    }

    public R<List<Flow>> list(FlowVO vo) {
        Page<Flow> page = flowRepository.list(vo);
        R<List<Flow>> ok = R.ok(page.getContent());
        ok.setTotal(page.getTotalElements());
        return ok;
    }
}
