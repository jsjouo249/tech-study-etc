import java.io.FileInputStream;
import java.util.HashMap;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class standardWord {

	public static HashMap<String, String> wordInit() throws Exception{

		HashMap<String, String> wordHm = new HashMap<>();

		FileInputStream file = new FileInputStream( "dataStandardInfo.xlsx" );
		XSSFWorkbook workBook = new XSSFWorkbook(file);

		//표준단어 정의서를 이용한 표준단어 정보
		XSSFSheet wordSheet = workBook.getSheet( "표준단어정의서" );
		int wordRow = wordSheet.getLastRowNum();

		//표준단어정의서 시트의 각 행별로 반복
		for(int i = 1; i < wordRow; i++) {

			XSSFRow row = wordSheet.getRow( i );

			if( null == row ) {
				continue;
			}

			int lastCellNum = row.getLastCellNum();

			String[] wordArr = new String[2];
			int arrIdx = 0;

			//해당 열의 마지막 셀까지 값을 표준단어해쉬맵에 설정해서 저장
			for(int j = 0; j < lastCellNum; j++) {

				XSSFCell cell = row.getCell( j );

				if( j == 0 || j == 3 ) {
					if( null != cell ) {
						switch(cell.getCellType()){
		                	case XSSFCell.CELL_TYPE_FORMULA:
		                		wordArr[arrIdx] = cell.getCellFormula();
		                		break;

		                	case XSSFCell.CELL_TYPE_NUMERIC:
		                		// 숫자일 경우, String형으로 변경하여 값을 읽는다.
		                		cell.setCellType( XSSFCell.CELL_TYPE_STRING );
		                		wordArr[arrIdx] = cell.getStringCellValue();
		                		break;

		                	case XSSFCell.CELL_TYPE_STRING:
		                		wordArr[arrIdx] = cell.getStringCellValue();
		                		break;

		                	case XSSFCell.CELL_TYPE_BLANK:
		                		wordArr[arrIdx] = "";
		                		break;

		                	case XSSFCell.CELL_TYPE_ERROR:
		                		wordArr[arrIdx] = cell.getStringCellValue();
		                		break;

		                	case XSSFCell.CELL_TYPE_BOOLEAN:
		                		wordArr[arrIdx] = cell.getStringCellValue();
		                		break;
						}

						arrIdx++;
					}
				}

				//2까지 채워지면 원하는 정보는 얻었으니 반복문 종료
				if( arrIdx >= 2 ) {
					break;
				}

			}

			//다 null이 아니면 도메인해쉬맵에 영문명을 key값으로 해당 영문명의 도메인 정보들 넣기
			if( !(null == wordArr[0] || null == wordArr[1] ) ) {
				wordHm.put( wordArr[0] , wordArr[1] );
			}

		}

		file.close();

		return wordHm;
	}
}
