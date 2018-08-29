package com.xing.weix.domain;

public class MemberChannel {

    /**
     *
     */
    private static final long serialVersionUID = -2284440422116518646L;
    private Integer id;

    private Integer memberId;           //账户主键

    private String name;                //微信昵称

    private String openid;              //微信openid

    private String headImg;             //微信头像

    private String moreinfo;            //json串

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getMemberId() {
        return memberId;
    }

    public void setMemberId(Integer memberId) {
        this.memberId = memberId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid == null ? null : openid.trim();
    }

    public String getHeadImg() {
        return headImg;
    }

    public void setHeadImg(String headImg) {
        this.headImg = headImg == null ? null : headImg.trim();
    }

    public String getMoreinfo() {
        return moreinfo;
    }

    public void setMoreinfo(String moreinfo) {
        this.moreinfo = moreinfo == null ? null : moreinfo.trim();
    }
}