package fr.outadev.twistoast.uiutils

import android.view.View
import android.view.ViewGroup.LayoutParams
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.LinearLayout

class ResizeAnimation(private val mView: View, duration: Int, private val mType: Int) : Animation() {
    private var mEndHeight: Int = 0
    private val mLayoutParams: LinearLayout.LayoutParams

    init {
        setDuration(duration.toLong())
        mEndHeight = mView.height
        mLayoutParams = mView.layoutParams as LinearLayout.LayoutParams

        if (mType == EXPAND) {
            mLayoutParams.height = 0
        } else {
            mLayoutParams.height = LayoutParams.WRAP_CONTENT
        }

        mView.visibility = View.VISIBLE
    }

    var height: Int
        get() = mView.height
        set(height) {
            mEndHeight = height
        }

    override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
        super.applyTransformation(interpolatedTime, t)

        if (interpolatedTime < 1.0f) {
            if (mType == EXPAND) {
                mLayoutParams.height = (mEndHeight * interpolatedTime).toInt()
            } else {
                mLayoutParams.height = (mEndHeight * (1 - interpolatedTime)).toInt()
            }

            mView.requestLayout()
        } else {
            if (mType == EXPAND) {
                mLayoutParams.height = LayoutParams.WRAP_CONTENT
                mView.requestLayout()
            } else {
                mView.visibility = View.GONE
            }
        }
    }

    companion object {
        val COLLAPSE = 1
        val EXPAND = 0
    }
}
