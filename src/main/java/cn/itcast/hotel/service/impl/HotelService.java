package cn.itcast.hotel.service.impl;

import cn.itcast.hotel.mapper.HotelMapper;
import cn.itcast.hotel.pojo.Hotel;
import cn.itcast.hotel.pojo.HotelDoc;
import cn.itcast.hotel.pojo.PageResult;
import cn.itcast.hotel.pojo.RequestParam;
import cn.itcast.hotel.service.IHotelService;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class HotelService extends ServiceImpl<HotelMapper, Hotel> implements IHotelService {
    @Resource
    private RestHighLevelClient restHighLevelClient;
    @Resource
    private SearchRequest searchRequest;
    @Resource private IndexRequest indexRequest;
    @Resource private DeleteRequest deleteRequest;


    @Override
    public PageResult search(RequestParam requestParam) {
        //query
        try {
            createBasicQuery(requestParam);
            //分页
            int page = requestParam.getPage();
            int size = requestParam.getSize();
            searchRequest.source().from((page - 1) * size).size(size);
            //按距离排序
            String location = requestParam.getLocation();
            if (location != null && !location.isEmpty()) {
                searchRequest.source().sort(SortBuilders.
                        geoDistanceSort("location", new GeoPoint(location))
                        .order(SortOrder.ASC)
                        .unit(DistanceUnit.KILOMETERS));
            }
            //发送请求
            SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            //返回结果
            return handleResponse(response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void insertById(Long id) {
        try {
            //在es中新增或修改
            Hotel hotel = getById(id);
            HotelDoc hotelDoc = new HotelDoc(hotel);
            //准备indexRequest对象,在启动类中交给spring管理，现在设置对应参数即可
            indexRequest.id(hotelDoc.getId().toString());
            //准备json文件
            indexRequest.source(JSON.toJSONString(hotelDoc), XContentType.JSON);
            //发送请求
            restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
            log.warn("es完成操作");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void deleteById(Long id) {
        try {
            //在es中删除
            deleteRequest.id(getById(id).getId().toString());
            restHighLevelClient.delete(deleteRequest, RequestOptions.DEFAULT);
            log.warn("es完成操作");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void createBasicQuery(RequestParam requestParam) {
        //创建组合搜索器
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        searchRequest.source().query(boolQuery);
        //must部分,关键字搜索
        String key = requestParam.getKey();
        if (key == null || key.isEmpty()) {
            boolQuery.must(QueryBuilders.matchAllQuery());
        } else {
            boolQuery.must(QueryBuilders.matchQuery("all", key));
        }
        //条件过滤
        if (requestParam.getCity() != null && !requestParam.getCity().isEmpty()) {
            boolQuery.filter(QueryBuilders.termQuery("city", requestParam.getCity()));
        }
        if (requestParam.getBrand() != null && !requestParam.getBrand().isEmpty()) {
            boolQuery.filter(QueryBuilders.termQuery("brand", requestParam.getBrand()));
        }
        if (requestParam.getStarName() != null && !requestParam.getStarName().isEmpty()) {
            boolQuery.filter(QueryBuilders.termQuery("starName", requestParam.getStarName()));
        }
        if (requestParam.getMaxPrice() != null && requestParam.getMinPrice() != null) {
            boolQuery.filter(QueryBuilders
                    .rangeQuery("price")
                    .gte(requestParam.getMinPrice())
                    .lte(requestParam.getMaxPrice()));
        }
    }

    private PageResult handleResponse(SearchResponse response) {
        //查询到的数据总数
        long count = response.getHits().getTotalHits().value;
        //准备存储hotelDoc的集合
        List<HotelDoc> hotelDocList = new ArrayList<>();
        Arrays.stream(response.getHits().getHits()).forEach(hit -> {
            HotelDoc hotelDoc = JSON.parseObject(hit.getSourceAsString(), HotelDoc.class);
            Object[] sortValues = hit.getSortValues();
            if (sortValues.length>0) {
                hotelDoc.setDistance(sortValues[0]);
            }
            hotelDocList.add(hotelDoc);

        });
        return new PageResult(count, hotelDocList);
    }
}
