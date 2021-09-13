package com.boyinet.demo.pipelineleakage.util;

import com.boyinet.demo.pipelineleakage.bean.Result;
import com.boyinet.demo.pipelineleakage.common.R;
import com.boyinet.demo.pipelineleakage.dto.CommonResponse;
import com.boyinet.demo.pipelineleakage.dto.PressureValueResponse;
import com.boyinet.demo.pipelineleakage.dto.SearchParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class HttpUtil {

    @Value("${pressure.history.get.url}")
    String getValueUrl;

    @Value("${leakage.result.post.url}")
    String postLeakUrl;


    private final RestTemplate restTemplate;

    public HttpUtil(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public PressureValueResponse getValue(SearchParam searchParam) {
        ResponseEntity<PressureValueResponse> responseEntity = restTemplate.postForEntity(getValueUrl, searchParam, PressureValueResponse.class);
        return responseEntity.getBody();
    }

    public boolean postLeak(R<Result> data) {
        ResponseEntity<CommonResponse> responseEntity = restTemplate.postForEntity(postLeakUrl, data, CommonResponse.class);
        return Integer.valueOf(200).equals(responseEntity.getBody().getCode());
    }
}
