package com.seek.food.merchant.Service.Impl;

import com.seek.food.config.NacosConfig.Common.CommonParamRulesConfig;
import com.seek.food.config.NacosConfig.Merchant.MerchantParamsRulesConfig;
import com.seek.food.dto.Common.EsSearchResult;
import com.seek.food.dto.Merchant.MerchantEsDTO;
import com.seek.food.merchant.Mapper.SearchMapper;
import com.seek.food.merchant.Service.SearchService;
import com.seek.food.util.Exception.BizException;
import com.seek.food.util.Exception.ErrorCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RefreshScope
public class SearchServiceImpl implements SearchService {

    private final SearchMapper searchMapper;
    private final CommonParamRulesConfig commonParamRulesConfig;
    private final MerchantParamsRulesConfig merchantParamsRulesConfig;

    @Autowired
    public SearchServiceImpl(SearchMapper searchMapper, CommonParamRulesConfig commonParamRulesConfig, MerchantParamsRulesConfig merchantParamsRulesConfig) {
        this.searchMapper = searchMapper;
        this.commonParamRulesConfig = commonParamRulesConfig;
        this.merchantParamsRulesConfig = merchantParamsRulesConfig;
    }

    //主动推送
    @Override
    public EsSearchResult<MerchantEsDTO> feedMerchant(int need, int distance, double lon, double lat, long seed
    , int shouldAmount,Double docScore,Long docId) {
        commonParamRulesConfig.lonAndLatCheck(lon, lat);
        commonParamRulesConfig.needNumberCheck(need);
        commonParamRulesConfig.seedNumberCheck(seed);
        commonParamRulesConfig.shouldAmountCheck(shouldAmount);
        return searchMapper.feedMerchant(lon,lat,distance,seed,need,shouldAmount,docScore,docId);
    }

    //搜索商家
    @Override
    public EsSearchResult<MerchantEsDTO> searchMerchant(String merchantName,double lon, double lat, int distance,int need
            ,Double docScore,Long docId) {
        if (merchantName==null)throw new BizException(ErrorCodeEnum.PARAM_ERROR);
        merchantParamsRulesConfig.merchantNameCheck(merchantName);
        commonParamRulesConfig.lonAndLatCheck(lon, lat);
        commonParamRulesConfig.needNumberCheck(need);
        return searchMapper.searchMerchant(merchantName,lon,lat,distance,need,docScore,docId);
    }
}
