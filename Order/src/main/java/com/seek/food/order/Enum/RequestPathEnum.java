package com.seek.food.order.Enum;

public class RequestPathEnum {

    public static final String Order = "/order";
    public static final String Order_Get_Simple="/simple";
    public static final String Order_Get_Simple_State="/simple/state";
    public static final String Order_Get_Detail="/detail";
    public static final String Order_Refund="/refund";
    public static final String Order_Merchant_Reject="/merchant/reject";
    public static final String Order_Merchant_Ack="/merchant/ack";
    public static final String Order_Merchant_Make="/merchant/make";
    public static final String Order_Rider_Accept="/rider/accept";
    public static final String Order_Rider_Ack="/rider/ack";
    public static final String Order_Rider_Delivery="/rider/delivery";
    public static final String Order_User_Receive="/user/receive";
    public static final String Order_Comment_Select="/comment/select";

    public static final String Order_Search = "/search";
    public static final String Order_Search_Rider_Order = "/rider";
}
