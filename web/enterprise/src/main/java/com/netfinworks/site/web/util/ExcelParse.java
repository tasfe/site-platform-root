package com.netfinworks.site.web.util;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.NumberToTextConverter;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.netfinworks.common.util.money.Money;


/**
 * Excel处理工具类(样式只有默认与字体加粗)
 * @author sunlong
 *
 */
public class ExcelParse{
	//public static final String OFFICE_EXCEL_2003_POSTFIX = "xls";
	//public static final String OFFICE_EXCEL_2010_POSTFIX = "xlsx";

	public static final String EMPTY = "";
	public static final String POINT = ".";
	public static final String NOT_EXCEL_FILE = "Not the Excel file!";
	
	private SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
	
	private Workbook excelObj = null;
	private boolean isInit = false;
	private Mode mode = Mode.NONE;
	private ExcelVersion ver = ExcelVersion.NONE;
	private String currentSheet;
	private int currentRow;
	private int currentColum;
	
	private CellStyle normalStyle = null;
	private CellStyle boldStyle = null;
	
	public enum Mode{
		Read,Write,NONE
	}
	
	public enum ExcelVersion{
		Excel_2003("xls"),Excel_2010("xlsx"),NONE("none");
		private String postfix;

		private ExcelVersion(String postfix) {
			this.postfix = postfix;
		}

		public String getPostfix() {
			return postfix;
		}
	}
	
	public class Element {
		private boolean isBold;
		private String value;
		public Element(boolean isBold, String value) {
			super();
			this.isBold = isBold;
			this.value = value;
		}
		public boolean isBold() {
			return isBold;
		}
		public void setBold(boolean isBold) {
			this.isBold = isBold;
		}
		public String getValue() {
			return value;
		}
		public void setValue(String value) {
			this.value = value;
		}
	}
	
	private ExcelParse(Mode mode,ExcelVersion ver) {
		super();
		this.mode = mode;
		this.ver = ver;
	}

	/**
	 * 构建读对象
	 * @param ver 可以通过getExcelVersion(filepath)获取
	 * @param is 输入流
	 * @return
	 * @throws IOException
	 */
	public static ExcelParse buildRead(ExcelVersion ver,InputStream is) throws IOException{
		ExcelParse parse = new ExcelParse(Mode.Read,ver);
		parse.initRead(is);
		return parse;
	}
	
	/**
	 * 构建写对象
	 * @param ver 可以通过getExcelVersion(filepath)获取
	 * @return
	 * @throws IOException
	 */
	public static ExcelParse buildWrite(ExcelVersion ver) throws IOException{
		ExcelParse parse = new ExcelParse(Mode.Write,ver);
		parse.initWrite();
		return parse;
	}
	
	public static ExcelVersion getExcelVersion(String path) {
         if (path == null || EMPTY.equals(path.trim())) {
             return ExcelVersion.NONE;
         }
         if (path.contains(POINT)) {
        	 String posix = path.substring(path.lastIndexOf(POINT) + 1, path.length());
        	 if(posix.equals(ExcelVersion.Excel_2003.getPostfix()))
        		 return ExcelVersion.Excel_2003;
        	 else if(posix.equals(ExcelVersion.Excel_2010.getPostfix()))
        		 return ExcelVersion.Excel_2010;
             return ExcelVersion.NONE;
         }
         return ExcelVersion.NONE;
     }
	
	/**
	 * 初始化读
	 * 
	 * @param is
	 * @return
	 * @throws IOException
	 */
	public ExcelParse initRead(InputStream is) throws IOException{
		if(!isInit && is!=null && mode.equals(Mode.Read)){
			isInit = true;
			if(getVer().equals(ExcelVersion.Excel_2010))
				excelObj = new XSSFWorkbook(is);
			else if(getVer().equals(ExcelVersion.Excel_2003))
				excelObj = new HSSFWorkbook(is);
			else
				throw new IOException("Excel version error!");
		}
		return this;
	}
	
