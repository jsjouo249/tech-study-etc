import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class colcWebCrawling {

	public static void main(String[] args) throws Exception {

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		System.out.println( "검색 : 1, 데이터 적재 : 2" );

		int choice = Integer.parseInt( br.readLine() );

		if( null == searchOrInsertWithDB.getMongoDb() ) {
			System.out.println( "Check connect to DB" );

		}else {

			if( choice == 1 ) {

				searchOrInsertWithDB.find();

			}else if( choice == 2 ) {

				System.out.println( "검색할 단어 입력 : " );

				String srchContent = br.readLine();

				/**
				 * siteInfo 클래스
				 * siteUrl			: 사이트 URL
				 * siteTitle		: 사이트 타이틀
				 * searchContent	: 검색 명
				 */
				ArrayList<siteInfo> ll = crawling.makeExcel( srchContent );

				//searchOrInsertWithDB.insertSiteInfo;

			}else {
				System.out.println( "알맞은 숫자" );
			}

		}




	}
}
