import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
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
import org.xml.sax.HandlerBase;

public class crawling {

	public static void main(String[] args) throws Exception{

		//검색 페이징 담는 큐
		Queue<String> stPg = new LinkedList<>();

		//중복된 사이트 제거하기 위한 해쉬맵
		HashMap<String, String> siteInfo = new HashMap<>();

		//검색 설정
		String searchMain = "https://www.google.com";
		String searchNextUrl = "/search?q=";
		System.out.println( "검색할 단어 입력 : " );

		BufferedReader contentReader = new BufferedReader(new InputStreamReader(System.in));
		String searchContent = contentReader.readLine();

		String searchPage	 = "&start=";
		stPg.add( searchMain + searchNextUrl + searchContent + searchPage + ( (1 - 1) * 10) );	//카지노

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
		oCell.setCellValue( "사이트 명" );
		/********************************************************************/


		File isExist = new File( fileNameExcel );
		if (isExist.exists()) {
			System.out.println( "이미 수집한 이력이 있습니다." );
			return;
		}

		try {

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
						System.out.println( "{ siteUrl : \"" + adSiteUrl[1].substring( 0 , adSiteUrl[1].indexOf( " 이 " ) ) + "\" , siteNm : \"" + adSiteNmList.get(i) + "\", searchContent : \"" + searchContent + "\"}," );
						siteInfo.put( adSiteUrl[1].substring( 0 , adSiteUrl[1].indexOf( " 이 " ) ).replace( "\"", "'") , adSiteNmList.get(i).replace( "\"", "'") );
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
						String aTagUrl = urlList.get(i);
						if( aTagUrl.contains( "›" ) ) {
							aTagUrl = aTagUrl.split( "›" )[0];
						}

						System.out.println( "{ siteUrl : \"" + aTagUrl.replace( "\"", "'") + "\", siteNm : \"" + nmList.get(i).replace( "\"", "'") + "\", searchContent : \"" + searchContent + "\"}," );
						siteInfo.put( aTagUrl.replace( "\"", "'") , nmList.get(i).replace( "\"", "'") );
					}
				}

				//해당 페이지에서 확인가능한 페이지 리스트 가져오기
				List<String> pageEle = doc.getElementsByClass("fl").eachText();	//해당 페이지의 리스트

				for(int i = pageEle.size() - 1; i >= 0; i--) {
					if( StringUtil.isNumeric( pageEle.get(i) ) ) {
						if( Integer.parseInt( pageEle.get(i) ) > lastPage ) {
							lastPage = Integer.parseInt( pageEle.get(i) );		//해당 페이지의 리스트의 마지막 숫자 = 현재 알 수 있는 마지막 페이지의 숫자
						}
						break;
					}

				}

				for( ; idx <= lastPage; idx++ ) {
					stPg.add( searchMain + searchNextUrl + searchContent + searchPage + ( (idx - 1) * 10 ) );//( 현재 페이지 - 1 ) * 10 = 검색 url의 시작 페이지 숫자
				}
			}

			for( String temp : siteInfo.keySet() ) {
				row = sheet.createRow( excelRow );
				row.createCell( 0 ).setCellValue( temp );
				row.createCell( 1 ).setCellValue( siteInfo.get( temp ) );
				excelRow++;
			}

			File excel = new File( fileNameExcel );
			FileOutputStream fileWrite = new FileOutputStream( excel );

			workbook.write( fileWrite );
			fileWrite.close();

		}catch ( Exception e ) {
			e.printStackTrace();
		}

	}
}
