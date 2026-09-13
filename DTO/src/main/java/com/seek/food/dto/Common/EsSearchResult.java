package com.seek.food.dto.Common;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch.core.search.Hit;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.core.SearchHit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EsSearchResult<T> {
    private List<T> results=new ArrayList<>();
    private List<Object> lastSortValues;

    public static <T> EsSearchResult<T> success(List<SearchHit<T>> hits) {
        if (hits==null|| hits.isEmpty()) return null;
        EsSearchResult<T> esSearchResult = new EsSearchResult<>();
        //设置data内容
        for (SearchHit<T> hit:hits) esSearchResult.getResults().add(hit.getContent());
        //设置SearchAfter的值
        esSearchResult.setLastSortValues(hits.getLast().getSortValues());
        return esSearchResult;
    }

    public List<FieldValue> toFieldValues() {
        List<FieldValue> fieldValues = new ArrayList<>();
        for (Object value : lastSortValues) fieldValues.add((FieldValue) value);
        return fieldValues;
    }
}
