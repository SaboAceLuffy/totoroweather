package app.sabo.com.totoroweather.util;

import android.text.TextUtils;

import app.sabo.com.totoroweather.db.WeatherDB;
import app.sabo.com.totoroweather.model.City;
import app.sabo.com.totoroweather.model.County;
import app.sabo.com.totoroweather.model.Province;

/**
 * Created by Administrator on 2015/8/10.
 */
public class AnalyzeDataUtil {

    /**
     * 解析和处理从服务器中返回的“省份”数据
     */
    public synchronized static boolean handleProvincesResponse(WeatherDB weatherDB,String response){

        if(!TextUtils.isEmpty(response)){
            String [] allProvinces = response.split(",");
            if (allProvinces != null && allProvinces.length >0){
                for (String p : allProvinces) {

                    String [] array = p.split("\\|");
                    Province province = new Province();
                    province.setProvinceCode(array[0]);
                    province.setProvinceName(array[1]);
                    //将解析出来的数据储存到Province表中
                    weatherDB.saveProvince(province);
                }
                return  true;
            }

        }
        return false;
    }

    /**
     * 解析并处理从服务器中返回的“市级”数据
     */
    public  static synchronized boolean handleCitiesResponse(WeatherDB weatherDB,String response,int provinceId){

        if (!TextUtils.isEmpty(response)){
            String [] allCities = response.split(",");
            if (allCities != null && allCities.length >0){
                for(String  c : allCities){
                    String [] array = c.split("\\|");
                    City city = new City();
                    city.setCityCode(array[0]);
                    city.setCityName(array[1]);
                    city.setProvinceId(provinceId);
                    //将解析出来的数据储存到City表中
                    weatherDB.saveCity(city);
                }
                return  true;
            
            }
        }
        return false;
    }
    public static synchronized boolean handleCountiesResponse(WeatherDB weatherDB,String response,int cityId){

        if (!TextUtils.isEmpty(response)){
            String [] allCounties = response.split(",");
            if (allCounties != null && allCounties.length >0){
                for(String  c : allCounties){
                    String [] array = c.split("\\|");
                    County county = new County();
                    county.setCountyCode(array[0]);
                    county.setCountyName(array[1]);
                    county.setCityId(cityId);
                    //将解析出来的数据储存到county表中
                    weatherDB.saveCounty(county);
                }
                return  true;

            }
        }
        return false;
    }



}
