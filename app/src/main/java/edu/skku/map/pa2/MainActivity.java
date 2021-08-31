package edu.skku.map.pa2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.net.UrlQuerySanitizer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Html;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static edu.skku.map.pa2.MyGridViewAdapter.gridImages;


public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE = 10;

    String clientId = "qTM3fVbgB3ZeQqPTxaJI";
    String clientSecret = "MgLs6PsVXY";
    EditText _text;
    String text;
    Button btn1, btn2;
    GridView gridView,gridView2,gridView3;
    static public int count;
    static boolean check_img=true;
    static Bitmap img;
    static public ArrayList<Bitmap> imgPic = new ArrayList<>();
    static public int Ans[] = new int[400];//정답배열
    static public int MyAns[] = new int[400];//내가 맞춘 배열
    static public ArrayList<Integer> leftNum = new ArrayList<>();//왼쪽 숫자 배열
    static public ArrayList<Integer> topNum = new ArrayList<>();//상단 숫자 배열
    static public ArrayList<Bitmap> img_one_d = new ArrayList<>();
    static public String responseBody;
    static public String pic_link;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn1 = (Button) findViewById(R.id.button);
        btn2 = (Button) findViewById(R.id.button2);
        _text = (EditText) findViewById(R.id.searchText);
        gridView = (GridView) findViewById(R.id.gridview);
        gridView2 = (GridView) findViewById(R.id.gridview2);
        gridView3 = (GridView) findViewById(R.id.gridview3);

