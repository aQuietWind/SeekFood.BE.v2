package com.seek.food.admin.Controller;

import com.seek.food.admin.Enum.RequestPathEnum;
import com.seek.food.admin.Service.SuggestionService;
import com.seek.food.dto.Admin.SuggestionDTO;
import com.seek.food.dto.Common.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequestMapping(RequestPathEnum.Admin_Suggestion)
@RestController
public class SuggestionController {

    private final SuggestionService suggestionService;

    public SuggestionController(SuggestionService suggestionService) {
        this.suggestionService = suggestionService;
    }

    //插入建议
    @PostMapping
    public Result<Void> insertSuggestion(String description, MultipartFile file){
        suggestionService.insertSuggestion(description,file);
        return Result.success();
    }

    //批量查询建议
    @GetMapping(RequestPathEnum.Admin_Suggestion_Get_List)
    public Result<List<SuggestionDTO>> getSuggestionList(int start, int need){
        return Result.success(suggestionService.getSuggestionList(start, need));
    }

    //确认评阅建议
    @PutMapping(RequestPathEnum.Admin_Suggestion_Ack)
    public Result<Void> ackSuggestion(long suggestionId){
        suggestionService.ackSuggestion(suggestionId);
        return Result.success();
    }
}
