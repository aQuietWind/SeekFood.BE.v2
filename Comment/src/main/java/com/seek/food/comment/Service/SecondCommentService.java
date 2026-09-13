package com.seek.food.comment.Service;

import com.seek.food.dto.Comment.SecondCommentDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface SecondCommentService {
    public void insertComment(String description, long firstCommentId, MultipartFile file);
    public void insertMerchantComment(String description, long firstCommentId, MultipartFile file);
    public List<SecondCommentDTO> getList(int start, int need, long firstCommentId);
    public void deleteComment(long commentId);
}
