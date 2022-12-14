package com.mengxiao.seckill.service.impl;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.common.utils.UuidUtils;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.common.to.mq.SeckillOrderTo;
import com.common.utils.R;
import com.common.vo.MemberResponseVo;
import com.mengxiao.seckill.feign.CouponFeignService;
import com.mengxiao.seckill.feign.ProductFeignService;
import com.mengxiao.seckill.interceptor.LoginUserInterceptor;
import com.mengxiao.seckill.service.SeckillService;
import com.mengxiao.seckill.to.SecKillSkuRedisTo;
import com.mengxiao.seckill.vo.SeckillSessionsWithSkus;
import com.mengxiao.seckill.vo.SkuInfoVo;
import com.mysql.cj.Session;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.i18n.filter.UntrustedUrlInput;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.xml.crypto.Data;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
@Slf4j
@Service
public class SeckillServiceImpl implements SeckillService {
    @Autowired
    CouponFeignService couponFeignService;
    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    ProductFeignService productFeignService;
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    RedissonClient redissonClient;
    public static final String SESSIONS_CHANGE_PREFIX="seckill:sessions:";
    public static final String SKUKILL_CHANGE_PREFIX="seckill:skus";
    public static final String SKU_STOCK_SEMAPHORE="seckill:stock";
    @Override
    public void uploadSeckillSkuLatest3Days() {
        //1.?????????????????????????????????????????????
        R session = couponFeignService.getLates3DaySession();
        if (session.getCode()==0){
            //????????????
            List<SeckillSessionsWithSkus> sessionData = session.getData(new TypeReference<List<SeckillSessionsWithSkus>>() {
            });
            //?????????redis
            //1.??????????????????
            saveSessionInfos(sessionData);
            //2.?????????????????????????????????
            saveSessionSkuInfos(sessionData);
        }
    }

