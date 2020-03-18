package kr.minecheat.mcauth.netty;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import kr.minecheat.mcauth.handler.LoginHandler;
import kr.minecheat.mcauth.handler.PlayHandler;
import kr.minecheat.mcauth.handler.ServerPingHandler;
import kr.minecheat.mcauth.packets.*;
import lombok.Getter;
import lombok.Setter;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;

public class MinecraftPacketHandler extends SimpleChannelInboundHandler<PacketHeader> {
    private ConnectionState connectionState = ConnectionState.PING;
    private ChannelHandlerContext handlerContext;

    private ServerPingHandler pingHandler = new ServerPingHandler(this);
    private LoginHandler loginHandler = new LoginHandler(this);
    private PlayHandler playHandler = new PlayHandler(this);

    @Getter
    private PacketState currentState = PacketState.STATUS;

    public void setCurrentState(PacketState state) {
        this.currentState = state;
        if (currentState == PacketState.PLAY) {
            playHandler.initiate();
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        handlerContext = ctx;
    }
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, PacketHeader packetHeader) throws Exception {

        PacketData pd = packetHeader.getData();
        if (currentState == PacketState.STATUS && pd.getPacketState() == PacketState.STATUS) {
            pingHandler.handlePacket(packetHeader);
        }
        if (currentState != PacketState.PLAY && pd.getPacketState() == PacketState.LOGIN || pd instanceof PacketStatus00ServerListPing && ((PacketStatus00ServerListPing) pd).getNextState() == 2) {
            loginHandler.handlePacket(packetHeader);
        }

        // FIND HANDLERS AND CALL IT
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    public void setEncryption(byte[] key) throws GeneralSecurityException {
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        handlerContext.channel().pipeline().addBefore("frameDecoder", "packetDecrypter", new MinecraftPacketDecrypter(secretKeySpec));
        handlerContext.channel().pipeline().addBefore("packetEncoder", "packetEncrypter", new MinecraftPacketEncrypter(secretKeySpec));
    }

    public ChannelFuture sendPacket(PacketHeader pHeader) throws Exception {
        ChannelFuture f = handlerContext.writeAndFlush(pHeader);
        return f;
    }
}
