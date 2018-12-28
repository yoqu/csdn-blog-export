package com.uyoqu.csdnblogexport;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import java.io.File;
import java.io.IOException;

/**
 * @author: yoqu
 * @date: 2018-12-06
 * @email: yoqulin@qq.com
 **/
@Service
public class BlogJsonPipeline implements Pipeline {

    @Value("${filePath}")
    String dataPath;

    JSONArray json = new JSONArray();

    @Override
    public void process(ResultItems resultItems, Task task) {
        String data = resultItems.get("json");
        if (StringUtils.isNotBlank(data)) {
            JSONObject object = JSON.parseObject(data);
            String date = resultItems.get("date");
            object.getJSONObject("data").put("date", date);
            json.add(object);
        }
    }

    public void write() {
        try {
            FileUtils.writeStringToFile(new File(dataPath),
                    JSON.toJSONString(json, SerializerFeature.PrettyFormat));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
