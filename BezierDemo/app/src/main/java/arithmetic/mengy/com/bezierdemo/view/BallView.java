package arithmetic.mengy.com.bezierdemo.view;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;

import arithmetic.mengy.com.bezierdemo.R;
import arithmetic.mengy.com.bezierdemo.bean.BallPaintBean;
import arithmetic.mengy.com.bezierdemo.utils.ScreenUtils;

/**
 * Author：mengyuan
 * Date  : 2017/4/18下午6:51
 * E-Mail:mengyuanzz@126.com
 * Desc  :
 */

public class BallView extends View {

    //--------------------------------无法改变的参数--------------------------------

    private BallPaintBean ballPaintBean;//圆坐标对象

    private int downX;
    //向左滑动 moveX>0
    //向右滑动 moveX<0
    private int moveX;
    private float currentFloat;//改变比例 0f-3f

    private float rightChangeRadius;//右边改变的距离
    private float leftChangeRadius;//左边改变的距离
    private float yChangeInstance;//Y轴压缩的距离
    private float moveInstance;//当前球的P0,X坐标;


    private int currentPosition = 0;//当前下标
    private float currentXinstance = 0;//当前X轴下标

    private ValueAnimator rightAnimator;//向右切换动画
    private ValueAnimator leftAmimator;//向左切换动画


    //--------------------------------可调参数--------------------------------
    private Paint ballChangePaint;//球的画笔
    private Paint ballBgPaint;//背景画笔
    private Path ballBgPath;//背景Path

    private long animotionTime = 800;//动画持续时间

    private int ballRadius = ScreenUtils.dp2Px(15);//半径

    private int length = 5;//条目数量

    private float ballMargin = 0.5f;//指示点的间距，为半径的倍数，最小为0r

    private float ARRIVATE_OVER_TIME = 2.5f;//球到达目的地的时间， 最大为3f，最小为2f


    public BallView(Context context) {
        super(context);
        initView();
    }

