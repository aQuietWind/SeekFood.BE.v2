package com.seek.food.order.Service.Impl;

import com.seek.food.config.NacosConfig.Common.CommonParamRulesConfig;
import com.seek.food.config.NacosConfig.Order.OrderRedisKeyConfig;
import com.seek.food.dto.Common.EsSearchResult;
import com.seek.food.dto.Order.RiderOrderEsDTO;
import com.seek.food.order.Mapper.SearchMapper;
import com.seek.food.order.Service.SearchService;
import com.seek.food.util.Context.TokenIdContext;
import com.seek.food.util.Redis.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@RefreshScope
@Service
@Slf4j
public class SearchServiceImpl implements SearchService {


    private final OrderRedisKeyConfig orderRedisKeyConfig;
    private final StringRedisTemplate stringRedisTemplate;
    private final CommonParamRulesConfig commonParamRulesConfig;
    private final SearchMapper searchMapper;

    public SearchServiceImpl(OrderRedisKeyConfig orderRedisKeyConfig, StringRedisTemplate stringRedisTemplate, CommonParamRulesConfig commonParamRulesConfig, SearchMapper searchMapper) {
        this.orderRedisKeyConfig = orderRedisKeyConfig;
        this.stringRedisTemplate = stringRedisTemplate;
        this.commonParamRulesConfig = commonParamRulesConfig;
        this.searchMapper = searchMapper;
    }

    //骑手获取未被抢的订单
    @Override
    public EsSearchResult<RiderOrderEsDTO> riderSearchOrders(double lat, double lon, int distance,int need, Double docDistance, Long docId){
        //检查参数
        commonParamRulesConfig.lonAndLatCheck(lon,lat);
        commonParamRulesConfig.needNumberCheck(need);
        if (docId != null)commonParamRulesConfig.commonIdCheck(docId);
        //获取骑手id
        long riderId= TokenIdContext.getAndCheck(commonParamRulesConfig.getRiderIdStart(),commonParamRulesConfig.getIdCapacity());
        //检查冷却期
        RedisUtil.checkCooldown(stringRedisTemplate,orderRedisKeyConfig.getOrderRiderGetEsOrderCooldown().getRedisKey(riderId)
                ,orderRedisKeyConfig.getOrderRiderGetEsOrderCooldown().getDuration());
        //返回结果
        return searchMapper.feedRiderOrder(lat,lon,distance,need,docDistance,docId);
    }


















}
