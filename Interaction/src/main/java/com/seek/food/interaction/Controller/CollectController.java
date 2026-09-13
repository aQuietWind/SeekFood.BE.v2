package com.seek.food.interaction.Controller;

import com.seek.food.dto.Common.Result;
import com.seek.food.interaction.Enum.RequestPathEnum;
import com.seek.food.interaction.Service.CollectService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(RequestPathEnum.Interaction_Collect)
public class CollectController {


    private final CollectService collectService;

    public CollectController(CollectService collectService) {
        this.collectService = collectService;
    }

    //收藏商家
    @PutMapping(RequestPathEnum.Interaction_Collect_Merchant)
    public Result<Boolean> collectMerchant(long merchantId, boolean value) {
        return Result.success(collectService.collectMerchant(merchantId,value));
    }

    //查看是否收藏该商家
    @GetMapping(RequestPathEnum.Interaction_Collect_Merchant)
    public Result<Boolean> getCollectMerchant(long merchantId) {
        return Result.success(collectService.getCollectMerchant(merchantId));
    }

    //查看收藏了哪些商家
    @GetMapping(RequestPathEnum.Interaction_Collect_Merchant_List)
    public Result<List<Long>> getCollectMerchant(int start, int need) {
        return Result.success(collectService.getCollectMerchantList(start,need));
    }
}