    public BallView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();

    }

    public BallView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        ballPaintBean = new BallPaintBean(ballRadius);

        initPaint();

        initPath();

    }


    /**
     * 初始化画笔
     */
    private void initPaint() {
        ballChangePaint = new Paint();
        ballChangePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        ballChangePaint.setColor(getResources().getColor(R.color.c_0050f0));

        ballBgPaint = new Paint();
        ballBgPaint.setColor(getResources().getColor(R.color.c_000000));
        ballBgPaint.setStyle(Paint.Style.STROKE);
    }

    /**
     * 初始化形状
     */
    private void initPath() {
        if (ballMargin < 0f) {
            ballMargin = 0f;
        }
        ballBgPath = new Path();
        for (int i = 0; i < length; i++) {
            ballBgPath.addCircle((ballMargin + 2) * i * ballRadius + ballRadius, ballRadius, ballRadius, Path.Direction.CW);
        }
    }

    /**
     * 向右切换
     *
     * @param positin 切换的下标
     */
    private void startRightAnimotion(int positin) {
        if (positin >= length) {
            return;
        }
        if (currentPosition > positin) {
            startLeftAnimotion(positin);
            return;
        }
        if (currentPosition == positin) {
            return;
        }
        //切换的下标数量
        final int changePositionNumber = positin - currentPosition;

        rightAnimator = ValueAnimator.ofFloat(currentFloat, 3f).setDuration(animotionTime);
        rightAnimator.setInterpolator(new LinearInterpolator());
        rightAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                currentFloat = (float) animation.getAnimatedValue();
                //平移的距离

                if (currentFloat <= 1) {
                    Log.i("my", "阶段111111");
                    // 右边增加r
                    rightChangeRadius = ballRadius * currentFloat * changePositionNumber;
                    //左边不动
                    leftChangeRadius = 0;
                    //Y轴不动
                    yChangeInstance = 0;
                } else if (currentFloat <= 2) {
                    Log.i("my", "阶段2222");
                    //右边减少0.5r
                    rightChangeRadius = (ballRadius - (currentFloat - 1) * (ballRadius * 0.5f)) * changePositionNumber;
                    //左边增加r
                    leftChangeRadius = (ballRadius * (currentFloat - 1)) * changePositionNumber;
                    //Y上下最大各压缩1/6
                    if (changePositionNumber > 3) {
                        yChangeInstance = ballRadius / 6 * (currentFloat - 1) * 3;
                    } else {
                        yChangeInstance = ballRadius / 6 * (currentFloat - 1) * changePositionNumber;
                    }

                } else {
                    Log.i("my", "阶段33333333333333");

                    if (currentFloat > ARRIVATE_OVER_TIME) {
                        //在该阶段，右边 与Y轴 是不变的，因为动画的动态值是随机的，而且与手机性能有关，可能2个动画的间隔会很大，导致图形未变回原形
                        // 这里保证图形变回原形
                        rightChangeRadius = 0;
                        yChangeInstance = 0;
                        //回弹效果
                        //回弹时间,除以二是  一半时间是收缩，另一半时间是回放
                        float reboundTime = (3f - ARRIVATE_OVER_TIME) / 2f;
                        //前半段，收缩
                        if (currentFloat < ARRIVATE_OVER_TIME + reboundTime) {
                            leftChangeRadius = -(currentFloat - ARRIVATE_OVER_TIME) * ballRadius * 1.5f;
                        } else {
                            leftChangeRadius = -(3f - currentFloat) * ballRadius * 1.5f;
                        }
                    } else {
                        //变回原形
                        rightChangeRadius = 0.5f * ballRadius * changePositionNumber - (currentFloat - 2f) / (ARRIVATE_OVER_TIME - 2f) * (ballRadius * 0.5f) * changePositionNumber;
                        leftChangeRadius = ballRadius * changePositionNumber - (currentFloat - 2f) / (ARRIVATE_OVER_TIME - 2f) * ballRadius * changePositionNumber;

                        if (changePositionNumber > 3) {
                            yChangeInstance = ballRadius / 6 * changePositionNumber - (currentFloat - 2f) / (ARRIVATE_OVER_TIME - 2f) * ballRadius / 6 * 3;

                        } else {
                            yChangeInstance = ballRadius / 6 * changePositionNumber - (currentFloat - 2f) / (ARRIVATE_OVER_TIME - 2f) * ballRadius / 6 * changePositionNumber;
                        }
                    }

                }


                if (currentFloat >= 1) {
                    float currentProgress = (currentFloat - 1f) / (ARRIVATE_OVER_TIME - 1f);
                    if (currentProgress > 1) {
                        currentProgress = 1;
                    }
                    moveInstance = currentXinstance + (ballMargin + 2f) * ballRadius * currentProgress * changePositionNumber;
                }
                Log.i("my", "ballRadius:" + ballRadius);
                Log.i("my", "currentFloat:" + currentFloat);
                Log.i("my", "changePositionNumber:" + changePositionNumber);
                Log.i("my", "currentXinstance:" + currentXinstance);
                Log.i("my", "moveInstance:" + moveInstance);
                Log.i("my", "rightChangeRadius:" + rightChangeRadius);
                Log.i("my", "leftChangeRadius:" + leftChangeRadius);
                Log.i("my", "---------------------------------------------");

                invalidate();
            }
        });
        rightAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                currentXinstance = moveInstance;
                currentFloat = 0;
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        rightAnimator.start();
        //改变下标
        currentPosition = positin;
    }

    /**
     * 向左切换
     *
     * @param positin 切换的下标
     */
    private void startLeftAnimotion(int positin) {
        if (positin < 0) {
            return;
        }
        if (currentPosition < positin) {
            startRightAnimotion(positin);
            return;
        }
        if (currentPosition == positin) {
            return;
        }

        final int changePositionNumber = currentPosition - positin;


        //切换的下标数量
        leftAmimator = ValueAnimator.ofFloat(currentFloat, 3).setDuration(animotionTime);
        leftAmimator.setInterpolator(new LinearInterpolator());
        leftAmimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                currentFloat = (float) animation.getAnimatedValue();

                if (currentFloat <= 1) {
                    Log.i("my", "阶段111111");
                    //右边不动
                    rightChangeRadius = 0;
                    // 左边增加 changePosition*r
                    leftChangeRadius = ballRadius * currentFloat * changePositionNumber;
                    //Y轴不动
                    yChangeInstance = 0;

                } else if (currentFloat <= 2) {
                    Log.i("my", "阶段2222");
                    //右边增加r
                    rightChangeRadius = ballRadius * (currentFloat - 1) * changePositionNumber;
                    //左边减少 changePosition* 0.5r
                    leftChangeRadius = (ballRadius - (currentFloat - 1) * (ballRadius * 0.5f)) * changePositionNumber;
                    //Y上下最大各压缩1/7*切换数量
                    if (changePositionNumber > 3) {
                        yChangeInstance = ballRadius / 6 * (currentFloat - 1) * 3;
                    } else {
                        yChangeInstance = ballRadius / 6 * (currentFloat - 1) * changePositionNumber;
                    }
                } else {
                    Log.i("my", "阶段33333333333333");
                    if (currentFloat > ARRIVATE_OVER_TIME) {
                        //在该阶段，右边 与Y轴 是不变的，因为动画的动态值是随机的，而且与手机性能有关，可能2个动画的间隔会很大，导致图形未变回原形
                        // 这里保证图形变回原形
                        leftChangeRadius = 0;
                        yChangeInstance = 0;
                        //回弹效果
                        //回弹时间,除以二是  一半时间是收缩，另一半时间是回放
                        float reboundTime = (3f - ARRIVATE_OVER_TIME) / 2f;
                        //前半段，收缩
                        if (currentFloat < ARRIVATE_OVER_TIME + reboundTime) {
                            rightChangeRadius = -(currentFloat - ARRIVATE_OVER_TIME) * ballRadius * 1.5f;
                        } else {
                            rightChangeRadius = -(3f - currentFloat) * ballRadius * 1.5f;
                        }
                    } else {
                        //变回原形
                        rightChangeRadius = ballRadius * changePositionNumber - (currentFloat - 2f) / (ARRIVATE_OVER_TIME - 2f) * ballRadius * changePositionNumber;
                        leftChangeRadius = 0.5f * ballRadius * changePositionNumber - (currentFloat - 2f) / (ARRIVATE_OVER_TIME - 2f) * (ballRadius * 0.5f) * changePositionNumber;
                        if (changePositionNumber > 3) {
                            yChangeInstance = ballRadius / 6 * changePositionNumber - (currentFloat - 2f) / (ARRIVATE_OVER_TIME - 2f) * ballRadius / 6 * 3;

                        } else {
                            yChangeInstance = ballRadius / 6 * changePositionNumber - (currentFloat - 2f) / (ARRIVATE_OVER_TIME - 2f) * ballRadius / 6 * changePositionNumber;
                        }
                    }
                }
                //位移距离计算
                if (currentFloat >= 1) {
                    float currentProgress = (currentFloat - 1f) / (ARRIVATE_OVER_TIME - 1f);
                    if (currentProgress > 1) {
                        currentProgress = 1;
                    }
                    moveInstance = currentXinstance - (ballMargin + 2f) * ballRadius * currentProgress * changePositionNumber;
                }
                Log.i("my", "ballRadius:" + ballRadius);
                Log.i("my", "currentFloat:" + currentFloat);
                Log.i("my", "moveInstance:" + moveInstance);
                Log.i("my", "rightChangeRadius:" + rightChangeRadius);
                Log.i("my", "leftChangeRadius:" + leftChangeRadius);
                Log.i("my", "---------------------------------------------");

                invalidate();
            }
        });
        leftAmimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                currentXinstance = moveInstance;
                currentFloat = 0;
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        leftAmimator.start();
        //改变下标
        currentPosition = positin;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //背景不变，直接绘制
        canvas.drawPath(ballBgPath, ballBgPaint);
        Path ballChangePath = new Path();
        ballChangePath.moveTo(ballPaintBean.p0.x, ballPaintBean.p0.y + yChangeInstance);
        //绘制形状
        //P1，P2,P3
        ballChangePath.cubicTo(ballPaintBean.p1.x, ballPaintBean.p1.y + yChangeInstance, ballPaintBean.p2.x + rightChangeRadius, ballPaintBean.p2.y, ballPaintBean.p3.x + rightChangeRadius, ballPaintBean.p3.y);
        //P4,P5,P6
        ballChangePath.cubicTo(ballPaintBean.p4.x + rightChangeRadius, ballPaintBean.p4.y, ballPaintBean.p5.x, ballPaintBean.p5.y - yChangeInstance, ballPaintBean.p6.x, ballPaintBean.p6.y - yChangeInstance);
        //P7,P8,P9
        ballChangePath.cubicTo(ballPaintBean.p7.x, ballPaintBean.p7.y - yChangeInstance, ballPaintBean.p8.x - leftChangeRadius, ballPaintBean.p8.y, ballPaintBean.p9.x - leftChangeRadius, ballPaintBean.p9.y);
        //P10,P11,P12
        ballChangePath.cubicTo(ballPaintBean.p10.x - leftChangeRadius, ballPaintBean.p10.y, ballPaintBean.p11.x, ballPaintBean.p11.y + yChangeInstance, ballPaintBean.p0.x, ballPaintBean.p0.y + yChangeInstance);

        canvas.translate(moveInstance, 0);
        canvas.drawPath(ballChangePath, ballChangePaint);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN://按下
                downX = (int) event.getRawX();
                break;
            case MotionEvent.ACTION_MOVE://滑动

                moveX = downX - (int) event.getRawX();

