package com.example.cyandev.androidplayground.test.behaviors;

import android.content.Context;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Scroller;

import com.example.cyandev.androidplayground.R;

import java.lang.ref.WeakReference;

public class ScrollBehavior extends CoordinatorLayout.Behavior<RecyclerView> {
    private int maxAlaDistance;

    public ScrollBehavior() {
    }

    public ScrollBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        scroller = new Scroller(context);
    }

    private WeakReference<View> dependentView;
    private View titleView;
    private int maxCollapsing;
    private Scroller scroller;
    private Handler handler;

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, RecyclerView child, View dependency) {
        if (dependency.getId() == R.id.center_content) {
            dependentView = new WeakReference<>(dependency);
            titleView = dependency.findViewById(R.id.tvContentTitle);
            handler = new Handler();
            return true;
        }
        return super.layoutDependsOn(parent, child, dependency);
    }

    /**
     * 嵌套布局初始位置
     *
     * @param parent
     * @param child
     * @param layoutDirection
     * @return
     */
    @Override
    public boolean onLayoutChild(CoordinatorLayout parent, RecyclerView child, int layoutDirection) {
        View childView = parent.findViewById(R.id.center_content);
        View search_bar = parent.findViewById(R.id.lin_searchbar);
        int dependBottomLine = childView.getBottom();

        maxCollapsing = Math.abs(search_bar.getBottom()-dependBottomLine);
        maxAlaDistance=childView.getTop()-search_bar.getBottom();
        child.layout(0, dependBottomLine, childView.getWidth(), parent.getHeight() + maxCollapsing);//补偿压缩距离
        return true;

//        return super.onLayoutChild(parent, child, layoutDirection);
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, RecyclerView child, View dependency) {
        child.setTranslationY(dependency.getTranslationY());
        float translationPer=(float) (maxAlaDistance-Math.abs(dependency.getTranslationY()))/maxAlaDistance;
        View search_bar = parent.findViewById(R.id.lin_searchbar);
        ImageView imageView = (ImageView) parent.findViewById(R.id.iv_avatar);
        search_bar.setAlpha(1-translationPer);
        imageView.setScaleY(translationPer>=0?translationPer:0);
        imageView.setScaleX(translationPer>=0?translationPer:0);
        imageView.setAlpha(translationPer);
        return true;
    }

    /**
     * onStartNestedScroll  // 是否开启嵌套滑动
     *
     * @param coordinatorLayout
     * @param child
     * @param directTargetChild
     * @param target
     * @param nestedScrollAxes
     * @return
     */
    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, RecyclerView child, View directTargetChild, View target, int nestedScrollAxes) {
        return (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
    }

    /**
     * onStartNestedScroll //接受后 后续回调
     *
     * @param coordinatorLayout
     * @param child
     * @param directTargetChild
     * @param target
     * @param nestedScrollAxes
     */
    @Override
    public void onNestedScrollAccepted(CoordinatorLayout coordinatorLayout, RecyclerView child, View directTargetChild, View target, int nestedScrollAxes) {
        scroller.abortAnimation();
        super.onNestedScrollAccepted(coordinatorLayout, child, directTargetChild, target, nestedScrollAxes);
    }

    /**
     * 即将开启嵌套滑动
     *
     * @param coordinatorLayout
     * @param child
     * @param target
     * @param dx
     * @param dy
     * @param consumed
     */
    @Override
    public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, RecyclerView child, View target, int dx, int dy, int[] consumed) {
        if (dy < 0) {//down scroll    //向上滑动 dy>0   向下滑动dy<0
            return;
        }
        View dependentView = this.dependentView.get();
        float toTranslate = dy - dependentView.getTranslationY();
        float concorrect = toTranslate > maxCollapsing ? maxCollapsing : toTranslate;
        dependentView.setTranslationY(-concorrect);
        if (dependentView.getTranslationY()>-maxCollapsing) {   //consumed 未消耗完值会传递给 RecyclerView
            consumed[1]=dy;
        }

    }


    /**
     * 子view滑动玩之后
     *
     * @param coordinatorLayout
     * @param child
     * @param target
     * @param dxConsumed
     * @param dyConsumed
     * @param dxUnconsumed
     * @param dyUnconsumed
     */
    @Override
    public void onNestedScroll(CoordinatorLayout coordinatorLayout, RecyclerView child, View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        if (dyUnconsumed < 0) {
            View dependView = dependentView.get();
            if (dependView.getTranslationY() - dyUnconsumed <= 0) {
                dependView.setTranslationY(dependView.getTranslationY() - dyUnconsumed);
            }

        }
//        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
    }

    /**
     * 滑动松手
     *
     * @param coordinatorLayout
     * @param child
     * @param target
     * @param velocityX
     * @param velocityY
     * @return
     */
    @Override
    public boolean onNestedPreFling(CoordinatorLayout coordinatorLayout, RecyclerView child, View target, float velocityX, float velocityY) {
//        return super.onNestedPreFling(coordinatorLayout, child, target, velocityX, velocityY);
        Log.d("onNestedPreFling",": "+velocityY);  //负 下   正 上
        return onDragEnd(velocityY);

    }

    /**
     * 嵌套滑动结束
     *
     * @param coordinatorLayout
     * @param child
     * @param target
     */
    @Override
    public void onStopNestedScroll(CoordinatorLayout coordinatorLayout, RecyclerView child, View target) {
//        super.onStopNestedScroll(coordinatorLayout, child, target);
        onDragEnd(600);
    }

    private Runnable scrollerRun = new Runnable() {
        @Override
        public void run() {
            boolean compute=scroller.computeScrollOffset();
            Log.d("compute",": "+compute);
            if (compute) {
                dependentView.get().setTranslationY(scroller.getCurrY());
                Log.d("compute",":getCurrY "+scroller.getCurrY());
                handler.post(this);
            }
        }
    };


    private boolean onDragEnd( float velocityY) {
        View dependView = dependentView.get();
        int currentTranslation = (int) dependView.getTranslationY();
        if (currentTranslation == 0 || Math.abs(currentTranslation) >= maxCollapsing) {
            return false;
        }

        if (Math.abs(velocityY)<800) {//速度小 则以当前位置判断
            if (Math.abs(currentTranslation) > maxCollapsing * 0.5f) {
                scroller.startScroll(0, currentTranslation, 0, -maxCollapsing-currentTranslation,(int) (1000000 / Math.abs(800)));
            } else {
                scroller.startScroll(0, currentTranslation, 0, -0-currentTranslation,(int) (1000000 / Math.abs(800)));
            }
        }else {
            if (velocityY>0) {
                scroller.startScroll(0, currentTranslation, 0, -maxCollapsing-currentTranslation,(int) (1000000 / Math.abs(velocityY)));
            }else  scroller.startScroll(0, currentTranslation, 0, -0-currentTranslation,(int) (1000000 / Math.abs(velocityY)));
        }

        handler.post(scrollerRun);
        return true;
    }

}
