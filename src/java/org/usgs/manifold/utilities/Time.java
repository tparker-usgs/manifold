package org.usgs.manifold.utilities;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Some simple static methods for time conversion related to ExportCVO
 *
 */
public class Time {

    /**
     * Method to convert "Unix time" (milliseconds seconds since midnight,
     * Jan 1, 1970 UTC) to J2KSeconds. It's a set offset of 946728000 seconds
     * which is approximately 30 years.
     *
     * @param unixTime the milliseconds since midnight, Jan 1, 1970.
     * @return the time in J2K seconds.
     */
    public static long toJ2KSeconds(long unixTime) {
        // that's a 'L' on the end of 1000 for a long, not the number 1.
        return (unixTime / 1000l) - 946728000;
    }

    /**
     * Method to convert "Unix time" (milliseconds seconds since midnight,
     * Jan 1, 1970 UTC) to J2KSeconds. It's a set offset of 946728000 seconds
     * which is approximately 30 years.
     *
     * @param unixTime the milliseconds since midnight, Jan 1, 1970.
     * @return the time in J2K seconds.
     */
    public static double toJ2KSeconds(double unixTime) {
        return Math.floor(unixTime / 1000) - 946728000;
    }

    /**
     * Converts the time given in a YYYYMMDDHHMMSS.SSS format into "Unix
     * time," milliseconds since midnight January 1st, 1970 UTC.
     *
     * @param time the time in a YYYYMMDDHHMMSS.SSS format.
     * @return the date converted to "Unix time," milliseconds since midnight
     * Jan 1, 1970 UTC.
     * @throws IllegalArgumentException if there is an error converting the
     * string into a valid date.
     */
    public static long parsePacketTime(String time) {
        int year = 0, month = 0, day = 0, hour = 0, minute = 0, second = 0;

        // Check the length of the date.
        if (time.length() != 18) {
            throw new IllegalArgumentException("'" + time + "' Does not match "
                    + "the required argument length of YYYYMMDDHHMMSS.SSS.");
        }

        // Try to convert input to integers
        try {
            year = Integer.parseInt(time.substring(0, 4));
            month = Integer.parseInt(time.substring(4, 6));
            month--; //Java uses 0 - 11 for months
            day = Integer.parseInt(time.substring(6, 8));
            hour = Integer.parseInt(time.substring(8, 10));
            minute = Integer.parseInt(time.substring(10, 12));
            second = Integer.parseInt(time.substring(12, 14));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("'" + time + "' contains "
                    + "characters that can not be converted into integers. "
                    + e.getMessage());
        }

        // Create a calendar for time manipulation.
        Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));

        // Causes an error to be thrown if the date isn't strictly legitamate.
        calendar.setLenient(false);

        try {
            calendar.set(year, month, day, hour, minute, second);
            return calendar.getTimeInMillis();
        } catch (IllegalArgumentException e) {
            System.out.println("year   - " + year );
            System.out.println("month  - " + month );
            System.out.println("day    - " + day );
            System.out.println("hour   - " + hour );
            System.out.println("minute - " + minute );
            System.out.println("second - " + second );
            throw new IllegalArgumentException("'" + time + "' is not a valid "
                    + "date. Error: " + e.getMessage());
        }
    }
}
