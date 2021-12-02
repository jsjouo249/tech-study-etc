package dustAndWeather;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

class weatherData{
	String tm;			//시간
	String stnNm;		//위치
	String ta;			//기온
	String rn;			//강수량
	String ws;			//풍속
	String wd;			//풍향
	String hm;			//습도

	public weatherData(String tm, String stnNm, String ta, String rn, String ws, String wd, String hm ) {
		this.tm 	= tm;
		this.stnNm 	= stnNm;
		this.ta 	= ta;

		if( null == rn ){
			this.rn = "";
		}else {
			this.rn = rn;
		}
		if( null == ws ){
			this.ws = "";
		}else {
			this.ws	= ws;
		}
		if( null == wd ){
			this.wd = "";
		}else {
			this.wd	= wd;
		}
		if( null == hm ){
			this.hm = "";
		}else {
			this.hm	= hm;
		}
	}
}

public class shortTermWeatherForeCast {

	static List<weatherData> ll = new ArrayList<>();

    public static void main(String[] args) throws IOException, JSONException {

    	BufferedWriter bw = new BufferedWriter(new OutputStreamWriter( System.out) );

        StringBuilder urlBuilder = new StringBuilder("http://apis.data.go.kr/1360000/AsosHourlyInfoService/getWthrDataList"); /*URL*/
        urlBuilder.append("?" + URLEncoder.encode("serviceKey","UTF-8") + "=pg8FRO9oCXu%2FzuZq1nHN2nUdZvuYTRTV%2BMDo0mO5QVdIxtk0A3BNLBl1122bg2uaprneUUn7h6P%2BMdKbZLY5gQ%3D%3D"); /*Service Key*/
        urlBuilder.append("&" + URLEncoder.encode("pageNo","UTF-8") 	+ "=" + URLEncoder.encode("1", "UTF-8")); /*페이지번호 Default : 10*/
        urlBuilder.append("&" + URLEncoder.encode("numOfRows","UTF-8") 	+ "=" + URLEncoder.encode("999", "UTF-8")); /*한 페이지 결과 수 Default : 1*/
        urlBuilder.append("&" + URLEncoder.encode("dataType","UTF-8") 	+ "=" + URLEncoder.encode("json", "UTF-8")); /*요청자료형식(XML/JSON) Default : XML*/
        urlBuilder.append("&" + URLEncoder.encode("dataCd","UTF-8") 	+ "=" + URLEncoder.encode("ASOS", "UTF-8")); /*자료 분류 코드(ASOS)*/
        urlBuilder.append("&" + URLEncoder.encode("dateCd","UTF-8") 	+ "=" + URLEncoder.encode("HR", "UTF-8")); /*날짜 분류 코드(HR)*/
        urlBuilder.append("&" + URLEncoder.encode("startDt","UTF-8") 	+ "=" + URLEncoder.encode("20100101", "UTF-8")); /*조회 기간 시작일(YYYYMMDD)*/
        urlBuilder.append("&" + URLEncoder.encode("startHh","UTF-8") 	+ "=" + URLEncoder.encode("01", "UTF-8")); /*조회 기간 시작시(HH)*/
        urlBuilder.append("&" + URLEncoder.encode("endDt","UTF-8") 		+ "=" + URLEncoder.encode("20100110", "UTF-8")); /*조회 기간 종료일(YYYYMMDD) (전일(D-1) 까지 제공)*/
        urlBuilder.append("&" + URLEncoder.encode("endHh","UTF-8") 		+ "=" + URLEncoder.encode("01", "UTF-8")); /*조회 기간 종료시(HH)*/
        urlBuilder.append("&" + URLEncoder.encode("stnIds","UTF-8") 	+ "=" + URLEncoder.encode("108", "UTF-8")); /*종관기상관측 지점 번호 (108 - 서울)*/

        URL url = new URL(urlBuilder.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-type", "application/json");

        BufferedReader rd;

        if(conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        } else {
            rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        }

        StringBuilder sb = new StringBuilder();
        String line;

        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }

        rd.close();
        conn.disconnect();

        jsonParser( new JSONObject( sb.toString() ) );

        System.out.println( sb.toString() );

        for( weatherData data : ll ) {
    		bw.write( "시간 : " + data.tm + " 위치 : " + data.stnNm + " 기온 : " + data.ta + " 강수량 : " + data.rn + " 풍속 : " + data.ws + " 풍향 : " + data.wd + " 습도 : " + data.hm + "\n");
    	}

        bw.flush();
        bw.close();

	}

    private static void jsonParser( JSONObject result ) throws JSONException{

        JSONObject response = (JSONObject) result.get("response");
        JSONObject body 	= (JSONObject) response.get("body");
        JSONObject items	= (JSONObject) body.get("items");
        JSONArray item		= (JSONArray) items.get("item");

    	for( int i = 0; i < item.length(); i++ ) {

        	String temp = item.getString( i );
        	temp = temp.substring( 1, temp.length() - 1 ).replace("\"", "");

        	StringTokenizer cut = new StringTokenizer( temp, "," );

        	String[] weatherDataOfTime = new String[7];

        	while( cut.hasMoreTokens() ) {

        		String dataInfo = cut.nextToken();
    			String[] data = dataInfo.split(":");

    			if( data.length >= 2 ) {
    				if( "tm".equals( data[0] ) ) {
            			weatherDataOfTime[0] = data[1];

            		}else if( "stnNm".equals( data[0] ) ) {
            			weatherDataOfTime[1] = data[1];

            		}else if( "ta".equals( data[0] ) ) {
            			weatherDataOfTime[2] = data[1];

            		}else if( "rn".equals( data[0] ) ) {
            			weatherDataOfTime[3] = data[1];

            		}else if( "ws".equals( data[0] ) ) {
            			weatherDataOfTime[4] = data[1];

            		}else if( "wd".equals( data[0] ) ) {
            			weatherDataOfTime[5] = data[1];

            		}else if( "hm".equals( data[0] ) ) {
            			weatherDataOfTime[6] = data[1];

            		}
    			}
        	}

        	ll.add( new weatherData( weatherDataOfTime[0] , weatherDataOfTime[1], weatherDataOfTime[2], weatherDataOfTime[3], weatherDataOfTime[4], weatherDataOfTime[5], weatherDataOfTime[6]) );
        }
    }
}
