package com.seek.food.admin.Mapper;

import com.seek.food.dto.Admin.SuggestionDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SuggestionMapper {
    public void insert(SuggestionDTO suggestion);
    public List<SuggestionDTO> getList(int start, int need);
    public boolean ack(long suggestionId);
    public String getImageAddrAfterAck(long suggestionId);
}
