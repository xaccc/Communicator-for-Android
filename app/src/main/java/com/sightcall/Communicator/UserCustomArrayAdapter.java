package com.sightcall.Communicator;

/***
 * This array adapter renders our custom view for each user in the spinner.
 * The view renders a user's presence as a colored circle and sets the name as the text of the view.
 * See layout/custom_spinner.xml for the layout.
 */

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.sightcall.api.User;
import com.sightcall.Communicator.R;

public class UserCustomArrayAdapter extends ArrayAdapter<User> {
	
	public UserCustomArrayAdapter(Context ctx, int txtViewResourceId, List<User> mFriends) {
		super(ctx, txtViewResourceId, mFriends);
	}
	
	@Override
	public View getDropDownView(int position, View cnvtView, ViewGroup prnt) {
		return getCustomView(position, cnvtView, prnt);
	}
	
	@Override
	public View getView(int position, View cnvtView, ViewGroup prnt) {
		return getCustomView(position, cnvtView, prnt);
	}
	
	// Render the view for the selected item in the spinner
	public View getCustomView(int position, View convertView, ViewGroup parent) {

		LayoutInflater inflater = LayoutInflater.from(getContext());
		View mySpinner = inflater.inflate(R.layout.custom_spinner, parent, false);
		
		TextView main = (TextView) mySpinner.findViewById(R.id.spinner_item_name);
		
		View circle = (View) mySpinner.findViewById(R.id.spinner_item_circle);
		
		User user = (User) getItem(position);
		main.setText(user.name);
		
		if (user.presence == 0) {
			circle.setBackground( getContext().getResources().getDrawable(R.drawable.redcircle));
		}
		else if (user.presence == 1) {
			circle.setBackground( getContext().getResources().getDrawable(R.drawable.greencircle));
		}
		else {
			circle.setBackground( getContext().getResources().getDrawable(R.drawable.circle));
		}
		
		return mySpinner;
	}

}
