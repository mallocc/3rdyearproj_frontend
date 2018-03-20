package com.example.mallocc.caloriecompanion;

/**
 * Created by hercu on 16-Mar-18.
 */

public class GlobalSettings {

    static public final String unitsNames[] = {
            "Grams",
            "Kilos"
    };



    static public final int UNITS_GRAMS = 0;
    static public final int UNITS_KILOS = 1;

    static public int units = UNITS_GRAMS;

    static public final String searchQuerySizes[] = {
            "1",
            "5",
            "10"
    };

    static public final int SEARCH_QUERY_SIZE_1 = 0;
    static public final int SEARCH_QUERY_SIZE_5 = 1;
    static public final int SEARCH_QUERY_SIZE_10 = 2;

    static public int searchQuerySize = SEARCH_QUERY_SIZE_1;

}
