package cn.jason31416.authX.util;

import com.github.retrooper.packetevents.protocol.nbt.*;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import net.kyori.adventure.nbt.*;

import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@SuppressWarnings({"DuplicatedCode", "DuplicateBranchesInSwitch"})
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DimensionUtil {
    @SneakyThrows
    private static @Nonnull String streamToString(InputStream in) {
        try (BufferedReader bufReader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(in), StandardCharsets.UTF_8))) {
            return bufReader.lines().collect(Collectors.joining("\n"));
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T as(Object obj) {
        return (T) obj;
    }

    private static @Nonnull NBTCompound tagToNbt(@Nonnull CompoundBinaryTag tag) {
        NBTCompound ret = new NBTCompound();
        for (Map.Entry<String, ? extends BinaryTag> subTag : tag.stream().collect(Collectors.toSet())) {
            switch(subTag.getValue().type().id()) {
                case 0x00 -> ret.setTag(subTag.getKey(), NBTEnd.INSTANCE);
                case 0x01 -> ret.setTag(subTag.getKey(), new NBTByte(((ByteBinaryTag) subTag.getValue()).value()));
                case 0x02 -> ret.setTag(subTag.getKey(), new NBTShort(((ShortBinaryTag) subTag.getValue()).value()));
                case 0x03 -> ret.setTag(subTag.getKey(), new NBTInt(((IntBinaryTag) subTag.getValue()).value()));
                case 0x04 -> ret.setTag(subTag.getKey(), new NBTLong(((LongBinaryTag) subTag.getValue()).value()));
                case 0x05 -> ret.setTag(subTag.getKey(), new NBTFloat(((FloatBinaryTag) subTag.getValue()).value()));
                case 0x06 -> ret.setTag(subTag.getKey(), new NBTDouble(((DoubleBinaryTag) subTag.getValue()).value()));
                case 0x07 -> ret.setTag(subTag.getKey(), new NBTByteArray(((ByteArrayBinaryTag) subTag.getValue()).value()));
                case 0x08 -> ret.setTag(subTag.getKey(), new NBTString(((StringBinaryTag) subTag.getValue()).value()));
                case 0x09 -> ret.setTag(subTag.getKey(), tagListToNbtList((ListBinaryTag) subTag.getValue()));
                case 0x0A -> ret.setTag(subTag.getKey(), tagToNbt((CompoundBinaryTag) subTag.getValue()));
                case 0x0B -> ret.setTag(subTag.getKey(), new NBTIntArray(((IntArrayBinaryTag) subTag.getValue()).value()));
                case 0x0C -> ret.setTag(subTag.getKey(), new NBTLongArray(((LongArrayBinaryTag) subTag.getValue()).value()));
            }
        }
        return ret;
    }

    private static @Nonnull NBTList<?> tagListToNbtList(@Nonnull ListBinaryTag tag) {
        NBTType<?> type = switch (tag.elementType().id()) {
            case 0x00 -> NBTType.END;
            case 0x01 -> NBTType.BYTE;
            case 0x02 -> NBTType.SHORT;
            case 0x03 -> NBTType.INT;
            case 0x04 -> NBTType.LONG;
            case 0x05 -> NBTType.FLOAT;
            case 0x06 -> NBTType.DOUBLE;
            case 0x07 -> NBTType.BYTE_ARRAY;
            case 0x08 -> NBTType.STRING;
            case 0x09 -> NBTType.LIST;
            case 0x0A -> NBTType.COMPOUND;
            case 0x0B -> NBTType.INT_ARRAY;
            case 0x0C -> NBTType.LONG_ARRAY;
            default -> NBTType.END;
        };

        NBTList<?> ret = new NBTList<>(type);
        for (BinaryTag subTag : tag) {
            switch (tag.elementType().id()) {
                case 0x00 -> ret.addTag(as(NBTEnd.INSTANCE));
                case 0x01 -> ret.addTag(as(new NBTByte(((ByteBinaryTag) subTag).value())));
                case 0x02 -> ret.addTag(as(new NBTShort(((ShortBinaryTag) subTag).value())));
                case 0x03 -> ret.addTag(as(new NBTInt(((IntBinaryTag) subTag).value())));
                case 0x04 -> ret.addTag(as(new NBTLong(((LongBinaryTag) subTag).value())));
                case 0x05 -> ret.addTag(as(new NBTFloat(((FloatBinaryTag) subTag).value())));
                case 0x06 -> ret.addTag(as(new NBTDouble(((DoubleBinaryTag) subTag).value())));
                case 0x07 -> ret.addTag(as(new NBTByteArray(((ByteArrayBinaryTag) subTag).value())));
                case 0x08 -> ret.addTag(as(new NBTString(((StringBinaryTag) subTag).value())));
                case 0x09 -> ret.addTag(as(tagListToNbtList((ListBinaryTag) subTag)));
                case 0x0A -> ret.addTag(as(tagToNbt((CompoundBinaryTag) subTag)));
                case 0x0B -> ret.addTag(as(new NBTIntArray(((IntArrayBinaryTag) subTag).value())));
                case 0x0C -> ret.addTag(as(new NBTLongArray(((LongArrayBinaryTag) subTag).value())));
            }
        }
        return ret;
    }

    @SneakyThrows
    public static @Nonnull NBTCompound getDimensionCodec(@Nonnull ClientVersion cv) {
        String nbtContent;
        if(cv.isOlderThan(ClientVersion.V_1_16))
            nbtContent = streamToString(DimensionUtil.class.getClassLoader().getResourceAsStream("dimension/codec_old.snbt"));
        else if(cv.isOlderThan(ClientVersion.V_1_18_2))
            nbtContent = streamToString(DimensionUtil.class.getClassLoader().getResourceAsStream("dimension/codec_1_16.snbt"));
        else if(cv.isOlderThan(ClientVersion.V_1_19))
            nbtContent = streamToString(DimensionUtil.class.getClassLoader().getResourceAsStream("dimension/codec_1_18_2.snbt"));
        else if(cv.isOlderThan(ClientVersion.V_1_19_1))
            nbtContent = streamToString(DimensionUtil.class.getClassLoader().getResourceAsStream("dimension/codec_1_19.snbt"));
        else if(cv.isOlderThan(ClientVersion.V_1_19_4))
            nbtContent = streamToString(DimensionUtil.class.getClassLoader().getResourceAsStream("dimension/codec_1_19_1.snbt"));
        else if(cv.isOlderThan(ClientVersion.V_1_20))
            nbtContent = streamToString(DimensionUtil.class.getClassLoader().getResourceAsStream("dimension/codec_1_19_4.snbt"));
        else if(cv.isOlderThan(ClientVersion.V_1_21))
            nbtContent = streamToString(DimensionUtil.class.getClassLoader().getResourceAsStream("dimension/codec_1_20.snbt"));
        else
            nbtContent = streamToString(DimensionUtil.class.getClassLoader().getResourceAsStream("dimension/codec_1_21.snbt"));
        return tagToNbt(TagStringIO.tagStringIO().asCompound(nbtContent));
    }
}