    public List<SecKillSkuRedisTo> blockHandler(BlockException e){
        log.error("getCurrentSeckillSkusResource????????????...");
        return null;
    }
    //???????????????????????????????????????????????????
    @SentinelResource(value = "getCurrentSeckillSkusResource",blockHandler = "blockHandler")
    @Override
    public List<SecKillSkuRedisTo> getCurrentSeckillSkus() {
        //1.??????????????????????????????????????????
        long time = new Date().getTime();
        try(Entry entry= SphU.entry("seckillSkus")) {
            Set<String> keys = redisTemplate.keys(SESSIONS_CHANGE_PREFIX + "*");
            for (String key : keys) {
                String replace = key.replace(SESSIONS_CHANGE_PREFIX, "");
                String[] s = replace.split("_");
                long start = Long.parseLong(s[0]);
                long end = Long.parseLong(s[1]);
                if (time>=start&&time<=end){
                    //2.???????????????????????????????????????????????????
                    List<String> range = redisTemplate.opsForList().range(key, -100, 100);
                    BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SKUKILL_CHANGE_PREFIX);
                    List<String> list = hashOps.multiGet(range);
                    if (list!=null){
                        List<SecKillSkuRedisTo> collect = list.stream().map(item -> {
                            SecKillSkuRedisTo redis = JSON.parseObject((String) item, SecKillSkuRedisTo.class);
                            return redis;
                        }).collect(Collectors.toList());
                        return collect;
                    }
                    break;
                }
            }
        }catch (BlockException e){
            log.error("??????????????????{}",e.getMessage());
        }
        return null;
    }

    @Override
    public SecKillSkuRedisTo getSkuSeckillInfo(Long skuId) {
        //??????????????????????????????????????????key
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SKUKILL_CHANGE_PREFIX);
        Set<String> keys = hashOps.keys();
        if (keys!=null&&keys.size()>0){
            String regx = "//d_" + skuId;
            for (String key : keys) {
                if (Pattern.matches(regx,key)){
                    String json = hashOps.get(key);
                    SecKillSkuRedisTo skuRedisTo = JSON.parseObject(json, SecKillSkuRedisTo.class);
                    //?????????
                    long current = new Date().getTime();
                    if (current>=skuRedisTo.getStartTime()&&current<=skuRedisTo.getEndTime()){
                    }else {
                        skuRedisTo.setRandomCode(null);
                    }
                    return skuRedisTo;
                }
            }
        }
        return null;
    }

    @Override
    public String kill(String killId, String key, Integer num) {
        MemberResponseVo respVo = LoginUserInterceptor.loginUser.get();
        //1.???????????????????????????????????????
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SKUKILL_CHANGE_PREFIX);
        String json = hashOps.get(killId);
        if (StringUtils.isEmpty(json)){
            return null;
        }else {
            SecKillSkuRedisTo redis = JSON.parseObject(json, SecKillSkuRedisTo.class);
            //???????????????
            Long startTime = redis.getStartTime();
            Long endTime = redis.getEndTime();
            long time = new Date().getTime();
            long ttl = endTime - startTime;
            //1.????????????????????????
            if (time>=startTime&&time<=endTime){
                //2.????????????????????????id
                String randomCode = redis.getRandomCode();
                String skuId = redis.getPromotionSessionId() + "_" + redis.getSkuId();
                if (randomCode.equals(key)&&killId.equals(skuId)){
                    //3.??????????????????????????????
                    if (num<redis.getSeckillLimit()){
                        //4.?????????????????????????????????
                        String redisKey = respVo.getId() + "_" + skuId;
                        //????????????
                        Boolean aBoolean = redisTemplate.opsForValue().setIfAbsent(redisKey, num.toString(),ttl,TimeUnit.MICROSECONDS);
                        if (aBoolean){
                            //????????????????????????????????????
                            RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + randomCode);
                            boolean b = semaphore.tryAcquire(num);
                            if (b){
                                String timeId = IdWorker.getTimeId();
                                SeckillOrderTo orderTo = new SeckillOrderTo();
                                orderTo.setOrderSn(timeId);
                                orderTo.setMemberId(respVo.getId());
                                orderTo.setNum(num);
                                orderTo.setPromotionSessionId(redis.getPromotionSessionId());
                                orderTo.setSkuId(redis.getSkuId());
                                orderTo.setSeckillPrice(redis.getSeckillPrice());
                                rabbitTemplate.convertAndSend("order-event-exchange","order.seckill.order",orderTo);
                                return timeId;
                            }else {
                                return null;
                            }
                        }else
                            return null;
                    }
                }else {
                    return null;
                }
            }else {
                return null;
            }
        }
        return null;
    }

    private void saveSessionSkuInfos(List<SeckillSessionsWithSkus> sessions) {
        sessions.stream().forEach(session->{
            long startTime = session.getStartTime().getTime();
            long endTime = session.getEndTime().getTime();
            String key = SESSIONS_CHANGE_PREFIX + startTime + "_" + endTime;
            Boolean hasKey=redisTemplate.hasKey(key);
            if (!hasKey){
                List<String> collect = session.getRelationSkus().stream().map(item ->item.getPromotionSessionId()+"_"+item.getSkuId().toString()).collect(Collectors.toList());
                //??????????????????
                redisTemplate.opsForList().leftPushAll(key,collect);
            }
        });
    }

    private void saveSessionInfos(List<SeckillSessionsWithSkus> sessions) {
        sessions.stream().forEach(session->{
            //??????hash??????
            BoundHashOperations<String, Object, Object> ops = redisTemplate.boundHashOps(SKUKILL_CHANGE_PREFIX);
            session.getRelationSkus().stream().forEach(seckillSkuVo -> {
                //4.?????????
                String token = UUID.randomUUID().toString().replace("-", "");
                if (!ops.hasKey(seckillSkuVo.getPromotionSessionId()+"_"+seckillSkuVo.getSkuId().toString())){
                    //????????????
                    SecKillSkuRedisTo redisTo = new SecKillSkuRedisTo();
                    //1.sku???????????????
                    R skuInfo = productFeignService.getSkuInfo(seckillSkuVo.getSkuId());
                    if (skuInfo.getCode()==0){
                        SkuInfoVo info = skuInfo.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                        });
                        redisTo.setSkuInfoVo(info);
                    }
                    //2.sku???????????????
                    BeanUtils.copyProperties(seckillSkuVo,redisTo);
                    //3.??????????????????????????????????????????
                    redisTo.setStartTime(session.getStartTime().getTime());
                    redisTo.setEndTime(session.getEndTime().getTime());
                    redisTo.setRandomCode(token);
                    String jsonString = JSON.toJSONString(redisTo);
                    ops.put(seckillSkuVo.getSkuId().toString(),jsonString);
                    //5.???????????????????????????????????????
                    RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + token);
                    //??????????????????????????????????????????
                    semaphore.trySetPermits(seckillSkuVo.getSeckillCount());
                }
            });
        });
    }
}
