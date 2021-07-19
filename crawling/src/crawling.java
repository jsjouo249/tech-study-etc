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

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Jsoup;
import org.jsoup.internal.StringUtil;
import org.jsoup.nodes.Document;
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

//	public static void main(String agrs[]) throws Exception {
//		makeExcel( "메리트 토토" );
//	}

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

		cellStyle.setFont( font );

		XSSFRow row = sheet.createRow( 0 );
		XSSFCell zCell = row.createCell( 0 );
		XSSFCell oCell = row.createCell( 1 );

		zCell.setCellStyle( cellStyle );
		oCell.setCellStyle( cellStyle );

		zCell.setCellValue( "사이트 URL" );
		oCell.setCellValue( "사이트 타이틀" );
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

				List<String> urlList = new ArrayList<String>();									//검색 결과 사이트 URL 리스트
				List<String> nmList  = new ArrayList<String>();									//검색 결과 사이트 명 리스트
				List<String> siteExplainList = doc.getElementsByClass( "lyLwlc" ).eachText();	//검색 사이트 설명

				List<String> adSiteUrlList = doc.getElementsByClass("zMz9yb").eachText();		//검색 결과 광고 사이트 URL div 클래스
				List<String> adSiteNmList  = doc.getElementsByClass( "v0nnCb" ).eachText();		//광고 사이트 명

				int adSize = adSiteUrlList.size();

				//광고 사이트는 따로 뽑아서 출력
				for(int i = 0; i < adSize; i++) {
					String[] adSiteUrl = adSiteUrlList.get(i).split("·");

					if( adSiteUrl.length >= 2 ) {
						System.out.println( excelRow + " { siteUrl : \"" + adSiteUrl[1].substring( 0 , adSiteUrl[1].indexOf( " 이 " ) ) + "\" , siteNm : \"" + adSiteNmList.get(i) + "\" , siteExplain : \"" + siteExplainList.get(i) + "\", searchContent : \"" + searchContent + "\"}," );
						siteInfoHs.add( new siteInfo( adSiteUrl[1].substring( 0 , adSiteUrl[1].indexOf( " 이 " ) ).replace( "\"", "'") , adSiteNmList.get(i).replace( "\"", "'"), siteExplainList.get(i), searchContent ) );
					}
				}

				Elements searchInfoDiv = doc.getElementsByClass("yuRUbf");		//검색 결과 사이트 URL div 클래스
				String[] searchInfoList = searchInfoDiv.toString().split("\n");

				Elements nmEle = doc.getElementsByClass( "DKV0Md" );			//검색 결과 site명 클래스

				//사이트 URL 리스트
				for(int i = 0; i < searchInfoList.length; i++) {
					if( searchInfoList[i].contains( "yuRUbf" ) ) {
						String temp = searchInfoList[i+1].split("\"")[1];
						urlList.add( URLDecoder.decode( URLDecoder.decode( temp ) ));
						i++;
					}
				}

				nmList = nmEle.eachText();			//사이트 명 리스트

				boolean isExcepUrl = false;			//검색 제외 단어 리스트 확인 용

				int urlSize = urlList.size();

				for(int i = 0; i < urlSize; i++) {
					isExcepUrl = false;

					//제외해야 하는 단어가 껴있으면 제외
					for( String excepword : excepWord ) {
						if( urlList.get( i ).contains( excepword ) ) {
							isExcepUrl = true;
							break;
						}
					}

					if( !isExcepUrl ) {
						String aTagUrl = urlList.get(i);
						if( aTagUrl.contains( "›" ) ) {
							aTagUrl = aTagUrl.split( "›" )[0];
						}

						System.out.println( excelRow + " { siteUrl : \"" + aTagUrl.replace( "\"", "'") + "\", siteNm : \"" + nmList.get(i).replace( "\"", "'") + "\" , siteExplain : \"" + siteExplainList.get( i + adSiteUrlList.size() ) + "\", searchContent : \"" + searchContent + "\"}," );
						siteInfoHs.add( new siteInfo( aTagUrl.replace( "\"", "'") , nmList.get(i).replace( "\"", "'"), siteExplainList.get( i + adSize ), searchContent ) );
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
