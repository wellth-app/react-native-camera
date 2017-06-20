package com.wellthapp.ContinuousRCTCamera;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableType;

import java.util.ArrayList;
import java.util.List;

public class Utils {

    public static List<String> unpackReadableStringArray(final ReadableArray readableArray) {
        final List<String> result = new ArrayList<>();
        if (readableArray != null) {
            final int size = readableArray.size();

            for (int i = 0; i < size; i++) {
                final ReadableType typeAtIndex = readableArray.getType(i);
                if (typeAtIndex.equals(ReadableType.String)) {
                    result.add(readableArray.getString(i));
                }
            }
        }
        return result;
    }

    public static byte[] toPrimitive(final Byte[] oBytes) {
        final byte[] bytes = new byte[oBytes.length];
        for (int i = 0; i < oBytes.length; i++) {
            bytes[i] = oBytes[i];
        }
        return bytes;
    }

}
