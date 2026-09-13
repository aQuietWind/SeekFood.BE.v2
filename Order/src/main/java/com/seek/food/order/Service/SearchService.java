package com.seek.food.order.Service;

import com.seek.food.dto.Common.EsSearchResult;
import com.seek.food.dto.Order.RiderOrderEsDTO;


public interface SearchService {
    public EsSearchResult<RiderOrderEsDTO> riderSearchOrders(double lat, double lon, int distance,int need,Double docDistance,Long docId);
}
