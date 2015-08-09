package app.sabo.com.totoroweather.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import app.sabo.com.totoroweather.model.City;
import app.sabo.com.totoroweather.model.County;
import app.sabo.com.totoroweather.model.Province;

/**
 * Created by Administrator on 2015/8/8.
 */
public class WeatherDB {

    /**
     * 数据库名
     */

    public static final String DB_NAME = "totoro_weather";

    /**
     * 数据库版本
     */

    public static final  int VERSION = 1;

    private static  WeatherDB totoroWeatherDB;
    private SQLiteDatabase db;

    /**
     * 将构造方法私有化,不对外提供实例化
     */

    private  WeatherDB (Context context){
        WeatherOpenHelper dbHelper = new WeatherOpenHelper(context,DB_NAME,null,VERSION);
        db = dbHelper.getWritableDatabase();

    }
    /**
     * 获取WeatherDB的实例
     */
    public synchronized static  WeatherDB getInstance(Context context){
        if (totoroWeatherDB == null)
        totoroWeatherDB = new WeatherDB(context);

        return totoroWeatherDB;
    }

    /**
     * 将Province实例储存到数据库
     */

    public void saveProvince(Province province){

        if (province != null) {
            ContentValues values = new ContentValues();
            values.put("province_name",province.getProvinceName());
            values.put("province_code",province.getProvinceCode());
            db.insert("Province",null,values);

        }
    }

    /**
     * 将City实例储存到数据库
     */

    public void saveCity(City city){
        if (city != null) {
            ContentValues values = new ContentValues();
            values.put("city_name",city.getCityName());
            values.put("city_code",city.getCityCode());
            values.put("province_id",city.getProvinceId());
            db.insert("City",null,values);
        }
    }

    /**
     *将County实例存储到数据库
     */
    public void saveCounty(County county){
        if (county != null) {
            ContentValues values = new ContentValues();
            values.put("County_name",county.getCountyName());
            values.put("County_code",county.getCountyCode());
            values.put("city_id",county.getCityId());
            db.insert("City",null,values);
        }
    }

    /**
     * 从数据库中读取全国的所有的省份信息
     */
    public List<Province> loadProvinces(){
        List<Province> list = new ArrayList<Province>();
        Cursor cursor =  db.query("Province", null, null, null, null, null, null);
        if (cursor.moveToFirst()){
            while (cursor.moveToNext()){
                Province province = new Province();
                province.setId(cursor.getInt(cursor.getColumnIndex("id")));
                province.setProvinceName(cursor.getString(cursor.getColumnIndex("provinceName")));
                province.setProvinceCode(cursor.getString(cursor.getColumnIndex("provinceCode")));
                list.add(province);
            }

        }

        return  list;
    }

    /**
     * 从数据库中获取某个省份下所有的城市信息
     */

    public List<City> loadCity(int provinceId){
        List<City> list = new ArrayList<City>();
        Cursor cursor = db.query("City",null,"province_id = ?",new String[]{String.valueOf(provinceId)},null,null,null);
        if (cursor.moveToFirst()){
            while (cursor.moveToNext()){
                City city = new City();
                city.setId(cursor.getInt(cursor.getColumnIndex("id")));
                city.setCityName(cursor.getString(cursor.getColumnIndex("city_name")));
                city.setCityCode(cursor.getString(cursor.getColumnIndex("city_code")));
                city.setProvinceId(provinceId);
                list.add(city);
            }
        }

        return  list;
    }

    /**
     *获取某个城市下所有的县的信息
     */
    public List<County> loadCounty(int cityId){
        List<County> list = new ArrayList<County>();
        Cursor cursor = db.query("County",null,"city_id = ?",new String[]{String.valueOf(cityId)},null,null,null);
        if (cursor.moveToFirst()){
            while (cursor.moveToNext()){
                County county = new County();
                county.setId(cursor.getInt(cursor.getColumnIndex("id")));
                county.setCountyName(cursor.getString(cursor.getColumnIndex("county_name")));
                county.setCountyCode(cursor.getString(cursor.getColumnIndex("county_code")));
                county.setCityId(cityId);
                list.add(county);

            }
        }
        return  list;
    }
 }















