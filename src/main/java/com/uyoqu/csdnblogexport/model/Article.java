package com.uyoqu.csdnblogexport.model;

import lombok.Data;

/**
 * @author: yoqu
 * @date: 2018-12-12
 * @email: yoqulin@qq.com
 **/
@Data
public class Article {
    private Integer articleedittype;
    private String channel;
    private String description;
    private String id;
    private String categories;
    private String title;
    private String type;
    private String content;
    private String markdowncontent;
    private String tags;
    private Integer status;
    private String date;

    public boolean isMd() {
        return markdowncontent != null && markdowncontent.length() > 0;
    }
}
