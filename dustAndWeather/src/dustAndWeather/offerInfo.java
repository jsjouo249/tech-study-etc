package dustAndWeather;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import org.json.JSONException;

public class offerInfo {

	public static void main(String[] args) throws IOException, JSONException {
		// TODO Auto-generated method stub

		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter( System.out) );

		String home   	= "수지";
		String office 	= "과천동";
		String workTime = "9~18";
		String day		= "20211007";

		List<dust> dustLL = fineDust.dust( home, office, day );

		boolean flag = true;

		for( dust data : dustLL ) {
    		if( flag ) {
    			bw.write( data.msurDt + "\n" );
    		}
    		bw.write( data.msrstnName + "\t\t" + " 미세먼지 : " + data.pm10Value + " / 초 미세먼지" + data.pm25Value + "\n" );
    		flag = !flag;
    	}

		List<weatherData> weatherLL = shortTermWeatherForeCast.weather( day );

		for( weatherData data : weatherLL ) {
    		bw.write( "일자 : " + data.tm + " 시 : " + data.hour + " 위치 : " + data.stnNm + " 기온 : " + data.ta + " 강수량 : " + data.rn + " 풍속 : " + data.ws + " 풍향 : " + data.wd + " 습도 : " + data.hm + "\n");
    	}

		bw.flush();
		bw.close();
	}

}
