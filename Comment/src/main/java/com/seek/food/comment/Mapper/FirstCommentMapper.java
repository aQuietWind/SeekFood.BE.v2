package com.seek.food.comment.Mapper;

import com.seek.food.dto.Comment.FirstCommentDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FirstCommentMapper {
    public void insertComment(FirstCommentDTO comment);
    public List<FirstCommentDTO> getSimple(long merchantId, int start, int need);
    public FirstCommentDTO getDetail(long commentId);
    public boolean deleteComment(long commentId,long userId);
    public boolean updateSecondCommentAmount(long commentId,int changeNumber);
    public boolean updateLikeAmount(long commentId,int changeNumber);
    public String getImageAddrAfterDelete(long commentId);
}
