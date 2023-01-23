package com.openathena;

// Code adapted from https://gis.stackexchange.com/a/418152/205005 user Nickname Nick
//     modified by github.com/mkrupczak3 for use with OpenAthena
//
// GPL-v3.0

public class CK42_Gauss_Krüger_Translator {
    // Параметры эллипсоида Красовского // Parameters of the Krasovsky ellipsoid
    static final double a = WGS84_CK42_Geodetic_Translator.aP;
    static final double b = 6356863.019d; // Малая (полярная) полуось // Small (polar) semi-axis
    static final double e2 = (Math.pow(a, 2.0d) - Math.pow(b, 2.0d)) / Math.pow(a, 2.0d);  // Эксцентриситет // Eccentricity
    static final double n = (a - b) / (a + b); // Приплюснутость // Flatness

    // Параметры зоны Гаусса-Крюгера // Parameters of the Gauss-Kruger zone
    static final double F = 1.0d; // Масштабный коэффициент // Scale factor
    static final double Lat0 = 0.0d; // Начальная параллель (в радианах) // Initial parallel (in radians)

    public static long[] CK42_Geodetic_to_Gauss_Krüger(double CK42_LatDegrees, double CK42_LonDegrees) {
        while (CK42_LonDegrees < 0.0d) {
            CK42_LonDegrees += 360.0d;
        }
        while (CK42_LonDegrees >= 360.0d) {
            CK42_LonDegrees -= 360.0d;
        }

        // Номер зоны Гаусса-Крюгера // Number of the Gauss-Kruger zone
        int zone = (int) (CK42_LonDegrees / 6.0 + 1);
        double Lon0 = (zone*6-3)* Math.PI/180;  // Центральный меридиан (в радианах)//Central Meridian (in radians)
        double N0 = 0.0;                  // Условное северное смещение для начальной параллели // Conditional north offset for the initial parallel
        double E0 = zone*1e6+500000.0d;    // Условное восточное смещение для центрального меридиана // Conditional eastern offset for the central meridian


        // Перевод широты и долготы в радианы // Converting latitude and longitude to radians
        double Lat = CK42_LatDegrees*Math.PI/180.0;
        double Lon = CK42_LonDegrees*Math.PI/180.0;

        // Вычисление переменных для преобразования // Calculating variables for conversion
        double sinLat = Math.sin(Lat);
        double cosLat = Math.cos(Lat);
        double tanLat = Math.tan(Lat);

        double v = a * F * Math.pow(1-e2* Math.pow(sinLat,2),-0.5d);
        double p = a*F*(1-e2) * Math.pow(1-e2*Math.pow(sinLat,2),-1.5d);
        double n2 = v/p-1;
        double M1 = (1+n+5.0d/4.0d* Math.pow(n,2) +5.0d/4.0d* Math.pow(n,3)) * (Lat-Lat0);
        double M2 = (3*n+3* Math.pow(n,2) +21.0d/8.0d* Math.pow(n,3)) * Math.sin(Lat - Lat0) * Math.cos(Lat + Lat0);
        double M3 = (15.0d/8.0d* Math.pow(n,2) +15.0d/8.0d* Math.pow(n,3))*Math.sin(2 * (Lat - Lat0))*Math.cos(2 * (Lat + Lat0));
        double M4 = 35.0d/24.0d* Math.pow(n,3) *Math.sin(3 * (Lat - Lat0)) * Math.cos(3 * (Lat + Lat0));
        double M = b*F*(M1-M2+M3-M4);
        double I = M+N0;
        double II = v/2 * sinLat * cosLat;
        double III = v/24 * sinLat * Math.pow(cosLat,3) * (5-Math.pow(tanLat,2)+9*n2);
        double IIIA = v/720 * sinLat * Math.pow(cosLat,5) * (61-58*Math.pow(tanLat,2)+Math.pow(tanLat,4));
        double IV = v * cosLat;
        double V = v/6 * Math.pow(cosLat,3) * (v/p-Math.pow(tanLat,2));
        double VI = v/120 * Math.pow(cosLat,5) * (5-18*Math.pow(tanLat,2)+Math.pow(tanLat,4)+14*n2-58*Math.pow(tanLat,2)*n2);

        // Вычисление северного и восточного смещения (в метрах) // Calculation of the north and east offset (in meters)
        double N = I+II* Math.pow(Lon-Lon0,2)+III* Math.pow(Lon-Lon0,4)+IIIA* Math.pow(Lon-Lon0,6);
        double E = E0+IV*(Lon-Lon0)+V* Math.pow(Lon-Lon0,3)+VI* Math.pow(Lon-Lon0,5);

        long[] outArr = new long[]{(long) N, (long) E};
        return outArr;
    }

}
