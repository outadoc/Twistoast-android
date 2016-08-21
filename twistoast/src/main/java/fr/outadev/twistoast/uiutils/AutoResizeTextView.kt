/*
 * Copyright Muhammad Wajeeh - 2013
 * Source https://stackoverflow.com/questions/5033012/auto-scale-textview-text-to-fit-within-bounds/17782522#17782522
 */

package fr.outadev.twistoast.uiutils

import android.annotation.TargetApi
import android.content.Context
import android.content.res.Resources
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Build
import android.text.Layout.Alignment
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.TextView

/**
 * a textView that is able to self-adjust its font size depending on the min and max size of the font, and its own size.
 * code is heavily based on this StackOverflow thread:
 * http://stackoverflow.com/questions/16017165/auto-fit-textview-for-android/21851239#21851239
 * It should work fine with most Android versions, but might have some issues on Android 3.1 - 4.04, as setTextSize will only
 * work for the first time.
 * More info here: https://code.google.com/p/android/issues/detail?id=22493 and here in case you wish to fix it:
 * http://stackoverflow.com/a/21851239/878126
 */
class AutoResizeTextView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : TextView(context, attrs, defStyle) {
    private val availableSpaceRect = RectF()
    private val sizeTester: SizeTester
    private var maxTextSize: Float = 0.toFloat()
    private var spacingMult = 1.0f
    private var spacingAdd = 0.0f
    private var minTextSize: Float = 0.toFloat()
    private var widthLimit: Int = 0
    private var maxLines: Int = 0
    private var initialized = false
    private var txtPaint: TextPaint? = null

    private interface SizeTester {
        /**
         * @param suggestedSize  Size of text to be tested
         * *
         * @param availableSpace available space in which text must fit
         * *
         * @return an integer < 0 if after applying `suggestedSize` to
         * * text, it takes less space than `availableSpace`, > 0
         * * otherwise
         */
        fun onTestSize(suggestedSize: Int, availableSpace: RectF): Int
    }

    init {
        // using the minimal recommended font size
        minTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12f, resources.displayMetrics)
        maxTextSize = textSize
        txtPaint = TextPaint(paint)

        // no value was assigned during construction
        if (maxLines == 0) {
            maxLines = NO_LINE_LIMIT
        }

