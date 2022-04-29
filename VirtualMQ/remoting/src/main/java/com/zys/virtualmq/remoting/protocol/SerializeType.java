package com.zys.virtualmq.remoting.protocol;

/**
 * @author zys
 * @date 2021/8/1 8:33 下午
 */
// 何种方式序列化
public enum SerializeType {
    JSON((byte) 1),
    VIRTUAL_MQ((byte) 2);

    private byte code;

    SerializeType(byte code) {
        this.code = code;
    }

    public static SerializeType valueOf(byte code) {
        for (SerializeType serializeType : SerializeType.values()) {
            if (serializeType.getCode() == code) {
                return serializeType;
            }
        }
        return null;
    }

    public byte getCode() {
        return code;
    }
}
