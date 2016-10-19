package com.netfinworks.site.domain.domain.member;

import java.io.Serializable;

/**
 *
 * <p>平台会员信息</p>
 * @author qinde
 * @version $Id: PlateUser.java, v 0.1 2013-12-19 下午5:04:21 qinde Exp $
 */
public class PlateUser implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 3753703705883042388L;
    /** 商铺的ID,以逗号分割 */
    private int               id;
    /**登陆名 */
    private String            login_name;
    /**移动电话*/
    private String            mobile;
    /**电子邮件地址*/
    private String            email;
    /**电子邮件验证状态 1已验证 0未验证 */
    private int               email_status;
    /**手机验证状态 1已验证 0未验证 */
    private int               mobile_status;
    /**帐号激活状态 1已激活 2已禁用 */
    private int               is_active;
    /**上次登录时间.格式：yyyy-mm-dd hh:mm:ss */
    private String            last_login_time;
    /**上次登录ip */
    private String            last_login_ip;
    /**注册时间.格式：yyyy-mm-dd hh:mm:ss*/
    private String            register_time;
    /**注册IP*/
    private String            register_ip;
    /**真实姓名*/
    private String            real_name;
    /**性别(1, 2) */
    private int               gender;
    /**性别名(男,女) */
    private String            gender_name;
    /**生日 yyyy-mm-dd */
    private String            birthday;
    /**地域ID*/
    private int               city_id;
    /** 地域名称 */
    private String            city_name;
    /**地域名称(以逗号分隔的完整地域名，包括上级地域的名称)*/
    private String            city_full_name;
    /**用户详细地址*/
    private String            address;
    /**图片ID*/
    private int               image_id;
    /**小头像URL */
    private String            small_face_url;
    /**中等头像URL */
    private String            mid_face_url;
    /**头像URL */
    private String            face_image_url;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLogin_name() {
        return login_name;
    }

    public void setLogin_name(String login_name) {
        this.login_name = login_name;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getEmail_status() {
        return email_status;
    }

    public void setEmail_status(int email_status) {
        this.email_status = email_status;
    }

    public int getMobile_status() {
        return mobile_status;
    }

    public void setMobile_status(int mobile_status) {
        this.mobile_status = mobile_status;
    }

    public int getIs_active() {
        return is_active;
    }

    public void setIs_active(int is_active) {
        this.is_active = is_active;
    }

    public String getLast_login_time() {
        return last_login_time;
    }

    public void setLast_login_time(String last_login_time) {
        this.last_login_time = last_login_time;
    }

    public String getLast_login_ip() {
        return last_login_ip;
    }

    public void setLast_login_ip(String last_login_ip) {
        this.last_login_ip = last_login_ip;
    }

    public String getRegister_time() {
        return register_time;
    }

    public void setRegister_time(String register_time) {
        this.register_time = register_time;
    }

    public String getRegister_ip() {
        return register_ip;
    }

    public void setRegister_ip(String register_ip) {
        this.register_ip = register_ip;
    }

    public String getReal_name() {
        return real_name;
    }

    public void setReal_name(String real_name) {
        this.real_name = real_name;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public String getGender_name() {
        return gender_name;
    }

    public void setGender_name(String gender_name) {
        this.gender_name = gender_name;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public int getCity_id() {
        return city_id;
    }

    public void setCity_id(int city_id) {
        this.city_id = city_id;
    }

    public String getCity_name() {
        return city_name;
    }

    public void setCity_name(String city_name) {
        this.city_name = city_name;
    }

    public String getCity_full_name() {
        return city_full_name;
    }

    public void setCity_full_name(String city_full_name) {
        this.city_full_name = city_full_name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getImage_id() {
        return image_id;
    }

    public void setImage_id(int image_id) {
        this.image_id = image_id;
    }

    public String getSmall_face_url() {
        return small_face_url;
    }

    public void setSmall_face_url(String small_face_url) {
        this.small_face_url = small_face_url;
    }

    public String getMid_face_url() {
        return mid_face_url;
    }

    public void setMid_face_url(String mid_face_url) {
        this.mid_face_url = mid_face_url;
    }

    public String getFace_image_url() {
        return face_image_url;
    }

    public void setFace_image_url(String face_image_url) {
        this.face_image_url = face_image_url;
    }

}
