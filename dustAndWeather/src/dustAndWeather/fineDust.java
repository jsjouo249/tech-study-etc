package dustAndWeather;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
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

class dust{
	String msurDt;		//측정일
	String msrstnName;	//측정소
	String pm10Value;	//미세먼지 평균농도
	String pm25Value;	//초미세먼지 평균농도

	public dust( String msurDt, String msrstnName, String pm10Value, String pm25Value ) {
		this.msurDt 	= msurDt;
		this.msrstnName = msrstnName;
		this.pm10Value 	= pm10Value;
		this.pm25Value 	= pm25Value;
	}
}

public class fineDust {

	public static List<dust> dust( String home, String office, String day ) throws IOException, JSONException{

		StringBuilder sb = init( day );

		List<dust> ll = jsonParser( new JSONObject( sb.toString() ), home, office );

		return ll;
	}

	private static StringBuilder init( String day ) throws IOException{

		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter( System.out) );

		//최근 3~6개월 데이터
		StringBuilder urlBuilder = new StringBuilder("http://apis.data.go.kr/B552584/ArpltnStatsSvc/getMsrstnAcctoRDyrg"); /*URL*/
        urlBuilder.append("?" + URLEncoder.encode("serviceKey","UTF-8") + "=pg8FRO9oCXu%2FzuZq1nHN2nUdZvuYTRTV%2BMDo0mO5QVdIxtk0A3BNLBl1122bg2uaprneUUn7h6P%2BMdKbZLY5gQ%3D%3D"); /*Service Key*/
        urlBuilder.append("&" + URLEncoder.encode("returnType","UTF-8") + "=" + URLEncoder.encode("json", "UTF-8")); /*xml 또는 json*/
        urlBuilder.append("&" + URLEncoder.encode("numOfRows","UTF-8") 	+ "=" + URLEncoder.encode("17000", "UTF-8")); /*한 페이지 결과 수*/
        urlBuilder.append("&" + URLEncoder.encode("pageNo","UTF-8") 	+ "=" + URLEncoder.encode( "1", "UTF-8")); /*페이지번호*/
        urlBuilder.append("&" + URLEncoder.encode("inqBginDt","UTF-8") 	+ "=" + URLEncoder.encode( day, "UTF-8")); /*조회시작일자*/
        urlBuilder.append("&" + URLEncoder.encode("inqEndDt","UTF-8") 	+ "=" + URLEncoder.encode( day, "UTF-8")); /*조회종료일자*/

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

		return sb;
	}

    public static void main(String[] args) throws IOException, JSONException {

    	BufferedWriter bw = new BufferedWriter(new OutputStreamWriter( System.out) );

		StringBuilder urlBuilder = new StringBuilder("http://apis.data.go.kr/B552584/ArpltnStatsSvc/getMsrstnAcctoRDyrg"); /*URL*/
        urlBuilder.append("?" + URLEncoder.encode("serviceKey","UTF-8") + "=?"); /*Service Key*/
        urlBuilder.append("&" + URLEncoder.encode("returnType","UTF-8") + "=" + URLEncoder.encode("json", "UTF-8")); /*xml 또는 json*/
        urlBuilder.append("&" + URLEncoder.encode("numOfRows","UTF-8") 	+ "=" + URLEncoder.encode("17000", "UTF-8")); /*한 페이지 결과 수*/
        urlBuilder.append("&" + URLEncoder.encode("pageNo","UTF-8") 	+ "=" + URLEncoder.encode( "1", "UTF-8")); /*페이지번호*/
        urlBuilder.append("&" + URLEncoder.encode("inqBginDt","UTF-8") 	+ "=" + URLEncoder.encode("20211007", "UTF-8")); /*조회시작일자*/
        urlBuilder.append("&" + URLEncoder.encode("inqEndDt","UTF-8") 	+ "=" + URLEncoder.encode("20211008", "UTF-8")); /*조회종료일자*/

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

        List<dust> ll = jsonParser( new JSONObject( sb.toString() ), "수지", "과천동" );

        boolean flag = true;

        System.out.println( sb );

    	for( dust data : ll ) {
    		if( flag ) {
    			bw.write( data.msurDt + "\n" );
    		}
    		bw.write( data.msrstnName + "\t\t" + " 미세먼지 : " + data.pm10Value + " / 초 미세먼지" + data.pm25Value + "\n" );
    		flag = !flag;
    	}

    	bw.flush();
    	bw.close();

    }

    private static List<dust> jsonParser( JSONObject result, String home, String office ) throws JSONException{

    	List<dust> ll = new ArrayList<>();

        JSONObject response = (JSONObject) result.get("response");
        JSONObject body 	= (JSONObject) response.get("body");
        JSONArray items		= (JSONArray) body.get("items");

    	for( int i = 0; i < items.length(); i++ ) {

        	String temp = items.getString( i );
        	temp = temp.substring( 1, temp.length() - 1 ).replace("\"", "");

        	if( temp.contains( home ) || temp.contains( office ) ) {

        		StringTokenizer cut = new StringTokenizer( temp, "," );

    			String[] dustData = new String[4];

        		while( cut.hasMoreTokens() ) {

        			String[] data = cut.nextToken().split(":");

        			if( "msurDt".equals( data[0] ) ) {
        				dustData[0] = data[1];
        			}else if( "msrstnName".equals( data[0] ) ) {
        				dustData[1] = data[1];
        			}else if( "pm10Value".equals( data[0] ) ) {
        				dustData[2] = data[1];
        			}else if( "pm25Value".equals( data[0] ) ) {
        				dustData[3] = data[1];
        			}
        		}

        		ll.add( new dust( dustData[0], dustData[1], dustData[2], dustData[3] ) );
        	}

        }

    	return ll;
    }
}
