package app.sabo.com.totoroweather.activity;




import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import app.sabo.com.totoroweather.R;
import app.sabo.com.totoroweather.db.WeatherDB;
import app.sabo.com.totoroweather.model.City;
import app.sabo.com.totoroweather.model.County;
import app.sabo.com.totoroweather.model.Province;
import app.sabo.com.totoroweather.util.AnalyzeDataUtil;
import app.sabo.com.totoroweather.util.HttpUtil;

/**
 * Created by Administrator on 2015/8/10.
 */
public class ChooseAreaActivity extends Activity {


    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;

    private ProgressDialog progressDialog;

    private TextView titleText;
    private ListView listView;

    private ArrayAdapter<String> adapter;

    private WeatherDB weatherDB;

    private List<String> dataList = new ArrayList<String>();

    /**
     * 省列表
     */
    private List<Province> provinceList;

    /**
     * 市列表
     */
    private List<City> cityList;

    /**
     * 县列表
     */
    private List<County> countyList;


    /**
     * 选中的省份
     */
    private Province selectedProvince;


    /**
     * 选中的城市
     */
    private City selectedCity;


    /**
     * 当前选中的级别
     */
    private int currentLevel;


    @Override

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.getBoolean("city_selected",false)){

            Intent intent = new Intent(this,WeatherActivity.class);
            startActivity(intent);

            finish();
            return;
        }
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.choose_area);
        //、、、、、、、、、、、、、、、
        weatherDB = WeatherDB.getInstance(this);

        titleText = (TextView) findViewById(R.id.title_text);
        listView = (ListView) findViewById(R.id.list_view);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (currentLevel == LEVEL_PROVINCE) {
                    selectedProvince = provinceList.get(position);
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    selectedCity = cityList.get(position);
                    queryCounties();
                }else if (currentLevel == LEVEL_COUNTY){
                    String countyCode = countyList.get(position).getCountyCode();
                    Intent intent = new Intent(ChooseAreaActivity.this,WeatherActivity.class);
                    intent.putExtra("county_code",countyCode);
                    startActivity(intent);
                    finish();
                }
            }


        });
        queryProvinces();//加载省级数据


    }

    /**
     * 查询全国所用省份，优先从数据库查询，如果没有再从服务器查询
     */
    private void queryProvinces() {
        provinceList = weatherDB.loadProvinces();
        if (provinceList.size() > 0) {
            //每次查询时都要清空原先dataList中的数据
            dataList.clear();
            for (Province province : provinceList
                    ) {
                dataList.add(province.getProvinceName());
            }
            //刷新数据
            adapter.notifyDataSetChanged();

            listView.setSelection(0);
            titleText.setText(R.string.china);

            currentLevel = LEVEL_PROVINCE;
        } else {
            queryFromServer(null, "province");
        }
    }


    /**
     * 查询省内所有城市,优先从数据库查询，如果没有再从服务器查询
     */

    private void queryCities() {
        cityList = weatherDB.loadCities(selectedProvince.getId());

        if (cityList.size() > 0) {
            dataList.clear();
            for (City city : cityList) {
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText(selectedProvince.getProvinceName());
            currentLevel = LEVEL_CITY;
        } else {
            queryFromServer(selectedProvince.getProvinceCode(), "city");
        }

    }

    private void queryCounties() {
        countyList = weatherDB.loadCounties(selectedCity.getId());

        if (countyList.size() > 0) {
            dataList.clear();
            for (County county : countyList
                    ) {
                dataList.add(county.getCountyName());

            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText(selectedCity.getCityName());
            currentLevel = LEVEL_COUNTY;
        } else {
            queryFromServer(selectedCity.getCityCode(), "county");
        }


    }

    /**
     * 根据传入的代号和类型从服务器上查询省市县的数据
     * @param code
     * @param type
     */
    private void queryFromServer(final String code, final String type) {

        String address;
        if (!TextUtils.isEmpty(code)){
            address = "http://www.weather.com.cn/data/list3/city"+code+".xml";
        }else {
            address = "http://www.weather.com.cn/data.list3.city.xml";
        }
        showProgressDialog();
        HttpUtil.sendHttpRequest(address, new HttpUtil.HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                boolean result = false;

                if ("province".equals(type)){
                    result = AnalyzeDataUtil.handleProvincesResponse(weatherDB,response);
                }
                else if ("city".equals(type)){
                    result = AnalyzeDataUtil.handleCitiesResponse(weatherDB,response,selectedProvince.getId());
                }
                else if ("county".equals(type)){
                    result = AnalyzeDataUtil.handleCountiesResponse(weatherDB,response,selectedCity.getId());

                }
                if (result){
                    //通过runOnUiThread()方法回到主线程处理逻辑
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("province".equals(type)){
                                queryProvinces();
                            }else if ("city".equals(type)){
                                queryCities();
                            }else if ("county".equals(type)){
                                queryCounties();
                            }
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {

//                通过runOnUiThread()方法回到主线程处理逻辑
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(ChooseAreaActivity.this,"加载失败",Toast.LENGTH_SHORT).show();

                    }
                });
            }
        });

    }

    private void showProgressDialog(){
        if (progressDialog == null){
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("正在加载。。。");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }
    private void closeProgressDialog(){
        if (progressDialog != null){
            progressDialog.dismiss();
        }
    }

    public void onBackPressed(){
        if (currentLevel == LEVEL_COUNTY){
            queryCities();
        }else if (currentLevel == LEVEL_CITY){
            queryProvinces();
        }
        else {
            finish();
        }
    }

}
