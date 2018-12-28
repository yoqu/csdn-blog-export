package com.uyoqu.csdnblogexport;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Selectable;

import java.util.List;

/**
 * @author: yoqu
 * @date: 2018-12-06
 * @email: yoqulin@qq.com
 **/
@Service
public class BlogPageProcessor implements PageProcessor {

    @Value("${cookie}")
    private String cookie;

    @Override
    public void process(Page page) {
        if (page.getUrl().get().contains("getArticle")) {
            //接口请求
            saveDoc(page);
        } else {
            resolvePage(page);
        }

    }

    public void resolvePage(Page page) {
        Selectable selectable = page.getHtml().$(".article-list-item");
        for (Selectable s : selectable.nodes()) {
            String url = s.links().regex("/postedit/[1-9]\\d*").get();
            String date = s.$("div.article-list-item-info > div.item-info-left > span:nth-child(2)", "text").get();
            if (StringUtils.isNotBlank(url) && url.lastIndexOf("/") != -1) {
                String id = url.substring(url.lastIndexOf("/") + 1);
                if (StringUtils.isNotBlank(id)) {
                    Request nextReq = new Request("https://mp.csdn.net/mdeditor/getArticle?id=" + id);
                    nextReq.putExtra("date", date);
                    page.addTargetRequest(nextReq);
                }
            }
        }
        List<String> targetUrls = page.getHtml().links().regex("https://mp.csdn.net/postlist/list/all/\\d").all();
        if (targetUrls != null) {
            page.addTargetRequests(targetUrls);
        }
    }

    public void saveDoc(Page page) {
        String date = (String) page.getRequest().getExtra("date");
        page.putField("date", date);
        page.putField("json", page.getRawText());
    }

    @Override
    public Site getSite() {
        return Site.me().addCookie("cookie", cookie)
                .addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.110 Safari/537.36");
    }


}