	/**
	 * 初始化写
	 * 
	 * @return
	 * @throws IOException
	 */
	public ExcelParse initWrite() throws IOException{
		if(!isInit && mode.equals(Mode.Write)){
			isInit = true;
			if(getVer().equals(ExcelVersion.Excel_2010))
				excelObj = new XSSFWorkbook();
			else if(getVer().equals(ExcelVersion.Excel_2003))
				excelObj = new HSSFWorkbook();
			else
				throw new IOException("Excel version error!");
			this.normalStyle = this.getExcelObj().createCellStyle();
			this.boldStyle = this.getExcelObj().createCellStyle();
			DataFormat  format = this.getExcelObj().createDataFormat();
			this.normalStyle.setDataFormat(format.getFormat("@"));//设置格式为文本
			this.boldStyle.setDataFormat(format.getFormat("@"));//设置格式为文本
			Font f = this.getExcelObj().createFont();
			f.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);         //字体增粗
			this.boldStyle.setFont(f);
		}
		return this;
	}
	
	/**
	 * 获取指定单元格位置的值
	 * @param sheet 从0开始
	 * @param row 从0开始
	 * @param colum 从0开始
	 * @return
	 */
	public String readOneCellVal(int sheet,int row,int colum){
		if(!isInit || excelObj==null || !mode.equals(Mode.Read))
			return null;
		return readOne(sheet, row, colum);
	}
	
	/**
	 * 获取指定行开始到结束的内容
	 * @param sheet 从0开始
	 * @param startrow 从0开始
	 * @param endrow 从0开始
	 * @return
	 */
	public List<List<String>> readListCellVal(int sheet,int startrow){
		if(!isInit || excelObj==null || !mode.equals(Mode.Read))
			return null;
		return readList(sheet,startrow,-1);
	}
	
	/**
	 * 获取指定行开始到指定行结束的内容
	 * @param sheet 从0开始
	 * @param startrow 从0开始
	 * @param endrow 从0开始
	 * @return
	 */
	public List<List<String>> readListCellVal(int sheet,int startrow,int endrow){
		if(!isInit || excelObj==null || !mode.equals(Mode.Read))
			return null;
		return readList(sheet,startrow,endrow);
	}
	
	private List<List<String>> readList(int sheetnum,int startrow,int endrow){
		List<List<String>> rows = new ArrayList<List<String>>();
		List<String> colums = null;
		if(sheetnum<0 || sheetnum >= this.getExcelObj().getNumberOfSheets())
			throw new IndexOutOfBoundsException("sheet "+sheetnum+" is not exist");
		Sheet sheet = this.getExcelObj().getSheetAt(sheetnum);
		if(startrow > sheet.getLastRowNum())
			return rows;
		int lastRow = sheet.getLastRowNum();
		if(endrow>0 && endrow<sheet.getLastRowNum())
			lastRow = endrow;
		for(int index_row = startrow;index_row<=lastRow;index_row++){
			Row row = sheet.getRow(index_row);
			if(row != null){
				colums = new ArrayList<String>();
				for(int index_col=0;index_col<row.getLastCellNum();index_col++){
					colums.add(readOneCellVal(sheetnum,index_row,index_col));
				}
				rows.add(colums);
			}
		}
		return rows;
	}
	
	/**
	 * 读取一个值
	 * @param sheet
	 * @param row
	 * @param colum
	 * @return
	 */
	private String readOne(int sheetnum,int rownum,int colum){
		if(sheetnum >= this.getExcelObj().getNumberOfSheets())
			throw new IndexOutOfBoundsException("sheet "+sheetnum+" is not exist");
		Sheet sheet = this.getExcelObj().getSheetAt(sheetnum);
		if(rownum > sheet.getLastRowNum())
			throw new IndexOutOfBoundsException("row is not exist");
		Row row = sheet.getRow(rownum);
		return getCellStringValue(row.getCell(colum));
	}
	
	/**
	 * 获取String类型的值
	 * @param cell
	 * @return
	 */
	private String getCellStringValue(Cell cell){
		String ret = null;
		if(cell == null)
			return ret;
		switch (cell.getCellType()) {
		case Cell.CELL_TYPE_BLANK:
			ret = "";
			break;
		case Cell.CELL_TYPE_BOOLEAN:
			ret = String.valueOf(cell.getBooleanCellValue());
			break;
		case Cell.CELL_TYPE_ERROR:
			ret = null;
			break;
		case Cell.CELL_TYPE_FORMULA:
			Workbook wb = cell.getSheet().getWorkbook();
			CreationHelper crateHelper = wb.getCreationHelper();
			FormulaEvaluator evaluator = crateHelper.createFormulaEvaluator();
			ret = getCellStringValue(evaluator.evaluateInCell(cell));
			break;
		case Cell.CELL_TYPE_NUMERIC:
			if (DateUtil.isCellDateFormatted(cell)) {
				Date theDate = cell.getDateCellValue();
				ret = format.format(theDate);
			} else {
				ret = NumberToTextConverter.toText(cell.getNumericCellValue());
			}
			break;
		case Cell.CELL_TYPE_STRING:
			ret = cell.getRichStringCellValue().getString();
			break;
		}
		return ret;
	}
	
	/**
	 * 创建页,默认会设置为当前工作页
	 * @param sheetname
	 * @return
	 */
	public ExcelParse createSheet(String sheetname){
		if(!isInit || excelObj==null || !mode.equals(Mode.Write))
			return this;
		this.getExcelObj().createSheet(sheetname);
		this.goSheet(sheetname);
		return this;
	}
	
	/**
	 * 在指定页指定行创建
	 * @param sheetname
	 * @param rownum
	 * @return
	 */
	public ExcelParse createRow(String sheetname,int rownum){
		if(!isInit || excelObj==null || !mode.equals(Mode.Write) || rownum<0)
			return this;
		this.getExcelObj().getSheet(sheetname).createRow(rownum);
		this.goRow(rownum);
		return this;
	}
	
	/**
	 * 在当前页指定行创建
	 * @param rownum
	 * @return
	 */
	public ExcelParse createRow(int rownum){
		this.createRow(getCurrentSheet(), rownum);
		return this;
	}
	
	/**
	 * 在当前页最后追加行
	 * @return
	 */
	public ExcelParse createRow(){
		this.createRow(getCurrentSheet(),getCurrentRow()+1);
		return this;
	}
	
	/**
	 * 指定页指定行指定列设置单元格值
	 * @param sheetname 
	 * @param rownum 行索引
	 * @param colum 列索引
	 * @param value 值(默认为字符串)
	 * @param isBold 字体是否粗体
	 * @return
	 */
	public ExcelParse createCell(String sheetname,int rownum,int colum,String value,boolean isBold){
		if(!isInit || excelObj==null || !mode.equals(Mode.Write) || rownum<0 || colum<0)
			return this;
		Cell c = this.getExcelObj().getSheet(sheetname).getRow(rownum).createCell(colum);
		c.setCellStyle(this.normalStyle);
		if(isBold){
			c.setCellStyle(this.boldStyle);
		}
		c.setCellValue(value);
		this.goRow(rownum);
		this.goColum(colum);
		return this;
	}
	
	/**
	 * 在当前页指定行指定列创建单元格
	 * @param rownum
	 * @param colum
	 * @param value
	 * @param isBold
	 * @return
	 */
	public ExcelParse createCell(int rownum,int colum,String value,boolean isBold){
		this.createCell(getCurrentSheet(), rownum, colum, value, isBold);
		return this;
	}
	
	/**
	 * 在当前页当前行指定列创建单元格
	 * @param colum
	 * @param value
	 * @param isBold
	 * @return
	 */
	public ExcelParse createCell(int colum,String value,boolean isBold){
		this.createCell(getCurrentRow(), colum, value, isBold);
		return this;
	}
	
	/**
	 * 在当前页当前行最后一列追加单元格
	 * @param value
	 * @param isBold
	 * @return
	 */
	public ExcelParse createCell(String value,boolean isBold){
		this.createCell(getCurrentColum()+1, value, isBold);
		return this;
	}
	
	/**
	 * 输出并关闭流
	 * @param os
	 * @throws IOException
	 */
	public void writeAndClose(OutputStream os) throws IOException{
		if(!isInit || excelObj==null || !mode.equals(Mode.Write) || os == null){
			if(os != null)
				os.close();
			return;
		}
		this.getExcelObj().write(os);
		os.close();
	}
	
	/**
	 * 输出，结束不关闭流
	 * @param os
	 * @throws IOException
	 */
	public void write(OutputStream os) throws IOException{
		if(!isInit || excelObj==null || !mode.equals(Mode.Write) || os == null)
			return;
		this.getExcelObj().write(os);
	}

	public String getCurrentSheet() {
		return currentSheet;
	}

	public int getCurrentRow() {
		return currentRow;
	}

	/**
	 * 设置工作页,同时当前行归0
	 * @param currentSheet
	 * @return
	 */
	public ExcelParse goSheet(String currentSheet) {
		this.currentSheet = currentSheet;
		goRow(-1);
		return this;
	}
	
	/**
	 * 设置当前行
	 * @param currentRow
	 * @return
	 */
	public ExcelParse goRow(int currentRow) {
		this.currentRow = currentRow;
		goColum(-1);
		return this;
	}
	

	public int getCurrentColum() {
		return currentColum;
	}

	public ExcelParse goColum(int currentColum) {
		this.currentColum = currentColum;
		return this;
	}

	public Mode getMode() {
		return mode;
	}

	public ExcelVersion getVer() {
		return ver;
	}
	
	public Workbook getExcelObj() {
		return excelObj;
	}

	public void setFormat(SimpleDateFormat format) {
		this.format = format;
	}
	
	public static String toFirstLetterUpperCase(String str) {
        if (str == null || str.length() < 2) {
            return str;
        }
        String firstLetter = str.substring(0, 1).toUpperCase();
        return firstLetter + str.substring(1, str.length());
    }
	
	public static void main(String[] args) throws FileNotFoundException, IOException {
		/*ExcelParse par = ExcelParse.buildRead(
				ExcelParse.getExcelVersion("D://Backup//桌面//test.xlsx"), 
				new FileInputStream("D://Backup//桌面//test.xlsx"));
		System.out.println(par.readOneCellVal(0, 0, 1));*/
		
		/*ExcelParse.buildWrite(ExcelVersion.Excel_2010)
		.createSheet("test")
		.createRow().createCell("文件类型1", true).createCell("小贷接口测试1", false).createCell("文件类型1", true).createCell("小贷接口测试1", false)
		.createRow().createCell("文件类型2", true).createCell("小贷接口测试2", false)
		.createRow().createCell("文件类型2", true).createCell("小贷接口测试2", false)
		.createRow()
		.createRow().createCell("列1", true).createCell("列2", true).createCell("列3", true).createCell("列3", true)
		.createRow().createCell("值1", false).createCell("20140101235959", false).createCell("列3", false).createCell("列3", false)
		.createRow().createCell("值2", false).createCell(null, false).createCell("列3", false).createCell("列3", false)
		.createRow().createCell("值3", false).createCell("列2", false).createCell("列3", false).createCell("列3", false)
		.writeAndClose(new FileOutputStream("D://test.xlsx"));*/
		
		int MAX_YEAR = 40;
		Money PER_YEAR_INTPUT = new Money(50000);
		double flat = 0.076;
		Money sum = new Money();
		for(int i=1;i<=MAX_YEAR;i++){
			sum = sum.add(PER_YEAR_INTPUT);
			System.out.println("time ("+i+") 本金:"+sum.toString());
			sum = sum.multiply(1+flat);
			System.out.println("time ("+i+") 本息:"+sum.toString());
			System.out.println("");
		}
		System.out.println("收益:"+sum.subtract(PER_YEAR_INTPUT.multiply(MAX_YEAR)));
	}
}
