package com.voc.service.trhird.feishu.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.voc.service.trhird.api.FlyBookSendApi;
import com.voc.service.trhird.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class FlyBookSendApiImpl implements FlyBookSendApi {

    private static final Logger logger = LoggerFactory.getLogger(FlyBookSendApiImpl.class);
    @Value("${third.feishu.send.msg.url:https://open.feishu.cn/open-apis/bot/v2/hook/c55f932b-7305-42f6-a1b6-32d6eaf7de71}")
    private String robotUrl;

    /**
     * @param contentModel 发送消息体
     * @return
     */
    @Override
    public Boolean sendRobotTextMag(MsgContentModel contentModel) {

        Assert.isTrue(ObjectUtil.isNotNull(contentModel) && CollectionUtil.isNotEmpty(contentModel.getContent()), "发送消息体不能为空");
        ContentParamModel contentParamModel = new ContentParamModel();
        Content content = new Content();
        ContentPost post = new ContentPost();
        ContentZhCn contentZhCn = new ContentZhCn();
        post.setContentZhCn(contentZhCn);
        content.setPost(post);
        contentParamModel.setContent(content);
        List<String> modelContentList = contentModel.getContent();
        List<List<ContentInfoModel>> contentInfoList = new ArrayList<>();
        for (String c : modelContentList) {
            List<ContentInfoModel> contentInfoModelList = new ArrayList<>();
            ContentInfoModel contentInfoModel = new ContentInfoModel();
            contentInfoModel.setTag("text");
            contentInfoModel.setText(c);
            contentInfoModelList.add(contentInfoModel);
            contentInfoList.add(contentInfoModelList);
        }
        contentZhCn.setContent(contentInfoList);
        contentZhCn.setTitle(contentModel.getTitle());
        return sendUrl(contentParamModel, robotUrl);
    }


    public Boolean sendUrl(ContentParamModel contentParamModel, String robotUrl) {
        try {
            logger.info("调用机器人发信息入参:{}", JSON.toJSONString(contentParamModel));
            URL url = new URL(robotUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            // 设置为true，否则不能上传数据
            conn.setDoOutput(true);
            // 创建要发送的JSON数据
            String jsonInputString = JSON.toJSONString(contentParamModel);
            // 发送数据
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            // 获取响应码以检查成功与否
            int responseCode = conn.getResponseCode();
            System.out.println("Response Code: " + responseCode);
            // 处理响应...
            conn.disconnect();
            String responseMessage = conn.getResponseMessage();
            System.out.println("Response Msg: " + responseMessage);
        } catch (Exception e) {
            e.printStackTrace();
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

}
