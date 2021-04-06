package com.ivxin.panresource.view;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class MyAdapter<V extends MyAdapterItemLayout<B>, B> extends BaseAdapter {
    private Context context;
    private List<B> list;
    private Class<V> vClass;
    private Class<B> bClass;

    public MyAdapter(Context context, List<B> list, Class<V> vClass, Class<B> bClass) {
        this.context = context;
        this.list = list;
        this.vClass = vClass;
        this.bClass = bClass;
    }


    public void setList(List<B> list) {
        this.list = list;
    }

    public List<B> getList() {
        return list;
    }

    public void remove(int position) {// 删除指定位置的item
        list.remove(position);
        this.notifyDataSetChanged();
    }

    public void insert(B item, int position) {// 在指定位置插入item
        list.add(position, item);
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return list == null ? 0 : list.size();
    }

    @Override
    public B getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressWarnings("unchecked")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        V view = null;
        try {
            if (convertView == null) {
                Constructor<V> constructor = vClass.getConstructor(Context.class);
                view = constructor.newInstance(context);
            } else {
                view = (V) convertView;
            }
            Method method = vClass.getMethod("setData", bClass, int.class, ViewGroup.class);
            method.invoke(view, list.get(position), position, parent);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return view;
    }

}
