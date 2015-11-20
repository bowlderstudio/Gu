package gupiao.general;

public class Stock {
    private String code;
    private String date;
    private float closePrice;
    private float ema12;
    private float ema26;
    private float diff;
    private float signal;
    private float histogram;

    public Stock(String code, String date, float price) {
        this.code = code;
        this.date = date;
        this.closePrice = price;
        this.ema12 = -1;
        this.ema26 = -1;
        this.signal = -1;
        this.histogram = -1;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public float getClosePrice() {
        return closePrice;
    }

    public void setClosePrice(float closePrice) {
        this.closePrice = closePrice;
    }

    public float getEma12() {
        return ema12;
    }

    public void setEma12(float ema12) {
        this.ema12 = ema12;
    }

    public float getEma26() {
        return ema26;
    }

    public void setEma26(float ema26) {
        this.ema26 = ema26;
    }

    public float getDiff() {
        return diff;
    }

    public void setDiff(float diff) {
        this.diff = diff;
    }

    public float getSignal() {
        return signal;
    }

    public void setSignal(float signal) {
        this.signal = signal;
    }

    public float getHistogram() {
        return histogram;
    }

    public void setHistogram(float histogram) {
        this.histogram = histogram;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
