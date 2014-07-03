package com.kosbrother.tool;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.novel.reader.R;
import com.novel.reader.util.NovelReaderUtil;

public class ExpandListDownLoadAdapter extends BaseExpandableListAdapter {

    private static LayoutInflater inflater = null;
    private final Activity        activity;
    // private String theNovelName;
    public ArrayList<Group>       theGroups;
    private final TextView        theCountText;

    public ExpandListDownLoadAdapter(Activity a, ArrayList<Group> mGroups, TextView countText) {

        activity = a;
        theGroups = mGroups;
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        theCountText = countText;
        // theNovelName = novelName;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return null;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

        ChildArticle child = theGroups.get(groupPosition).getChildItem(childPosition);

        View vi = convertView;
        vi = inflater.inflate(R.layout.item_expandable_download_child, null);
        TextView text = (TextView) vi.findViewById(R.id.expandlist_child);
        String childString = NovelReaderUtil.translateTextIfCN(activity,child.getTitle());
        text.setText(childString);

        CheckBox checkBox = (CheckBox) vi.findViewById(R.id.checkbox_child);
        TextView textDownload = (TextView) vi.findViewById(R.id.text_child_downloaded);

        // check downloaded or not
        Boolean isDownloaded = child.isDownload();
        if (isDownloaded) {
            checkBox.setVisibility(View.GONE);
            textDownload.setVisibility(View.VISIBLE);
        }

        // 重新產生 CheckBox 時，將存起來的 isChecked 狀態重新設定
        checkBox.setChecked(child.getChecked());

        // 點擊 CheckBox 時，將狀態存起來
        checkBox.setOnClickListener(new Child_CheckBox_Click(Integer.valueOf(groupPosition), Integer.valueOf(childPosition)));

        return vi;
    }

    private void setCountText() {
        // TODO Auto-generated method stub
        int totalCount = 0;
        for (int i = 0; i < theGroups.size(); i++) {
            totalCount = totalCount + theGroups.get(i).getGroupCheckedCount();
        }
        theCountText.setText("共選了 " + Integer.toString(totalCount) + " 項");
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return theGroups.get(groupPosition).getChildrenCount();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return theGroups.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return theGroups.size();
    }

    @Override
    public void onGroupCollapsed(int groupPosition) {
        super.onGroupCollapsed(groupPosition);
    }

    @Override
    public void onGroupExpanded(int groupPosition) {
        super.onGroupExpanded(groupPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

        Group group = theGroups.get(groupPosition);

        View vi = convertView;
        vi = inflater.inflate(R.layout.item_expandable_download_parent, null);
        TextView text = (TextView) vi.findViewById(R.id.expandlist_parent);
        String groupString = NovelReaderUtil.translateTextIfCN(activity,group.getTitle());
        text.setText(groupString);

        int id = (!isExpanded) ? R.drawable.right_arrow : R.drawable.up_arrow;
        ImageView image = (ImageView) vi.findViewById(R.id.expandlist_parent_button);
        image.setImageResource(id);

        // 重新產生 CheckBox 時，將存起來的 isChecked 狀態重新設定
        CheckBox checkBox = (CheckBox) vi.findViewById(R.id.checkbox_parent);
        TextView textDownload = (TextView) vi.findViewById(R.id.text_parent_downloaded);

        // check 是否都下載了
        Boolean isAllDownloaded = true;
        for (int i = 0; i < group.getChildrenCount(); i++) {
            if (!group.getChildItem(i).isDownload())
                isAllDownloaded = false;
        }

        if (isAllDownloaded) {
            checkBox.setVisibility(View.GONE);
            textDownload.setVisibility(View.VISIBLE);
        }

        checkBox.setChecked(group.getChecked());
        // 點擊 CheckBox 時，將狀態存起來
        checkBox.setOnClickListener(new Group_CheckBox_Click(Integer.valueOf(groupPosition)));

        return vi;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    class Group_CheckBox_Click implements OnClickListener {
        private final int groupPosition;

        Group_CheckBox_Click(int groupPosition) {
            this.groupPosition = groupPosition;
        }

        public void onClick(View v) {
            theGroups.get(groupPosition).toggle();

            // 將 Children 的 isChecked 全面設成跟 Group 一樣
            int childrenCount = theGroups.get(groupPosition).getChildrenCount();
            boolean groupIsChecked = theGroups.get(groupPosition).getChecked();
            for (int i = 0; i < childrenCount; i++)
                theGroups.get(groupPosition).getChildItem(i).setChecked(groupIsChecked);

            // 注意，一定要通知 ExpandableListView 資料已經改變，ExpandableListView 會重新產生畫面
            notifyDataSetChanged();
            setCountText();
        }
    }

    class Child_CheckBox_Click implements OnClickListener {
        private final int groupPosition;
        private final int childPosition;

        Child_CheckBox_Click(int groupPosition, int childPosition) {
            this.groupPosition = groupPosition;
            this.childPosition = childPosition;
        }

        public void onClick(View v) {
            theGroups.get(groupPosition).getChildItem(childPosition).toggle();

            // 檢查 Child CheckBox 是否有全部勾選，以控制 Group CheckBox
            int childrenCount = theGroups.get(groupPosition).getChildrenCount();
            boolean childrenAllIsChecked = true;
            for (int i = 0; i < childrenCount; i++) {
                if (!theGroups.get(groupPosition).getChildItem(i).getChecked())
                    childrenAllIsChecked = false;
            }

            theGroups.get(groupPosition).setChecked(childrenAllIsChecked);

            // 注意，一定要通知 ExpandableListView 資料已經改變，ExpandableListView 會重新產生畫面
            notifyDataSetChanged();
            setCountText();
        }
    }

}