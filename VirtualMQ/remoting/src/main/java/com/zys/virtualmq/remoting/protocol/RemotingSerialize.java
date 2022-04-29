package com.zys.virtualmq.remoting.protocol;

import com.alibaba.fastjson.JSON;
import org.springframework.util.StringUtils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author zys
 * @date 2021/8/2 11:51 上午
 */
public class RemotingSerialize {
    private final static Charset CHARSET_UTF8 = Charset.forName("UTF-8");

    public static byte[] encode(Object obj) {
        String json = toJson(obj, false);
        if (json.isEmpty()) {
            return null;
        }
        return json.getBytes(CHARSET_UTF8);
    }

    public static String toJson(Object obj, boolean prettyFormat) {
        return JSON.toJSONString(obj, prettyFormat);
    }
}
