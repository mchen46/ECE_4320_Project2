package edu.cornell.motiontrainingapp;

/**
 * The input value from the csv file
 */

public class InputValue {

    private double time;
    private double Ax;
    private double Ay;
    private double Az;
    private double Wx;
    private double Wy;
    private double Wz;
    private int AxCondition;
    private int AyCondition;
    private int AzCondition;
    private int WxCondition;
    private int WyCondition;
    private int WzCondition;
    private String stepDescription;

    /**
     * Constructor for an input value
     *
     * @param time
     * @param Ax
     * @param Ay
     * @param Az
     * @param Wx
     * @param Wy
     * @param Wz
     * @param AxCondition
     * @param AyCondition
     * @param AzCondition
     * @param WxCondition
     * @param WyCondition
     * @param WzCondition
     * @param text
     */
    public InputValue(double time, double Ax, double Ay, double Az, double Wx, double Wy, double Wz, int AxCondition, int AyCondition, int AzCondition, int WxCondition, int WyCondition, int WzCondition, String text) {
        this.time = time;
        this.Ax = Ax;
        this.Ay = Ay;
        this.Az = Az;
        this.Wx = Wx;
        this.Wy = Wy;
        this.Wz = Wz;
        this.AxCondition = AxCondition;
        this.AyCondition = AyCondition;
        this.AzCondition = AzCondition;
        this.WxCondition = WxCondition;
        this.WyCondition = WyCondition;
        this.WzCondition = WzCondition;
        this.stepDescription = text;

    }

    public InputValue() {
    }

    /**
     * @return A formated version of this input values, in a String form
     */
    public String toString() {

        return "Values: Time = " + time + ", Ax = " + Ax + ", Ay = " + Ay + ", Az = " + Az + ", Wx = " + Wx + ", Wy = " + Wy + ", Wz = " + Wz
                + ", AxCondition = " + AxCondition + ", AyCondition = " + AyCondition + ", AzCondition = " + AzCondition
                + ", WxCondition = " + WxCondition + ", WyCondition = " + WyCondition + ", WzCondition = " + WzCondition
                + "Step description: " + stepDescription;

    }

    /**
     * @return A string with all the values from the csv file
     */
    public String getCSVLine() {

        return "( " + time + "," + Ax + "," + Ay + "," + Az + "," + Wx + "," + Wy + "," + Wz
                + "," + AxCondition + "," + AyCondition + "," + AzCondition + ","
                + WxCondition + "," + WyCondition + "," + WzCondition +
                "," + stepDescription + ")";
    }

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }

    public double getAx() {
        return Ax;
    }

    public void setAx(double ax) {
        Ax = ax;
    }

    public double getAy() {
        return Ay;
    }

    public void setAy(double ay) {
        Ay = ay;
    }

    public double getAz() {
        return Az;
    }

    public void setAz(double az) {
        Az = az;
    }

    public double getWx() {
        return Wx;
    }

    public void setWx(double wx) {
        Wx = wx;
    }

    public double getWy() {
        return Wy;
    }

    public void setWy(double wy) {
        Wy = wy;
    }

    public double getWz() {
        return Wz;
    }

    public void setWz(double wz) {
        Wz = wz;
    }

    public int getAxCondition() {
        return AxCondition;
    }

    public void setAxCondition(int axCondition) {
        AxCondition = axCondition;
    }

    public int getAyCondition() {
        return AyCondition;
    }

    public void setAyCondition(int ayCondition) {
        AyCondition = ayCondition;
    }

    public int getAzCondition() {
        return AzCondition;
    }

    public void setAzCondition(int azCondition) {
        AzCondition = azCondition;
    }

    public int getWxCondition() {
        return WxCondition;
    }

    public void setWxCondition(int wxCondition) {
        WxCondition = wxCondition;
    }

    public int getWyCondition() {
        return WyCondition;
    }

    public void setWyCondition(int wyCondition) {
        WyCondition = wyCondition;
    }

    public int getWzCondition() {
        return WzCondition;
    }

    public void setWzCondition(int wzCondition) {
        WzCondition = wzCondition;
    }

    public String getStepDescription() {
        return stepDescription;
    }

    public void setStepDescription(String stepDescription) {
        this.stepDescription = stepDescription;
    }
}
