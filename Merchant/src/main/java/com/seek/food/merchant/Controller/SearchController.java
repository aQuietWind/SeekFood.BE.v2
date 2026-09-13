package com.seek.food.merchant.Controller;

import com.seek.food.dto.Common.EsSearchResult;
import com.seek.food.dto.Common.Result;
import com.seek.food.dto.Merchant.MerchantEsDTO;
import com.seek.food.merchant.Enum.RequestPathEnum;
import com.seek.food.merchant.Service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(RequestPathEnum.Search)
public class SearchController {

    private final SearchService searchService;

    @Autowired
    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    //主动推送的商家，参数主要由前端决定
    @GetMapping(RequestPathEnum.Search_Feed)
    public Result<EsSearchResult<MerchantEsDTO>> feedMerchant(int need, int distance, double lon, double lat, long seed, int shouldAmount
            ,Double docScore,Long docId){
        return Result.success(searchService.feedMerchant(need,distance,lon,lat,seed,shouldAmount,docScore,docId));
    }

    //搜索商家，参数主要由前端决定
    @GetMapping
    public Result<EsSearchResult<MerchantEsDTO>> feedMerchant(String merchantName,double lon, double lat, int distance,int need
            ,Double docScore,Long docId){
        return Result.success(searchService.searchMerchant(merchantName,lon,lat,distance,need,docScore,docId));
    }

















}
