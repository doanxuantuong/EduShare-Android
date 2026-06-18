package com.example.edushare.common.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateTimeUtils {

    private static final long MILLIS_IN_MINUTE = 60 * 1000;
    private static final long MILLIS_IN_HOUR = 60 * MILLIS_IN_MINUTE;
    private static final long MILLIS_IN_DAY = 24 * MILLIS_IN_HOUR;

    public static Date parseDate(String time) {
        if (time == null || time.isEmpty()) return null;

        String[] patterns = {
                "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                "yyyy-MM-dd'T'HH:mm:ssXXX",
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd HH:mm:ss"
        };

        for (String pattern : patterns) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat(pattern, Locale.getDefault());

                inputFormat.setLenient(false);

                if (pattern.contains("'Z'")) {
                    inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                } else if (pattern.equals("yyyy-MM-dd HH:mm:ss")) {
                    inputFormat.setTimeZone(TimeZone.getDefault());
                }
                return inputFormat.parse(time);
            } catch (ParseException ignored) {
                // Tiếp tục thử pattern khác
            }
        }
        return null; // Trả về null nếu không parse được thay vì crash app
    }

    public static String getHourTime(String time) {
        Date date = parseDate(time);
        if (date == null) return "";

        SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        outputFormat.setTimeZone(TimeZone.getDefault());
        return outputFormat.format(date);
    }

    // Logic hiển thị thời gian linh hoạt (dùng cho cả ChatList và Message)
    public static String getRelativeTime(String timeAt) {
        Date date = parseDate(timeAt);
        if (date == null) return timeAt != null ? timeAt : "";

        long diffMillis = System.currentTimeMillis() - date.getTime();

        if (diffMillis < MILLIS_IN_MINUTE) {
            return "Vừa xong";
        } else if (diffMillis < MILLIS_IN_HOUR) {
            return (diffMillis / MILLIS_IN_MINUTE) + " phút trước";
        } else if (diffMillis < MILLIS_IN_DAY) {
            return (diffMillis / MILLIS_IN_HOUR) + " giờ trước";
        } else if (diffMillis < 2 * MILLIS_IN_DAY) {
            SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            return "Hôm qua, " + outputFormat.format(date);
        } else if (diffMillis < 7 * MILLIS_IN_DAY) {
            return (diffMillis / MILLIS_IN_DAY) + " ngày trước";
        } else {
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy, HH:mm", Locale.getDefault());
            return outputFormat.format(date);
        }
    }

    // Kiểm tra xem 2 tin nhắn có cách nhau quá xa không (ví dụ > 1 giờ)
    public static boolean isTimeDifferenceSignificant(String time1, String time2) {
        Date date1 = parseDate(time1);
        Date date2 = parseDate(time2);

        if (date1 == null || date2 == null) return false;

        long diffMillis = Math.abs(date1.getTime() - date2.getTime());
        return diffMillis > MILLIS_IN_HOUR;
    }

    public static String getSeparatorTime(String timeAt) {
        Date date = parseDate(timeAt);
        if (date == null) return "";

        // Do tính thời gian ở trên là tính số milisecond
        // Nếu như như nhắn lúc 23h59 và bên kia nhận lúc 00h02
        // Thì lúc này nó sẽ nghĩ vẫn là hôm nay do chỉ có 2 phút trôi qua chứ không phải 24h
        // Lúc này cần đến lịch để tăng độ chính xác

        // Lấy ngày đầu vào đưa vào lịch
        Calendar messageCal = Calendar.getInstance();
        messageCal.setTime(date);

        // Tạo ra thêm lịch 2 để lấy ngay hiện tại
        Calendar todayCal = Calendar.getInstance();

        // Format thời gian khi nó còn trong 2 ngày
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        // Kiểm tra xem có phải cùng năm và cùng ngày trong năm (Hôm nay) không
        if (todayCal.get(Calendar.YEAR) == messageCal.get(Calendar.YEAR) && todayCal.get(Calendar.DAY_OF_YEAR) == messageCal.get(Calendar.DAY_OF_YEAR)) {
            return "Hôm nay, " + timeFormat.format(date);
        }

        // Lùi lại 1 ngày để kiểm tra Hôm qua
        todayCal.add(Calendar.DAY_OF_YEAR, -1);
        if (todayCal.get(Calendar.YEAR) == messageCal.get(Calendar.YEAR) &&
                todayCal.get(Calendar.DAY_OF_YEAR) == messageCal.get(Calendar.DAY_OF_YEAR)) {
            return "Hôm qua, " + timeFormat.format(date);
        }

        // Nếu quá 2 ngày (không phải hôm nay và hôm qua)
        SimpleDateFormat fullFormat = new SimpleDateFormat("dd/MM/yyyy, HH:mm", Locale.getDefault());
        return fullFormat.format(date);
    }
}