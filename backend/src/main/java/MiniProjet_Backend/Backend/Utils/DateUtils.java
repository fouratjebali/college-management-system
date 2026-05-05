package MiniProjet_Backend.Backend.Utils;

import lombok.experimental.UtilityClass;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@UtilityClass
public class DateUtils {
    
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
    
    /**
     * Format a LocalDateTime to ISO format
     */
    public static String formatToIso(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(ISO_FORMATTER) : null;
    }

    /**
     * Parse a LocalDateTime from ISO format
     */
    public static LocalDateTime parseFromIso(String dateString) {
        return dateString != null ? LocalDateTime.parse(dateString, ISO_FORMATTER) : null;
    }

    /**
     * Get the current timestamp
     */
    public static LocalDateTime getCurrentTimestamp() {
        return LocalDateTime.now();
    }
}

