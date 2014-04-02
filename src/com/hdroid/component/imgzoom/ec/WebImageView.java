/*
	Copyright (c) 2011 Rapture In Venice

	Permission is hereby granted, free of charge, to any person obtaining a copy
	of this software and associated documentation files (the "Software"), to deal
	in the Software without restriction, including without limitation the rights
	to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
	copies of the Software, and to permit persons to whom the Software is
	furnished to do so, subject to the following conditions:

	The above copyright notice and this permission notice shall be included in
	all copies or substantial portions of the Software.

	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
	IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
	FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
	AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
	LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
	OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
	THE SOFTWARE.
*/

package com.hdroid.component.imgzoom.ec;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.animation.AlphaAnimation;
import android.widget.*;


public class WebImageView extends ImageView {
    AlphaAnimation animation;

	public WebImageView(Context context) {
		super(context);
	}

	public WebImageView(Context context, AttributeSet attSet) {
		super(context, attSet);
        animation  = new AlphaAnimation(0, 1);
        animation.setDuration(500);//设置动画持续时间
	}
	
	public WebImageView(Context context, AttributeSet attrs, int defStyle) { 
        super(context, attrs, defStyle);
    } 
    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        this.startAnimation(animation);
    }
}
