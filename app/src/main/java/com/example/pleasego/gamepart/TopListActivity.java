package com.example.pleasego.gamepart;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pleasego.R;
import com.example.pleasego.utils.Apis;
import com.example.pleasego.utils.HttpDealResponse;
import com.example.pleasego.utils.OkHttpUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;

public class TopListActivity extends AppCompatActivity {

    private List<TopListVO> topListVOs = new ArrayList<>();
    private MyAdapter myAdapter = new MyAdapter();
    private RecyclerView topList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_list);
        topList = (RecyclerView) findViewById(R.id.top_list);
        getTopListData();

    }

    /**
     * 从服务器获取金钱前100的人
     */
    private void getTopListData() {
        OkHttpUtils.doGet(Apis.getTopList, new HttpDealResponse() {
            @Override
            public void dealResponse(ResponseBody responseBody) {
                try {
                    JSONObject jsonObject = new JSONObject(responseBody.string());
                    if ("true".equals(jsonObject.getString("success"))) {
                        JSONArray jsonArray = jsonObject.getJSONArray("data");
                        int size = jsonArray.length();
                        for (int i = 0; i < size; i++) {
                            TopListVO topListVO = new TopListVO();
                            JSONObject item = jsonArray.getJSONObject(i);
                            topListVO.username = item.getString("username");
                            topListVO.money = item.getString("money");
                            topListVOs.add(topListVO);
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                topList.setLayoutManager(new LinearLayoutManager(TopListActivity.this));
                                topList.setAdapter(myAdapter);
                            }
                        });
                    }
                } catch (JSONException | IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(TopListActivity.this, "服务器或网络异常", Toast.LENGTH_SHORT).show();
                        }
                    });
                    e.printStackTrace();
                }

            }

            @Override
            public void dealError(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(TopListActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    /**
     * 自定义的适配器
     */
    class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new MyViewHolder(LayoutInflater.from(TopListActivity.this).inflate(R.layout.top_list_item, parent, false));
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            holder.topListMoney.setText(topListVOs.get(position).money);
            holder.topListUsername.setText(topListVOs.get(position).username);
        }

        @Override
        public int getItemCount() {
            return topListVOs.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            TextView topListUsername;
            TextView topListMoney;

            public MyViewHolder(View view) {
                super(view);
                topListUsername = (TextView) view.findViewById(R.id.top_list_username);

                topListMoney = (TextView) view.findViewById(R.id.top_list_money);
            }
        }
    }

    /**
     * 每一项的VO类
     */
    private class TopListVO {
        String username;
        String money;
    }

}