//                currentFloat = moveX / ballRadius;
//                //向左边滑动
//                if (currentFloat > 0) {
//                    if (currentPosition == 0) {
//                        return true;
//                    }
//                    if (currentFloat > 1) {
//                        currentFloat = 1;
//                        startLeftAnimotion(currentPosition - 1);
//                        return true;
//                    } else {
//                        //右边不动
//                        rightChangeRadius = 0;
//                        // 左边增加 changePosition*r
//                        leftChangeRadius = ballRadius * currentFloat;
//                    }
//
//                }
//                //向右边滑动
//                else {
//                    if (currentPosition == length - 1) {
//                        return true;
//                    }
//                    currentFloat = Math.abs(currentFloat);
//                    if (currentFloat > 1) {
//                        currentFloat = 1;
//                        startRightAnimotion(currentPosition + 1);
//                        return true;
//                    } else {
//                        // 右边增加r
//                        rightChangeRadius = ballRadius * currentFloat;
//                        //左边不动
//                        leftChangeRadius = 0;
//                    }
//                }
//                postInvalidate();
                break;
            case MotionEvent.ACTION_UP://抬起
                break;

        }
        return super.onTouchEvent(event);

    }


    public void setCurrentPosition(int position) {
        startLeftAnimotion(position);
    }

    public void setRadius(int radius) {
        ballRadius = ScreenUtils.dp2Px(radius);
        reInitView();
    }

    public void setMargin(float margin) {
        ballMargin = margin;
        reInitView();
    }

    public void setLength(int length) {
        this.length = length;
        reInitView();

    }

    private void reInitView() {
        currentXinstance = 0;
        currentPosition = 0;
        moveInstance = 0;
        initView();
        postInvalidate();
    }

    public int getRadius() {
        return ballRadius;
    }

    public float getMargin() {
        return ballMargin;
    }

    public int getLength() {
        return length;
    }
}
