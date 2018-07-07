package com.tongtong.purchaser.frament;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tongtong.purchaser.utils.CodeUtil;

/**
 * Created by Administrator on 2018-02-03.
 */

public class BaseFrament extends Fragment{
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return new TextView(getActivity());
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        if(Build.VERSION.SDK_INT>=24){
//            checkNet();
//        }
    }
//
//    @TargetApi(24)
//    private void checkNet(){
//        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
//        connectivityManager.requestNetwork(new NetworkRequest.Builder().build(),
//                new ConnectivityManager.NetworkCallback() {
//                    @Override public void onAvailable(Network network) {
//                        onNetChange(1);
//                    }
//                });
    public boolean verification(int code){
        if(code == CodeUtil.TOKEN_NO_CODE){
            return false;
        }
        else if(code == CodeUtil.TOKEN_INVALID_CODE){
            return false;
        }
        else{
            return true;
        }
    }


}
