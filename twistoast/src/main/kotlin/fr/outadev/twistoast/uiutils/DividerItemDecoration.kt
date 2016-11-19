/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.outadev.twistoast.uiutils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.ViewUtils
import android.util.TypedValue
import android.view.View
import fr.outadev.twistoast.IRecyclerAdapterAccess

class DividerItemDecoration(context: Context, orientation: Int) : RecyclerView.ItemDecoration() {

    private val leftAdditionalPadding: Int
    private val divider: Drawable
    private var orientation: Int = 0

    init {
        val r = context.resources
        leftAdditionalPadding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 95f, r.displayMetrics).toInt()

        val a = context.obtainStyledAttributes(ATTRS)
        divider = a.getDrawable(0)
        a.recycle()
        setOrientation(orientation)
    }

    fun setOrientation(orientation: Int) {
        if (orientation != HORIZONTAL_LIST && orientation != VERTICAL_LIST) {
            throw IllegalArgumentException("invalid orientation")
        }

        this.orientation = orientation
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State?) {
        if (orientation == VERTICAL_LIST) {
            drawVertical(c, parent)
        }
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State?) {
        if (orientation == VERTICAL_LIST) {
            outRect.set(0, 0, 0, divider.intrinsicHeight)
        } else {
            outRect.set(0, 0, divider.intrinsicWidth, 0)
        }
    }

    fun drawVertical(c: Canvas, parent: RecyclerView) {
        val isRtl = ViewUtils.isLayoutRtl(parent)

        val left = parent.paddingLeft + leftAdditionalPadding
        val right = parent.width - parent.paddingRight

        val childCount = parent.childCount

        for (i in 0..childCount - 1) {
            val child = parent.getChildAt(i)

            // Check if we should draw the separator or not
            if (!(parent.adapter as IRecyclerAdapterAccess).shouldItemHaveSeparator(parent.getChildAdapterPosition(child))) {
                continue
            }

            val params = child.layoutParams as RecyclerView.LayoutParams
            val top = child.bottom + params.bottomMargin
            val bottom = top + divider.intrinsicHeight

            if (!isRtl) {
                divider.setBounds(left, top, right, bottom)
            } else {
                // Fix for right-to-left layout (invert left padding and right padding)
                divider.setBounds(left - leftAdditionalPadding, top, right - leftAdditionalPadding, bottom)
            }

            divider.draw(c)
        }
    }

    companion object {
        val HORIZONTAL_LIST = LinearLayoutManager.HORIZONTAL
        val VERTICAL_LIST = LinearLayoutManager.VERTICAL
        private val ATTRS = intArrayOf(android.R.attr.listDivider)
    }

}