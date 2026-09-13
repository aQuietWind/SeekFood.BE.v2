package com.seek.food.interaction.Controller;

import com.seek.food.dto.Common.Result;
import com.seek.food.interaction.Enum.RequestPathEnum;
import com.seek.food.interaction.Service.LikeService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(RequestPathEnum.Interaction_Like)
public class LikeController {

    private final LikeService likeService;

    public LikeController(LikeService likeService) {
        this.likeService = likeService;
    }

    //点赞商家
    @PutMapping(RequestPathEnum.Interaction_Like_Merchant)
    public Result<Boolean> likeMerchant(long merchantId,boolean value) {
        return Result.success(likeService.likeMerchant(merchantId,value));
    }

    //点赞一级评论
    @PutMapping(RequestPathEnum.Interaction_Like_Comment_First)
    public Result<Boolean> likeFirstComment(long commentId,boolean value) {
        return Result.success(likeService.likeFirstComment(commentId,value));
    }

    //点赞二级评论
    @PutMapping(RequestPathEnum.Interaction_Like_Comment_Second)
    public Result<Boolean> likeSecondComment(long commentId,boolean value) {
        return Result.success(likeService.likeSecondComment(commentId,value));
    }

    //查看对商家的点赞
    @GetMapping(RequestPathEnum.Interaction_Like_Merchant)
    public Result<Boolean> getLikeMerchant(long merchantId,boolean value) {
        return Result.success(likeService.getLikeMerchant(merchantId));
    }

    //查看对一级评论的点赞
    @GetMapping(RequestPathEnum.Interaction_Like_Comment_First)
    public Result<Boolean> getLikeFirstComment(long commentId,boolean value) {
        return Result.success(likeService.getLikeFirstComment(commentId));
    }

    //查看对二级评论的点赞
    @GetMapping(RequestPathEnum.Interaction_Like_Comment_Second)
    public Result<Boolean> getLikeSecondComment(long commentId) {
        return Result.success(likeService.getLikeSecondComment(commentId));
    }








}
