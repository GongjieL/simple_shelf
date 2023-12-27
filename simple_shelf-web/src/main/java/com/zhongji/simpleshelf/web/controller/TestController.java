package com.zhongji.simpleshelf.web.controller;

import com.zhongji.simpleshelf.common.bo.bi.SummaryCaliber;
import com.zhongji.simpleshelf.common.bo.bi.orderandinvoice.BDOrderAndInvoiceSummary;
import com.zhongji.simpleshelf.common.enums.TimeDescEnum;
import com.zhongji.simpleshelf.core.service.impl.BiStatisticsSummaryServiceImpl;
import com.zhongji.simpleshelf.api.client.http.HttpApiClient;
import com.zhongji.simpleshelf.api.client.mq.kafka.KafkaProducerClient;
import com.zhongji.simpleshelf.api.client.mq.rabbitmq.KgBootRabbitmqClient;
import com.zhongji.simpleshelf.api.client.redis.KgBootRedisClient;
import com.zhongji.simpleshelf.common.enums.TimeEnum;
import com.zhongji.simpleshelf.dao.domain.StatisticsSummary;
import com.zhongji.simpleshelf.dao.service.impl.CwflNewServiceImpl;
import com.zhongji.simpleshelf.util.DateUtils;
import com.zhongji.simpleshelf.web.response.BaseWebResponse;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/test")
public class TestController {


    @Autowired
    private CwflNewServiceImpl cwflNewService;

    @Autowired
    private BiStatisticsSummaryServiceImpl biStatisticsSummaryService;

    @Autowired
    private HttpApiClient httpApiClient;

//    @Autowired
//    private KafkaProducerClient kafkaProducerClient;

//    @Resource(name = "stringRedisTemplate")
//    RedisTemplate redisTemplate;

//    @Autowired
//    private KgBootRedisClient kgBootRedisClient;


    //    @Resource(name = "kgBootRabbitTemplate")
    @Autowired
    KgBootRabbitmqClient kgBootRabbitmqClient;

    @Autowired
    RabbitTemplate rabbitTemplate;


    @GetMapping(value = "/abc")
    public BaseWebResponse<List<BDOrderAndInvoiceSummary>> test(@RequestParam String topic,
                                                                @RequestParam String message) {
//        kgBootRabbitmqClient.syncSend(topic,message);
//        kgBootRabbitmqClient.send(topic, "hello");
//        rabbitTemplate.convertAndSend(exchangeName,"hello","hello world", correlationData);

//        System.out.println(1);
//        for (int i = 0; i < 5; i++) {
//            kafkaProducerClient.sendMessageAsync(topic, 1, null, message);
//        }
//        Subject subject = SecurityUtils.getSubject();
//        kgbootSessionService.list();
//        UsernamePasswordToken token = new UsernamePasswordToken("zhangsan", "123456");
//        subject.login(token);
//        System.out.println(1);

//        ListenableFuture<CacheExecuteResult> future =
//                kgBootRedisClient.eliminateCache("jjj");
//        future.addCallback(new ListenableFutureCallback<CacheExecuteResult>() {
//            @Override
//            public void onFailure(Throwable ex) {
//                System.out.println("failure");
//            }
//
//            @Override
//            public void onSuccess(CacheExecuteResult result) {
//                System.out.println("success");
//            }
//        });
//
//        redisTemplate.opsForValue().get("");
////        kafkaProducerClient.sendMessageAsync("test-1",null,null,"test abc");
//        HttpBaseRequest<Map<String, String>> request = new HttpBaseRequest<>();
//        operateLogService.test();
//        request.setUrl("http://43.135.135.141:8080/openai/auth");
//        request.setHttpMethod(HttpMethod.GET);
//        Map<String, Object> data = new HashMap<>();
//        data.put("wd", "%E8%A7%A3%E6%9E%90");
//        request.setUrlVariables(data);
//        request.setAnalysisRespCode("common");
//        HttpBaseResponse<String> httpResponse = httpApiClient.getHttpResponse(request);
        LocalDate startLocalDate = DateUtils.getIntervalTime(LocalDate.now(), TimeEnum.MONTH, -1);
        LocalDate endLocalDate = DateUtils.getIntervalTime(startLocalDate, TimeEnum.MONTH, 1);


        Date startDate = Date.from(startLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(endLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        List<String> list = new ArrayList<>();
        list.add(TimeEnum.MONTH.getCode());
        list.add(TimeEnum.WEEK.getCode());
        List<SummaryCaliber> summaryCalibers = new ArrayList<>();
        SummaryCaliber weekSummaryCaliber = new SummaryCaliber();
        weekSummaryCaliber.setTimeUnit(TimeEnum.WEEK.getCode());
        weekSummaryCaliber.setTimeDesc(TimeDescEnum.PRE.getCode());
        SummaryCaliber monthSummaryCaliber = new SummaryCaliber();
        monthSummaryCaliber.setTimeUnit(TimeEnum.MONTH.getCode());
        monthSummaryCaliber.setTimeDesc(TimeDescEnum.PRE.getCode());

        SummaryCaliber yearSummaryCaliber = new SummaryCaliber();
        yearSummaryCaliber.setTimeUnit(TimeEnum.YEAR.getCode());
        yearSummaryCaliber.setTimeDesc(TimeDescEnum.PRE.getCode());

        summaryCalibers.add(weekSummaryCaliber);
        summaryCalibers.add(monthSummaryCaliber);
        summaryCalibers.add(yearSummaryCaliber);

//        List<BDOrderAndInvoiceSummary> bdOrderAndInvoiceSummaries = biStatisticsSummaryService.buildBDOrderAndInvoiceSummary(TimeDescEnum.PRE.name(), list);
        List<BDOrderAndInvoiceSummary> bdOrderAndInvoiceSummaries = biStatisticsSummaryService.buildBDOrderAndInvoiceSummary(new Date(), summaryCalibers);

//        List<StatisticsSummary> statisticsSummaries = cwflNewService.listErpSidSummary(startDate, endDate);

        return BaseWebResponse.<List<BDOrderAndInvoiceSummary>>builder()
                .success(true)
                .data(bdOrderAndInvoiceSummaries)
                .code(200)
                .build();
    }


    @GetMapping(value = "/bcd")
    public BaseWebResponse<String> test2(@RequestParam String abc) {
        Subject subject = SecurityUtils.getSubject();
        return null;
    }

    public static void main(String[] args) throws InterruptedException {
        while (true) {
            System.out.println(foo());
            Thread.sleep(3000);
        }
    }

    private static int foo() {
        return 100;
    }


}
