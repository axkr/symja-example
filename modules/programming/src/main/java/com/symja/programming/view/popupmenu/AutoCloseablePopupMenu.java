package com.symja.programming.view.popupmenu;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleObserver;

import com.symja.programming.R;
import com.symja.programming.utils.ViewUtils;

import java.util.ArrayList;
import java.util.List;

public class AutoCloseablePopupMenu extends AutoCloseablePopupWindow implements LifecycleObserver {

    private final List<MenuItem> menuItems = new ArrayList<>();

    private ArrayAdapter<MenuItem> adapter;

    @Nullable
    private CharSequence title;
    @Nullable
    private OnMenuItemClickListener onMenuItemClickListener;

    public AutoCloseablePopupMenu(@NonNull FragmentActivity context) {
        super(context);

        setWidth(ViewUtils.dpToPx(context, 250));
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);

        setOutsideTouchable(false);
        setTouchable(true);
        setFocusable(true);
        setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        setElevation(20);

        setContentView(createView(context));
    }


    @NonNull
    private View createView(Context context) {
        @SuppressLint("InflateParams") View view = LayoutInflater.from(context).inflate(R.layout.popup_menu, null);
        TextView txtTitle = view.findViewById(R.id.txt_title);
        if (title == null) {
            txtTitle.setVisibility(View.GONE);
        } else {
            txtTitle.setVisibility(View.VISIBLE);
            txtTitle.setText(title);
        }

        // filter visible items
        ListView listView = view.findViewById(R.id.list_item_view);

        adapter = new ArrayAdapter<MenuItem>(context, R.layout.list_item_popup_menu) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                View view;
                if (convertView == null) {
                    view = inflater.inflate(R.layout.list_item_popup_menu, parent, false);
                } else {
                    view = convertView;
                }
                MenuItem menuItem = getItem(position);

                TextView title = view.findViewById(R.id.title);
                title.setText(menuItem.getTitle(context));

                TextView subtitleView = view.findViewById(R.id.subtitle);
                CharSequence subtitle = menuItem.getSubtitle(context);
                if (subtitle == null || subtitle.length() == 0) {
                    subtitleView.setVisibility(View.GONE);
                } else {
                    subtitleView.setVisibility(View.VISIBLE);
                    subtitleView.setText(subtitle);
                }

                ImageView iconView = view.findViewById(R.id.icon_view);
                Drawable icon = menuItem.getIcon(context);
                if (icon == null) {
                    iconView.setVisibility(View.GONE);
                } else {
                    iconView.setVisibility(View.VISIBLE);
                    iconView.setImageDrawable(icon);
                }

                view.setEnabled(menuItem.isEnabled());
                return view;
            }
        };
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view1, position, id) -> {
            MenuItem menuItem = (MenuItem) parent.getItemAtPosition(position);
            if (menuItem.isEnabled()) {
                if (onMenuItemClickListener != null) {
                    onMenuItemClickListener.onMenuItemClick(menuItem);
                    dismiss();
                }
            }
        });
        return view;
    }

    @Override
    public void showAsDropDown(@NonNull View anchor) {

        adapter.clear();
        adapter.addAll(this.menuItems);

        super.showAsDropDown(anchor);
    }

    public void setOnMenuItemClickListener(@Nullable OnMenuItemClickListener onMenuItemClickListener) {
        this.onMenuItemClickListener = onMenuItemClickListener;
    }

    public void addItem(MenuItem item) {
        this.menuItems.add(item);
    }

    public void inflate(@MenuRes int menuId, @NonNull View anchorView) {
        PopupMenu popupMenu = new PopupMenu(context, anchorView);
        popupMenu.getMenuInflater().inflate(menuId, popupMenu.getMenu());
        Menu menu = popupMenu.getMenu();
        for (int i = 0; i < menu.size(); i++) {
            android.view.MenuItem androidItem = menu.getItem(i);
            MenuItem menuItem = new MenuItem(androidItem.getItemId(), androidItem.getTitle());
            menuItem.setIcon(androidItem.getIcon());
            addItem(menuItem);
        }
    }
}
