import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.IDN;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;

//https://jsoup.org/apidocs/org/jsoup/nodes/Document.html

import java.util.*;

public class crawling {

	public static void main(String[] args) throws Exception{

		//검색 페이징 담는 큐
		Queue<String> stPg = new LinkedList<>();

		//검색 설정
		String searchMain = "https://www.google.com";
		String searchNextUrl = "/search?q=";
		System.out.println( "검색할 단어 입력 : " );

		BufferedReader contentReader = new BufferedReader(new InputStreamReader(System.in));
		String searchContent = contentReader.readLine();

		//String searchContent = "토토 핫";

		String searchPage	 = "&start=";
		stPg.add( searchMain + searchNextUrl + searchContent + searchPage + ( (1 - 1) * 10) );	//카지노

		//제외 단어 리스트			나무위키  위키피디아
		String[] excepWord = { "namu", "wiki" };

		int idx = 2;	//현재 pageNum
		int sn = 1;		//순번

		//파일 생성
		String fileName = "C:" + File.separator + "Users" + File.separator + "ngcc" + File.separator + "Desktop" + File.separator + "검색" + File.separator + searchContent + ".txt";

		File isExist = new File(fileName);
		if (isExist.exists()) {
			System.out.println( "이미 수집한 이력이 있습니다." );
			return;
		}

		try {

			BufferedWriter bw = new BufferedWriter( new FileWriter( fileName, true ) );

			while( !stPg.isEmpty() ) {

				String url = stPg.poll();

				Document doc = Jsoup.connect( url ).get();

				List<String> urlList = new ArrayList<String>();
				List<String> nmList  = new ArrayList<String>();

				List<String> adSiteNmList = doc.getElementsByClass( "v0nnCb" ).eachText();	//광고 사이트 명

				List<String> adSiteUrlList = doc.getElementsByClass("zMz9yb").eachText();	//검색 결과 광고 사이트 URL div 클래스

				//광고 사이트는 따로 뽑아서 출력
				for(int i = 0; i < adSiteUrlList.size(); i++) {
					String[] adSiteUrl = adSiteUrlList.get(i).split("·");

					if( adSiteUrl.length >= 2 ) {
						System.out.println( "{ siteUrl : \"" + adSiteUrl[1].substring( 0 , adSiteUrl[1].indexOf( " 이 " ) ) + "\" , siteNm : \"" + adSiteNmList.get(i) + "\", sn : NumberInt(" + sn + "), searchContent : \"" + searchContent + "\"}," );
						bw.write( sn + " : \t" + adSiteUrl[1].substring( 0 , adSiteUrl[1].indexOf( " 이 " ) ).replace( "\"", "'") + "\t\t\t\t\t" + adSiteNmList.get(i).replace( "\"", "'") + "\n" );
						sn++;
					}
				}

				Elements searchInfoDiv = doc.getElementsByClass("yuRUbf");		//검색 결과 사이트 URL div 클래스
				String[] searchInfoList = searchInfoDiv.toString().split("\n");

				Elements nmEle = doc.getElementsByClass( "DKV0Md" );			//검색 결과 site명 클래스

				//사이트 url 리스트
				for(int i = 0; i < searchInfoList.length; i++) {
					if( searchInfoList[i].contains( "yuRUbf" ) ) {
						String temp = searchInfoList[i+1].split("\"")[1];
						urlList.add( URLDecoder.decode( URLDecoder.decode( temp ) ));
						i++;
					}
				}

				nmList = nmEle.eachText();			//사이트 명 리스트

				boolean isExcepUrl = false;

				for(int i = 0; i < urlList.size(); i++) {
					isExcepUrl = false;

					//제외해야 하는 단어가 껴있으면 제외
					for( String excepword : excepWord ) {
						if( urlList.get( i ).contains( excepword ) ) {
							isExcepUrl = true;
							break;
						}
					}

					if( !isExcepUrl ) {
						bw.write( sn + " : \t" + urlList.get(i) + "\t\t\t\t\t" + nmList.get(i) + "\n" );
						String aTagUrl = urlList.get(i);
						if( aTagUrl.contains( "›" ) ) {
							aTagUrl = aTagUrl.split( "›" )[0];
						}

						System.out.println( "{ siteUrl : \"" + aTagUrl.replace( "\"", "'") + "\", siteNm : \"" + nmList.get(i).replace( "\"", "'") + "\", sn : NumberInt(" + sn + "), searchContent : \"" + searchContent + "\"}," );

						sn++;
					}
				}

				//해당 페이지에서 확인가능한 페이지 리스트 가져오기
				List<String> pageEle = doc.getElementsByClass("fl").eachText();	//해당 페이지의 리스트

				int lastPage = Integer.parseInt( pageEle.get( pageEle.size() - 1 ) );	//해당 페이지의 리스트의 마지막 숫자 = 현재 알 수 있는 마지막 페이지의 숫자

				for( ; idx <= lastPage; idx++ ) {
					stPg.add( searchMain + searchNextUrl + searchContent + searchPage + ( (idx - 1) * 10 ) );//( 현재 페이지 - 1 ) * 10 = 검색 url의 시작 페이지 숫자
				}
			}

			bw.flush();
			bw.close();

		}catch ( Exception e ) {
			e.printStackTrace();
		}

	}
}
