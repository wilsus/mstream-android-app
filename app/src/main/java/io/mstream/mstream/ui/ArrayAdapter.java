package io.mstream.mstream.ui;

import android.support.v7.widget.RecyclerView;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Base ArrayAdapter for RecyclerView from https://gist.github.com/passsy/f8eecc97c37e3de46176
 * @param <T> the object type in the array
 * @param <V> the ViewHolder of the RecyclerView
 */
public abstract class ArrayAdapter<T, V extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<V> {

    private List<T> mObjects;

    /**
     * Creates an ArrayAdapter from a list of objects.
     */
    public ArrayAdapter(final List<T> objects) {
        mObjects = objects;
    }

    /**
     * Adds the specified object at the end of the array.
     * @param object The object to add at the end of the array.
     */
    public void add(final T object) {
        mObjects.add(object);
        notifyItemInserted(getItemCount() - 1);
    }

    /**
     * Adds the specified objects at the end of the array.
     */
    public void add(final List<T> objects) {
        if (getItemCount() == 0) {
            mObjects.addAll(0, objects);
        } else {
            mObjects.addAll(getItemCount() - 1, objects);
        }
        notifyItemRangeInserted(getItemCount() - objects.size(), objects.size());
    }

    /**
     * Remove all elements from the list.
     */
    public void clear() {
        final int size = getItemCount();
        mObjects.clear();
        notifyItemRangeRemoved(0, size);
    }

    @Override
    public int getItemCount() {
        return mObjects.size();
    }

    /**
     * Gets the object at the given position.
     */
    public T getItem(final int position) {
        return mObjects.get(position);
    }

    /**
     * Gets the item id at the given position.
     */
    public long getItemId(final int position) {
        return position;
    }

    /**
     * Returns the position of the specified item in the array.
     * @param item The item to retrieve the position of.
     * @return The position of the specified item.
     */
    public int getPosition(final T item) {
        return mObjects.indexOf(item);
    }

    /**
     * Inserts the specified object at the specified index in the array.
     * @param object The object to insert into the array.
     * @param index  The index at which the object must be inserted.
     */
    public void insert(final T object, int index) {
        mObjects.add(index, object);
        notifyItemInserted(index);

    }

    /**
     * Removes the specified object from the array.
     * @param object The object to remove.
     */
    public void remove(T object) {
        final int position = getPosition(object);
        mObjects.remove(object);
        notifyItemRemoved(position);
    }

    /**
     * Sorts the content of this adapter using the specified comparator.
     * @param comparator The comparator used to sort the objects contained in this adapter.
     */
    public void sort(Comparator<? super T> comparator) {
        Collections.sort(mObjects, comparator);
        notifyItemRangeChanged(0, getItemCount());
    }
}
