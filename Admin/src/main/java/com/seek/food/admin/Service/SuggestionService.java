package com.seek.food.admin.Service;

import com.seek.food.dto.Admin.SuggestionDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface SuggestionService {
    public void insertSuggestion(String description, MultipartFile file);
    public List<SuggestionDTO> getSuggestionList(int start, int need);
    public void ackSuggestion(long suggestionId);












}
