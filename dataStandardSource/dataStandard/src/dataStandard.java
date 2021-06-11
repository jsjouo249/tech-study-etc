import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class dataStandard {

	public static void main(String[] args) throws Exception {

		Scanner input = new Scanner(System.in);

		//엑셀의 도메인 정의서 정보
		HashMap<String, domainInfo> domainInfo = domain.domainInit();

		//엑셀의 표준단어 정의서 정보
		HashMap<String, String> standardInfo = standardWord.wordInit();

		System.out.println( "1 = 엑셀 데이터 표준화 진행, 2 = 컬럼 생성" );
		String str = input.nextLine();

		//1이면 엑셀 데이터표준화 진행, 2면 컬럼 만들기
		if( "1".equals( str ) ) {
			makeExcel( domainInfo, standardInfo );
		}else {

			System.out.println( "만드실 컬럼의 한글명을 입력해주세요. (종료 : 끝)" );

			str = input.nextLine();

			System.out.println( makeColumnNm( str , domainInfo, standardInfo) );
		}


	}

	//한글의 컬럼명을 받으면 표준단어정의서와 도메인정의서를 통해 컬럼명 생성
	public static String makeColumnNm( String columnName, HashMap<String, domainInfo> domainInfo, HashMap<String, String> standardInfo ) {

		String[] split = columnName.split(" ");

		StringBuilder sb = new StringBuilder();

		for(int i = 0; i < split.length; i++) {
			String temp = standardTerm.makeColumnNm( standardInfo, split[i]);

			if( "".equals( temp ) ) {
				System.out.println( split[i] + " : 표준단어 정의서에 부합하지 않는 단어입니다." );
				return "";
			}

			sb.append( temp );

			if( i < split.length - 1 ) {
				sb.append( "_" );
			}

		}

		return sb.toString();
	}

	//표준용어정의서 생성
	public static void makeExcel( HashMap<String, domainInfo> domainInfo, HashMap<String, String> standardInfo ) throws Exception {
		HashMap<String, standardTermInfo> termHm = standardTerm.makeStandardTerm( domainInfo, standardInfo );
		standardTerm.makeExcel( termHm );
	}
}
