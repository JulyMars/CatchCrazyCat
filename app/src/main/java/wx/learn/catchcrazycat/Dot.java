package wx.learn.catchcrazycat;

/**
 * Created by wangxiong on 2017/3/19.
 */

public class Dot {
    public static int STATUS_OFF = 0;
    public static int STATUS_ON = 1;
    public static int STATUS_IN = 2;

    private int status;
    private int r;
    private int c;
    private float centerx;
    private float centery;
    private float radius;

    public Dot(int r, int c) {
        this.r = r;
        this.c = c;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getR() {
        return r;
    }

    public void setR(int r) {
        this.r = r;
    }

    public int getC() {
        return c;
    }

    public void setC(int c) {
        this.c = c;
    }

    public void setRC(int r, int c) {
        this.r = r;
        this.c = c;
    }

    public void setCircle(float cx, float cy, float r) {
        centerx = cx;
        centery = cy;
        radius = r;
    }

    public boolean touchIn(float tx, float ty) {
        if (Math.abs(tx - centerx) > radius || Math.abs(ty - centery) > radius) {
            return false;
        }
        return true;
    }
}