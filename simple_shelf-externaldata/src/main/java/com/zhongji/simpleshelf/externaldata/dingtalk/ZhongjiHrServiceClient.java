package com.zhongji.simpleshelf.externaldata.dingtalk;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zhongji.simpleshelf.api.client.http.HttpApiClient;
import com.zhongji.simpleshelf.api.client.http.HttpBaseRequest;
import com.zhongji.simpleshelf.api.client.http.HttpBaseResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.Map;

@Service
public class ZhongjiHrServiceClient {
    @Autowired
    private HttpApiClient httpApiClient;


    private static final Integer DEFAULT_PAGE_SIZE = 2000;

    private static final String API_KEY = "*";
    private static final String P_PWD = "*";
    private static final String P_USER = "*";
    private static final String DEPT_URL = "*";
    private static final String USER_URL = "*";


    /**
     * 列出部门
     * @param pageNum
     * @return
     */

    public JSONArray listDepartments(Integer pageNum) {
        //查询
        HttpBaseRequest httpBaseRequest = new HttpBaseRequest();
        httpBaseRequest.setHttpMethod(HttpMethod.GET);
        httpBaseRequest.setUrl(DEPT_URL);
        httpBaseRequest.setAnalysisRespCode("zhongjiHr");
        Map<String, Object> data = new HashMap<>();
        data.put("p_user", P_USER);
        data.put("p_password", P_PWD);
        data.put("apikey", API_KEY);
        data.put("p_page_num", pageNum);
        httpBaseRequest.setUrlVariables(data);
        HttpBaseResponse<JSONObject> httpResponse = httpApiClient.getHttpResponse(httpBaseRequest);
        //todo retry
        if (!httpResponse.getSuccess()) {
            return null;
        }
        JSONArray jsonArray = null;
        try {
            jsonArray = httpResponse.getData().getJSONObject("soap:Envelope")
                    .getJSONObject("soap:Body")
                    .getJSONObject("unit_record")
                    .getJSONArray("record");
        } catch (Exception e) {
            //todo 异常信息

        }
        return jsonArray;
    }

    
    private void getByDataPageNum(HttpBaseRequest httpBaseRequest) {
        //获取数据
        HttpBaseResponse<String> httpResponse = httpApiClient.getHttpResponse(httpBaseRequest);
        //todo 后续跳过而不结束
        if (!httpResponse.getSuccess()) {
            return;
        }
        JSONArray records = JSON.parseObject(httpResponse.getData()).getJSONObject("soap:Envelope")
                .getJSONObject("unit_record")
                .getJSONArray("record");
        if (CollectionUtils.isEmpty(records) || records.size() < DEFAULT_PAGE_SIZE) {
            return;
        }
        //todo 处理逻辑 spi
        httpBaseRequest.getUrlVariables().put("p_page_num", (Integer) httpBaseRequest.getUrlVariables().get("p_page_num") + 1);
        getByDataPageNum(httpBaseRequest);
    }

}
