package com.dadasign.xcretrieve.history;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.DropBoxManager;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.dadasign.xcretrieve.BaseActivity;
import com.dadasign.xcretrieve.R;
import com.dadasign.xcretrieve.model.TrackingMessage;
import com.dadasign.xcretrieve.utils.LocationDataFormats;
import com.dadasign.xcretrieve.utils.Persistence;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jakub on 2015-07-19.
 */
public class HistoryListFragment extends Fragment implements  HistoryChangeListener{
    private Persistence persistence;
    private HistoryListAdapter adapter;
    public DetailHandler detailHandler;
    private ListView tracking_list;
    private static String LIST_ID = "tracking_list";

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        Log.v(this.getClass().toString(), "onViewStateRestored");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HistoryActivity act = (HistoryActivity) this.getActivity();
        this.persistence = act.persistence;
        adapter = new HistoryListAdapter(getActivity(), R.layout.history_list_item, R.id.title,persistence,detailHandler);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onPause() {
        Bundle b = new Bundle();
        this.onSaveInstanceState(b);
        super.onPause();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser && tracking_list!=null){
            tracking_list.refreshDrawableState();
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
    }

    public DetailHandler getDetailHandler(){
        if(detailHandler==null){
            Activity activity = getActivity();
            if(activity!=null && activity instanceof HistoryActivity) {
                detailHandler = (HistoryActivity) activity;
            }
        }
        return detailHandler;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        Log.v(this.getClass().toString(), "onCreateView");
        View v = inflater.inflate(R.layout.history_list, container, false);
        tracking_list = (ListView) v.findViewById(R.id.trackingMessages);
        tracking_list.setAdapter(adapter);
        tracking_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                getDetailHandler().displayDetailItem(adapter.getItem(position));
                Log.v("HistoryListFragment", "Item clicked");
            }
        });
        tracking_list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(final AdapterView<?> adapterView, final View view, final int item_index, long l) {
                new AlertDialog.Builder(getActivity())
                .setItems(R.array.history_item_list, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        persistence.deleteTrackingMessage(adapter.getItem(item_index).id);
                    }
                })
                .create().show();
                return true;
            }
        });
        tracking_list.setItemChecked(0, true);
        return v;
    }

    @Override
    public void onEditListItem(TrackingMessage msg) {
        for(int c=0; c< adapter.getCount(); c++){
            TrackingMessage msgOld = adapter.getItem(c);
            if(msg.id==msgOld.id){
                int pos = adapter.getPosition(msgOld);
                adapter.remove(msgOld);
                adapter.insert(msg,pos);
                adapter.notifyDataSetChanged();
                if(detailHandler.getDetailItem().id==msgOld.id) {
                    detailHandler.setDetailItem(msg);
                }
                return;
            }
        }
    }

    @Override
    public void onNewListItem(TrackingMessage msg) {
        adapter.insert(msg,0);
        adapter.notifyDataSetChanged();
        if(msg.messageType==TrackingMessage.MSG_TYPE_RECEIVED || msg.messageType == TrackingMessage.MSG_TYPE_TEST) {
            getDetailHandler();
            if(detailHandler!=null) {
                detailHandler.displayDetailItem(msg);
            }
        }
    }

    @Override
    public void onRemoveItem(long id) {
        for(int c=0; c< adapter.getCount(); c++){
            TrackingMessage msg = adapter.getItem(c);
            if(msg.id==id){
                adapter.remove(msg);
                adapter.notifyDataSetChanged();
                return;
            }
        }
    }

    @Override
    public void onClearListItems() {
        Log.e("HistoryListFragment","onClearListItems is not expected to be called.");
    }
}
