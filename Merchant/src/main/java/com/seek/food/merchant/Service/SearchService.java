package com.seek.food.merchant.Service;

import com.seek.food.dto.Common.EsSearchResult;
import com.seek.food.dto.Merchant.MerchantEsDTO;


public interface SearchService {
    public EsSearchResult<MerchantEsDTO> feedMerchant(int need, int distance, double lon, double lat, long seed, int shouldAmount
            ,Double docScore,Long docId);
    public EsSearchResult<MerchantEsDTO> searchMerchant(String merchantName,double lon, double lat, int distance,int need
            ,Double docScore,Long docId);
}
