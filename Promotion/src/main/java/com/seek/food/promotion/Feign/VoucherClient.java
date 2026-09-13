package com.seek.food.promotion.Feign;

import com.seek.food.dto.Common.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

//可改为动态指定名称，但是需要在Config模块手写一个枚举类
@FeignClient("voucher")
public interface VoucherClient {
    @GetMapping("/connection/exist")
    Result<Boolean> connectionExist( @RequestParam("promotionId") long promotionId);
    @GetMapping("/merchant/exist")
    Result<Boolean> merchantVoucherExist( @RequestParam("voucherId") long voucherId);
}
