package com.wangl.chartutils.poi;


import com.wangl.chartutils.poi.config.CellConfigBean;
import com.wangl.chartutils.poi.config.ChartCellConfig;
import com.wangl.chartutils.poi.config.ExcelName;
import com.wangl.chartutils.poi.utils.ChartType;
import com.wangl.chartutils.poi.utils.CheckUtil;
import com.wangl.chartutils.poi.utils.ListUtil;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Chart;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFChart;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.openxmlformats.schemas.drawingml.x2006.chart.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *@Dsecriiption TODO
 *@Author liwang
 *@version 2021/6/28、10:52
 **/
public class ExcelChartUtil {
    private SXSSFWorkbook wb = new SXSSFWorkbook();
    private SXSSFSheet sheet = null;
    private List<CellConfigBean> cellConfigBeanList = new ArrayList<>();
    // 标题
    private List<String> allTitleArr = new ArrayList<String>();
    //全部数据
    private List<Map<String, Object>> allDataList = new ArrayList<Map<String, Object>>();

    private Map<String, String> dataFormatMap = new HashMap();

    int charCount = 10;
    //数值类型是否使用千分位
    private boolean isThousandth = false;
    String tempPaht = "/temp/zebra/";
    //是否单独只要表格
    private boolean isTable = false;
    //是否单独只要图表
    private boolean isChart = false;

    private String fileName = "default";

    private int[] xIndex;

    private String sheetName ="";

    private void handleChart(List<?> beanList,String chartType){

        chartType = CheckUtil.isEmpty(chartType)? ChartType.BARCHART.code():chartType;
        // 标题

        if(CheckUtil.isEmpty(beanList))
            throw new NullPointerException("导出参数为空");

        generateExport(beanList.get(0).getClass());
        if(cellConfigBeanList==null) return;

        setAxisChar(xIndex);

        // 字段名
        List<Integer> fldNameArr = new ArrayList();
        //定义横坐标分租列
        AtomicInteger col = new AtomicInteger();

        List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();

        beanList.forEach(bean ->{
            Map<String, Object> dataMap = new HashMap<>();
            Map<String, Object> allDataMap = new HashMap<>();
            int v = 0;

            for (int i = 0; i < cellConfigBeanList.size(); i++) {
                CellConfigBean cellBean = cellConfigBeanList.get(i);
                String filedName = cellBean.getFieldName();
                Object fieldValue = fieldFilter(bean, filedName);
                //图表数据
                if(cellBean.isAxisChar() || cellBean.isChart()){
                    v = v+1;
                    String value = "value"+(v);
                    int index = cellBean.getIndex();
                    dataMap.put(value, fieldValue);
                    if(bean == beanList.get(0)){
                        if(!cellBean.isAxisChar()) {
                            fldNameArr.add(index);
                        }else{
                            col.set(index);
                        }
                    }
                }
                //如果只需要图表的话就不需要全部数据了
                if(isChart) continue;
                //===全部数据===
                String allValue = "value"+(i+1);
                if(bean == beanList.get(0)) {
                    dataFormatMap.put(allValue, dataStyle(cellBean));
                }

                if(dataFormatMap.get(allValue).equals("0.00%")){
                    BigDecimal value = CheckUtil.isEmpty(fieldValue)? BigDecimal.ZERO:new BigDecimal(fieldValue.toString());
                    if(value.compareTo(BigDecimal.ZERO)!=0){
                        fieldValue = value.divide(new BigDecimal(100),4, BigDecimal.ROUND_HALF_UP);
                    }
                }
                allDataMap.put(allValue, fieldValue);
            }
            dataList.add(dataMap);
            allDataList.add(allDataMap);
        });

        cellConfigBeanList.forEach(cellBean->{
            String aliasName = cellBean.getAliasName();
            allTitleArr.add(aliasName);
        });

        Collections.sort(fldNameArr);
        if(!isTable) {
            // 创建柱状图
            if (chartType.equals(ChartType.BARCHART.code()))
                createBarChart(col.intValue(), fldNameArr, dataList);
                // 创建饼状图
            else if (chartType.equals(ChartType.PIECHART.code()))
                createPieChart(col.intValue(), fldNameArr, dataList);
                // 创建折线图
            else if (chartType.equals(ChartType.TIMEXYCHAR.code()))
                createTimeXYChar(col.intValue(), fldNameArr, dataList);
                // 创建面积图
            else if (chartType.equals(ChartType.AREACHART.code()))
                createAreaChart(col.intValue(), fldNameArr, dataList);
        }else{
            createTable();
        }
    }

    public byte[] getExcelBytes(List<?> beanList,String chartType) {
        byte[] rByte = null;
        try {
            handleChart(beanList,chartType);
//            FileOutputStream out = new FileOutputStream(new File("D:/jcdemo/" + System.currentTimeMillis() + ".xlsx"));
//            wb.write(out);
//            out.close();
            rByte = getBytes();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            clearData();
        }
        return rByte;
    }

