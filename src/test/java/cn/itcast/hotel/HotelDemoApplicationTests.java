package cn.itcast.hotel;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetIndexResponse;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

import static cn.itcast.hotel.Constants.HotelConstants.MAPPING_TEMPLATE;

@SpringBootTest
@Slf4j
class HotelDemoApplicationTests {
    private RestHighLevelClient restClient;

    @Test
    public void testInit() {
        System.out.println(restClient.toString());
    }

    @Test
    //创建索引库
    public void creatHotelIndex() throws IOException {
        //1.创建request对象
        CreateIndexRequest request = new CreateIndexRequest("hotel");
        //2.准备请求参数，dsl语句
        request.source(MAPPING_TEMPLATE, XContentType.JSON);
        //3.发送请求
        restClient.indices().create(request, RequestOptions.DEFAULT);
    }

    //删除索引库
    @Test
    public void testDeleteHotelIndex() throws IOException {
        DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest("hotel");
        restClient.indices().delete(deleteIndexRequest, RequestOptions.DEFAULT);
    }

    @Test
    public void testExistHotelIndex() throws IOException {
        GetIndexRequest getIndexRequest = new GetIndexRequest("hotel");
        boolean exists = restClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
        log.warn(exists ? "存在" : "不存在");

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
