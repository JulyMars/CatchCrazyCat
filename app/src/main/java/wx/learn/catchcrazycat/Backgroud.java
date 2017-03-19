package wx.learn.catchcrazycat;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.inputmethodservice.Keyboard;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

/**
 * Created by wangxiong on 2017/3/19.
 */

public class Backgroud extends SurfaceView implements View.OnTouchListener {
    private static final int ROW = 10;
    private static final int COL = 10;
    private int INIT_BLOCKS = 8;
    private int DOT_WIDTH = 40;

    private Dot dots[][];
    private Dot cat;

    public Backgroud(Context context) {
        super(context);
        getHolder().addCallback(callback);

        dots = new Dot[ROW][COL];
        for (int r = 0; r < ROW; r++) {
            for (int c = 0; c < COL; c++) {
                dots[r][c] = new Dot(r, c);
            }
        }

        cat = new Dot(0, 0);
        setOnTouchListener(this);
        initGame();
    }

    private void initGame() {
        int r, c;
        for (r = 0; r < ROW; r++) {
            for (c = 0; c < COL; c++) {
                dots[r][c].setStatus(Dot.STATUS_OFF);
            }
        }

        r = (int) (Math.random() * ROW / 4 + ROW / 4);
        c = (int) (Math.random() * COL / 4 + COL / 4);
        cat.setRC(r, c);
        dots[r][c].setStatus(Dot.STATUS_IN);

        for (int i = 0; i < INIT_BLOCKS; ) {
            r = (int) (Math.random() * ROW);
            c = (int) (Math.random() * COL);
            if (dots[r][c].getStatus() == Dot.STATUS_OFF) {
                dots[r][c].setStatus(Dot.STATUS_ON);
                i++;
            }
        }
    }

    private void redraw() {
        int r, c;

        Canvas canvas = getHolder().lockCanvas();
        canvas.drawColor(Color.LTGRAY);
        Paint paint = new Paint();
        for (r = 0; r < ROW; r++) {
            for (c = 0; c < COL; c++) {
                float offsetx = (r % 2 == 0 ? 0 : DOT_WIDTH / 2) + DOT_WIDTH / 4 * 3;
                float offsety = DOT_WIDTH / 4 * 3;
                if (dots[r][c].getStatus() == Dot.STATUS_OFF) {
                    paint.setColor(0xFFEEEEEE);
                } else if (dots[r][c].getStatus() == Dot.STATUS_ON) {
                    paint.setColor(0xFFFFEE00);
                } else {
                    paint.setColor(0xFFFF0000);
                }
                dots[r][c].setCircle((float) c * DOT_WIDTH + offsetx, (float) r * DOT_WIDTH + offsety, DOT_WIDTH / 2);
                canvas.drawCircle((float) c * DOT_WIDTH + offsetx, (float) r * DOT_WIDTH + offsety, DOT_WIDTH / 2, paint);
            }
        }
        getHolder().unlockCanvasAndPost(canvas);

    }

    private void touchDot(float x, float y) {
        int r, c;
        Dot touchDot = null;
        for (r = 0; r < ROW; r++) {
            for (c = 0; touchDot == null && c < COL; c++) {
                if (dots[r][c].getStatus() == Dot.STATUS_OFF && dots[r][c].touchIn(x, y)) {
                    touchDot = dots[r][c];
                    break;
                }
            }
        }
        if (touchDot != null) {
            touchDot.setStatus(Dot.STATUS_ON);
            redraw();
        }
    }

    SurfaceHolder.Callback callback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            redraw();
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            DOT_WIDTH = Math.min(width / (COL + 1), height / (ROW + 1));
            redraw();
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }
    };

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            touchDot(event.getX(), event.getY());
            catMove();
        }
        return false;
    }

    private void catMove() {
        Vector<Dot> nd = new Vector<>();
        Vector<Integer> diss = new Vector<>();
        for (int i = 1; i < 7; i++) {
            Dot dot = getNeighbour(cat, i);
            if (dot != null) {
                int dis = getEdgeDistance(cat, i);
                if (dis != 0) {
                    if (dis < 0) {
                        diss.add(-dis);
                    } else {
                        if(dis == 1){
                            diss.add(100000);
                        }else{
                            diss.add(100 - dis);
                        }
                    }
                    nd.add(dot);
                }
            }
        }
        if (nd.size() > 0) {
            Vector<Integer> diss_ori = (Vector<Integer>) diss.clone();
            Collections.sort(diss, new Comparator<Integer>() {
                @Override
                public int compare(Integer o1, Integer o2) {
                    return o1 - o2;
                }
            });
            int sumDis = 0;
            for (int i = 0; i < diss.size(); i++) {
                sumDis += diss.get(i);
            }
            int r = (int) (Math.random() * sumDis);
            int rValue = 0;
            for (int i = diss.size() - 1; i >= 0; i--) {
                r -= diss.get(i);
                if (r <= 0) {
                    rValue = diss.get(i);
                    break;
                }
            }
            Vector<Dot> rDots = new Vector<>();
            for (int i = 0; i < diss_ori.size(); i++) {
                if(diss_ori.get(i) == rValue){
                    rDots.add(nd.get(i));
                }
            }

            Dot target = rDots.get((int) (Math.random()*rDots.size()));
            target.setStatus(Dot.STATUS_IN);
            dots[cat.getR()][cat.getC()].setStatus(Dot.STATUS_OFF);
            cat.setRC(target.getR(), target.getC());
            redraw();

            if (isEdge(cat)) {
                gameLose();
            }
        } else {
            if (isEdge(cat)) {
                gameLose();
            } else {
                gameWin();
            }
        }

    }

    private void gameWin() {
        Toast.makeText(getContext(), "you win", Toast.LENGTH_SHORT).show();
        initGame();
        redraw();
    }

    private void gameLose() {
        Toast.makeText(getContext(), "lose", Toast.LENGTH_SHORT).show();
        initGame();
        redraw();
    }

    private boolean isEdge(Dot dot) {
        if (dot.getR() * dot.getC() == 0 || dot.getR() + 1 == ROW || dot.getC() + 1 == COL) {
            return true;
        }
        return false;
    }

    private Dot getNeighbour(Dot dot, int dir) {
        int r = dot.getR();
        int c = dot.getC();
        switch (dir) {
            case 1:
                r = r;
                c = c - 1;
                break;
            case 2:
                if (r % 2 == 0) {
                    r = r - 1;
                    c = c - 1;
                } else {
                    r = r - 1;
                    c = c;
                }
                break;
            case 3:
                if (r % 2 == 0) {
                    r = r - 1;
                    c = c;
                } else {
                    r = r - 1;
                    c = c + 1;
                }
                break;
            case 4:
                r = r;
                c = c + 1;
                break;
            case 5:
                if (r % 2 == 0) {
                    r = r + 1;
                    c = c;
                } else {
                    r = r + 1;
                    c = c + 1;
                }
                break;
            case 6:
                if (r % 2 == 0) {
                    r = r + 1;
                    c = c - 1;
                } else {
                    r = r + 1;
                    c = c;
                }
                break;
        }
        if (r >= 0 && c >= 0 && r < ROW && c < COL) {
            return dots[r][c];
        }
        return null;
    }

    private int getEdgeDistance(Dot dot, int dir) {
        int distance = 0;
        while (true) {
            dot = getNeighbour(dot, dir);
            if (dot != null && dot.getStatus() == Dot.STATUS_OFF) {
                distance++;
            } else {
                if (dot != null) {
                    distance *= -1;
                }
                break;
            }
        }
        return distance;
    }
}
