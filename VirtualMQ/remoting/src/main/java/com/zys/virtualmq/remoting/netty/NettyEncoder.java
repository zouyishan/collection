package com.zys.virtualmq.remoting.netty;

import com.zys.virtualmq.remoting.protocol.RemotingCommand;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

/**
 * @author zys
 * @date 2021/8/1 8:10 下午
 */
@Slf4j
@ChannelHandler.Sharable
public class NettyEncoder extends MessageToByteEncoder<RemotingCommand> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, RemotingCommand remotingCommand, ByteBuf byteBuf) {
        try {
            byteBuf.writeBytes(remotingCommand.encodeHeader());
            byte[] body = remotingCommand.getBody();
            if (body != null) {
                byteBuf.writeBytes(body);
            }
        } catch (Exception e) {
            log.error("encode exception");
            if (remotingCommand != null) {
                log.error(remotingCommand.toString());
            }

        }
    }
}
