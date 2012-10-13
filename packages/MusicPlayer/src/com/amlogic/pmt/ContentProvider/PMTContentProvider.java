package com.amlogic.pmt.ContentProvider;
import android.content.Context;

import com.amlogic.ContentProvider.UserContentProvider;


public class PMTContentProvider {
	private Context mycontext = null;
	private UserContentProvider UCP = null;
	public PMTContentProvider(Context context)
	{
		mycontext = context;
		UCP = new UserContentProvider(mycontext);
	}
	public void setMyParam(String name,String value)
	{
		UCP.setParams(name, value);
	}
	public String getMyParam(String name)
	{
		return UCP.getParams(name);
	}
	
}
