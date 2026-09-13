package com.seek.food.comment.Controller;

import com.seek.food.comment.Enum.RequestPathEnum;
import com.seek.food.comment.Service.SecondCommentService;
import com.seek.food.dto.Comment.SecondCommentDTO;
import com.seek.food.dto.Common.Result;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequestMapping(RequestPathEnum.Comment_Second)
@RestController
public class SecondCommentController {


    private final SecondCommentService secondCommentService;

    public SecondCommentController(SecondCommentService secondCommentService) {
        this.secondCommentService = secondCommentService;
    }

    //插入一条二级评论
    @PostMapping
    public Result<Void> insertComment(String description, long firstCommentId, @RequestBody MultipartFile file) {
        secondCommentService.insertComment(description, firstCommentId, file);
        return Result.success();
    }

    //插入一条商家回复
    @PostMapping(RequestPathEnum.Comment_Second_Merchant)
    public Result<Void> insertMerchantComment(String description, long firstCommentId, @RequestBody MultipartFile file) {
        secondCommentService.insertMerchantComment(description, firstCommentId, file);
        return Result.success();
    }

    //批量查询二级评论
    @GetMapping(RequestPathEnum.Comment_Second_Get_List)
    public Result<List<SecondCommentDTO>> getList(int start, int need,long firstCommentId) {
        return Result.success(secondCommentService.getList(start, need, firstCommentId));
    }

    //删除二级评论
    @DeleteMapping
    public Result<Void> deleteComment(long commentId) {
        secondCommentService.deleteComment(commentId);
        return Result.success();
    }



}
