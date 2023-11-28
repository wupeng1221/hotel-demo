package cn.itcast.hotel;

import cn.itcast.hotel.pojo.HotelDoc;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

@SpringBootTest
@Slf4j
public class HotelQueryTest {
    private RestHighLevelClient restClient;
    private static final SearchRequest searchRequest = new SearchRequest("hotel");

    private void handleResponse(SearchResponse response) {
        SearchHits hits = response.getHits();
        log.warn("共找到{}条数据", hits.getTotalHits().value);
        Arrays.stream(hits.getHits()).forEach(
                hit -> log.warn("hotelDoc={}", JSON.parseObject(hit.getSourceAsString(), HotelDoc.class))
        );
    }

    private SearchResponse handleSearch() throws IOException {
        return restClient.search(searchRequest, RequestOptions.DEFAULT);
    }

    private void handleAll() throws IOException {
        handleResponse(handleSearch());
    }

    @Test
    void testQueryAll() throws IOException {
        searchRequest.source().query(QueryBuilders.matchAllQuery());
        handleAll();
    }

    @Test
        //全文检索查询 match
    void testQueryMatch() throws IOException {
        searchRequest.source()
                .query(QueryBuilders.matchQuery("all", "如家"));
        handleAll();
    }

    @Test
        //全文检索查询 multi_match，多字段查询
    void testQueryMultiMatch() throws IOException {
        searchRequest.source()
                .query(QueryBuilders.multiMatchQuery(
                        "如家", "name", "brand"
                ));
        handleAll();
    }

    @Test
        //term精确查询
    void testTermQuery() throws IOException {
        searchRequest.source()
                .query(QueryBuilders.termQuery("city", "上海"));
        handleAll();
    }

    @Test
        //range范围查询
    void testRangeQuery() throws IOException {
        searchRequest.source().query(
                QueryBuilders.rangeQuery("price").gte(1000)
        );
        handleAll();
    }

    @Test
        //bool查询
    void testBoolQuery() throws IOException {
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        boolQueryBuilder.must(QueryBuilders.termQuery("city", "上海"));
        boolQueryBuilder.should(QueryBuilders.termsQuery("brand", "如家", "喜来登"));
        boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(3000));
        searchRequest.source().query(boolQueryBuilder);
        handleAll();
    }

    @Test
        //排序和分页查询
    void testPageAndSortQuery() throws IOException {
        searchRequest.source()
                .query(QueryBuilders.matchAllQuery())
                .from(10).size(3)
                .sort("score", SortOrder.DESC)
                .sort("price", SortOrder.ASC);
        handleAll();
    }

    @Test
        //高亮测试
    void testHighlight() throws IOException {
        searchRequest.source()
                .query(QueryBuilders.matchQuery("all", "如家"));
        searchRequest.source().highlighter(
                new HighlightBuilder().field("name").requireFieldMatch(false)
        );
        //高亮结果进行处理
        SearchResponse response = restClient.search(searchRequest, RequestOptions.DEFAULT);
        SearchHit[] searchHits = response.getHits().getHits();
        Arrays.stream(searchHits).forEach(hit -> {
            String json = hit.getSourceAsString();
            HotelDoc hotelDoc = JSON.parseObject(json, HotelDoc.class);
            //获取高亮结果
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            //根据字段名获取高亮结果
            if (!CollectionUtils.isEmpty(highlightFields)) {
                HighlightField highlightName = highlightFields.get("name");
                if (highlightName != null) {
                    //取出高亮的值
                    String name = highlightName.getFragments()[0].string();
                    hotelDoc.setName(name);
                    log.warn(hotelDoc + "");
                }
            }
        });
    }

    @BeforeEach
    public void init() {
        this.restClient = new RestHighLevelClient(RestClient.builder(
                HttpHost.create("http://8.130.103.35:9200")
        ));
    }

    @AfterEach
    public void flush() throws IOException {
        this.restClient.close();
    }
}
