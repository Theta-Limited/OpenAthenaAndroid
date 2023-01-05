package com.openathena;

/**
 * Thank you author raven and github.com/Dimowner
 * Adapted by mkrupczak3 for use in OpenAthena
 * GPL v3.0
 *
 * Original javadoc comments in Russian language available at:
 * https://github.com/Dimowner/WGS84_TO_SK42/blob/master/app/src/main/java/ua/app/dronnen/mapapplication/WGS84_SK42_Translator.java
 *
 * Class for translating geographic coordinates in WGS84 and CK42 systems.
 * Code copied from www.gis-lab.info
 * Класс для перевода географических координат в системах WGS84 и СК42.
 * Код скопирован с ресурса www.gis-lab.info
 * http://gis-lab.info/qa/wgs84-sk42-wgs84-formula.html
 * http://gis-lab.info/qa/datum-transform-methods.html
 * @author Raven
 */
public class WGS84_CK42_Geodetic_Translator {

    /**
     * Recalculation of latitude from WGS-84 to CK-42.
     * @param Bd latitude
     * @param Ld longitude
     * @param H height
     * @return latitude in CK-42
     */
    public static double WGS84_CK42_Lat(double Bd, double Ld, double H) {
        return Bd - dB(Bd, Ld, H) / 3600;
    }

//    /**
//     * Пересчет широты из СК-42 в WGS-84.
//     * @param Bd широта
//     * @param Ld долгота
//     * @param H высота
//     * @return широта в WGS-84
//     */
//    public static double SK42_WGS84_Lat(double Bd, double Ld, double H) {
//        return Bd + dB(Bd, Ld, H) / 3600;
//    }

    /**
     * Recalculation of longitude from WGS-84 to CK-42.
     * @param Bd latitude
     * @param Ld longitude
     * @param H height
     * @return longitude in CK-42
     */
    public static double WGS84_CK42_Long(double Bd, double Ld, double H) {
        return Ld - dL(Bd, Ld, H) / 3600;
    }

//    /**
//     * Пересчет долготы из СК-42 в WGS-84.
//     * @param Bd широта
//     * @param Ld долгота
//     * @param H высота
//     * @return долгота в WGS-84
//     */
//    public static double CK42_WGS84_Long(double Bd, double Ld, double H) {
//        return Ld + dL(Bd, Ld, H) / 3600;
//    }

    /**
     *
     * @param Bd latitude
     * @param Ld longitude
     * @param H height
     * @return
     */
    public static double dB(double Bd, double Ld, double H) {
        double B = Bd * Math.PI / 180;
        double L = Ld * Math.PI / 180;
        double M = a * (1 - e2) / Math.pow((1 - e2 * Math.pow(Math.sin(B), 2)), 1.5);
        double N = a * Math.pow((1 - e2 * Math.pow(Math.sin(B), 2)), -0.5);
        double result = ro / (M + H) * (N / a * e2 * Math.sin(B) * Math.cos(B) * da
                + (Math.pow(N, 2) / Math.pow(a, 2) + 1) * N * Math.sin(B) *
                Math.cos(B) * de2 / 2 - (dx * Math.cos(L) + dy * Math.sin(L)) *
                Math.sin(B) + dz * Math.cos(B)) - wx * Math.sin(L) * (1 + e2 *
                Math.cos(2 * B)) + wy * Math.cos(L) * (1 + e2 * Math.cos(2 * B)) -
                ro * ms * e2 * Math.sin(B) * Math.cos(B);
        return result;
    }

    /**
     *
     * @param Bd latitude
     * @param Ld longitude
     * @param H height
     *@return
     */
    public static double dL (double Bd, double Ld, double H) {
        double B = Bd * Math.PI / 180;
        double L = Ld * Math.PI / 180;
        double N = a * Math.pow((1 - e2 * Math.pow(Math.sin(B), 2)), -0.5);
        return ro / ((N + H) * Math.cos(B)) * (-dx * Math.sin(L) + dy * Math.cos(L))
                + Math.tan(B) * (1 - e2) * (wx * Math.cos(L) + wy * Math.sin(L)) - wz;
    }

    /**
     *
     * @param Bd latitude (CK-42)
     * @param Ld longitude (CK-42)
     * @param H height (CK-42)
     *@return height, in meters (WGS84)
     */
    public static double CK42_WGS84_Alt(double Bd, double Ld, double H) {
        double B = Bd * Math.PI / 180;
        double L = Ld * Math.PI / 180;
        double N = a * Math.pow((1 - e2 * Math.pow(Math.sin(B), 2)), -0.5);
        double dH = -a / N * da + N * Math.pow(Math.sin(B), 2) * de2 / 2 +
                (dx * Math.cos(L) + dy * Math.sin(L)) *
                        Math.cos(B) + dz * Math.sin(B) - N * e2 *
                Math.sin(B) * Math.cos(B) *
                (wx / ro * Math.sin(L) - wy / ro * Math.cos(L)) +
                (Math.pow(a, 2) / N + H) * ms;
        return H + dH;
    }

    // Математические константы // Mathematical constants
    public static final double ro = 206264.8062;          // Число угловых секунд в радиане // Number of arcseconds in radians
    // Эллипсоид Красовского // Krasovsky's ellipsoid
    public static final double aP = 6378245;              // Большая полуось // Semi-major axis
    public static final double  alP = 1 / 298.3;          // Сжатие // Compression
    public static final double  e2P = 2 * alP - Math.pow(alP, 2);  // Квадрат эксцентриситета // Square of eccentricity
    // Эллипсоид WGS84 (GRS80, эти два эллипсоида сходны по большинству параметров)
    // Ellipsoid WGS84 (GRS80, these two ellipsoids are similar in most parameters)
    public static final double aW = 6378137;                  // Большая полуось // Major axis
    public static final double alW = 1 / 298.257223563;       // Сжатие // Compression
    public static final double e2W = 2 * alW - Math.pow(alW, 2);// Квадрат эксцентриситета // Square of eccentricity
    // Вспомогательные значения для преобразования эллипсоидов
    // Auxiliary values for transforming ellipsoids
    public static final double  a   = (aP + aW) / 2;
    public static final double  e2  = (e2P + e2W) / 2;
    public static final double  da  = aW - aP;
    public static final double  de2 = e2W - e2P;
    // Линейные элементы трансформирования, в метрах
    // Linear transform elements, in meters
    public static final double dx = 23.92;
    public static final double dy = -141.27;
    public static final double dz = -80.9;
    // Угловые элементы трансформирования, в секундах
    // Corner transform elements, in seconds
    public static final double wx = 0;
    public static final double wy = 0;
    public static final double wz = 0;
    // Дифференциальное различие масштабов
    // Differential scale difference
    public static final double ms = 0;
}
