package org.mule.extension.parquet.internal.int96;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.parquet.io.api.Binary;

public final class ParquetTimestampUtils {
    private static final String INT_96_BYTE_REGEX = "\\[-?\\d+, -?\\d+, -?\\d+, -?\\d+, -?\\d+, -?\\d+, -?\\d+, -?\\d+, -?\\d+, -?\\d+, -?\\d+,"
            + " -?\\d+\\]";
    private static final Pattern PATTERN = Pattern.compile(INT_96_BYTE_REGEX);
    private static final int JULIAN_EPOCH_OFFSET_DAYS = 2_440_588;
    private static final long MILLIS_IN_DAY = TimeUnit.DAYS.toMillis(1);
    private static final long NANOS_PER_MILLISECOND = TimeUnit.MILLISECONDS.toNanos(1);

    private ParquetTimestampUtils() {
    }

    /**
     * Given a String containing INT96 byte[12] arrays, converts each in-place to a
     * formatting
     * timestamp String.
     *
     * @param jsonRecord the JSON record containing INT96 byte[12] arrays
     * @return the String with byte arrays converted to timestamps
     */
    public static String convertInt96(String jsonRecord) {
        Matcher matcher = PATTERN.matcher(jsonRecord);
        if (matcher.find()) {
            int startIdx = matcher.start();
            int endIdx = matcher.end();
            byte[] bytes = extractMatch(jsonRecord, matcher);
            Binary binary = Binary.fromReusedByteArray(bytes);
            long timestampMillis = ParquetTimestampUtils.getTimestampMillis(binary);
            ZonedDateTime zonedDateTime = Instant.ofEpochMilli(timestampMillis).atZone(ZoneId.of("UTC"));
            String formattedTimestamp = DateTimeFormatter.ISO_ZONED_DATE_TIME.format(zonedDateTime);
            String updatedRecord = jsonRecord.substring(0, startIdx)
                    + "\""
                    + formattedTimestamp
                    + "\""
                    + jsonRecord.substring(endIdx);
            return convertInt96(updatedRecord);
        }
        return jsonRecord;
    }

    /**
     * Returns GMT timestamp from binary encoded parquet timestamp (12 bytes -
     * julian date + time of
     * day nanos).
     *
     * @param timestampBinary INT96 parquet timestamp
     * @return timestamp in millis, GMT timezone
     */
    private static long getTimestampMillis(Binary timestampBinary) {
        if (timestampBinary.length() != 12) {
            return 0L;
        }
        byte[] bytes = timestampBinary.getBytes();

        // little endian encoding - need to invert byte order
        long timeOfDayNanos = ByteBuffer.wrap(
                new byte[] {
                        bytes[7], bytes[6], bytes[5], bytes[4], bytes[3], bytes[2], bytes[1], bytes[0]
                })
                .getLong();
        int julianDay = ByteBuffer.wrap(new byte[] { bytes[11], bytes[10], bytes[9], bytes[8] }).getInt();

        return julianDayToMillis(julianDay) + (timeOfDayNanos / NANOS_PER_MILLISECOND);
    }

    private static long julianDayToMillis(int julianDay) {
        return (julianDay - JULIAN_EPOCH_OFFSET_DAYS) * MILLIS_IN_DAY;
    }

    private static byte[] extractMatch(String jsonRecord, Matcher matcher) {
        String extracted = jsonRecord.substring(matcher.start(), matcher.end());
        String removedBrackets = extracted.substring(1, extracted.length() - 1);
        String[] split = removedBrackets.split(", ");
        return toByte(split);
    }

    private static byte[] toByte(String[] strs) {
        byte[] bytes = new byte[strs.length];
        for (int i = 0; i < strs.length; i++) {
            bytes[i] = Byte.parseByte(strs[i]);
        }
        return bytes;
    }
}
