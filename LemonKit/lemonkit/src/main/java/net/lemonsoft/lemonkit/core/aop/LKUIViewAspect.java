package net.lemonsoft.lemonkit.core.aop;


import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;

import net.lemonsoft.lemonkit.core.parser.LKUIAttrsParser;
import net.lemonsoft.lemonkit.enums.LKUIDelegateOnDrawState;
import net.lemonsoft.lemonkit.interfaces.ui.LKUIView;
import net.lemonsoft.lemonkit.model.LKUIExtensionModel;
import net.lemonsoft.lemonkit.tools.LKDrawableTool;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

/**
 * 对LKUI控件的公共方法部分的AOP
 * 切面注入方法
 * Created by lemonsoft on 2017/1/31.
 */
@Aspect
public class LKUIViewAspect {

    // 存储LKUIExtensionModel对象的池，使用SparseArray，而不是Map，为了提高效率
    private static SparseArray<LKUIExtensionModel> lkPool = new SparseArray<>();

    @After("execution(net.lemonsoft.lemonkit.ui.view.LK*.new(..))")
    public void lkConstructor(JoinPoint joinPoint) throws Throwable {
        if (!(joinPoint.getTarget() instanceof LKUIView))
            return;
        Object[] args = joinPoint.getArgs();
        LKUIExtensionModel model = new LKUIExtensionModel();
        if (args.length >= 2)
            model = LKUIAttrsParser.parse((View) joinPoint.getTarget(), (AttributeSet) args[1]);
        lkPool.put(getLKKey(joinPoint), model);
        applyLKComplete(model, (LKUIView) joinPoint.getTarget());
    }

    @Around("execution(* net.lemonsoft.lemonkit.ui.view.LK*.onDraw(..))")
    public void lkOnDraw(ProceedingJoinPoint joinPoint) throws Throwable {
        boolean canRun = containLKWithJoinPoint(joinPoint);
        if (canRun)
            lkPool.get(getLKKey(joinPoint)).onDrawHandler(
                    (Canvas) joinPoint.getArgs()[0],
                    LKUIDelegateOnDrawState.PRE_SUPER_DRAW
            );
        joinPoint.proceed();
        if (canRun)
            lkPool.get(getLKKey(joinPoint)).onDrawHandler(
                    (Canvas) joinPoint.getArgs()[0],
                    LKUIDelegateOnDrawState.AFT_SUPER_DRAW
            );
    }

    @Around("execution(* net.lemonsoft.lemonkit.ui.view.LK*.setBackground(..))")
    public void lkSetBackground(ProceedingJoinPoint joinPoint) throws Throwable {
        // 下面的代码针对圆角矩形背景进行了适配和修改
        Drawable[] args = null;
        if (containLKWithJoinPoint(joinPoint))
            args = new Drawable[]{// 创建圆角矩形drawable参数
                    LKDrawableTool.createRoundCornerDrawable(
                            (Drawable) joinPoint.getArgs()[0],
                            lkPool.get(getLKKey(joinPoint)).getCornerRadius()
                    )
            };
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            joinPoint.proceed(args == null ? joinPoint.getArgs() : args);
        else
            ((View) joinPoint.getTarget()).setBackgroundDrawable(args == null ? ((Drawable) joinPoint.getArgs()[0]) : args[0]);
    }

    @Before("execution(* net.lemonsoft.lemonkit.ui.view.LK*.finalize(..))")
    public LKUIExtensionModel lkGetLk(JoinPoint joinPoint) throws Throwable {
        if (containLKWithJoinPoint(joinPoint))
            return lkPool.get(getLKKey(joinPoint));
        return null;
    }

    @After("execution(* net.lemonsoft.lemonkit.ui.view.LK*.finalize(..))")
    public void lkFinalize(JoinPoint joinPoint) throws Throwable {
        if (containLKWithJoinPoint(joinPoint))
            lkPool.remove(getLKKey(joinPoint));
    }

    private Integer getLKKey(JoinPoint joinPoint) {
        return joinPoint.getTarget().hashCode();
    }

    private boolean containLKWithJoinPoint(JoinPoint joinPoint) {
        return lkPool.indexOfKey(getLKKey(joinPoint)) >= 0;
    }

    private void applyLKComplete(LKUIExtensionModel lk, LKUIView lkui) {
        View view = (View) lkui;
        lk.setView(view);
        if (lk.getCornerRadius() >= 0 && view.getBackground() != null)
            lkui.setBackground(
                    LKDrawableTool.createRoundCornerDrawable(view.getBackground(), lk.getCornerRadius())
            );
    }

}
