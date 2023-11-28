package cn.itcast.hotel.constants;

public class MqConstants {
    /*
     * 交换机
     */
    public static final String HOTEL_EXCHANGE = "hotel.topic";
    /*
     *监听新增和修改的队列
     */
    public static final String HOTEL_INSERT_QUEUE = "hotel.insert.queue";
    /*
     *监听删除的队列
     */
    public static final String HOTEL_DELETE_QUEUE = "hotel.delete.queue";
    /*
     *路由地址
     */
    public static final String ROUTINE_INSERT_KEY = "hotel.insert";
    public static final String ROUTINE_DELETE_KEY = "hotel.delete";

}
