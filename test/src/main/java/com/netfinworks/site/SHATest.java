package com.netfinworks.site;

import org.junit.Test;

import com.netfinworks.common.util.MD5Builder;
import com.netfinworks.ues.util.UesUtils;

public class SHATest {

    void printSha(String pwd, String salt){

        String hash = UesUtils.hashSignContent(pwd);
        System.out.println("密码：" + pwd + " 的sha-256 的结果,sha-256("+pwd+") =");
        System.out.println(hash);

        System.out.println("密码：" + pwd + " 的sha-256 的结果,sha-256("+pwd+") 加上盐：" + salt + "后sha-256 的结果为,sha-256(sha-256("+pwd+")+" + salt+")=");

        String tt = UesUtils.hashSignContent(hash + salt);
        System.out.println(tt);
        System.out.println("----------------------------");
    }

    @Test
    public void test() {

        printSha("weibopay","5957");

    }
    @Test
    public void md5() {
       String result= MD5Builder.getMD5("13621722085md5760881697c81313b77c7784e0bb232cc");
        System.out.println(result.substring(0,16));
    }

}
