package com.zhongji.simpleshelf.api.strategy.http;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.zhongji.simpleshelf.api.client.http.HttpBaseResponse;
import org.springframework.stereotype.Service;

public class ZhongjiHrRespProcessor extends AbstractRespProcessor<JSONObject> {
    @Override
    public String processorCode() {
        return "zhongjiHr";
    }

    @Override
    public HttpBaseResponse<JSONObject> analysisResp(String respStr) {
        HttpBaseResponse<JSONObject> httpBaseResponse = new HttpBaseResponse();
        httpBaseResponse.setData(
                JSON.parseObject(respStr));
        httpBaseResponse.setSuccess(true);
        httpBaseResponse.setCode(200);
        return httpBaseResponse;
    }
}
