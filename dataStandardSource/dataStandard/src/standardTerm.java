import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

class standardTermInfo {
	String columnNm;
	String columnEngNm;
	String domainNm;
	String domainEngNm;
	String dataType;
	String dataLength;

	public standardTermInfo( String columnNm, String columnEngNm, String domainNm,	String domainEngNm, String dataType, String dataLength ) {
		this.columnNm	 = columnNm;
		this.columnEngNm = columnEngNm;
		this.domainNm	 = domainNm;
		this.domainEngNm = domainEngNm;
		this.dataType	 = dataType;
		this.dataLength	 = dataLength;
	}

}

public class standardTerm {

	public static HashMap<String, standardTermInfo> makeStandardTerm( HashMap<String, domainInfo> domainInfo, HashMap<String, String> standardInfo ) throws Exception{

		HashMap<String, standardTermInfo> termHm = new HashMap<>();

		FileInputStream file = new FileInputStream( "sample_sng.xlsx" );

		XSSFWorkbook workBook = new XSSFWorkbook(file);

		//표준단어 정의서를 이용한 표준단어 정보
		XSSFSheet wordSheet = workBook.getSheetAt( 1 );
		int wordRow = wordSheet.getLastRowNum();

		System.out.println( "WORDROW : " + wordRow );

		//표준단어정의서 시트의 각 행별로 반복
		for(int i = 1; i <= wordRow; i++) {

			XSSFRow row = wordSheet.getRow( i );

			if( null == row ) {
				continue;
			}

			//컬럼명인 표준용어 한글명을 가져온다
			XSSFCell column = row.getCell( 2 );
			String columnNm = column.getStringCellValue();

			if( "".equals( columnNm ) ) {
				continue;
			}

			//컬럼명을 띄어쓰기 기준으로 자른 뒤,
			String[] split = columnNm.split(" ");
			StringBuilder sb = new StringBuilder();

			boolean isStandard = true;

			for(int j = 0; j < split.length; j++) {

				String temp = makeColumnNm( standardInfo, split[j] );//standardInfo.get( split[j] );

				//만들 컬럼명의 단어가 표준단어정의서에 없으면 표준화에 맞지 않아 스킵
				if( null == temp ) {
					isStandard = false;
					break;
				}
				if( "".equals( temp ) ) {
					//sb가 _면 알맞은 컬럼명을 찾지 못한상태로 잘못된 컬럼명
					isStandard = false;
					break;
				}

				sb.append( temp );
				if( j < split.length - 1 ) {
					sb.append( "_" );
				}
			}

			//컬럼명의 마지막이 도메인
			String domain = split[ split.length - 1 ];

			if( isStandard && isNotNull( domainInfo, domain) ) {
				termHm.put( columnNm , new standardTermInfo( columnNm, sb.toString() , domain, domainInfo.get( domain ).domainEngNm, domainInfo.get( domain ).dataType, domainInfo.get( domain).dataLength) );//한글 컬럼 명, 영문 명, 도에인 한글명, 도메인 영문명, 데이터 타입, 길이
			}else {
				termHm.put( columnNm , new standardTermInfo( columnNm, "-", "-", "-", "-", "-") );//표준화가 안되면 스킵
			}
		}

		file.close();

		for(String a : termHm.keySet())
			System.out.println( a );

		return termHm;
	}

	//컬럼 명을 만드는 단어 중, 영문은 표준 용어 명에 없고 영문명에 있을 수 도 있어서, 표준용어 명을 찾고 없으면 영문명에서 찾기
	public static String makeColumnNm( HashMap<String, String> standardInfo, String temp ) {

		String word = "";

		if( null == standardInfo.get( temp ) ) {
			for( String tempKey : standardInfo.keySet() ) {
				if( standardInfo.get( tempKey ).equals( temp ) ) {
					word = standardInfo.get( tempKey );
					return word;
				}
			}
		}else {
			word = standardInfo.get( temp );
		}

		return word;
	}

	//모든정보가 null이 아닌지 확인
	private static boolean isNotNull( HashMap<String, domainInfo> domainHm, String domain ) {

		if( null == domainHm.get( domain ) || null == domainHm.get( domain ).dataLength || null == domainHm.get( domain ).dataType || null == domainHm.get( domain ).domainClass || null == domainHm.get( domain ).domainEngNm ) {
			return false;
		}

		return true;
	}


	public static void makeExcel( HashMap<String, standardTermInfo> termHm ) throws Exception {
		FileInputStream file = new FileInputStream( "sample_sng.xlsx" );

		XSSFWorkbook workBook = new XSSFWorkbook(file);

		//표준단어 정의서를 이용한 표준단어 정보
		XSSFSheet wordSheet = workBook.getSheetAt( 1 );
		int lastRow = wordSheet.getLastRowNum();

		for(int i = 1; i <= lastRow; i++) {
			//XSSFRow resultRow = wordSheet.createRow( i );
			XSSFRow resultRow = wordSheet.getRow( i );

			String columnNm = resultRow.getCell( 2 ).getStringCellValue();

			System.out.println( columnNm );

			resultRow.createCell( 3 ).setCellValue( termHm.get( columnNm ).columnEngNm );
			resultRow.createCell( 4 ).setCellValue( termHm.get( columnNm ).dataType );
			resultRow.createCell( 5 ).setCellValue( termHm.get( columnNm ).dataLength );
			resultRow.createCell( 6 ).setCellValue( termHm.get( columnNm ).domainNm );
			resultRow.createCell( 7 ).setCellValue( termHm.get( columnNm ).domainEngNm );

			/*resultRow.createCell( 0 ).setCellValue( i-1 );
			resultRow.createCell( 1 ).setCellValue( ll.get( i-1 ).columnNm );
			resultRow.createCell( 2 ).setCellValue( ll.get( i-1 ).columnEngNm );
			resultRow.createCell( 3 ).setCellValue( ll.get( i -1).domainNm );
			resultRow.createCell( 4 ).setCellValue( ll.get( i -1).domainEngNm );
			resultRow.createCell( 5 ).setCellValue( ll.get( i -1).dataType );
			resultRow.createCell( 6 ).setCellValue( ll.get( i -1).dataLength );*/
		}

		FileOutputStream fileWrite = new FileOutputStream( "sample_sng.xlsx" );

		workBook.write( fileWrite );
		fileWrite.close();
	}
}
