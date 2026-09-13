package com.seek.food.order.Mapper;

import co.elastic.clients.elasticsearch._types.DistanceUnit;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import com.seek.food.config.NacosConfig.Order.OrderRiderOrderEsTableConfig;
import com.seek.food.dto.Common.EsSearchResult;
import com.seek.food.dto.Order.RiderOrderEsDTO;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Component;

import java.util.List;

//不能用@Mapper,只能用这个
@Component
public class SearchMapper {


    private final OrderRiderOrderEsTableConfig orderRiderOrderEsTableConfig;
    private final ElasticsearchOperations elasticsearchOperations;

    public SearchMapper(OrderRiderOrderEsTableConfig orderRiderOrderEsTableConfig, ElasticsearchOperations elasticsearchOperations) {
        this.orderRiderOrderEsTableConfig = orderRiderOrderEsTableConfig;
        this.elasticsearchOperations = elasticsearchOperations;
    }


    //获取随机推送的商家
    public EsSearchResult<RiderOrderEsDTO> feedRiderOrder(double lat, double lon, int distance, int need, Double docDistance, Long docId) {
        SearchHits<RiderOrderEsDTO> result;
        //判断是否为SearchAfter
        if (docId == null||docDistance==null) result=getFeedRequest(lon,lat,distance,need,null);
        else result=getFeedRequest(lon,lat,distance,need,List.of(docDistance,docId));
        //返回结果
        return EsSearchResult.success(result.getSearchHits());
    }


    //构建推送请求
    private SearchHits<RiderOrderEsDTO> getFeedRequest(double lon, double lat, int distance, int need, List<Object> lastSearch){
        //构建bool查询函数
        Query boolQuery = QueryBuilders.bool(b -> b
                //过滤已经被获取的订单
                .filter(f -> f.term(t -> t.field(orderRiderOrderEsTableConfig.getAccept()).value(false)))
                //限定坐标半径内
                .must(m -> m.geoDistance(g -> g
                        .field(orderRiderOrderEsTableConfig.getDeliveryLatLon())
                        .location(loc -> loc.latlon(ll -> ll.lat(lat).lon(lon)))
                        .distance(distance + "km")
                ))
        );

        List<SortOptions> sortList = List.of(
                //构造距离由近到远的排序
                SortOptions.of(s -> s
                        .geoDistance(g -> g
                                .field(orderRiderOrderEsTableConfig.getDeliveryLatLon())
                                .location(loc -> loc.latlon(ll -> ll.lat(lat).lon(lon)))
                                .order(SortOrder.Asc)      // asc = 近→远
                                .unit(DistanceUnit.Kilometers)
                        )
                ),
                SortOptions.of(s -> s.field(f -> f.field(orderRiderOrderEsTableConfig.getOrderId()).order(SortOrder.Asc)))
        );

        // 执行查询，自动映射实体类
        return elasticsearchOperations.search(quickGetNativeQuery(boolQuery,sortList,lastSearch,need)
                , RiderOrderEsDTO.class);
    }

    //快速汇总所有条件进行查询
    private NativeQuery quickGetNativeQuery(Query allQuery, List<SortOptions> sortList, List<Object> lastSearch, int need){
        //构建请求模板,并且绑定查询函数与排序函数
        NativeQueryBuilder searchQuery = NativeQuery.builder()
                //包裹bool主查询与重算分,并且总分求和
                .withQuery(allQuery)
                //绑定排序
                .withSort(sortList)
                //分页
                .withPageable(PageRequest.of(0,need));
        //判断是否需要进行SearchAfter
        if (lastSearch != null&&!lastSearch.isEmpty()) searchQuery.withSearchAfter(lastSearch);
        return searchQuery.build();
    }
}
