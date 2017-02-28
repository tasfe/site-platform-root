package com.yongda.site.wallet.app.util.test;

import com.meidusa.fastjson.JSON;
import com.meidusa.fastjson.JSONArray;
import com.meidusa.fastjson.JSONObject;
import com.netfinworks.common.util.money.Money;
import com.netfinworks.site.core.common.constants.RegexRule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * <br>
 * 作者： zhangweijie <br>
 * 日期： 2016/12/23-9:16<br>
 */
public class Test {
    public static void main(String[] args) {
        testThreadPool();
    }



    public static void testThreadPool(){
        ThreadPoolExecutor executor = new ThreadPoolExecutor(5, 10, 200, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<Runnable>(5));

        for(int i=0;i<15;i++){
            MyTask myTask = new MyTask(i);
            executor.execute(myTask);
            System.out.println("线程池中线程数目："+executor.getPoolSize()+"，队列中等待执行的任务数目："+
                    executor.getQueue().size()+"，已执行玩别的任务数目："+executor.getCompletedTaskCount());
        }
        executor.shutdown();
    }



    public static void testString(){
        String tradeMemo = "油卡充值-充值100.00元   1000113300004232392";
        tradeMemo  = tradeMemo.substring(tradeMemo.indexOf("元")+1,tradeMemo.length()).trim();
        System.out.println(tradeMemo);
    }


    public static void testfastjson(){
        /**
         * 练习阿里的fastjson json转换
         */
        //list转换成json格式字符串
        List list = new ArrayList();
        list.add("abc");
        list.add(123);
        System.out.println("list集合转换成json格式字符串:-"+JSON.toJSONString(list));

        JSONArray jsonObject = (JSONArray)JSON.toJSON(list);
        System.out.println("list集合转换成json数组:-"+jsonObject.get(1));

        Map map = new HashMap();
        map.put("jj","1234");
        map.put("jj2","zwj123");
        System.out.println("Map集合转换成json格式字符串:-"+JSON.toJSONString(map));

        User user = new User();
        user.setId(123);
        user.setName("zwj");
        System.out.println("javaBean转换成json格式字符串:-"+JSON.toJSONString(user));
        String strUser =  JSON.toJSONString(user);
        User user1 = JSON.parseObject(strUser,User.class);
        System.out.println(user1.getId());


        User user2 = new User();
        user2.setId(456);
        user2.setName("han");

        List<User> listUser = new ArrayList<User>();
        listUser.add(user);
        listUser.add(user2);
        System.out.println("----"+JSON.toJSONString(listUser));
        JSONArray jsonArray = (JSONArray)JSON.toJSON(listUser);
        JSONObject jsonObject1 = jsonArray.getJSONObject(0);
        User user3 = JSON.parseObject(jsonObject1.toJSONString(),User.class);
        System.out.println(user3.toString());
    }
}
