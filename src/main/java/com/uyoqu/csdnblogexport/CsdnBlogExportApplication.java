package com.uyoqu.csdnblogexport;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.uyoqu.csdnblogexport.model.Article;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

import java.io.File;
import java.io.IOException;
import java.util.Date;

@SpringBootApplication
@Slf4j
public class CsdnBlogExportApplication implements CommandLineRunner {

    @Autowired
    PageProcessor pageProcessor;

    @Autowired
    BlogJsonPipeline pipeline;

    @Value("${filePath}")
    String dataPath;
    @Value("${genDir}")
    String genDir;

    public static void main(String[] args) {
        SpringApplication.run(CsdnBlogExportApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        if (args != null) {
            if (args[0].equals("d")) {
                Spider.create(pageProcessor)
                        .addPipeline(pipeline)
                        .addUrl("https://mp.csdn.net").run();
                pipeline.write();
            } else if (args[0].equals("g")) {
                generate();
            }
        }
    }

    public void generate() {
        File file = new File(dataPath);
        if (!file.exists()) {
            log.error("文件不存在，请先下载后再生成");
            return;
        }
        File dir = new File(genDir);
        try {
            FileUtils.forceMkdir(dir);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        try {
            String text = FileUtils.readFileToString(file, "utf-8");
            JSONArray array = JSON.parseArray(text);
            for (int i = 0; i < array.size(); i++) {
                JSONObject obj = array.getJSONObject(i);
                Article article = obj.getObject("data", Article.class);
                writeToFile(article);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

    public void writeToFile(Article article) throws IOException {
        String date = article.getDate();
        if (StringUtils.isNotBlank(date)) {
            Date date1 = DateUtils.parseDate(date, new String[]{"yyyy年MM月dd日 HH:mm:ss"});
            date = DateUtils.formatDate(date1, "yyyy-MM-dd HH:mm:ss");
        }
        StringBuilder builder = new StringBuilder();
        builder.append("---")
                .append("\ntitle: ").append(article.getTitle())
                .append("\ndate: ").append(date)
                .append("\ntoc: true");
        if (StringUtils.isNotBlank(article.getTags())) {
            String[] tags = article.getTags().split(",");
            if (tags.length > 0) {
                builder.append("\ntag:");
                for (int i = 0; i < tags.length; i++) {
                    builder.append("\n- [").append(tags[i]).append("]");
                }
            }
        }
        if (StringUtils.isNotBlank(article.getCategories())) {
            String[] categories = article.getCategories().split(",");
            if (categories.length > 0) {
                builder.append("\ncategories:");
                for (int i = 0; i < categories.length; i++) {
                    builder.append("\n- [").append(categories[i]).append("]");
                }
            }
        }
        builder.append("\n---\n");
        builder.append(article.getMarkdowncontent());
        File writeFile = null;
        writeFile = new File(genDir + "/" + article.getTitle().replace(" ", "") + ".md");
        FileUtils.writeStringToFile(writeFile, builder.toString());
    }
}
