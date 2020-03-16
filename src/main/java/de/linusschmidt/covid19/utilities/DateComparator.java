package de.linusschmidt.covid19.utilities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;

public class DateComparator implements Comparator<String> {

    public int compare(String date1, String date2) {
        try {
            return new SimpleDateFormat("M/d/yy").parse(date1).compareTo(new SimpleDateFormat("M/d/yy").parse(date2));
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
    }
}