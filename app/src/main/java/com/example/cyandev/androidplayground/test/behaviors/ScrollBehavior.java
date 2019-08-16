package com.example.cyandev.androidplayground.test.behaviors;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import com.example.cyandev.androidplayground.R;

import java.lang.ref.WeakReference;

public class ScrollBehavior extends CoordinatorLayout.Behavior<RecyclerView> {
    public ScrollBehavior() {
    }

    public ScrollBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private  WeakReference<View> dependentView;
    private View titleView;
    private int maxCollapsing;
    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, RecyclerView child, View dependency) {
        if (dependency.getId()==R.id.center_content) {
            dependentView = new WeakReference<>(dependency);
             titleView= dependency.findViewById(R.id.tvContentTitle);
            return  true;
        }
        return super.layoutDependsOn(parent, child, dependency);
    }

    /**
     * 嵌套布局初始位置
     * @param parent
     * @param child
     * @param layoutDirection
     * @return
     */
    @Override
    public boolean onLayoutChild(CoordinatorLayout parent, RecyclerView child, int layoutDirection) {
       View childView= parent.findViewById(R.id.center_content);
        int dependBottomLine=childView.getBottom();

        maxCollapsing= Math.abs(titleView.getTop());
        child.layout(0,dependBottomLine,childView.getWidth(),parent.getHeight()+maxCollapsing);//补偿压缩距离
        return  true;

//        return super.onLayoutChild(parent, child, layoutDirection);
    }

    /**
     * onStartNestedScroll  // 是否开启嵌套滑动
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
     * @param coordinatorLayout
     * @param child
     * @param directTargetChild
     * @param target
     * @param nestedScrollAxes
     */
    @Override
    public void onNestedScrollAccepted(CoordinatorLayout coordinatorLayout, RecyclerView child, View directTargetChild, View target, int nestedScrollAxes) {
        super.onNestedScrollAccepted(coordinatorLayout, child, directTargetChild, target, nestedScrollAxes);
    }

    /**
     * 即将开启嵌套滑动
     * @param coordinatorLayout
     * @param child
     * @param target
     * @param dx
     * @param dy
     * @param consumed
     */
    @Override
    public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, RecyclerView child, View target, int dx, int dy, int[] consumed) {
        if (dy<0){//down scroll    //向上滑动 dy>0   向下滑动dy<0
            return;
        }
       View dependentView= this.dependentView.get();
        float lastTranslationY=dependentView.getTranslationY();
       float toTranslate= dy-dependentView.getTranslationY();
       float  concorrect=toTranslate>maxCollapsing?maxCollapsing:toTranslate;
        dependentView.setTranslationY(-concorrect);
        consumed[1]=(int)(dependentView.getTranslationY()-lastTranslationY);
//        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed);
    }

    /**
     * 子view滑动玩之后
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
        if(dyUnconsumed<0){
          View dependView=  dependentView.get();
            if (dependView.getTranslationY()-dyUnconsumed<=0) {
                dependView.setTranslationY(dependView.getTranslationY()-dyUnconsumed);
            }

        }
//        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
    }

    /**
     * 滑动松手
     * @param coordinatorLayout
     * @param child
     * @param target
     * @param velocityX
     * @param velocityY
     * @return
     */
    @Override
    public boolean onNestedPreFling(CoordinatorLayout coordinatorLayout, RecyclerView child, View target, float velocityX, float velocityY) {
        return super.onNestedPreFling(coordinatorLayout, child, target, velocityX, velocityY);
    }

    /**
     * 嵌套滑动结束
     * @param coordinatorLayout
     * @param child
     * @param target
     */
    @Override
    public void onStopNestedScroll(CoordinatorLayout coordinatorLayout, RecyclerView child, View target) {
        super.onStopNestedScroll(coordinatorLayout, child, target);
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, RecyclerView child, View dependency) {
        child.setTranslationY(dependency.getTranslationY());
        return  true;
    }
}
