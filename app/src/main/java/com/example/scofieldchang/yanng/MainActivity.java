package com.example.scofieldchang.yanng;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import net.sf.json.JSON;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private Button searchBtn;
    private EditText editText;
    private ListView listView;
    List<DisplayUnitData> list;
    private int LISTVIEW = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUI();


    }
    private void initUI(){
        editText = (EditText) findViewById(R.id.input_edit);

        searchBtn = (Button) findViewById(R.id.seatch_btn);
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String input_number = editText.getText().toString();
                Log.d("info", input_number);
                ((Thread) new NetWorkThread(input_number)).start();
            }
        });

        listView = (ListView) findViewById(R.id.list_view);
    }
    private SimpleAdapter getSimpleAdapter(List<DisplayUnitData> list){
        SimpleAdapter simpleAdapter = new SimpleAdapter(this, getData(list), R.layout.vlist,
                new String[]{"id", "name"}, new int[]{R.id.id, R.id.name});
        return simpleAdapter;
    }
    private List<Map<String, Object>> getData(List<DisplayUnitData> list){
        List<Map<String, Object>> lists = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < list.size(); ++i){
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("id", list.get(i).id);
            map.put("name", list.get(i).name);
            lists.add(map);
        }
        return lists;
    }
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1 :{
                    SimpleAdapter simpleAdapter = getSimpleAdapter(list);
                    listView.setAdapter(simpleAdapter);
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            if (list != null){
                                DisplayUnitData data = list.get(i);
                                Log.d("info","url is " + data.url);
                                Intent intent = new Intent(MainActivity.this, WebActivity.class);
                                intent.putExtra("url", data.url);
                                startActivity(intent);
                            }
                        }
                    });
                    break;
                }
            }
        }
    };

    private class NetWorkThread extends Thread{
        String number;
        String str = "http://z.ohao.ren/index.php?m=HJB&a=index&id=";
        String strjson = "[{\"id\":1,\"name\":null,\"url\":\"http://weibo.com/\"}&{\"id\":2,\"name\":null,\"url\":\"http://zhihu.com/\"}]";
        public NetWorkThread(String number){
            this.number = number;
        }
        @Override
        public void run() {
            Log.d("info", "NetWorkThread start");
            String result = decode2(executeHttpGet());
            Log.d("info", result);
            list = getDisplayFormat(result);
            mHandler.sendEmptyMessage(LISTVIEW);
            for(int i = 0; i < list.size(); i++){
                Log.d("info", list.get(i).url);
                System.out.println("url: " + list.get(i).url);
            }
        }
        public DisplayUnitData getDisplayDataFormat(JSONObject jsonObject){
            DisplayUnitData displayData = new DisplayUnitData();
            try {
                displayData.id = jsonObject.getString("id");
                displayData.name = jsonObject.getString("name");
                displayData.url = jsonObject.getString("url");
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return displayData;
        }
        public List<DisplayUnitData> getDisplayFormat(String jsonString){
            Log.d("info", "getDisplayFormat");
            List<DisplayUnitData> lists = new ArrayList<DisplayUnitData>();
            String strings = jsonString.substring(1, jsonString.length()-1);
            Log.d("info",strings);
            if (strings.contains("&")){
                String[] jsons = strings.split("&");
                for (int i =0; i < jsons.length; ++i){
                    Log.d("info", "json string: " + jsons[i]);
                    try {
                        JSONTokener jsonParser = new JSONTokener(jsons[i]);
                        JSONObject jsonObject =  (JSONObject)jsonParser.nextValue();
                        DisplayUnitData displayData = getDisplayDataFormat(jsonObject);
                        lists.add(displayData);
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                return lists;
            }else{
                try {
                    JSONTokener jsonParser = new JSONTokener(strings);
                    JSONObject jsonObject = (JSONObject)jsonParser.nextValue();
                    DisplayUnitData displayUnitData = getDisplayDataFormat(jsonObject);
                    list.add(displayUnitData);
                    return list;
                } catch (JSONException e){
                    e.printStackTrace();
                }
                finally {
                    return null;
                }

            }

        }
        public String executeHttpGet() {
            String result = null;
            URL url = null;
            HttpURLConnection connection = null;
            InputStreamReader in = null;
            Log.d("info", "executeHttpGet");
            try {
                url = new URL("http://z.ohao.ren/index.php?m=HJB&a=index&id=15029484116");
                connection = (HttpURLConnection) url.openConnection();
                System.out.println(connection.getResponseMessage());
                in = new InputStreamReader(connection.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(in);
                StringBuffer strBuffer = new StringBuffer();
                String line = null;
                while ((line = bufferedReader.readLine()) != null) {
                    strBuffer.append(line);
                }
                result = strBuffer.toString();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
            return result;
        }
        public String decode2( String s )
        {
            StringBuilder sb = new StringBuilder( s.length() );
            char[] chars = s.toCharArray();
            for( int i = 0; i < chars.length; i++ )
            {
                char c = chars[i];
                if( c == '\\' && chars[i + 1] == 'u')
                {
                    char cc = 0;
                    for( int j = 0; j < 4; j++ )
                    {
                        char ch = Character.toLowerCase( chars[i + 2 + j] );
                        if( '0' <= ch && ch <= '9' || 'a' <= ch && ch <= 'f' )
                        {
                            cc |= ( Character.digit( ch, 16 ) << ( 3 - j ) * 4 );
                        }else
                        {
                            cc = 0;
                            break;
                        }
                    }
                    if ( cc > 0 )
                    {
                        i += 5;
                        sb.append( cc );
                        continue;
                    }
                }
                sb.append( c );
            }
            return sb.toString();
        }

    }
}
