package kr.minecheat.mcauth.handler;

import kr.minecheat.mcauth.Server;
import kr.minecheat.mcauth.mcdata.*;
import kr.minecheat.mcauth.netty.MinecraftPacketHandler;
import kr.minecheat.mcauth.packets.*;

import java.util.Arrays;
import java.util.TimerTask;

public class PlayHandler extends PacketHandler {
    public PlayHandler(MinecraftPacketHandler nettyHandler) {
        super(nettyHandler);
    }

    private long willSendKeepAlive;
    private long willReceiveKeepAliveBefore;
    private boolean recievedKeepAlive = false;
    private PacketPlay00KeepAlive lastKeepAlive;

    @Override
    public void handlePacket(PacketHeader packetHeader) throws Exception {
        if (packetHeader.getData() instanceof PacketPlay00KeepAlive) {
            if (lastKeepAlive != null && lastKeepAlive.getRandomId() == ((PacketPlay00KeepAlive) packetHeader.getData()).getRandomId()) {
                recievedKeepAlive = true;
            } else if (lastKeepAlive == null) {
                recievedKeepAlive = true;
            }
        } else if (packetHeader.getData() instanceof PacketPlayIn01ChatMessage) {
            String chat = ((PacketPlayIn01ChatMessage) packetHeader.getData()).getChat();
            sendPacket(new PacketPlayOut02Chat(new Chat.Builder().setText(chat).setBold(true).setExtra(
                    Arrays.asList(
                            new Chat.Builder().setText("복사하기")
                                    .setBold(true)
                                    .setColor(ChatColor.LIGHT_GREEN)
                                    .setHoverEvent(new ChatHoverEvent(ChatHoverEvent.Action.SHOW_TEXT, new Chat.Builder().setText("복사하기").build()))
                                    .setClickEvent(new ChatClickEvent(ChatClickEvent.Action.REPLACE_CHATBOX, chat)).build()
                    )
            ).build(), ChatPosition.SYSTEM_MESSAGE));
        }
    }

    public void initiate() throws Exception {
        willSendKeepAlive = System.currentTimeMillis() + 15000;
        willReceiveKeepAliveBefore = System.currentTimeMillis() + 30000;
        Server.getKeepAlivetimer().schedule(new KeepAliveTask(), 1000, 1000);

        sendPacket(new PacketPlayOut01JoinGame(16, GameMode.SURVIVAL, Dimension.OVERWORLD, Difficulty.PEACEFUL, 20, LevelType.FLAT, false));
        sendPacket(new PacketPlayOut3FPluginChannel("MC|Brand","MineCheat"));
        sendPacket(new PacketPlayOut41ServerDifficulty(Difficulty.PEACEFUL));
        sendPacket(new PacketPlayOut05SpawnPosition(new Location(8, 32, 8)));
        sendPacket(new PacketPlayOut39PlayerAbilities((byte) PlayerAbilities.INVULNERABLE, 0, 0.1f));
        sendPacket(new PacketPlayOut09HeldItemChange((byte) 0));
        sendPacket(new PacketPlayOut02Chat(new Chat.Builder().setText("ㅎㅇㅎㅇ").build(), ChatPosition.SYSTEM_MESSAGE));
        PlayerListItem item = new PlayerListItem.Builder()
                                        .setAction(PlayerListItem.Action.ADD)
                                        .addActionValue(new PlayerListItem.ActionValue.AddPlayer(nettyHandler.getUserData().getUid(), nettyHandler.getUserData().getUsername(), nettyHandler.getUserData().getProperties(), GameMode.SURVIVAL, 10, null)).build();
        sendPacket(new PacketPlayOut38PlayerListItem(item));
        sendPacket(new PacketPlayOut38PlayerListItem(item));
        sendPacket(new PacketPlayOut08PlayerPositionAndLook(8, 32, 8, 0, 90, (byte) 0));
        sendPacket(new PacketPlayOut03TimeUpdate(6000,6000));
        sendPacket(new PacketPlayOut26MapChunkBulkButDeveloperIsLazyAndRefusingToImplement());
        sendPacket(new PacketPlayOut08PlayerPositionAndLook(8, 32, 8, 0, 90, (byte) 0));
        sendPacket(new PacketPlayOut08PlayerPositionAndLook(8, 32, 8, 0, 90, (byte) 0));
        sendPacket(new PacketPlayOut08PlayerPositionAndLook(8, 32, 8, 0, 90, (byte) 0));
        sendPacket(new PacketPlayOut08PlayerPositionAndLook(8, 32, 8, 0, 90, (byte) 0));
        sendPacket(new PacketPlayOut08PlayerPositionAndLook(8, 32, 8, 0, 90, (byte) 0));
        sendPacket(new PacketPlayOut08PlayerPositionAndLook(8, 32, 8, 0, 90, (byte) 0));
        sendPacket(new PacketPlayOut08PlayerPositionAndLook(8, 32, 8, 0, 90, (byte) 0));
        sendPacket(new PacketPlayOut08PlayerPositionAndLook(8, 32, 8, 0, 90, (byte) 0));

    }

    public class KeepAliveTask extends TimerTask {

        @Override
        public void run() {
            try {
                if (!recievedKeepAlive && willReceiveKeepAliveBefore < System.currentTimeMillis()) {
                    nettyHandler.disconnect("disconnect.timeout");
                    return;
                }
                if (willSendKeepAlive < System.currentTimeMillis()) {
                    sendPacket(lastKeepAlive = new PacketPlay00KeepAlive(Server.getRandom().nextInt()));
                    willSendKeepAlive = System.currentTimeMillis() + 15000;
                    willReceiveKeepAliveBefore = System.currentTimeMillis() + 30000;
                    recievedKeepAlive = false;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
