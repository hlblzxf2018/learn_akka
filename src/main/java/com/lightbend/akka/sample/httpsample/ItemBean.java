package com.lightbend.akka.sample.httpsample;

import com.alibaba.fastjson.JSON;

public class ItemBean {
    private String itemId;

    public ItemBean (String itemId) {
        this.itemId = itemId;
    }
    public String getItemId() {
        return itemId;
    }



    public void setItemId(String itemId) {
        this.itemId = itemId;
    }
    public static void main(String[] args){
        ItemBean item = new ItemBean("11111");
        System.out.println(JSON.toJSONString(item));
    }
}
