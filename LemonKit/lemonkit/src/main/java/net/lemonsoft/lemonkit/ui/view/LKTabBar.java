package net.lemonsoft.lemonkit.ui.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import net.lemonsoft.lemonkit.core.LKUIAttrsCore;
import net.lemonsoft.lemonkit.enums.LKUIDelegateOnDrawState;
import net.lemonsoft.lemonkit.interfaces.ui.LKUIView;
import net.lemonsoft.lemonkit.model.LKUIExtensionModel;
import net.lemonsoft.lemonkit.tools.LKDrawableTool;

/**
 * LemonKit UI 选项卡栏，源自iOS - UITabBar
 * Created by lemonsoft on 2017/1/30.
 */

public class LKTabBar extends RelativeLayout implements LKUIView {

    public LKUIExtensionModel lk;

    public LKTabBar(Context context) {
        super(context);
    }

    public LKTabBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        LKUIAttrsCore.parse(this, attrs);
    }

    public LKTabBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LKUIAttrsCore.parse(this, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public LKTabBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        LKUIAttrsCore.parse(this, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (lk != null)
            lk.onDrawHandler(canvas, LKUIDelegateOnDrawState.PRE_SUPER_DRAW);
        super.onDraw(canvas);
        if (lk != null)
            lk.onDrawHandler(canvas, LKUIDelegateOnDrawState.AFT_SUPER_DRAW);
    }

    @Override
    public void setLk(LKUIExtensionModel lk) {
        this.lk = lk;
        lk.setView(this);
        if (lk.getCornerRadius() >= 0 && getBackground() != null)
            setBackground(
                    LKDrawableTool.createRoundCornerDrawable(getBackground(), lk.getCornerRadius())
            );
    }

    @Override
    public void setBackground(Drawable background) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            super.setBackground(background);
        else
            super.setBackgroundDrawable(background);
    }

}