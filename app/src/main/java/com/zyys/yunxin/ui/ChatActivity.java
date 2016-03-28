package com.zyys.yunxin.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;


import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.zyys.yunxin.R;
import com.zyys.yunxin.adapter.MsgAdapter;
import com.zyys.yunxin.adb.MySQLiteHelper;
import com.zyys.yunxin.bean.Msg;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {


    private static ListView msgListView;
    private static EditText inputText;
    private static MsgAdapter msgAdapter;
    private static List<Msg> msgList = new ArrayList<Msg>();
    private static boolean isFirst = true;
    private String sessionId = "";
    private SessionTypeEnum sessionType = SessionTypeEnum.typeOfValue(0);

    private MySQLiteHelper helper;
    public static String voipAccount = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        inputText = (EditText) findViewById(R.id.input_text);//info
        msgListView = (ListView) findViewById(R.id.msg_list_view);
        helper = new MySQLiteHelper(ChatActivity.this);
//
        Intent intent = getIntent();
        voipAccount = intent.getStringExtra("voipAccount");
        sessionId = voipAccount;

        if (!voipAccount.equals("")){//第一次进来没有历史消息。。
            //查询数据库对应账号的历史消息
            msgList = helper.selectHistory(voipAccount);/////有问题？？？

            if (msgList.size()>0){
                msgAdapter = new MsgAdapter(msgList, ChatActivity.this);
                msgListView.setAdapter(msgAdapter);
                msgListView.setSelection(msgListView.getBottom());
            }
        }
        isFirst = false;
    }

    public void sendInfo(View view) {
        String text = inputText.getText().toString();//要发送的消息
            if (!text.equals("")) {
                // 创建文本消息
                IMMessage message = MessageBuilder.createTextMessage(
                        sessionId, // 聊天对象的 ID，如果是单聊，为用户帐号，如果是群聊，为群组 ID
                        sessionType, // 聊天类型，单聊或群组
                        text // 文本内容
                );

                // 发送消息。如果需要关心发送结果，可设置回调函数。发送完成时，会收到回调。如果失败，会有具体的错误码。
                NIMClient.getService(MsgService.class).sendMessage(message,true);
                    //首先把要发送的消息存进数据库
                    helper.insertHistoryInfo("history", voipAccount, text, Msg.TYPE_SEND);

                    Msg msg = new Msg(text,Msg.TYPE_SEND);
                    msgList.add(msg);
                    msgAdapter.notifyDataSetChanged();

//                    msgAdapter = new MsgAdapter(msgList, ChatActivity.this);
//                    msgListView.setAdapter(msgAdapter);
                    msgListView.setSelection(msgListView.getBottom());
                    inputText.setText("");


            }else {
                Toast.makeText(this,"消息不能为空！",Toast.LENGTH_SHORT).show();
            }
    }

    //接收广播发出的消息
    public static class ChatReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String massge = intent.getStringExtra("content");
            String voipAccount = intent.getStringExtra("voipAccount");
            if (!isFirst) {
                if (massge.indexOf("{") == 0) {
                    Msg msg = new Msg(massge, Msg.TYPE_OTHER);
                    msgList.add(msg);
                    msgAdapter.notifyDataSetChanged();
                } else {
                    Msg msg = new Msg(massge, Msg.TYPE_RECEIVED);
                    msgList.add(msg);
                    msgAdapter.notifyDataSetChanged();
                }

            }

        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //销毁接收消息观察者
    }
}
