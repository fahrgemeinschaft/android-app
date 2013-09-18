/**
 * Copyright 2013 Bo Wang
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.calciumion.widget;

import java.util.ArrayList;
import java.util.Iterator;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

/**
 * This is an implementation of {@link PagerAdapter} that wraps it up like the
 * {@link Adapter} interface.
 * 
 * @author bowang
 * 
 */
public abstract class BasePagerAdapter extends PagerAdapter {

    ArrayList<Object> instantiatedItems = new ArrayList<Object>();
    ArrayList<Object> destroyedItems = new ArrayList<Object>();

    @Override
    public final void startUpdate(ViewGroup container) {
        instantiatedItems.clear();
        destroyedItems.clear();
    }

    @Override
    public final Object instantiateItem(ViewGroup container, int position) {
        Object o = getItem(position);
        instantiatedItems.add(o);
        return o;
    }

    @Override
    public final void destroyItem(ViewGroup container, int position,
            Object object) {
        destroyedItems.add(object);
    }

    @Override
    public final void finishUpdate(ViewGroup container) {
        ArrayList<View> recycledViews = new ArrayList<View>();

        // Remove views backing destroyed items from the specified container,
        // and queue them for recycling.
        for (int i = 0; destroyedItems.size() > 0
                && i < container.getChildCount(); i++) {
            View v = container.getChildAt(i);
            Iterator<Object> it = destroyedItems.iterator();
            while (it.hasNext()) {
                if (isViewFromObject(v, it.next())) {
                    container.removeView(v);
                    recycledViews.add(v);
                    it.remove();
                    break;
                }
            }
        }

        // Render views and attach them to the container. Page views are reused
        // whenever possible.
        for (Object instantiatedItem : instantiatedItems) {
            View convertView = null;
            if (recycledViews.size() > 0)
                convertView = recycledViews.remove(0);
            convertView = getView(instantiatedItem, convertView, container);
            convertView.setTag(instantiatedItem);
            container.addView(convertView);
        }

        instantiatedItems.clear();
        recycledViews.clear();
    }

    @Override
    public final boolean isViewFromObject(View view, Object object) {
        return view.getTag() != null && view.getTag() == object;
    }

    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    /**
     * Get the data item associated with the specified position in the data set.
     * 
     * @param position
     *            Position of the item whose data we want within the adapter's
     *            data set.
     * @return The data at the specified position
     */
    protected abstract Object getItem(int position);

    /**
     * Get a View that displays the data at the specified position in the data
     * set.
     * 
     * @param object
     *            The data item whose view we want to render.
     * @param convertView
     *            The view to be reused.
     * @param parent
     *            The parent that this view will eventually be attached to.
     * @return A View corresponding to the data at the specified position.
     */
    protected abstract View getView(Object object, View convertView,
            ViewGroup parent);

}