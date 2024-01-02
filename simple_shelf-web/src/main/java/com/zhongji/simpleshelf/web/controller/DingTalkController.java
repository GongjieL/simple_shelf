package com.zhongji.simpleshelf.web.controller;

import com.zhongji.simpleshelf.core.service.impl.DingTalkServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/dingtalk")
public class DingTalkController {
    @Autowired
    private DingTalkServiceImpl dingTalkService;
    @GetMapping("/batchCreateDepartments")
    public String batchCreateDepartments() throws IOException {
        dingTalkService.batchUpdateDepartments();
        return null;
    }

}
