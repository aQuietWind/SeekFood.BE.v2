package com.seek.food.comment.Mapper;

import com.seek.food.dto.Comment.SecondCommentDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SecondCommentMapper {
    public boolean insertComment(SecondCommentDTO comment);
    public List<SecondCommentDTO> getList(int start, int need, long firstCommentId);
    public boolean deleteComment(long commentId,long accountId);
    public boolean updateLikeAmount(long commentId,int changeNumber);
    public String getImageAddrAfterDelete(long commentId);
}
