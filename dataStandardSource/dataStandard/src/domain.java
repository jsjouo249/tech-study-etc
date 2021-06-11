import java.io.FileInputStream;
import java.util.HashMap;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

class domainInfo{
	String domainEngNm;	//도메인 영문명
	String dataType;	//데이터 타입
	String dataLength;	//데이터 길이
	String domainClass;	//도메인 분류

	public domainInfo( String domainEngNm, String dataType, String dataLength, String domainClass ) {
		this.domainEngNm	= domainEngNm;
		this.dataType		= dataType;
		this.dataLength		= dataLength;
		this.domainClass	= domainClass;
	}
}

public class domain {

	public static HashMap<String, domainInfo> domainInit() throws Exception{

		HashMap<String, domainInfo> domainHm = new HashMap<>();

		FileInputStream file = new FileInputStream( "dataStandardInfo.xlsx" );
		XSSFWorkbook workBook = new XSSFWorkbook(file);

		//도메인 정의서를 이용한 도메인 만들기
		XSSFSheet domainSheet = workBook.getSheet( "도메인정의서" );
		int domainRow = domainSheet.getLastRowNum();

		//도메인정의서 시트의 각 행별로 반복
		for(int i = 1; i < domainRow; i++) {

			XSSFRow row = domainSheet.getRow( i );

			if( null == row ) {
				continue;
			}

			int lastCellNum = row.getLastCellNum();

			String[] domainArr = new String[5];
			int arrIdx = 0;//domain배열에 넣을 때 위치 잡아주는 변수

			//해당 열의 마지막 셀까지 값을 domain배열에 설정해서 저장
			for(int j = 0; j < lastCellNum; j++) {

				XSSFCell cell = row.getCell( j );

				if( j == 1 || j == 2 || j == 4 || j == 5 || j == 6 ) {
					if( null != cell ) {
						switch(cell.getCellType()){
		                	case XSSFCell.CELL_TYPE_FORMULA:
		                		domainArr[arrIdx] = cell.getCellFormula();
		                		break;

		                	case XSSFCell.CELL_TYPE_NUMERIC:
		                		// 숫자일 경우, String형으로 변경하여 값을 읽는다.
		                		cell.setCellType( XSSFCell.CELL_TYPE_STRING );
		                		domainArr[arrIdx] = cell.getStringCellValue();
		                		break;

		                	case XSSFCell.CELL_TYPE_STRING:
		                		domainArr[arrIdx] = cell.getStringCellValue();
		                		break;

		                	case XSSFCell.CELL_TYPE_BLANK:
		                		domainArr[arrIdx] = "";
		                		break;

		                	case XSSFCell.CELL_TYPE_ERROR:
		                		domainArr[arrIdx] = cell.getStringCellValue();
		                		break;

		                	case XSSFCell.CELL_TYPE_BOOLEAN:
		                		domainArr[arrIdx] = cell.getStringCellValue();
		                		break;
						}

						arrIdx++;
					}
				}

			}

			//다 null이 아니면 도메인해쉬맵에 영문명을 key값으로 해당 영문명의 도메인 정보들 넣기
			if( !(null == domainArr[0] || null == domainArr[1] || null == domainArr[2] || null == domainArr[3] || null == domainArr[4]) ) {
				domainInfo domainInfo = new domainInfo( domainArr[1], domainArr[2], domainArr[3], domainArr[4] );
				domainHm.put( domainArr[0] , domainInfo);
			}

		}

		file.close();

		return domainHm;
	}
}
