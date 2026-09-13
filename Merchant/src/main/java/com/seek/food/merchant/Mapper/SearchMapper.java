package com.seek.food.merchant.Mapper;

import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.seek.food.config.NacosConfig.Merchant.MerchantEsTableConfig;
import com.seek.food.dto.Common.EsSearchResult;
import com.seek.food.dto.Merchant.MerchantEsDTO;
import com.seek.food.merchant.EsRepository.MerchantRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
@Slf4j
public class SearchMapper {

    private final MerchantEsTableConfig merchantEsTableConfig;
    private final ElasticsearchOperations elasticsearchOperations;
    //排序函数
    private static List<SortOptions> sortList ;


    @Autowired
    SearchMapper(MerchantEsTableConfig merchantEsTableConfig, ElasticsearchOperations elasticsearchOperations) {
        this.merchantEsTableConfig = merchantEsTableConfig;
        this.elasticsearchOperations = elasticsearchOperations;
    }

    @PostConstruct
    public void init(){
        sortList = List.of(
                SortOptions.of(s -> s.score(sc -> sc.order(SortOrder.Desc))),
                SortOptions.of(s -> s.field(f -> f.field(merchantEsTableConfig.getMerchantId()).order(SortOrder.Asc))));
    }

    //获取随机推送的商家
    public EsSearchResult<MerchantEsDTO> feedMerchant(double lon, double lat, int distance , long seed, int need, int shouldAmount
            ,Double docScore,Long docId) {
        SearchHits<MerchantEsDTO> result;
        if (docId == null||docScore==null) result=getFeedRequest(lon,lat,distance,seed,need,shouldAmount,null);
        else result=getFeedRequest(lon,lat,distance,seed,need,shouldAmount,List.of(docScore,docId));
        //返回结果
        return EsSearchResult.success(result.getSearchHits());
    }


    //获取搜索的商家
    public EsSearchResult<MerchantEsDTO> searchMerchant(String merchantName,double lon, double lat, int distance,int need
            ,Double docScore,Long docId) {
        SearchHits<MerchantEsDTO> result;
        if (docId == null||docScore==null) result=getSearchRequest(merchantName,lon,lat,distance,need,null);
        else result=getSearchRequest(merchantName,lon,lat,distance,need,List.of(docScore,docId));
        //返回结果
        return EsSearchResult.success(result.getSearchHits());
    }

    //构建搜索请求
    public SearchHits<MerchantEsDTO> getSearchRequest(String merchantName,double lon, double lat, int distance,int need
    ,List<Object> lastSearch){
        //构建bool查询函数
        Query boolQuery = QueryBuilders.bool(b -> b
                //只查询目标商户
                .filter(f -> f.match(m -> m.field(merchantEsTableConfig.getMerchantName()).query(merchantName) ))
                .filter(f -> f.term(t -> t.field(merchantEsTableConfig.getIsDelete()).value(false)))
                //只查询限定坐标半径内
                .must(m -> m.geoDistance(g -> g
                                .field(merchantEsTableConfig.getMerchantLocation())
                                .location(loc -> loc.latlon(ll -> ll.lat(lat).lon(lon)))
                                .distance(distance + "km")
                        ))
        );

        //汇总查询条件
        Query allQuery=QueryBuilders.functionScore(fs -> fs
                .query(boolQuery)
                .boostMode(FunctionBoostMode.Sum)
        );
        // 执行查询，自动映射MerchantEsDTO
        return elasticsearchOperations.search(quickGetNativeQuery(allQuery,sortList,lastSearch,need), MerchantEsDTO.class);
    }


    //构建推送请求
    private SearchHits<MerchantEsDTO> getFeedRequest(double lon, double lat, int distance , long seed, int need, int shouldAmount
    , List<Object> lastSearch){
        //构建bool查询函数
        Query boolQuery = QueryBuilders.bool(b -> b
                //过滤不营业商户
                .filter(f -> f.term(t -> t.field(merchantEsTableConfig.getIsOpen()).value(true)))
                .filter(f -> f.term(t -> t.field(merchantEsTableConfig.getIsDelete()).value(false)))
                //限定坐标半径内
                .must(m -> m.geoDistance(g -> g
                        .field(merchantEsTableConfig.getMerchantLocation())
                        .location(loc -> loc.latlon(ll -> ll.lat(lat).lon(lon)))
                        .distance(distance + "km")
                ))
                //对收藏量达标的商家加分
                .should(s -> s.range(r -> r.number(num -> num
                        .field(merchantEsTableConfig.getMerchantCollectAmount())
                        .gte((double) shouldAmount)
                )))
        );

        //重打分函数，固定seed保证分页稳定
        FunctionScore functionScore = FunctionScore.of(f -> f
                .randomScore(
                        RandomScoreFunction.of( r -> r
                        .seed(String.valueOf(seed))
                        .field(merchantEsTableConfig.getMerchantId()) )
                )
                .weight((double) (seed % 10))
        );


        Query allQuery=QueryBuilders.functionScore(fs -> fs
                .query(boolQuery)
                .functions(functionScore)
                .boostMode(FunctionBoostMode.Sum)
        );

        // 执行查询，自动映射MerchantEsDTO
        return elasticsearchOperations.search(quickGetNativeQuery(allQuery,sortList,lastSearch,need)
                , MerchantEsDTO.class);
    }

    //快速汇总所有条件
    private NativeQuery quickGetNativeQuery(Query allQuery,List<SortOptions> sortList,List<Object> lastSearch,int need){
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
