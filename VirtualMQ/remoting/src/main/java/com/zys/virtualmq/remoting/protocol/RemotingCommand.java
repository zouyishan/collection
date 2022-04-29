package com.zys.virtualmq.remoting.protocol;

import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;

/**
 * @author zys
 * @date 2021/8/1 8:11 下午
 */
public class RemotingCommand {
    private transient byte[] body;
    //TODO 可以通过读取配置文件配置
    private SerializeType currentSerializeType = SerializeType.VIRTUAL_MQ;

    public ByteBuffer encodeHeader() {
        return this.encodeHeader(this.body == null ? this.body.length : 0);
    }

    private byte[] headerEncoder() {
        if (this.currentSerializeType == SerializeType.VIRTUAL_MQ) {
            return VirtualMQSerialize.virtualMQEncode(this);
        } else {
            return RemotingSerialize.encode(this);
        }
    }

    public static byte[] markProtocolType(int source, SerializeType type) {
        byte[] result = new byte[4];

        result[0] = type.getCode();
        result[1] = (byte) ((source >> 16) & 0xFF);
        result[2] = (byte) ((source >> 8) & 0xFF);
        result[3] = (byte) (source & 0xFF);
        return result;
    }

    public ByteBuffer encodeHeader(int bodyLength) {
        int length = 4;

        byte[] headerData;
        headerData = this.headerEncoder();

        length += headerData.length;

        if (this.body != null) {
            length += body.length;
        }

        ByteBuffer allocate = ByteBuffer.allocate(length + 4 - bodyLength);

        allocate.putInt(length);

        allocate.put(markProtocolType(headerData.length, currentSerializeType));

        allocate.put(headerData);

        if (this.body != null) {
            allocate.put(this.body);
        }

        allocate.flip();

        return allocate;
    }

    public byte[] getBody() {
        return body;
    }
}
