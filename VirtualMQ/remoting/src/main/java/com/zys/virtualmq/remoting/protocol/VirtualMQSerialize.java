package com.zys.virtualmq.remoting.protocol;

import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * @author zys
 * @date 2021/8/1 8:40 下午
 */
public class VirtualMQSerialize {
    private static final Charset CHARSET_UTF8 = Charset.forName("UTF-8");

    //TODO 这个有点难顶，header要放什么呢？
    public static byte[] virtualMQEncode(RemotingCommand cmd) {

        return null;
    }
}
