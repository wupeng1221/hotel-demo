package cn.itcast.hotel;

import cn.itcast.hotel.pojo.Hotel;
import cn.itcast.hotel.pojo.HotelDoc;
import cn.itcast.hotel.service.IHotelService;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
@SpringBootTest
public class HotelDocTest {
    @Resource
    private IHotelService hotelService;
    private RestHighLevelClient restClient;

    @Test
        //新增文档
    void testAddDoc() throws IOException {
        //根据id查酒店对象
        Hotel hotel = hotelService.getById(61083L);
        //转换为doc类型
        HotelDoc hotelDoc = new HotelDoc(hotel);
        IndexRequest request = new IndexRequest("hotel").id(hotel.getId().toString());
        //将对象序列化为json
        request.source(JSON.toJSONString(hotelDoc), XContentType.JSON);
        //发送请求
        restClient.index(request, RequestOptions.DEFAULT);
    }


    @Test
        //查询文档
    void testQueryDoc() throws IOException {
        GetRequest getRequest = new GetRequest("hotel", "61083");
        GetResponse response = restClient.get(getRequest, RequestOptions.DEFAULT);
        String json = response.getSourceAsString();
        log.warn(json);
    }

    @Test
        //更新文档
    void testUpdateDoc() throws IOException {
        UpdateRequest updateRequest = new UpdateRequest("hotel", "61083");
        updateRequest.doc(
                "price", "999",
                "starName", "四钻"
        );
        restClient.update(updateRequest, RequestOptions.DEFAULT);
    }

    @Test
        //删除文档
    void testDeleteDoc() throws IOException {
        DeleteRequest deleteRequest = new DeleteRequest("hotel", "61083");
        restClient.delete(deleteRequest, RequestOptions.DEFAULT);
    }

    @Test
        //测试批量添加文档
    void testBulkRequest() throws IOException {
        List<Hotel> hotels = hotelService.list();
        BulkRequest bulkRequest = new BulkRequest();
        hotels.forEach(hotel -> bulkRequest.add(
                new IndexRequest("hotel")
                        .id(hotel.getId().toString())
                        .source(JSON.toJSONString(new HotelDoc(hotel)), XContentType.JSON)
        ));
        restClient.bulk(bulkRequest, RequestOptions.DEFAULT);
    }

    @Test
    public void testInit() {
        System.out.println(restClient.toString());
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
