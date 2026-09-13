package com.seek.food.order.Controller;

import com.seek.food.dto.Common.EsSearchResult;
import com.seek.food.dto.Common.Result;
import com.seek.food.dto.Order.RiderOrderEsDTO;
import com.seek.food.order.Enum.RequestPathEnum;
import com.seek.food.order.Service.SearchService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping(RequestPathEnum.Order_Search)
@RestController
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping(RequestPathEnum.Order_Search_Rider_Order)
    public Result<EsSearchResult<RiderOrderEsDTO>> getRiderOrder(double lat, double lon, int distance,int need, Double docDistance, Long docId){
        return Result.success(searchService.riderSearchOrders(lat, lon, distance, need, docDistance, docId));
    }
}
