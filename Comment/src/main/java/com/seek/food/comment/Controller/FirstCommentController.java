package com.seek.food.comment.Controller;

import com.seek.food.dto.Comment.FirstCommentDTO;
import com.seek.food.dto.Common.Result;
import com.seek.food.comment.Enum.RequestPathEnum;
import com.seek.food.comment.Service.FirstCommentService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequestMapping(RequestPathEnum.Comment_First)
@RestController
public class FirstCommentController {

    private final FirstCommentService firstCommentService;

    public FirstCommentController(FirstCommentService firstCommentService) {
        this.firstCommentService = firstCommentService;
    }

    @PostMapping
    public Result<Void> insertComment(String description, long orderId,@RequestBody MultipartFile file) {
        firstCommentService.insertComment(description,orderId,file);
        return Result.success();
    }
    @GetMapping(RequestPathEnum.Comment_First_Get_Simple)
    public Result<List<FirstCommentDTO>> getSimple(int start, int need, long merchantId) {
        return Result.success(firstCommentService.getSimple(merchantId,start,need));
    }
    @GetMapping(RequestPathEnum.Comment_First_Get_Detail)
    public Result<FirstCommentDTO> getDetail(long commentId) {
        return Result.success(firstCommentService.getDetail(commentId));
    }

    @DeleteMapping
    public Result<Void> deleteComment(long commentId) {
        firstCommentService.deleteComment(commentId);
        return Result.success();
    }
}