        // prepare size tester:
        sizeTester = object : SizeTester {
            internal val textRect = RectF()

            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            override fun onTestSize(suggestedSize: Int, availableSpace: RectF): Int {
                txtPaint!!.textSize = suggestedSize.toFloat()
                val transformationMethod = transformationMethod
                val text: String

                if (transformationMethod != null) {
                    text = transformationMethod.getTransformation(getText(), this@AutoResizeTextView).toString()
                } else {
                    text = getText().toString()
                }

                val singleLine = maxLines == 1
                if (singleLine) {
                    textRect.bottom = txtPaint!!.fontSpacing
                    textRect.right = txtPaint!!.measureText(text)
                } else {
                    val layout = StaticLayout(text, txtPaint, widthLimit, Alignment.ALIGN_NORMAL,
                            spacingMult, spacingAdd, true)
                    // return early if we have more lines
                    if (maxLines != NO_LINE_LIMIT && layout.lineCount > maxLines) {
                        return 1
                    }

                    textRect.bottom = layout.height.toFloat()
                    var maxWidth = -1

                    for (i in 0..layout.lineCount - 1) {
                        if (maxWidth < layout.getLineRight(i) - layout.getLineLeft(i)) {
                            maxWidth = layout.getLineRight(i).toInt() - layout.getLineLeft(i).toInt()
                        }
                    }

                    textRect.right = maxWidth.toFloat()
                }

                textRect.offsetTo(0f, 0f)

                // may be too small, don't worry we will find the best match
                if (availableSpace.contains(textRect))
                {
                    return -1
                }
                // else, too big
                return 1
            }
        }
        initialized = true
    }

    override fun setAllCaps(allCaps: Boolean) {
        super.setAllCaps(allCaps)
        adjustTextSize()
    }

    override fun setTypeface(tf: Typeface) {
        super.setTypeface(tf)
        adjustTextSize()
    }

    override fun setTextSize(size: Float) {
        maxTextSize = size
        adjustTextSize()
    }

    override fun setMaxLines(maxlines: Int) {
        super.setMaxLines(maxlines)
        maxLines = maxlines
        adjustTextSize()
    }

    override fun getMaxLines(): Int {
        return maxLines
    }

    override fun setSingleLine() {
        super.setSingleLine()
        maxLines = 1
        adjustTextSize()
    }

    override fun setSingleLine(singleLine: Boolean) {
        super.setSingleLine(singleLine)
        if (singleLine) {
            maxLines = 1
        } else {
            maxLines = NO_LINE_LIMIT
        }
        adjustTextSize()
    }

    override fun setLines(lines: Int) {
        super.setLines(lines)
        maxLines = lines
        adjustTextSize()
    }

    override fun setTextSize(unit: Int, size: Float) {
        val c = context
        val r: Resources
        if (c == null) {
            r = Resources.getSystem()
        } else {
            r = c.resources
        }
        maxTextSize = TypedValue.applyDimension(unit, size, r.displayMetrics)
        adjustTextSize()
    }

    override fun setLineSpacing(add: Float, mult: Float) {
        super.setLineSpacing(add, mult)
        spacingMult = mult
        spacingAdd = add
    }

    /**
     * Set the lower text size limit and invalidate the view

     * @param minTextSize
     */
    fun setMinTextSize(minTextSize: Float) {
        this.minTextSize = minTextSize
        adjustTextSize()
    }

    private fun adjustTextSize() {
        // This is a workaround for truncated text issue on ListView, as shown here: https://github
        // .com/AndroidDeveloperLB/AutoFitTextView/pull/14
        // TODO think of a nicer, elegant solution.
        //    post(new Runnable()
        //    {
        //    @Override
        //    public void run()
        //      {
        if (!initialized) {
            return
        }

        val startSize = minTextSize.toInt()
        val heightLimit = measuredHeight - compoundPaddingBottom - compoundPaddingTop
        widthLimit = measuredWidth - compoundPaddingLeft - compoundPaddingRight

        if (widthLimit <= 0) {
            return
        }

        txtPaint = TextPaint(paint)
        availableSpaceRect.right = widthLimit.toFloat()
        availableSpaceRect.bottom = heightLimit.toFloat()
        superSetTextSize(startSize)
        //      }
        //    });
    }

    private fun superSetTextSize(startSize: Int) {
        val textSize = binarySearch(startSize, maxTextSize.toInt(), sizeTester, availableSpaceRect)
        super.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize.toFloat())
    }

    private fun binarySearch(start: Int, end: Int, sizeTester: SizeTester, availableSpace: RectF): Int {
        var lastBest = start
        var lo = start
        var hi = end - 1
        var mid: Int

        while (lo <= hi) {
            mid = (lo + hi).ushr(1)
            val midValCmp = sizeTester.onTestSize(mid, availableSpace)

            if (midValCmp < 0) {
                lastBest = lo
                lo = mid + 1
            } else if (midValCmp > 0) {
                hi = mid - 1
                lastBest = hi
            } else {
                return mid
            }
        }

        // make sure to return last best
        // this is what should always be returned
        return lastBest
    }

    override fun onTextChanged(text: CharSequence, start: Int, before: Int, after: Int) {
        super.onTextChanged(text, start, before, after)
        adjustTextSize()
    }

    override fun onSizeChanged(width: Int, height: Int, oldwidth: Int, oldheight: Int) {
        super.onSizeChanged(width, height, oldwidth, oldheight)
        if (width != oldwidth || height != oldheight) {
            adjustTextSize()
        }
    }

    companion object {
        private val NO_LINE_LIMIT = -1
    }
}