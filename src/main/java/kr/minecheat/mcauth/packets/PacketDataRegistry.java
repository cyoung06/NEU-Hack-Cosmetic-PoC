package kr.minecheat.mcauth.packets;

import java.util.*;

public class PacketDataRegistry {
    private static final HashMap<Integer, Class<PacketData>[]> readablePackets = new HashMap<>();
    private static final HashMap<Integer, List<Class<PacketData>>> allPackets = new HashMap<>();


    static {
        register(new PacketStatus00ServerListPing());
        register(new PacketStatus00ServerListPingResponse());
        register(new PacketLogin00Disconnect());
        register(new PacketStatus01JustAPing());
        register(new PacketLogin00LoginStart());
        register(new PacketLogin01EncryptionRequest());
        register(new PacketLogin01EncryptionResponse());
        register(new PacketLogin03SetCompression());
        register(new PacketPlay00KeepAlive());
    }

    private static void register(PacketData packet) {
        if (allPackets.containsKey(packet.getPacketID())) {
            allPackets.get(packet.getPacketID()).add((Class<PacketData>) packet.getClass());
        } else {
            allPackets.put(packet.getPacketID(), new ArrayList<>(Arrays.asList((Class < PacketData >) packet.getClass())));
        }

        if (packet.getPacketType() != PacketType.CLIENTBOUND) {
            if (!readablePackets.containsKey(packet.getPacketID())) {
                readablePackets.put(packet.getPacketID(), new Class[3]);
            }
            readablePackets.get(packet.getPacketID())[packet.getPacketState().ordinal()] = (Class<PacketData>) packet.getClass();
        }
    }

    public static PacketData getPacketData(PacketHeader header, PacketState state) throws IllegalAccessException, InstantiationException {
        Class<PacketData>[] dataList = readablePackets.get(header.getPacketId());
        Class<PacketData> data = dataList[state.ordinal()];
        Objects.requireNonNull(data);
        return data.newInstance();
    }
}
