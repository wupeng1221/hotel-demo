package cn.itcast.hotel;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.apache.http.HttpHost;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@MapperScan("cn.itcast.hotel.mapper")
@SpringBootApplication
public class HotelDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(HotelDemoApplication.class, args);
    }
    @Bean
    public RestHighLevelClient restHighLevelClient (){
        return new RestHighLevelClient(RestClient.builder(
                HttpHost.create("http://8.130.103.35:9200")
        ));
    }
    @Bean
    public SearchRequest searchRequest(){
        return new SearchRequest("hotel");
    }
    @Bean
    public IndexRequest indexRequest(){
        return new IndexRequest("hotel");
    }
    @Bean
    public DeleteRequest deleteRequest(){
        return new DeleteRequest("hotel");
    }
}
