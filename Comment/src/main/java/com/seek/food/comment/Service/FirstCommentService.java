package com.seek.food.comment.Service;

import com.seek.food.dto.Comment.FirstCommentDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FirstCommentService {
    public void insertComment(String description, long orderId,MultipartFile file);
    public List<FirstCommentDTO> getSimple(long merchantId, int start, int need);
    public FirstCommentDTO getDetail(long commentId);
    public void deleteComment(long commentId);
}
