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
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
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

    @Override
    public PageResult search(RequestParam requestParam) {
        //query
        try {
            String key = requestParam.getKey();
            if (key == null || key.isEmpty()) {
                searchRequest.source().query(QueryBuilders.matchAllQuery());
            } else {
                searchRequest.source().query(QueryBuilders.matchQuery("all", key));
            }
            //分页
            int page = requestParam.getPage();
            int size = requestParam.getSize();
            searchRequest.source().from((page - 1) * size).size(size);
            //发送请求
            SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            //返回结果
            return handleResponse(response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    private PageResult handleResponse(SearchResponse response) {
        //查询到的数据总数
        long count = response.getHits().getTotalHits().value;
        //准备存储hotelDoc的集合
        List<HotelDoc> hotelDocList = new ArrayList<>();
        Arrays.stream(response.getHits().getHits()).forEach(hit -> {
            HotelDoc hotelDoc = JSON.parseObject(hit.getSourceAsString(), HotelDoc.class);
            hotelDocList.add(hotelDoc);
        });
        return new PageResult(count, hotelDocList);
    }
}
