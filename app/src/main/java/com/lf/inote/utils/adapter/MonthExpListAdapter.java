/** 
 * @项目名称：INote   
 * @文件名：MonthExpListAdapter.java    
 * @版本信息：
 * @日期：2015-6-3    
 * @Copyright 2015 www.517na.com Inc. All rights reserved.         
 */
package com.lf.inote.utils.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.lf.inote.R;
import com.lf.inote.model.Bill;
import com.lf.inote.ui.bill.EditBillActivity;

import java.util.ArrayList;
import java.util.Map;

/**    
 *     
 * @项目名称：INote    
 * @类名称：MonthExpListAdapter    
 * @类描述：    
 * @创建人：lianfeng    
 * @创建时间：2015-6-3 上午8:40:56    
 * @修改人：lianfeng    
 * @修改时间：2015-6-3 上午8:40:56    
 * @修改备注：    
 * @version     
 *     
 */
public class MonthExpListAdapter extends BaseExpandableListAdapter {

	private Context mContext;

	private Map<String, ArrayList<Bill>> mMap;

	private Object[] mKeySet;

	public MonthExpListAdapter(Context context, Map<String, ArrayList<Bill>> map) {
		mContext = context;
		mMap = map;
		mKeySet = (Object[]) mMap.keySet().toArray();
	}

	public void updateData(Map<String, ArrayList<Bill>> map) {
		if (map != null) {
			mMap.clear();
			mMap.putAll(map);
			mKeySet = (Object[]) mMap.keySet().toArray();
		}
	}

	@Override
	public int getGroupCount() {
		return mMap.size();
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return mMap.get(mKeySet[groupPosition]).size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return mKeySet[groupPosition];
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return mMap.get(mKeySet[groupPosition]).get(childPosition);
	}

	@Override
	public long getGroupId(int groupPosition) {
		return 0;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return 0;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		GroupHolder item = null;
		if (null == convertView) {
			item = new GroupHolder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.explv_group_item, null);
			item.mTvTitle = (TextView) convertView.findViewById(R.id.tv_group_item_title);
			convertView.setTag(item);
		} else {
			item = (GroupHolder) convertView.getTag();
		}

		item.mTvTitle.setText(mKeySet[groupPosition].toString());

		return convertView;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
		ChildHolder item = null;
		if (null == convertView) {
			item = new ChildHolder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.explv_child_item_month, null);
			item.mTvUsage = (TextView) convertView.findViewById(R.id.tv_month_bill_item_usage);
			item.mTvDate = (TextView) convertView.findViewById(R.id.tv_month_bill_item_date);
			item.mTvAmount = (TextView) convertView.findViewById(R.id.tv_month_bill_item_amunt);
			convertView.setTag(item);
		} else {
			item = (ChildHolder) convertView.getTag();
		}

		item.mTvUsage.setText(mMap.get(mKeySet[groupPosition]).get(childPosition).getRemark());
		Bill bill = mMap.get(mKeySet[groupPosition]).get(childPosition);
		item.mTvDate.setText(bill.getDate());
		if (bill.getType() == EditBillActivity.TYPE_IN) {
			item.mTvAmount.setText("+￥" + bill.getMoney());
			item.mTvAmount.setTextColor(mContext.getResources().getColor(R.color.green));
		} else if (bill.getType() == EditBillActivity.TYPE_OUT) {
			item.mTvAmount.setText("-￥" + bill.getMoney());
			item.mTvAmount.setTextColor(mContext.getResources().getColor(R.color.font_red_color));
		}

		return convertView;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	private class GroupHolder {
		TextView mTvTitle;
	}

	private class ChildHolder {
		TextView mTvUsage;
		TextView mTvDate;
		TextView mTvAmount;
	}
}
