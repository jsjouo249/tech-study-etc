import java.io.File;
import java.io.FileOutputStream;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.StringTokenizer;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Jsoup;
import org.jsoup.internal.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

class siteInfo{
	String siteUrl;			//검색 결과 : 사이트 URL
	String siteTitle;		//검색 결과 : 사이트 타이틀( 검색 화면에 보여지는 검색 결과 )
	String siteExplain;		//검색 정보
	String searchContent;	//검색 단어

	public siteInfo( String siteUrl, String siteTitle, String siteExplain, String searchContent ) {
		this.siteUrl 	   = siteUrl;
		this.siteTitle     = siteTitle;
		this.siteExplain   = siteExplain;
		this.searchContent = searchContent;
	}

	@Override
	public boolean equals(Object obj) {
		siteInfo compare = (siteInfo) obj;
		//키가 될 siteUrl로 동일한지 비교
		return ( this.siteUrl.equals( compare.siteUrl ) );
	}

	@Override
	public int hashCode() {
		//키가 될 siteUrl로 해쉬코드 생성
		return siteUrl.hashCode();
	}
}

public class crawling {

	public static HashSet<siteInfo> makeExcel( String srchContent ) throws Exception{

		//검색 페이징 담는 큐
		Queue<String> stPg = new LinkedList<>();

		//중복된 사이트 제거하기 위한 해쉬셋 - iterator사용가능해서 사용
		HashSet<siteInfo> siteInfoHs = new HashSet<>();

		//검색 설정
		String searchMain = "https://www.google.com";
		String searchNextUrl = "/search?q=";

		String searchContent = srchContent;

		String searchPage	 = "&start=";
		stPg.add( searchMain + searchNextUrl + searchContent + searchPage + ( (1 - 1) * 10) );

		//제외 단어 리스트			나무위키  위키피디아
		String[] excepWord = { "namu", "wiki" };

		int idx = 2;	  //현재 pageNum이되며, 다음 페이지의 시작 pageNum이 됨
		int lastPage = 0; //마지막 pageNum

		//파일 생성
		String fileNameExcel = "C:" + File.separator + "Users" + File.separator + "ngcc" + File.separator + "Desktop" + File.separator + "검색" + File.separator + searchContent + ".xlsx";

		/*****************************엑셀 초기 셋팅***************************************/
		int excelRow = 1;

		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet sheet = workbook.createSheet( searchContent );

		XSSFFont font = workbook.createFont();
		font.setFontHeightInPoints( (short) 14 );
		font.setBold( true );

		CellStyle cellStyle = workbook.createCellStyle();

		sheet.setColumnWidth( 0,  (sheet.getColumnWidth(0)) + (short)20480 );
		sheet.setColumnWidth( 1,  (sheet.getColumnWidth(1)) + (short)20480 );
		sheet.setColumnWidth( 2,  (sheet.getColumnWidth(2)) + (short)20480 );

		cellStyle.setFont( font );

		XSSFRow row = sheet.createRow( 0 );
		XSSFCell zCell = row.createCell( 0 );
		XSSFCell oCell = row.createCell( 1 );
		XSSFCell tCell = row.createCell( 2 );

		zCell.setCellStyle( cellStyle );
		oCell.setCellStyle( cellStyle );
		tCell.setCellStyle( cellStyle );

		zCell.setCellValue( "사이트 URL" );
		oCell.setCellValue( "사이트 타이틀" );
		tCell.setCellValue( "사이트 설명" );
		/********************************************************************/

		File isExist = new File( fileNameExcel );
		if (isExist.exists()) {
			System.out.println( "이미 수집한 이력이 있습니다." );
			return null;
		}

		try {

			//검색 시, 나온 페이지 전체 조회
			while( !stPg.isEmpty() ) {

				String url = stPg.poll();

				Document doc = Jsoup.connect( url ).get();

				List<Element> adList   = doc.getElementsByClass( "uEierd" );	//광고 클래스 : uEierd
				List<Element> siteList = doc.getElementsByClass( "tF2Cxc" );	//검색 클래스 : tF2Cxc

				//광고 검색 결과
				for( Element ele : adList ) {

					String adSiteUrl 			= ele.getElementsByClass( "zMz9yb" ).eachText().get(0);	//광고 사이트 Url
					String adSiteTitle  		= ele.getElementsByClass( "v0nnCb" ).eachText().get(0); //광고 사이트 타이틀

					List<String> adSiteExpList 	= ele.getElementsByClass( "lyLwlc" ).eachText();        //광고 사이트 설명 - 여러 줄인 경우 존재
					StringBuilder adSiteExpStr  = new StringBuilder();									//광고 사이트 설명을 합치기 위한 stringbuilder

					int adSiteExpLength = adSiteExpList.size();

					//사이트 설명이 여러건이면
					for(int j = 0; j < adSiteExpLength; j++) {
						adSiteExpStr.append( adSiteExpList.get( j ) );
					}

					//광고· - siteUrl 처음에 붙기 때문에, 광고· 제거하기 위한 substring
					adSiteUrl = adSiteUrl.substring( 3 );

					System.out.println( " { siteUrl : \"" + adSiteUrl + "\" , siteNm : \"" + adSiteTitle + "\" , siteExplain : \"" + adSiteExpStr + "\", searchContent : \"" + searchContent + "\"}," );
					siteInfoHs.add( new siteInfo( adSiteUrl , adSiteTitle, adSiteExpStr.toString(), searchContent ) );
				}


				//검색 결과
				for( Element ele : siteList ) {

					String siteNm  	   = ele.getElementsByClass( "DKV0Md" ).eachText().get( 0 );	//사이트 명

					String siteUrl 	   = "";														//사이트 URL 담을 변수
					String urlResult   = ele.getElementsByClass( "yuRUbf" ).toString();				//사이트 URL을 담은 div 소스
					StringTokenizer st = new StringTokenizer( urlResult, "\n" );

					//사이트 URL 추출
					while( st.hasMoreTokens() ) {
						if( st.nextToken().contains( "yuRUbf" ) ) {
							String temp = st.nextToken().split("\"")[1];
							siteUrl = ( URLDecoder.decode( URLDecoder.decode( temp ) ) );
							break;
						}
					}

					List<String> siteExpList = ele.getElementsByClass( "lyLwlc" ).eachText();	//사이트 설명 - 여러 줄인 경우 존재
					StringBuilder siteExpStr = new StringBuilder();								//사이트 설명 합치는 stringbuilder

					int siteExpLength 		 = siteExpList.size();

					for(int j = 0; j < siteExpLength; j++) {
						siteExpStr.append( siteExpList.get( j ) );
					}

					boolean isExcepUrl = false;

					//제외해야 하는 단어가 껴있으면 제외
					for( String excepword : excepWord ) {
						if( siteUrl.contains( excepword ) ) {
							isExcepUrl = true;
							break;
						}
					}

					if( !isExcepUrl ) {
						if( siteUrl.contains( "›" ) ) {
							siteUrl = siteUrl.split( "›" )[0];
						}

						siteUrl = siteUrl.replace( "\"", "'");
						siteNm  = siteNm.replace( "\"", "'");

						System.out.println( " { siteUrl : \"" + siteUrl + "\", siteNm : \"" + siteNm + "\" , siteExplain : \"" + siteExpStr + "\", searchContent : \"" + searchContent + "\"}," );
						siteInfoHs.add( new siteInfo( siteUrl , siteNm, siteExpStr.toString(), searchContent ) );
					}
				}

				//해당 페이지에서 확인가능한 페이지 리스트 가져오기
				List<String> pageEle = doc.getElementsByClass("fl").eachText();	//해당 페이지의 번호 및 이동하는 태그 리스트

				for(int i = pageEle.size() - 1; i >= 0; i--) {
					if( StringUtil.isNumeric( pageEle.get(i) ) ) {
						if( Integer.parseInt( pageEle.get(i) ) > lastPage ) {
							lastPage = Integer.parseInt( pageEle.get(i) );		//해당 페이지의 리스트의 마지막 숫자 = 현재 알 수 있는 마지막 페이지의 숫자
						}
						break;
					}
				}

				//마지막 페이지가 늘어나면 stack에 담은 마지막 페이지와 마지막 숫자 맞추는 반복문
				for( ; idx <= lastPage; idx++ ) {
					stPg.add( searchMain + searchNextUrl + searchContent + searchPage + ( (idx - 1) * 10 ) );//( 현재 페이지 - 1 ) * 10 = 검색 URL의 시작 페이지 숫자
				}

			}

			//중복제거 한 사이트 url을 엑셀데이터와 리스트에 삽입
			Iterator<siteInfo> hsIter = siteInfoHs.iterator();

			while( hsIter.hasNext() ) {
				siteInfo data = hsIter.next();

				row = sheet.createRow( excelRow );
				row.createCell( 0 ).setCellValue( data.siteUrl );
				row.createCell( 1 ).setCellValue( data.siteTitle );
				row.createCell( 2 ).setCellValue( data.siteExplain );
				excelRow++;
			}

			File excel = new File( fileNameExcel );
			FileOutputStream fileWrite = new FileOutputStream( excel );

			workbook.write( fileWrite );
			fileWrite.close();

		}catch ( Exception e ) {
			e.printStackTrace();
		}

		return siteInfoHs;
	}
}
