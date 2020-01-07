package com.example.project2_test1;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private ListView noticeListView;
    private NoticeListAdapter adapter;
    private List<Notice> noticedList;

    private String name = "유희승";

    private TextView student_name;
    private TextView student_id;
    private TextView student_email;

    private ImageButton attCheck;
    private ImageButton kaiPortal;
    private ImageButton klms;

    String targetNotice = "http://192.249.19.252:1780/notes/";
    String targetStudent = "http://192.249.19.252:1780/students/" + name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        student_name = findViewById(R.id.student_name);
        student_id = findViewById(R.id.student_id);
        student_email = findViewById(R.id.student_email);

        student_name.setText(name);

        attCheck = findViewById(R.id.att_check);
        kaiPortal = findViewById(R.id.kai_portal);
        klms = findViewById(R.id.klms);

        attCheck.setOnClickListener(this);
        kaiPortal.setOnClickListener(this);
        klms.setOnClickListener(this);

        noticeListView = findViewById(R.id.noticeListView);
        noticedList = new ArrayList<Notice>();
        adapter = new NoticeListAdapter(getApplicationContext(), noticedList);
        noticeListView.setAdapter(adapter);

        final LinearLayout notice = findViewById(R.id.notice);

        new BackgroundTask().execute(targetNotice);
        new BackgroundTask().execute(targetStudent);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.att_check:
                break;
            case R.id.kai_portal:
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://portal.kaist.ac.kr/"));
                startActivity(intent);
                break;
            case R.id.klms:
                Intent intent2 = new Intent(Intent.ACTION_VIEW, Uri.parse("https://klms.kaist.ac.kr/login.php"));
                startActivity(intent2);
                break;
        }
    }


    //PHP서버에 접속해서 JSON타입으로 데이터를 가져옴
    class BackgroundTask extends AsyncTask<String, Void, String> {

        ProgressDialog asyncDialog = new ProgressDialog(MainActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            asyncDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            asyncDialog.setMessage("로딩중");
            asyncDialog.show();

        }

        //실제 데이터를 가져오는 부분임
        @Override
        protected String doInBackground(String... url) {
            try{

                for (int i = 0; i < 3; i++) {
                    asyncDialog.setProgress(i * 20);
                    Thread.sleep(300);
                }

                URL Noticeurl = new URL(url[0]);
                HttpURLConnection httpURLConnectionNotice = (HttpURLConnection)Noticeurl.openConnection();
                httpURLConnectionNotice.setRequestMethod("GET");
                InputStream inputStreamNotice = httpURLConnectionNotice.getInputStream();
                BufferedReader bufferedReaderNotice = new BufferedReader(new InputStreamReader(inputStreamNotice));
                String tempNotice;//결과 값을 여기에 저장함
                StringBuilder stringBuilderNotice = new StringBuilder();

                //버퍼생성후 한줄씩 가져옴
                while((tempNotice = bufferedReaderNotice.readLine()) != null){
                    stringBuilderNotice.append(tempNotice + "\n");
                }

                bufferedReaderNotice.close();
                inputStreamNotice.close();
                httpURLConnectionNotice.disconnect();


/*
                URL Studenturl = new URL(targetStudent);
                HttpURLConnection httpURLConnectionStudent = (HttpURLConnection)Studenturl.openConnection();
                httpURLConnectionStudent.setRequestMethod("GET");
                InputStream inputStreamStudent = httpURLConnectionStudent.getInputStream();
                BufferedReader bufferedReaderStudent = new BufferedReader(new InputStreamReader(inputStreamStudent));
                String tempStudent;
                StringBuilder stringBuilderStudent = new StringBuilder();

                while((tempStudent = bufferedReaderStudent.readLine()) != null){
                    stringBuilderStudent.append(tempStudent + "\n");
                }

                bufferedReaderStudent.close();
                inputStreamStudent.close();
                httpURLConnectionStudent.disconnect();*/

                return stringBuilderNotice.toString().trim();//결과값이 여기에 리턴되면 이 값이 onPostExcute의 파라미터로 넘어감

            }catch(Exception e){
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        //여기서는 가져온 데이터를 Notice객체에 넣은뒤 리스트뷰 출력을 위한 List객체에 넣어주는 부분
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            asyncDialog.dismiss();
            try{
                //JSONObject jsonObject = new JSONObject(result);
                JSONArray jsonArray = new JSONArray(result);
                int count = 0;
                String noticeContent, noticeName, noticeDate;

                //json타입의 값을 하나씩 빼서 Notice 객체에 저장후 리스트에 추가하는 부분
                while(count < jsonArray.length()){
                    JSONObject object = jsonArray.getJSONObject(count);
                    if (object.has("student_id")) {
                        student_name.setText(object.getString("name"));
                        student_id.setText(object.getString("student_id"));

                    }
                    else {
                    noticeContent = object.getString("note_content");
                    noticeName = object.getString("note_name");
                    noticeDate = object.getString("note_date");
                    Notice notice = new Notice(noticeContent, noticeName, noticeDate);
                    noticedList.add(notice);
                    adapter.notifyDataSetChanged();
                    }
                    count++;
                }

            }catch (Exception e){
                e.printStackTrace();
            }


        }
    }
}