//        int[] arr={1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1};
//        int[] a=GetBlockNumber(arr);
//        System.out.println("배열길이:" +a.length);
//        for(int i=0;i<a.length;i++)
//            System.out.println(a[i]);
        //검색버튼
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                text = _text.getText().toString();
                try {
                    text = URLEncoder.encode(text, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException("검색어 인코딩 실패", e);
                }
                new Thread() {
                    public void run() {
                        String apiURL = "https://openapi.naver.com/v1/search/image?query=" + text + "&display=1&start=1";
                        Map<String, String> requestHeaders = new HashMap<>();
                        requestHeaders.put("X-Naver-Client-Id", clientId);
                        requestHeaders.put("X-Naver-Client-Secret", clientSecret);
                        responseBody = get(apiURL, requestHeaders);
                        // System.out.println(responseBody);
                        //파싱
                        JsonParser parser = new JsonParser();
                        Object object = parser.parse(responseBody);
                        JsonObject Json_obj = (JsonObject) object;
                        JsonArray array = (JsonArray) Json_obj.get("items");
                        for (int i = 0; i < array.size(); i++) {
                            JsonObject obj = (JsonObject) array.get(i);
                            pic_link = obj.get("link").toString();
                        }
                        pic_link = pic_link.substring(1, pic_link.length() - 1);
                        System.out.println(pic_link);
                        new DownloadFilesTask().execute(pic_link);
                    }
                }.start();
            }
        });
        //갤러리 버튼
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                count = 0;
                startActivityForResult(intent, REQUEST_CODE);
                if(img_one_d.isEmpty())
                    check_img = true;
                else {
                    img_one_d.clear();
                    check_img = true;
                }
            }
        });
        //그리드뷰 눌렀을 때
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int pos =position;
                if(Ans[pos]==1){
                    img_one_d.set(position,Black(img_one_d.get(position)));
                    gridView.setAdapter(new MyGridViewAdapter(getApplicationContext(), img_one_d));
                    MyAns[pos]=1;
                }
                else{
                    for(int j=0;j<400;j++) {
                        img_one_d.set(j, White(img_one_d.get(j)));
                        MyAns[j]=0;
                    }
                    gridView.setAdapter(new MyGridViewAdapter(getApplicationContext(), img_one_d));
                    Toast.makeText(getApplicationContext(),"WRONG!", Toast.LENGTH_SHORT).show();
                }
                if(SolveCheck()==1)
                    Toast.makeText(getApplicationContext(),"FINISH!", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                try {
                    InputStream in = getContentResolver().openInputStream(data.getData());
                    img = BitmapFactory.decodeStream(in);
                    in.close();
                    if(check_img == true)
                    {
                        Get_Gridview(img);
                        check_img = false;
                    }
                    MakeLeftNum();
                    gridView3.setAdapter(new MyGridNum1(this, R.layout.num_layout, leftNum));
                    MakeTopNum();
                    gridView2.setAdapter(new MyGridNum1(this, R.layout.num_layout, topNum));

                } catch (Exception e) {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "사진 선택 취소", Toast.LENGTH_LONG).show();
            }
        }
    }
    private class DownloadFilesTask extends AsyncTask<String,Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... strings) {
            Bitmap bmp = null;
            try {
                String img_url = strings[0]; //url of the image
                URL url = new URL(img_url);
                bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bmp;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected void onPostExecute(Bitmap result) {
            img_one_d.clear();
            imgPic.clear();
            count=0;
            Get_Gridview(result);
            MakeLeftNum();
            gridView3.setAdapter(new MyGridNum1(getApplicationContext(), R.layout.num_layout, leftNum));
            MakeTopNum();
            gridView2.setAdapter(new MyGridNum1(getApplicationContext(), R.layout.num_layout, topNum));
        }
    }
    //다맞았는지 확인
    public int SolveCheck(){
        int c=1;
        for(int i=0;i<400;i++)
            if(Ans[i]!=MyAns[i])
                c=0;
        return c;
    }

    //그리드뷰 잘라서 만들기
    private void Get_Gridview(Bitmap img) {
        Matrix matrix = new Matrix();
        matrix.preScale(1.0f, 1.0f);
        img = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, false);
        img = Bitmap.createScaledBitmap(img, 200, 200, true);
        img = BlackWhitePhoto(img);
        int w = img.getWidth() / 20;
        int h = img.getHeight() / 20;
        int k = 0;

        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 20; j++) {
                imgPic.add(BlockColor(Bitmap.createBitmap(img, j * w, i * h, w, h)));
            }
        }
        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 20; j++) {
                img_one_d = imgPic;
                k = k + 1;
            }
        }
        gridView.setAdapter(new MyGridViewAdapter(this, img_one_d));
    }

    //네이버 검색 api 이미지 가져오기
    private static String get(String apiUrl, Map<String, String> requestHeaders) {
        HttpURLConnection con = connect(apiUrl);
        try {
            con.setRequestMethod("GET");
            for (Map.Entry<String, String> header : requestHeaders.entrySet()) {
                con.setRequestProperty(header.getKey(), header.getValue());
            }
            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) { // 정상 호출
                return readBody(con.getInputStream());
            } else { // 에러 발생
                return readBody(con.getErrorStream());
            }
        } catch (IOException e) {
            throw new RuntimeException("API 요청과 응답 실패", e);
        } finally {
            con.disconnect();
        }
    }

    private static HttpURLConnection connect(String apiUrl) {
        try {
            URL url = new URL(apiUrl);
            return (HttpURLConnection) url.openConnection();
        } catch (MalformedURLException e) {
            throw new RuntimeException("API URL이 잘못되었습니다. : " + apiUrl, e);
        } catch (IOException e) {
            throw new RuntimeException("연결이 실패했습니다. : " + apiUrl, e);
        }
    }

    private static String readBody(InputStream body) {
        InputStreamReader streamReader = new InputStreamReader(body);

        try (BufferedReader lineReader = new BufferedReader(streamReader)) {
            StringBuilder responseBody = new StringBuilder();
            String line;
            while ((line = lineReader.readLine()) != null) {
                responseBody.append(line);
            }
            return responseBody.toString();
        } catch (IOException e) {
            throw new RuntimeException("API 응답을 읽는데 실패했습니다.", e);
        }
    }

    //사진을 검정/흰색으로만 만들어줌 (if grayscale value>128 ->black / else white)
    private Bitmap BlackWhitePhoto(Bitmap img) {
        int w = img.getWidth();
        int h = img.getHeight();
        Bitmap GrayScale = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        int A, R, G, B;
        int pixel_color, gray;
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                pixel_color = img.getPixel(i, j);
                A = Color.alpha(pixel_color);
                R = Color.red(pixel_color);
                G = Color.green(pixel_color);
                B = Color.blue(pixel_color);
                gray = (int) (0.2989 * R + 0.5870 * G + 0.1140 * B);
                if (gray > 128)
                    gray = 0;
                else
                    gray = 255;
                GrayScale.setPixel(i, j, Color.argb(A, gray, gray, gray));
            }
        }
        return GrayScale;
    }

    //블럭의 average grayscale>128이면 all black, 아니면 all white
    private Bitmap BlockColor(Bitmap img) {
        int w = img.getWidth();
        int h = img.getHeight();
        Bitmap GrayScale = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_4444);
        int R, G, B;
        int pixel_color, gray, total = 0;
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                pixel_color = img.getPixel(i, j);
                R = Color.red(pixel_color);
                G = Color.green(pixel_color);
                B = Color.blue(pixel_color);
                gray = (int) (0.2989 * R + 0.5870 * G + 0.1140 * B);
                total += gray;

                GrayScale.setPixel(i, j, Color.argb(255, gray, gray, gray));
            }
        }
        int average = total / (w * h);
        if (average > 128) {//검정
            for (int i = 0; i < w; i++)
                for (int j = 0; j < h; j++)
                    GrayScale.setPixel(i, j, Color.rgb( 255,255,255));
                    //GrayScale.setPixel(i, j, Color.rgb(0,0,0));
            Ans[count] = 1;
        //    System.out.println("Ans["+count+"]:"+Ans[count]);
            count += 1;
        } else {//흰색
            for (int i = 0; i < w; i++)
                for (int j = 0; j < h; j++)
                    GrayScale.setPixel(i, j, Color.rgb( 255, 255, 255));
            Ans[count] = 0;
            count += 1;
        }
        return GrayScale;
    }
    public Bitmap Black(Bitmap img){
        int w = img.getWidth();
        int h = img.getHeight();
        Bitmap GrayScale = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_4444);
        for (int i = 0; i < w; i++)
            for (int j = 0; j < h; j++)
                GrayScale.setPixel(i, j, Color.rgb(0,0,0));
        return GrayScale;
    }
    public Bitmap White(Bitmap img){
        int w = img.getWidth();
        int h = img.getHeight();
        Bitmap GrayScale = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_4444);
        for (int i = 0; i < w; i++)
            for (int j = 0; j < h; j++)
                GrayScale.setPixel(i, j, Color.rgb(255,255,255));
        return GrayScale;
    }

    public int[] GetBlockNumber(int[] arr) {
        int len = arr.length;
        int[] zero_index = new int[len+2];
        int z=1,k=0;
        int[] ans = new int[len];
        for(int i=0;i<len;i++){
            if(arr[i]==0){
                zero_index[z]=i+1;
                z+=1;
            }
        }
        zero_index[z]=21;
        for(int i=0;i<z;i++){
            if(zero_index[i+1]-zero_index[i]>1){
                ans[k]=zero_index[i+1]-zero_index[i]-1;
                k+=1;
            }
            else
                continue;
        }
        int[] Answer = new int[k];
        for(int j=0;j<k;j++)
            Answer[j]=ans[j];
        return Answer;
    }
    //arr배열의 n열
    public int[] GetColumn( int n){
        int r[] = new int[20];
        int j=0;
        for(int i=0;i<20;i++){
            r[j]=Ans[n-1+20*i];
            j+=1;
        }
        return r;
    }
    //arr배열의 n행
    public int[] GetRow(int n){
        int[] r = new int[20];
        int j=0;
        for(int i=0;i<20;i++) {
            r[j]=Ans[20*(n-1)+i];
     //       System.out.println("r["+j+"]"+r[j]);
            j+=1;
        }
        return r;
    }
    public void MakeLeftNum(){
        if(!leftNum.isEmpty())
            leftNum.clear();
        for(int j=0;j<80;j++)
            leftNum.add(0);
        for(int i=0;i<20;i++){
            int r[] = GetBlockNumber(GetRow(i+1)) ;
            if(r.length == 0){
                for(int j=0;j<4;j++)
                    leftNum.set(i*4+j,0);
            }
            else if(r.length==1){
                leftNum.set(i*4,0);
                leftNum.set(i*4+1,0);
                leftNum.set(i*4+2,0);
                leftNum.set(i*4+3,r[0]);
            }
            else if(r.length==2){
                leftNum.set(i*4,0);
                leftNum.set(i*4+1,0);
                leftNum.set(i*4+2,r[0]);
                leftNum.set(i*4+3,r[1]);
            }
            else if(r.length==3){
                leftNum.set(i*4,0);
                leftNum.set(i*4+1,r[0]);
                leftNum.set(i*4+2,r[1]);
                leftNum.set(i*4+3,r[2]);
            }
            else if(r.length==4){
                leftNum.set(i*4,r[0]);
                leftNum.set(i*4+1,r[1]);
                leftNum.set(i*4+2,r[2]);
                leftNum.set(i*4+3,r[3]);
            }
        }

    }
    public void MakeTopNum(){
        if(!topNum.isEmpty())
            topNum.clear();
        for(int j=0;j<80;j++)
            topNum.add(0);
        for(int i=0;i<20;i++){
            int r[] = GetBlockNumber(GetColumn(i+1));
            System.out.println();
            if(r.length == 0){
                topNum.set(i,0);
                topNum.set(i+20,0);
                topNum.set(i+40,0);
                topNum.set(i+60,0);
            }
            else if(r.length==1){
                topNum.set(i,0);
                topNum.set(i+20,0);
                topNum.set(i+40,0);
                topNum.set(i+60,r[0]);
            }
            else if(r.length==2){
                topNum.set(i,0);
                topNum.set(i+20,0);
                topNum.set(i+40,r[0]);
                topNum.set(i+60,r[1]);
            }
            else if(r.length==3){
                topNum.set(i,0);
                topNum.set(i+20,r[0]);
                topNum.set(i+40,r[1]);
                topNum.set(i+60,r[2]);
            }
            else if(r.length==4){
                topNum.set(i,r[0]);
                topNum.set(i+20,r[1]);
                topNum.set(i+40,r[2]);
                topNum.set(i+60,r[3]);
            }
        }
    }
}