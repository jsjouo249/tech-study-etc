import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;

public class colcWebCrawling {

	public static void main(String[] args) throws Exception {

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		System.out.println( "검색 : 1, 데이터 적재 : 2" );

		int choice = Integer.parseInt( br.readLine() );

		//getMongoCollection으로 db연동 및 해당 collection 확인
		if( null != searchOrInsertWithDB.getMongoCollection() ) {
			if( choice == 1 ) {

				System.out.println( "검색할 명령어 입력" );

				String srchContent = br.readLine();

				//검색
				searchOrInsertWithDB.find( srchContent );

			}else if( choice == 2 ) {
				//데이터 적재
				System.out.println( "데이터 적재 할 검색어 입력 : " );

				String srchContent = br.readLine();

				/**
				 * siteInfo 클래스
				 * siteUrl			: 사이트 URL
				 * siteTitle		: 사이트 타이틀
				 * siteExplain		: 사이트 설명
				 * searchContent	: 검색 명
				 *
				 * return ArrayList
				 * 이미 검색한 이력이 있으면 return null;
				 */
				HashSet<siteInfo> siteInfoHs = crawling.makeExcel( srchContent );

				if( null != siteInfoHs ) {
					searchOrInsertWithDB.insert( siteInfoHs );
				}

			}else {
				System.out.println( "알맞은 숫자" );
			}
		}

	}
}
