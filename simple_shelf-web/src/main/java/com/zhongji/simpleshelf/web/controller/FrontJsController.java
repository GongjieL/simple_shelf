package com.zhongji.simpleshelf.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class FrontJsController {
    @GetMapping("/index")
    public String index() {
        return "index";
    }

    @GetMapping("/index2")
    public String index2() {
        //1、FLD45 机动车、销售增值 2、内销，空白 3（4）、FLD0，不锈钢、吕河、碳钢 4、（cwfl_new.shuom like '%液罐%' or cwfl_new.shuom like '%箱%'）
        //5、
        return "index2";
    }

    @GetMapping("/index3")
    public String index3() {
        return "index3";
    }
}