    private byte[] getBytes() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            wb.write(baos);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            baos.close();
        }
        return baos.toByteArray();
    }


    //根据bean的名称获取bean的值
    private Object fieldFilter(Object beanClass, String fieldName) {
        try {

            Field[] fields = beanClass.getClass().getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                Field sourceField = fields[i];
                boolean sourceAccessible = sourceField.isAccessible();
                if (!sourceAccessible)
                    sourceField.setAccessible(true);
                if (fieldName.equals(sourceField.getName())) {
                    return sourceField.get(beanClass);
                }
            }
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    //生成导出标题跟图表标题
    private void generateExport(Class<?> classBean){
        Field[] declaredFields = classBean.getDeclaredFields();

        for (Field declaredField : declaredFields) {
            ChartCellConfig fieldAnnotation = declaredField.getDeclaredAnnotation(ChartCellConfig.class);
            Optional.ofNullable(fieldAnnotation).ifPresent(f->{
                CellConfigBean bean = new CellConfigBean();
//                if(f.isChart()){
//                    Class<?> type = declaredField.getType();
//                    if(type.equals("class java.lang.Boolean") || type.equals("class java.lang.String")){
//                        new Throwable("isChart注解只能为计算类型");
//                    }
//                }
                bean = bean.toBuilder().index(f.index())
                        .aliasName(f.aliasName())
                        .isChart(f.isChart())
                        .fieldName(declaredField.getName())
                        .fieldType(declaredField.getType().getSimpleName())
                        .axisChar(f.axisChar())
                        .format(f.format())
                        .build();
                cellConfigBeanList.add(bean);
            });
        }
        ListUtil.sort(cellConfigBeanList,"index",true);

        ExcelName excelName = classBean.getDeclaredAnnotation(ExcelName.class);
        Optional.ofNullable(excelName).ifPresent(e->{
            fileName = e.name();
        });

    }



    /**
     * 单独创建表格(堆积图，多组)
     *
     * @throws IOException
     */
    private void createTable(){
        // 创建一个sheet页
        sheet = wb.createSheet(fileName);
        drawSheet0Table(sheet);
    }


    /**
     * 创建柱状图(堆积图，多组)
     *
     * @throws IOException
     */
    private void createBarChart(int col, List<Integer> fldNameArr, List<Map<String, Object>> dataList) {
        // 创建一个sheet页
        if(!isChart)
            sheet = wb.createSheet(this.sheetName+"柱形图");
        // drawSheet0Table(sheet,titleArr,fldNameArr,dataList);
        // 堆积=STBarGrouping.STACKED 多组=STBarGrouping.CLUSTERED
        boolean result = drawSheet0Map(sheet, STBarGrouping.CLUSTERED, fldNameArr, dataList, col);
        System.out.println("生成柱状图(堆积or多组)-->" + result);
    }


    /**
     * 生成柱状图
     *
     * @param sheet
     *            页签
     * @param group
     *            柱状图类型(堆积,多组)
     * @param fldNameArr
     *            坐标名称
     * @param dataList
     *            统计数据
     * @return
     */
    private boolean drawSheet0Map(SXSSFSheet sheet, STBarGrouping.Enum group, List<Integer> fldNameArr,
                                  List<Map<String, Object>> dataList, int col) {
        boolean result = false;
        int count = dataList.size();
        // 获取sheet名称
        String sheetName = sheet.getSheetName();
        if(!isChart) result = drawSheet0Table(sheet);
        // 创建一个画布
        Drawing<?> drawing = sheet.createDrawingPatriarch();
        // 画一个图区域
        // 前四个默认0，从第8行到第25行,从第0列到第6列的区域
        ClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 0, count+2, charCount, count+19);
        // 创建一个chart对象
        Chart chart = drawing.createChart(anchor);
        CTChart ctChart = ((XSSFChart) chart).getCTChart();
        CTPlotArea ctPlotArea = ctChart.getPlotArea();
        // 创建柱状图模型
        CTBarChart ctBarChart = ctPlotArea.addNewBarChart();
        CTBoolean ctBoolean = ctBarChart.addNewVaryColors();
        ctBarChart.getVaryColors().setVal(true);
        // 设置图类型
        ctBarChart.addNewGrouping().setVal(group);
        ctBoolean.setVal(true);
        ctBarChart.addNewBarDir().setVal(STBarDir.COL);
        // 是否添加左侧坐标轴
        ctChart.addNewDispBlanksAs().setVal(STDispBlanksAs.ZERO);
        ctChart.addNewShowDLblsOverMax().setVal(true);
        // 设置这两个参数是为了在STACKED模式下生成堆积模式；(standard)标准模式时需要将这两行去掉
        if ("stacked".equals(group.toString()) || "percentStacked".equals(group.toString())) {
            ctBarChart.addNewGapWidth().setVal(150);
            ctBarChart.addNewOverlap().setVal((byte) 100);
        }
        // 创建序列,并且设置选中区域
        int dataCount = count<charCount?count:charCount;
        for (int i = 0; i < fldNameArr.size() ; i++) {
            int index = fldNameArr.get(i);

            CTBarSer ctBarSer = ctBarChart.addNewSer();
            CTSerTx ctSerTx = ctBarSer.addNewTx();
            // 图例区
            CTStrRef ctStrRef = ctSerTx.addNewStrRef();
            // 选定区域第0行,第1,2,3列标题作为图例 //1 2 3
            String legendDataRange = new CellRangeAddress(0, 0, index, index).formatAsString(sheetName, true);
            ctStrRef.setF(legendDataRange);
            ctBarSer.addNewIdx().setVal(i);
            // 横坐标区
            CTAxDataSource cttAxDataSource = ctBarSer.addNewCat();
            ctStrRef = cttAxDataSource.addNewStrRef();
            // 选第0列,第1-6行作为横坐标区域
            String axisDataRange = new CellRangeAddress(1, dataCount, col, col).formatAsString(sheetName, true);
            ctStrRef.setF(axisDataRange);
            // 数据区域
            CTNumDataSource ctNumDataSource = ctBarSer.addNewVal();
            CTNumRef ctNumRef = ctNumDataSource.addNewNumRef();
            // 选第1-6行,第1-3列作为数据区域 //1 2 3
            String numDataRange = new CellRangeAddress(1, dataCount, index, index).formatAsString(sheetName,
                    true);
            ctNumRef.setF(numDataRange);
            // 添加柱状边框线
            ctBarSer.addNewSpPr().addNewLn().addNewSolidFill().addNewSrgbClr().setVal(new byte[] { 0, 0, 0 });
            // 设置负轴颜色不是白色
            ctBarSer.addNewInvertIfNegative().setVal(false);
            // 设置标签格式
            ctBoolean.setVal(false);
            CTDLbls newDLbls = ctBarSer.addNewDLbls();
            newDLbls.setShowLegendKey(ctBoolean);
            ctBoolean.setVal(true);
            newDLbls.setShowVal(ctBoolean);
            ctBoolean.setVal(false);
            newDLbls.setShowCatName(ctBoolean);
            newDLbls.setShowSerName(ctBoolean);
            newDLbls.setShowPercent(ctBoolean);
            newDLbls.setShowBubbleSize(ctBoolean);
            newDLbls.setShowLeaderLines(ctBoolean);
        }
        // 告诉BarChart它有坐标轴，并给它们id
        ctBarChart.addNewAxId().setVal(123456);
        ctBarChart.addNewAxId().setVal(123457);
        // 横坐标
        CTCatAx ctCatAx = ctPlotArea.addNewCatAx();
        ctCatAx.addNewAxId().setVal(123456); // id of the cat axis
        CTScaling ctScaling = ctCatAx.addNewScaling();
        ctScaling.addNewOrientation().setVal(STOrientation.MIN_MAX);
        ctCatAx.addNewAxPos().setVal(STAxPos.B);
        ctCatAx.addNewCrossAx().setVal(123457); // id of the val axis
        ctCatAx.addNewTickLblPos().setVal(STTickLblPos.NEXT_TO);
        // 纵坐标
        CTValAx ctValAx = ctPlotArea.addNewValAx();
        ctValAx.addNewAxId().setVal(123457); // id of the val axis
        ctValAx.addNewMajorGridlines().addNewSpPr().addNewLn().addNewSolidFill().addNewSrgbClr().setVal(
                new XSSFColor(new java.awt.Color(0 , 176, 240)).getRGB());

        ctScaling = ctValAx.addNewScaling();
        ctScaling.addNewOrientation().setVal(STOrientation.MIN_MAX);
        // 设置位置
        ctValAx.addNewAxPos().setVal(STAxPos.L);
        ctValAx.addNewCrossAx().setVal(123456); // id of the cat axis
        ctValAx.addNewTickLblPos().setVal(STTickLblPos.NEXT_TO);
        // 是否删除主左边轴
        ctValAx.addNewDelete().setVal(false);
        // 是否删除横坐标
        ctCatAx.addNewDelete().setVal(false);
        // legend图注
        // if(true){
        CTLegend ctLegend = ctChart.addNewLegend();
        ctLegend.addNewLegendPos().setVal(STLegendPos.B);
        ctLegend.addNewOverlay().setVal(false);
        // }
        return result;
    }
    /**
     * 创建面积图
     *
     * @throws IOException
     */

    private void createAreaChart(int col, List<Integer> fldNameArr,
                                 List<Map<String, Object>> dataList) {
        // 创建一个sheet页
        sheet = wb.createSheet(this.sheetName+"面积图");
        boolean result = drawSheet1Map(sheet, "is3D", fldNameArr, dataList, col);
        System.out.println("生成面积图-->" + result);
    }
    /**
     * 生成面积图
     * @param sheet
     * @param type
     * @param fldNameArr
     * @param dataList
     * @param col
     * @return
     */
    private boolean drawSheet1Map(SXSSFSheet sheet, String type, List<Integer> fldNameArr,
                                  List<Map<String, Object>> dataList, int col) {
        boolean result = false;
        int count = dataList.size();
        // 获取sheet名称
        String sheetName = sheet.getSheetName();
        result = drawSheet0Table(sheet);
        // 创建一个画布
        Drawing<?> drawing = sheet.createDrawingPatriarch();
        // 画一个图区域
        // 前四个默认0，从第8行到第25行,从第0列到第6列的区域
        ClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 0, count+2, charCount, count+19);
        // 创建一个chart对象
        Chart chart = drawing.createChart(anchor);
        CTChart ctChart = ((XSSFChart) chart).getCTChart();
        CTPlotArea ctPlotArea = ctChart.getPlotArea();
        CTAreaChart ctAreaChart = ctPlotArea.addNewAreaChart();
        CTBoolean ctBoolean = ctAreaChart.addNewVaryColors();
        ctAreaChart.addNewGrouping().setVal(STGrouping.STANDARD);
        // 创建序列,并且设置选中区域
        int dataCount = count<charCount?count:charCount;
        for (int i = 0; i < fldNameArr.size(); i++) {
            int index = fldNameArr.get(i);
            CTAreaSer ctAreaSer = ctAreaChart.addNewSer();
            CTSerTx ctSerTx = ctAreaSer.addNewTx();
            // 图例区
            CTStrRef ctStrRef = ctSerTx.addNewStrRef();
            // 选定区域第0行,第1,2,3列标题作为图例 //1 2 3
            String legendDataRange = new CellRangeAddress(0, 0, index, index).formatAsString(sheetName, true);
            ctStrRef.setF(legendDataRange);
            ctAreaSer.addNewIdx().setVal(i);
            // 横坐标区
            CTAxDataSource cttAxDataSource = ctAreaSer.addNewCat();
            ctStrRef = cttAxDataSource.addNewStrRef();
            // 选第0列,第1-6行作为横坐标区域
            String axisDataRange = new CellRangeAddress(1, dataCount, col, col).formatAsString(sheetName, true);
            ctStrRef.setF(axisDataRange);
            // 数据区域
            CTNumDataSource ctNumDataSource = ctAreaSer.addNewVal();
            CTNumRef ctNumRef = ctNumDataSource.addNewNumRef();
            // 选第1-6行,第1-3列作为数据区域 //1 2 3
            String numDataRange = new CellRangeAddress(1, dataCount, index, index).formatAsString(sheetName,
                    true);
            ctNumRef.setF(numDataRange);
            // 设置标签格式
            ctBoolean.setVal(false);
            CTDLbls newDLbls = ctAreaSer.addNewDLbls();
            newDLbls.setShowLegendKey(ctBoolean);
            ctBoolean.setVal(true);
            newDLbls.setShowVal(ctBoolean);
            ctBoolean.setVal(false);
            newDLbls.setShowCatName(ctBoolean);
            newDLbls.setShowSerName(ctBoolean);
            newDLbls.setShowPercent(ctBoolean);
            newDLbls.setShowBubbleSize(ctBoolean);
            newDLbls.setShowLeaderLines(ctBoolean);
            /*
             * //是否是平滑曲线 CTBoolean addNewSmooth = ctAreaSer.addNewSmooth();
             * addNewSmooth.setVal(false); //是否是堆积曲线 CTMarker addNewMarker =
             * ctAreaSer.addNewMarker(); CTMarkerStyle addNewSymbol =
             * addNewMarker.addNewSymbol();
             * addNewSymbol.setVal(STMarkerStyle.NONE);
             */
        }
        // telling the BarChart that it has axes and giving them Ids
        ctAreaChart.addNewAxId().setVal(123456);
        ctAreaChart.addNewAxId().setVal(123457);
        // cat axis
        CTCatAx ctCatAx = ctPlotArea.addNewCatAx();
        ctCatAx.addNewAxId().setVal(123456); // id of the cat axis
        CTScaling ctScaling = ctCatAx.addNewScaling();
        ctScaling.addNewOrientation().setVal(STOrientation.MIN_MAX);
        ctCatAx.addNewAxPos().setVal(STAxPos.B);
        ctCatAx.addNewCrossAx().setVal(123457); // id of the val axis
        ctCatAx.addNewTickLblPos().setVal(STTickLblPos.NEXT_TO);
        // val axis
        CTValAx ctValAx = ctPlotArea.addNewValAx();
        ctValAx.addNewAxId().setVal(123457); // id of the val axis
        ctScaling = ctValAx.addNewScaling();
        ctScaling.addNewOrientation().setVal(STOrientation.MIN_MAX);
        ctValAx.addNewAxPos().setVal(STAxPos.L);
        ctValAx.addNewCrossAx().setVal(123456); // id of the cat axis
        ctValAx.addNewTickLblPos().setVal(STTickLblPos.NEXT_TO);
        // 是否删除主左边轴
        ctValAx.addNewDelete().setVal(false);
        // 是否删除横坐标
        ctCatAx.addNewDelete().setVal(false);
        // legend图注
        CTLegend ctLegend = ctChart.addNewLegend();
        ctLegend.addNewLegendPos().setVal(STLegendPos.B);
        ctLegend.addNewOverlay().setVal(false);
        return result;
    }
    /**
     * 创建饼状图
     *
     * @throws IOException
     */
    private void createPieChart(int col, List<Integer> fldNameArr, List<Map<String, Object>> dataList) {
        // 创建一个sheet页
        sheet = wb.createSheet(this.sheetName+"饼状图");
        boolean result = drawSheet2Map(sheet, "is3D", fldNameArr, dataList, col);
        System.out.println("生成饼状图(普通or3D)-->" + result);
    }
    /**
     * 创建饼状图
     *
     * @param sheet
     *            页签
     * @param type
     *            图类型(3D或者普通)
     * @param fldNameArr
     *            (类标题)
     * @param dataList
     *            (填充数据)
     * @param col
     *            (标题)
     * @return
     */
    private boolean drawSheet2Map(SXSSFSheet sheet, String type, List<Integer> fldNameArr,
                                  List<Map<String, Object>> dataList, int col) {
        boolean result = false;
        int count = dataList.size();
        // 获取sheet名称
        String sheetName = sheet.getSheetName();
        result = drawSheet0Table(sheet);
        // 创建一个画布
        Drawing<?> drawing = sheet.createDrawingPatriarch();
        // 画一个图区域
        // 前四个默认0，从第8行到第25行,从第0列到第6列的区域
        ClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 0, count+2, charCount, count+19);
        // 创建一个chart对象
        Chart chart = drawing.createChart(anchor);
        CTChart ctChart = ((XSSFChart) chart).getCTChart();
        CTPlotArea ctPlotArea = ctChart.getPlotArea();
        CTBoolean ctBoolean = null;
        CTPie3DChart ctPie3DChart = null;
        CTPieChart ctPieChart = null;
        // 创建饼状图模型
        if (type.equals("is3D")) {
            ctPie3DChart = ctPlotArea.addNewPie3DChart();
            ctBoolean = ctPie3DChart.addNewVaryColors();
        } else {
            ctPieChart = ctPlotArea.addNewPieChart();
            ctBoolean = ctPieChart.addNewVaryColors();
        }
        int dataCount = count<charCount?count:charCount;
        // 创建序列,并且设置选中区域
        for (int i = 0; i < fldNameArr.size(); i++) {
            int index = fldNameArr.get(i);
            CTPieSer ctPieSer = null;
            if (type.equals("is3D")) {
                ctPieSer = ctPie3DChart.addNewSer();
            } else {
                ctPieSer = ctPieChart.addNewSer();
            }
            CTSerTx ctSerTx = ctPieSer.addNewTx();
            // 图例区
            CTStrRef ctStrRef = ctSerTx.addNewStrRef();
            // 选定区域第0行,第1,2,3列标题作为图例 //1 2 3
            String legendDataRange = new CellRangeAddress(0, 0, index, index).formatAsString(sheetName, true);
            ctStrRef.setF(legendDataRange);
            ctPieSer.addNewIdx().setVal(i);
            // 横坐标区
            CTAxDataSource cttAxDataSource = ctPieSer.addNewCat();
            ctStrRef = cttAxDataSource.addNewStrRef();
            // 选第0列,第1-6行作为横坐标区域
            String axisDataRange = new CellRangeAddress(1, dataCount, col, col).formatAsString(sheetName, true);
            ctStrRef.setF(axisDataRange);
            // 数据区域
            CTNumDataSource ctNumDataSource = ctPieSer.addNewVal();
            CTNumRef ctNumRef = ctNumDataSource.addNewNumRef();
            // 选第1-6行,第1-3列作为数据区域 //1 2 3
            String numDataRange = new CellRangeAddress(1, dataCount, index, index).formatAsString(sheetName,
                    true);
            ctNumRef.setF(numDataRange);
            // 显示边框线
            ctPieSer.addNewSpPr().addNewLn().addNewSolidFill().addNewSrgbClr().setVal(new byte[] { 0, 0, 0 });
            // 设置标签格式
            ctBoolean.setVal(true);
        }
        // legend图注
        CTLegend ctLegend = ctChart.addNewLegend();
        ctLegend.addNewLegendPos().setVal(STLegendPos.B);
        ctLegend.addNewOverlay().setVal(true);
        return result;
    }
    /**
     * 创建折线图
     *
     * @throws IOException
     */
    private void createTimeXYChar(int col, List<Integer> fldNameArr, List<Map<String, Object>> dataList) {
        // 创建一个sheet页
        sheet = wb.createSheet(this.sheetName+"折线图");
        // 第二个参数折线图类型:line=普通折线图,line-bar=折线+柱状图
        boolean result = drawSheet3Map(sheet, "line", fldNameArr, dataList, col);
        System.out.println("生成折线图(折线图or折线图-柱状图)-->" + result);
    }
    /**
     * 生成折线图
     *
     * @param sheet
     *            页签
     * @param type
     *            类型
     * @param fldNameArr
     *            X轴标题
     * @param dataList
     *            填充数据
     * @param col
     *            图例标题
     * @return
     */
    private boolean drawSheet3Map(SXSSFSheet sheet, String type, List<Integer> fldNameArr,
                                  List<Map<String, Object>> dataList, int col) {
        boolean result = false;
        int count = dataList.size();
        // 获取sheet名称
        String sheetName = sheet.getSheetName();
        result = drawSheet0Table(sheet);
        // 创建一个画布
        Drawing<?> drawing = sheet.createDrawingPatriarch();
        // 画一个图区域
        // 前四个默认0，从第8行到第25行,从第0列到第6列的区域
        ClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 0, count+2, charCount, count+19);
        // 创建一个chart对象
        Chart chart = drawing.createChart(anchor);
        CTChart ctChart = ((XSSFChart) chart).getCTChart();
        CTPlotArea ctPlotArea = ctChart.getPlotArea();
        if (type.equals("line-bar")) {
            CTBarChart ctBarChart = ctPlotArea.addNewBarChart();
            CTBoolean ctBoolean = ctBarChart.addNewVaryColors();
            ctBarChart.getVaryColors().setVal(true);
            // 设置类型
            ctBarChart.addNewGrouping().setVal(STBarGrouping.CLUSTERED);
            ctBoolean.setVal(true);
            ctBarChart.addNewBarDir().setVal(STBarDir.COL);
            // 是否添加左侧坐标轴
            ctChart.addNewDispBlanksAs().setVal(STDispBlanksAs.ZERO);
            ctChart.addNewShowDLblsOverMax().setVal(true);
            int dataCount = count<charCount?count:charCount;
            // 创建序列,并且设置选中区域
            for (int i = 0; i < fldNameArr.size(); i++) {
                int index = fldNameArr.get(i);
                CTBarSer ctBarSer = ctBarChart.addNewSer();
                CTSerTx ctSerTx = ctBarSer.addNewTx();
                // 图例区
                CTStrRef ctStrRef = ctSerTx.addNewStrRef();
                // 选定区域第0行,第1,2,3列标题作为图例 //1 2 3
                String legendDataRange = new CellRangeAddress(0, 0, index, index).formatAsString(sheetName, true);
                ctStrRef.setF(legendDataRange);
                ctBarSer.addNewIdx().setVal(i);
                // 横坐标区
                CTAxDataSource cttAxDataSource = ctBarSer.addNewCat();
                ctStrRef = cttAxDataSource.addNewStrRef();
                // 选第0列,第1-6行作为横坐标区域
                String axisDataRange = new CellRangeAddress(1, dataCount, col, col).formatAsString(sheetName, true);
                ctStrRef.setF(axisDataRange);
                // 数据区域
                CTNumDataSource ctNumDataSource = ctBarSer.addNewVal();
                CTNumRef ctNumRef = ctNumDataSource.addNewNumRef();
                // 选第1-6行,第1-3列作为数据区域 //1 2 3
                String numDataRange = new CellRangeAddress(1, dataCount, index, index).formatAsString(sheetName,
                        true);
                ctNumRef.setF(numDataRange);
                ctBarSer.addNewSpPr().addNewLn().addNewSolidFill().addNewSrgbClr().setVal(new byte[] { 0, 0, 0 });
                // 设置负轴颜色不是白色
                ctBarSer.addNewInvertIfNegative().setVal(false);
                // 设置标签格式
                ctBoolean.setVal(false);
                CTDLbls newDLbls = ctBarSer.addNewDLbls();
                newDLbls.setShowLegendKey(ctBoolean);
                ctBoolean.setVal(true);
                newDLbls.setShowVal(ctBoolean);
                ctBoolean.setVal(false);
                newDLbls.setShowCatName(ctBoolean);
                newDLbls.setShowSerName(ctBoolean);
                newDLbls.setShowPercent(ctBoolean);
                newDLbls.setShowBubbleSize(ctBoolean);
                newDLbls.setShowLeaderLines(ctBoolean);
            }
            // telling the BarChart that it has axes and giving them Ids
            ctBarChart.addNewAxId().setVal(123456);
            ctBarChart.addNewAxId().setVal(123457);
            // cat axis
            CTCatAx ctCatAx = ctPlotArea.addNewCatAx();
            ctCatAx.addNewAxId().setVal(123456); // id of the cat axis
            CTScaling ctScaling = ctCatAx.addNewScaling();
            ctScaling.addNewOrientation().setVal(STOrientation.MIN_MAX);
            ctCatAx.addNewAxPos().setVal(STAxPos.B);
            ctCatAx.addNewCrossAx().setVal(123457); // id of the val axis
            ctCatAx.addNewTickLblPos().setVal(STTickLblPos.NEXT_TO);
            // val axis
            CTValAx ctValAx = ctPlotArea.addNewValAx();
            ctValAx.addNewAxId().setVal(123457); // id of the val axis
            ctScaling = ctValAx.addNewScaling();
            ctScaling.addNewOrientation().setVal(STOrientation.MIN_MAX);
            ctValAx.addNewAxPos().setVal(STAxPos.L);
            ctValAx.addNewCrossAx().setVal(123456); // id of the cat axis
            ctValAx.addNewTickLblPos().setVal(STTickLblPos.NEXT_TO);
        }
        // 折线图
        CTLineChart ctLineChart = ctPlotArea.addNewLineChart();
        CTBoolean ctBoolean = ctLineChart.addNewVaryColors();
        ctLineChart.addNewGrouping().setVal(STGrouping.STANDARD);
        // 创建序列,并且设置选中区域
        int dataCount = count<charCount?count:charCount;
        for (int i = 0; i < fldNameArr.size(); i++) {
            int index = fldNameArr.get(i);
            CTLineSer ctLineSer = ctLineChart.addNewSer();
            CTSerTx ctSerTx = ctLineSer.addNewTx();
            // 图例区
            CTStrRef ctStrRef = ctSerTx.addNewStrRef();
            // 选定区域第0行,第1,2,3列标题作为图例 //1 2 3
            String legendDataRange = new CellRangeAddress(0, 0, index, index).formatAsString(sheetName, true);
            ctStrRef.setF(legendDataRange);
            ctLineSer.addNewIdx().setVal(i);
            // 横坐标区
            CTAxDataSource cttAxDataSource = ctLineSer.addNewCat();
            ctStrRef = cttAxDataSource.addNewStrRef();
            // 选第0列,第1-6行作为横坐标区域
            String axisDataRange = new CellRangeAddress(1, dataCount, col, col).formatAsString(sheetName, true);
            ctStrRef.setF(axisDataRange);
            // 数据区域
            CTNumDataSource ctNumDataSource = ctLineSer.addNewVal();
            CTNumRef ctNumRef = ctNumDataSource.addNewNumRef();
            // 选第1-6行,第1-3列作为数据区域 //1 2 3
            String numDataRange = new CellRangeAddress(1, dataCount, index, index).formatAsString(sheetName,
                    true);
            ctNumRef.setF(numDataRange);
            // 设置标签格式
            ctBoolean.setVal(false);
            CTDLbls newDLbls = ctLineSer.addNewDLbls();
            newDLbls.setShowLegendKey(ctBoolean);
            ctBoolean.setVal(true);
            newDLbls.setShowVal(ctBoolean);
            ctBoolean.setVal(false);
            newDLbls.setShowCatName(ctBoolean);
            newDLbls.setShowSerName(ctBoolean);
            newDLbls.setShowPercent(ctBoolean);
            newDLbls.setShowBubbleSize(ctBoolean);
            newDLbls.setShowLeaderLines(ctBoolean);
            // 是否是平滑曲线
            CTBoolean addNewSmooth = ctLineSer.addNewSmooth();
            addNewSmooth.setVal(false);
            // 是否是堆积曲线
            CTMarker addNewMarker = ctLineSer.addNewMarker();
            CTMarkerStyle addNewSymbol = addNewMarker.addNewSymbol();
            addNewSymbol.setVal(STMarkerStyle.NONE);
        }
        // telling the BarChart that it has axes and giving them Ids
        ctLineChart.addNewAxId().setVal(123456);
        ctLineChart.addNewAxId().setVal(123457);
        // cat axis
        CTCatAx ctCatAx = ctPlotArea.addNewCatAx();
        ctCatAx.addNewAxId().setVal(123456); // id of the cat axis
        CTScaling ctScaling = ctCatAx.addNewScaling();
        ctScaling.addNewOrientation().setVal(STOrientation.MIN_MAX);
        ctCatAx.addNewAxPos().setVal(STAxPos.B);
        ctCatAx.addNewCrossAx().setVal(123457); // id of the val axis
        ctCatAx.addNewTickLblPos().setVal(STTickLblPos.NEXT_TO);
        // val axis
        CTValAx ctValAx = ctPlotArea.addNewValAx();
        ctValAx.addNewAxId().setVal(123457); // id of the val axis
        ctScaling = ctValAx.addNewScaling();
        ctScaling.addNewOrientation().setVal(STOrientation.MIN_MAX);
        ctValAx.addNewAxPos().setVal(STAxPos.L);
        ctValAx.addNewCrossAx().setVal(123456); // id of the cat axis
        ctValAx.addNewTickLblPos().setVal(STTickLblPos.NEXT_TO);
        // 是否删除主左边轴
        ctValAx.addNewDelete().setVal(false);
        // 是否删除横坐标
        if (type.equals("line-bar")) {
            ctCatAx.addNewDelete().setVal(true);
        }
        CTLegend ctLegend = ctChart.addNewLegend();
        ctLegend.addNewLegendPos().setVal(STLegendPos.B);
        ctLegend.addNewOverlay().setVal(false);
        return result;
    }
    /**
     * 生成数据表
     *
     * @param sheet sheet页对象
     * @return 是否生成成功
     */
    private boolean drawSheet0Table(SXSSFSheet sheet) {
        // 测试时返回值
        boolean result = true;
        // 初始化表格样式
        List<CellStyle> styleList = tableStyle();
        // 根据数据创建excel第一行标题行
        SXSSFRow row0 = sheet.createRow(0);
        for (int i = 0; i < allTitleArr.size(); i++) {
            // 设置标题
            row0.createCell(i).setCellValue(allTitleArr.get(i));
            // 设置标题行样式
            row0.getCell(i).setCellStyle(styleList.get(0));
        }
        // 填充数据
        for (int i = 0; i < allDataList.size(); i++) {
            // 获取每一项的数据
            Map<String, Object> data = allDataList.get(i);
            // 设置每一行的字段标题和数据
            SXSSFRow rowi = sheet.createRow(i + 1);
            for (int j = 0; j < data.size(); j++) {
                // 判断是否是标题字段列
                String key = "value" + (j + 1);
                Object param = data.get(key);
                if(param==null) param="";
                //rowi.createCell(j).setCellValue(Double.valueOf(String.valueOf(data.get("value" + (j + 1)))));
                if(ClassType.DATE.validateType(param)){
                    rowi.createCell(j).setCellValue((Date) param);
                }else if(ClassType.INT.validateType(param)){
                    rowi.createCell(j).setCellValue(Integer.valueOf(param.toString()));
                }else if(ClassType.LONG.validateType(param)){
                    rowi.createCell(j).setCellValue(Long.valueOf(param.toString()));
                }else if(ClassType.DOUBLE.validateType(param)){
                    rowi.createCell(j).setCellValue(Double.valueOf(param.toString()));
                }else if(ClassType.BIGDECIMAL.validateType(param)){
                    rowi.createCell(j).setCellValue(Double.valueOf(param.toString()));
                }else if(ClassType.FLOAT.validateType(param)){
                    rowi.createCell(j).setCellValue(Float.valueOf(param.toString()));
                }else if(param==null){
                    rowi.createCell(j).setCellValue("");
                }else{
                    rowi.createCell(j).setCellValue((String) param);
                }
                sheet.getRow(i + 1).getCell(j).setCellStyle(dataStyle(key));
                // 设置数据样式
                //sheet.getRow(i + 1).getCell(j).setCellStyle(styleList.get(2));
            }
        }
        return result;
    }



    /**
     * 生成表格样式
     *
     * @return
     */
    private List<CellStyle> tableStyle() {
        List<CellStyle> cellStyleList = new ArrayList<CellStyle>();
        // 样式准备
        // 标题样式
        CellStyle style = wb.createCellStyle();
        style.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN); // 下边框
        style.setBorderLeft(BorderStyle.THIN);// 左边框
        style.setBorderTop(BorderStyle.THIN);// 上边框
        style.setBorderRight(BorderStyle.THIN);// 右边框
        style.setAlignment(HorizontalAlignment.CENTER);
        cellStyleList.add(style);
        CellStyle style1 = wb.createCellStyle();
        style1.setBorderBottom(BorderStyle.THIN); // 下边框
        style1.setBorderLeft(BorderStyle.THIN);// 左边框
        style1.setBorderTop(BorderStyle.THIN);// 上边框
        style1.setBorderRight(BorderStyle.THIN);// 右边框
        style1.setAlignment(HorizontalAlignment.CENTER);
        cellStyleList.add(style1);
        CellStyle cellStyle = wb.createCellStyle();
        cellStyle.setBorderTop(BorderStyle.THIN);// 上边框
        cellStyle.setBorderBottom(BorderStyle.THIN); // 下边框
        cellStyle.setBorderLeft(BorderStyle.THIN);// 左边框
        cellStyle.setBorderRight(BorderStyle.THIN);// 右边框
        cellStyle.setAlignment(HorizontalAlignment.CENTER);// 水平对齐方式
        // cellStyle.setVerticalAlignment(VerticalAlignment.TOP);//垂直对齐方式
        cellStyleList.add(cellStyle);
        return cellStyleList;
    }

    //设置列的格式
    private String dataStyle(CellConfigBean cellBean){
        String type = cellBean.getFieldType();
        String dateFormat = "";
        if(!CheckUtil.isEmpty(cellBean.getFormat())){
            dateFormat=cellBean.getFormat();
        }else {
            if (type.equals("Date")) {
                dateFormat = "yyyy/MM/dd";
            } else if (type.equals("int") || type.equals("Long")) {
                dateFormat = isThousandth ? "#,##0" : "0";
            } else if (type.equals("double") || type.equals("float")) {
                dateFormat = isThousandth ? "#,##0.00" : "0.00";
            } else if (type.equals("BigDecimal")) {
                dateFormat = isThousandth ? "#,##0.0000" : "0.0000";
            } else {
                dateFormat = "@";
            }
        }
        return dateFormat;
    }


    //设置单元格的格式
    private CellStyle dataStyle(String key){
        CellStyle cellStyle = wb.createCellStyle();
        DataFormat dataFormat = wb.createDataFormat();
        cellStyle.setBorderTop(BorderStyle.THIN);// 上边框
        cellStyle.setBorderBottom(BorderStyle.THIN); // 下边框
        cellStyle.setBorderLeft(BorderStyle.THIN);// 左边框
        cellStyle.setBorderRight(BorderStyle.THIN);// 右边框
        if(dataFormatMap.get(key)!=null){
            cellStyle.setDataFormat(dataFormat.getFormat(dataFormatMap.get(key)));
        }
        return cellStyle;
    }

    private void clearData(){
        cellConfigBeanList.clear();
        allTitleArr.clear();
        allDataList.clear();
        dataFormatMap.clear();
        xIndex = null;
        isTable = false;
        isChart = false;
    }

    //设置横坐标的属性
    private void setAxisChar(int... index){

        if(index==null)return;

        cellConfigBeanList.forEach(config->{
            if(config.isAxisChar())
                config.setAxisChar(false);
        });

        for (int i : index) {
            cellConfigBeanList.get(i).setAxisChar(true);
        }
    }

    public void setCharCount(int charCount) {
        this.charCount = charCount;
    }

    public void setIsThousandth(boolean isThousandth) {
        this.isThousandth = isThousandth;
    }

    public void setTempPaht(String tempPaht) {
        this.tempPaht = tempPaht;
    }

    public void setIsTable(boolean isTable) {
        this.isTable = isTable;
    }

    public String getFileName() {
        return fileName;
    }

    public void setxIndex(int... xIndex) {
        this.xIndex = xIndex;
    }

    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }

    public void setChart(SXSSFSheet sheet) {
        this.isChart = true;
        this.sheet = sheet;
    }

}