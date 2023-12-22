package com.zhongji.simpleshelf.web.controller;

import com.zhongji.simpleshelf.api.client.http.HttpApiClient;
import com.zhongji.simpleshelf.api.client.mq.kafka.KafkaProducerClient;
import com.zhongji.simpleshelf.api.client.mq.rabbitmq.KgBootRabbitmqClient;
import com.zhongji.simpleshelf.api.client.redis.KgBootRedisClient;
import com.zhongji.simpleshelf.dao.domain.erpsid.CwflNew;
import com.zhongji.simpleshelf.dao.mapper.erpsid.CwflNewMapper;
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
import java.util.List;

@RestController
@RequestMapping("/test")
public class TestController {


    @Autowired
    private CwflNewMapper cwflNewMapper;

    @Autowired
    private HttpApiClient httpApiClient;

    @Autowired
    private KafkaProducerClient kafkaProducerClient;

    @Resource(name = "stringRedisTemplate")
    RedisTemplate redisTemplate;

    @Autowired
    private KgBootRedisClient kgBootRedisClient;


    //    @Resource(name = "kgBootRabbitTemplate")
    @Autowired
    KgBootRabbitmqClient kgBootRabbitmqClient;

    @Autowired
    RabbitTemplate rabbitTemplate;


    @GetMapping(value = "/abc")
    public BaseWebResponse<String> test(@RequestParam String topic,
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
        List<CwflNew> cwflNews = cwflNewMapper.listSomeB("");

        return BaseWebResponse.<String>builder()
                .success(true)
                .data("hello world")
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
