package com.seek.food.config.Enum;

public class MQNameKeyEnum {
    public static final String User_Exchange_Register_Fund_Queue ="${mq.name.bind.user-exchange.register-fund-queue.name}";
    public static final String User_Exchange_Delete_File_Queue ="${mq.name.bind.user-exchange.delete-file-user-queue.name}";
    public static final String User_Exchange_Delete_Fund_Queue="${mq.name.bind.user-exchange.delete-fund-queue.name}";

    public static final String Merchant_Exchange_Delete_File_Queue ="${mq.name.bind.merchant-exchange.delete-file-merchant-queue.name}";
    public static final String Merchant_Exchange_Delete_Merchant_Queue ="${mq.name.bind.merchant-exchange.delete-merchant-queue.name}";
    public static final String Merchant_Exchange_Es_Sync_Merchant_Queue ="${mq.name.bind.merchant-exchange.es-sync-merchant-queue.name}";
    public static final String Merchant_Exchange_Delete_All_Employee_Queue ="${mq.name.bind.merchant-exchange.delete-all-employee-queue.name}";
    public static final String Merchant_Exchange_Delete_All_Meal_Queue ="${mq.name.bind.merchant-exchange.delete-all-meal-queue.name}";

    public static final String Employee_Exchange_Delete_File_Queue ="${mq.name.bind.employee-exchange.delete-file-employee-queue.name}";
    public static final String Employee_Exchange_Change_Amount_Employee_Queue ="${mq.name.bind.employee-exchange.change-employee-amount-queue.name}";

    public static final String Meal_Exchange_Delete_File_Queue ="${mq.name.bind.meal-exchange.delete-file-meal-queue.name}";

    public static final String Fund_Exchange_Use_Voucher_Queue ="${mq.name.bind.fund-exchange.use-voucher-queue.name}";

    public static final String Promotion_Exchange_Register_Voucher_Connection_Queue ="${mq.name.bind.promotion-exchange.register-voucher-connection-queue.name}";

    public static final String Voucher_Exchange_Register_Order_Ack_Queue ="${mq.name.bind.voucher-exchange.order-ack-queue.name}";

    public static final String Order_Exchange_Register_Fund_Order_Record_Queue ="${mq.name.bind.order-exchange.register-fund-order-record-queue.name}";
    public static final String Order_Exchange_Rollback_Fund_Queue ="${mq.name.bind.order-exchange.rollback-fund-queue.name}";
    public static final String Order_Exchange_Rollback_Voucher_Queue ="${mq.name.bind.order-exchange.rollback-voucher-queue.name}";
    public static final String Order_Exchange_Transfer_Fund_Queue ="${mq.name.bind.order-exchange.transfer-fund-queue.name}";
    public static final String Order_Exchange_Change_Merchant_Order_Amount_Queue ="${mq.name.bind.order-exchange.change-merchant-order-amount-queue.name}";
    public static final String Order_Exchange_Change_Meal_Sales_Volume_Queue ="${mq.name.bind.order-exchange.change-meal-sales-volume-queue.name}";
    public static final String Order_Exchange_Change_User_Order_Amount_Queue ="${mq.name.bind.order-exchange.change-user-order-amount-queue.name}";
    public static final String Order_Exchange_Chat_Room_Init_Queue ="${mq.name.bind.order-exchange.chat-room-init-queue.name}";
    public static final String Order_Exchange_Chat_Room_Complete_Queue ="${mq.name.bind.order-exchange.chat-room-complete-queue.name}";

    public static final String Rider_Exchange_Delete_File_Rider_Queue ="${mq.name.bind.rider-exchange.delete-file-rider-queue.name}";

    public static final String Comment_Exchange_Delete_File_Comment_Queue ="${mq.name.bind.comment-exchange.delete-file-comment-queue.name}";
    public static final String Comment_Exchange_Change_Merchant_First_Comment_Amount_Queue ="${mq.name.bind.comment-exchange.change-merchant-first-comment-amount-queue.name}";
    public static final String Comment_Exchange_Change_Second_Comment_Amount_Queue ="${mq.name.bind.comment-exchange.change-second-comment-amount-queue.name}";

    public static final String Interaction_Exchange_Change_First_Comment_Like_Amount_Queue ="${mq.name.bind.interaction-exchange.change-first-comment-like-amount-queue.name}";
    public static final String Interaction_Exchange_Change_Second_Comment_Like_Amount_Queue ="${mq.name.bind.interaction-exchange.change-second-comment-like-amount-queue.name}";
    public static final String Interaction_Exchange_Change_Merchant_Like_Amount_Queue ="${mq.name.bind.interaction-exchange.change-merchant-like-amount-queue.name}";
    public static final String Interaction_Exchange_Change_Merchant_Collect_Amount_Queue ="${mq.name.bind.interaction-exchange.change-merchant-collect-amount-queue.name}";
    public static final String Interaction_Exchange_Sync_Like_State_Queue ="${mq.name.bind.interaction-exchange.sync-like-state-queue.name}";
    public static final String Interaction_Exchange_Sync_Collect_State_Queue ="${mq.name.bind.interaction-exchange.sync-collect-state-queue.name}";

    public static final String Admin_Exchange_Delete_File_Comment_Queue ="${mq.name.bind.admin-exchange.delete-file-admin-queue.name}";

    public static final String Chat_Exchange_Delete_File_Chat_Impl_Queue ="${mq.name.bind.chat-exchange.delete-file-chat-impl-queue.name}";
    public static final String Chat_Exchange_Chat_Inform_Queue ="${mq.name.bind.chat-exchange.chat-inform-queue.name}";

    public static final String Dead_Letter_Exchange_Delete_File_Meal_Impl_Queue ="${mq.name.bind.dead-letter-exchange.delete-file-meal-impl-queue.name}";
    public static final String Dead_Letter_Exchange_Delete_All_File_Meal_Impl_Queue ="${mq.name.bind.dead-letter-exchange.delete-all-file-meal-impl-queue.name}";
    public static final String Dead_Letter_Exchange_Rollback_All_Fund_Impl_Queue ="${mq.name.bind.dead-letter-exchange.rollback-all-fund-impl-queue.name}";
    public static final String Dead_Letter_Exchange_Delete_File_Chat_Impl_Queue ="${mq.name.bind.dead-letter-exchange.delete-file-chat-impl-queue.name}";

    public static final String Error_Exchange_Name="${mq.name.bind.error-exchange.exchange-name}";
    public static final String Error_Queue_Name="${mq.name.bind.error-exchange.error-queue.name}";
}
