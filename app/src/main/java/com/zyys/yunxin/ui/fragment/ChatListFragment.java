package com.zyys.yunxin.ui.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.msg.MsgServiceObserve;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.zyys.yunxin.Api;
import com.zyys.yunxin.MyApplication;
import com.zyys.yunxin.R;
import com.zyys.yunxin.adapter.ChatFiendListAdapter;
import com.zyys.yunxin.adb.MySQLiteHelper;
import com.zyys.yunxin.bean.Account;
import com.zyys.yunxin.bean.Msg;
import com.zyys.yunxin.bean.VoipInfo;
import com.zyys.yunxin.ui.ChatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class ChatListFragment extends Fragment {


    private MySQLiteHelper helper;
    private List<Msg> msgsList = new ArrayList<>();
    private ListView lv_chat_friend_list;
    private RequestQueue mQueue;
    private ArrayList<VoipInfo> voipInfoList = new ArrayList<>();
    private ChatFiendListAdapter adapter;
    private int num = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.chat_list_fragment, container, false);
        mQueue = Volley.newRequestQueue(getActivity());//初始化队列
        receiverInfo();//
        lv_chat_friend_list = (ListView) view.findViewById(R.id.lv_chat_friend_list);
        //如果数据库不为空，则从数据库取出数据适配好友列表
        helper = new MySQLiteHelper(getActivity());
        List<Account> accountList = helper.selectAll();
        if (accountList.size() > 0){
            Account account = accountList.get(0);
            String url = Api.avatarUrl + account;
            sendRequest(url);
        }
        return view;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        lv_chat_friend_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //点击跳转到聊天界面
                //点击获取对方账号，与其进入聊天状态
                // 传递voipAccount到聊天ChatActivity,点击item之前已经存到数据库了
                String voipaccount = helper.selectAll().get(position).getVoipaccount();
                Log.d("ChatListFragment", voipaccount.toString());
                Intent intent = new Intent(getContext(), ChatActivity.class);
                intent.putExtra("voipAccount", voipaccount);
                startActivity(intent);
            }
        });

    }

    public void receiverInfo(){

        Observer<List<IMMessage>> incomingMessageObserver =
                new Observer<List<IMMessage>>() {

                    @Override
                    public void onEvent(List<IMMessage> messages) {

                        String content = messages.get(0).getContent().toString();

                        Log.d("content===", content.toString());


                        String voipAccount = messages.get(0).getFromAccount();


                        //发出广播
                        Intent intent = new Intent("com.zyys.yunxin.ui.INFORMATION");
                        intent.putExtra("content", content.toString());
                        MyApplication.getContext().sendBroadcast(intent);



                        //Msg msg = new Msg(content,Msg.TYPE_RECEIVED);

                        //msgsList.add(message);

                        MySQLiteHelper sqLiteHelper = new MySQLiteHelper(MyApplication.getContext());
                        //把消息存到数据库
                        if (content.toString().indexOf("{") == 0){
                            sqLiteHelper.insertHistoryInfo("history", voipAccount,content.toString(),Msg.TYPE_OTHER);
                        }else {
                            sqLiteHelper.insertHistoryInfo("history", voipAccount,content.toString(),Msg.TYPE_RECEIVED);
                        }

                        ArrayList<Account> accountList = helper.selectAll();//搜索数据库
                        //第一个好友发来消息，保存voipAccount到数据库，并且显示到好友列表中
                        if (accountList.size() == 0) {//第一次进来数据库肯定为空，先获取好友数据适配一次
                            //把voipAccount存进数据库
                            Account account = new Account(voipAccount);
                            helper.insert("account", account);
                            //调用请求头像和名字的方法
                            String url = Api.avatarUrl + voipAccount;
                            sendRequest(url);
                        }
                        boolean flag = true;
                        //第二个好友进来的时候,判断是否存在过数据库，如果没有存过，把voipAccount保存数据库，并且显示到好友列表
                        if (accountList.size() > 0) {//第二次进来就只需判断是否需要存进数据库
                            for (int i = 0; i < accountList.size(); i++) {//第一个好友再次进来,遍历数据库
                                if (voipAccount.equals(accountList.get(i).getVoipaccount().toString())) {//不相同的voipAccount才保存
                                    flag = false;//只要循环遍历到相同的就进来
                                    break;
                                }
                            }
                            if (flag){
                                Log.d("ChatListFragment", "msgsList:不相同的voipAccount才保存" );
                                Account account = new Account(voipAccount);
                                helper.insert("account", account);
                                //有新的患者发送消息过来
                                String url = Api.avatarUrl + voipAccount;
                                addFriendList(url);//往好友列表上继续添加好友
                                flag = false;
                            }
                        }

                    }
                };
        NIMClient.getService(MsgServiceObserve.class)
                .observeReceiveMessage(incomingMessageObserver, true);

    }


    //get : name img
    public void sendRequest(String url) {
        Log.d("url==", url.toString());
        voipInfoList = new ArrayList<>();
        StringRequest request = new StringRequest(
                Request.Method.GET,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject object = new JSONObject(response);
                            JSONObject data = object.getJSONObject("data");
                            String name = data.getString("name");
                            String imgUlr = data.getString("avatar");//头像图片url
                            //set voipInfo data
                            VoipInfo voipInfo = new VoipInfo(imgUlr, name);
                            helper = new MySQLiteHelper(getActivity());
                            List<Account> accountList = helper.selectAll();

                            if (num < accountList.size()-1) {
                                num++;
                                String url = Api.avatarUrl + accountList.get(num);
                                sendRequest(url);
                            }
                            voipInfoList.add(voipInfo);
                            if(num == accountList.size()-1){//添加完数据之后再适配
                                adapter = new ChatFiendListAdapter(getActivity(), voipInfoList);
                                lv_chat_friend_list.setAdapter(adapter);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                });
        mQueue.add(request);
    }

    //新的好友发送消息过来，把他继续添加到好友列表
    //get : name img
    public void addFriendList(String url) {
        voipInfoList = new ArrayList<>();
        StringRequest request = new StringRequest(
                Request.Method.GET,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject object = new JSONObject(response);
                            JSONObject data = object.getJSONObject("data");
                            String name = data.getString("name");
                            String imgUlr = data.getString("avatar");//头像图片url
                            //set voipInfo data
                            VoipInfo voipInfo = new VoipInfo(imgUlr, name);
                            voipInfoList.add(voipInfo);
                            adapter.addData(voipInfoList);//往好友列表上接续添加好友
                            adapter.notifyDataSetChanged();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                });
        mQueue.add(request);
    }


}
