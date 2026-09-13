package com.seek.food.comment.Feign;

import com.seek.food.dto.Comment.FirstCommentDTO;
import com.seek.food.dto.Common.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

//可改为动态指定名称，但是需要在Config模块手写一个枚举类
@FeignClient("order")
public interface OrderClient {
    @GetMapping("/order/comment/select")
    Result<FirstCommentDTO> commentSelect(@RequestParam("orderId") long orderId);
}
