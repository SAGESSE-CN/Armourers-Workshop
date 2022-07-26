package moe.plushie.armourers_workshop.utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import moe.plushie.armourers_workshop.core.network.CustomPacket;
import moe.plushie.armourers_workshop.init.ModLog;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;

public class PacketSplitter {

    private final byte firstFlag = -2;
    private final byte lastFlag = -3;

    private final HashMap<UUID, ArrayList<ByteBuf>> receivedBuffers = new HashMap<>();
    private final ExecutorService executor = Executors.newFixedThreadPool(2, r -> new Thread(r, "Network-Data-Coder"));

    public PacketSplitter() {
    }

    public void split(final CustomPacket message, Function<FriendlyByteBuf, Packet<?>> builder, int partSize, Consumer<Packet<?>> consumer) {
        executor.submit(() -> {
            FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
            buffer.writeInt(message.getPacketID());
            message.encode(buffer);
            buffer.capacity(buffer.readableBytes());
            // when packet exceeds the part size, it will be split automatically
            int bufferSize = buffer.readableBytes();
            if (bufferSize <= partSize) {
                Packet<?> packet = builder.apply(buffer);
                consumer.accept(packet);
                return;
            }
            for (int index = 0; index < bufferSize; index += partSize) {
                ByteBuf partPrefix = Unpooled.buffer(4);
                if (index == 0) {
                    partPrefix.writeInt(firstFlag);
                } else if ((index + partSize) >= bufferSize) {
                    partPrefix.writeInt(lastFlag);
                } else {
                    partPrefix.writeInt(-1);
                }
                int resolvedPartSize = Math.min(bufferSize - index, partSize);
                ByteBuf buffer1 = Unpooled.wrappedBuffer(partPrefix, buffer.retainedSlice(buffer.readerIndex(), resolvedPartSize));
                buffer.skipBytes(resolvedPartSize);
                Packet<?> packet = builder.apply(new FriendlyByteBuf(buffer1));
                consumer.accept(packet);
            }
            buffer.release();
        });
    }

    public void merge(UUID uuid, FriendlyByteBuf buffer, Consumer<FriendlyByteBuf> consumer) {
        int packetState = buffer.getInt(0);
        if (packetState < 0) {
            ArrayList<ByteBuf> playerReceivedBuffers = receivedBuffers.computeIfAbsent(uuid, k -> new ArrayList<>());
            if (packetState == firstFlag) {
                if (!playerReceivedBuffers.isEmpty()) {
                    ModLog.warn("aw2:split received out of order - inbound buffer not empty when receiving first");
                    playerReceivedBuffers.clear();
                }
            }
            buffer.skipBytes(4); // skip header
            playerReceivedBuffers.add(buffer.retainedDuplicate()); // we need to keep writer/reader index
            if (packetState == lastFlag) {
                executor.submit(() -> {
                    // ownership will transfer to full buffer, so don't call release again.
                    FriendlyByteBuf full = new FriendlyByteBuf(Unpooled.wrappedBuffer(playerReceivedBuffers.toArray(new ByteBuf[0])));
                    playerReceivedBuffers.clear();
                    consumer.accept(full);
                    full.release();
                });
            }
            return;
        }
        if (buffer.readableBytes() < 3000) { // 3k
            consumer.accept(buffer);
            return;
        }
        ByteBuf receivedBuf = buffer.retainedDuplicate(); // we need to keep writer/reader index
        executor.submit(() -> {
            consumer.accept(new FriendlyByteBuf(receivedBuf));
            receivedBuf.release();
        });
    }
}