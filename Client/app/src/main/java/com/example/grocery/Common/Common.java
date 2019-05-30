package com.example.grocery.Common;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Switch;

import com.example.grocery.Model.Category;
import com.example.grocery.Model.User;

public class Common {
    public static User currentUser;
    public static final String DELETE = "Delete";
    public static boolean isConnectedToInternet(Context context)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager != null)
        {
            NetworkInfo[] info = connectivityManager.getAllNetworkInfo();
            if(info != null)
            {
                for(int i =0;i<info.length;i++)
                {
                    if(info[i].getState() == NetworkInfo.State.CONNECTED)
                        return true;
                }
            }
        }
        return false;
    }
    public static String convertCodeToStatus(String code )
    {
        String status="";
        switch (code)
        {
            case "0":
                status="Placed";
                break;
            case "1":
                status="On the way";
                break;
            default:
                status="Shipped";
                break;
        }
        return status;
    }

}
