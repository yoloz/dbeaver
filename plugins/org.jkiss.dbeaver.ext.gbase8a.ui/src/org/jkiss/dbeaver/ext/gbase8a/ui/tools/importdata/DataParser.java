//package org.jkiss.dbeaver.ext.gbase8a.ui.tools.importdata;
//
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.InputStreamReader;
//import java.io.RandomAccessFile;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Iterator;
//import java.util.List;
//import org.apache.commons.lang3.StringEscapeUtils;
//import org.apache.poi.hssf.usermodel.HSSFDateUtil;
//import org.apache.poi.hssf.usermodel.HSSFWorkbook;
//import org.apache.poi.ss.usermodel.Cell;
//import org.apache.poi.ss.usermodel.CellType;
//import org.apache.poi.ss.usermodel.DateUtil;
//import org.apache.poi.ss.usermodel.Row;
//import org.apache.poi.ss.usermodel.Sheet;
//import org.apache.poi.ss.usermodel.Workbook;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;
//import org.dom4j.Document;
//import org.dom4j.Element;
//import org.dom4j.io.SAXReader;
//import org.eclipse.jface.dialogs.MessageDialog;
//import org.eclipse.swt.widgets.Composite;
//import org.jkiss.dbeaver.ext.gbase8a.data.GBase8aMessages;
////import org.jkiss.utils.csv.CSVReader;
//import org.supercsv.io.CsvListReader;
//import org.supercsv.io.ICsvListReader;
//import org.supercsv.prefs.CsvPreference;
//
//public class DataParser {
//  private String[] headers = null;
//  private int colSize = 0;
//  private int rowSize = 0;
//
//  public DataParser() {
//  }
//
//  public List<String[]> parseToList(String fileName, ParseOption parseOption) throws Exception {
//    File file = new File(fileName);
//    String charset = IConstants.CHARSET_ARRAY[parseOption.getCharset()];
//    List<String[]> content = new ArrayList();
//    new ArrayList();
//    InputStreamReader freader = null;
//    ICsvListReader csvReader = null;
//
//    try {
//      freader = new InputStreamReader(new FileInputStream(file), charset);
//      csvReader = new CsvListReader(freader, CsvPreference.STANDARD_PREFERENCE);
//      if (parseOption.isHasHeader()) {
//        this.headers = csvReader.getHeader(true);
//      }
//
//      List line;
//      while((line = csvReader.read()) != null) {
//        content.add((String[])line.toArray(new String[0]));
//      }
//
//      if (this.headers != null) {
//        this.colSize = this.headers.length;
//      } else if (content.size() > 0) {
//        this.colSize = ((String[])content.get(0)).length;
//      } else {
//        this.colSize = 0;
//      }
//
//      this.rowSize = content.size();
//    } catch (Exception var13) {
//      Exception e = var13;
//      throw e;
//    } finally {
//      if (freader != null) {
//        freader.close();
//      }
//
//      if (csvReader != null) {
//        csvReader.close();
//      }
//
//    }
//
//    return content;
//  }
//
//  public List<String[]> parseToList(String fileName, ParseOption parseOption, int lineNum, Composite container) throws Exception {
//    List<String[]> content = new ArrayList();
//    String fileType = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
//    if (fileType.toUpperCase().equals("TXT")) {
//      content = this.TxtParseToList(fileName, parseOption, lineNum, container);
//    } else if (fileType.toUpperCase().equals("CSV")) {
//      content = this.CsvParseToList(fileName, parseOption, lineNum);
//    } else if (fileType.toUpperCase().equals("XML")) {
//      content = this.XmlParseToList(fileName, parseOption, lineNum);
//    } else if (fileType.toUpperCase().equals("XLS")) {
//      content = this.XlsParseToList(fileName, parseOption, lineNum);
//    } else if (fileType.toUpperCase().equals("XLSX")) {
//      ExcelXlsxReader excelXlsxReader = new ExcelXlsxReader(parseOption);
//      this.rowSize = excelXlsxReader.process(fileName);
//      List<String[]> rows = excelXlsxReader.getRows();
//      if (lineNum > rows.size()) {
//        lineNum = rows.size();
//      }
//
//      for(int i = 0; i < lineNum; ++i) {
//        ((List)content).add((String[])rows.get(i));
//      }
//
//      this.headers = excelXlsxReader.getHeaders();
//      if (this.headers != null) {
//        this.colSize = this.headers.length;
//      }
//    } else {
//      MessageDialog.openWarning(container.getShell(), GBase8aMessages.import_properties_tip, GBase8aMessages.import_properties_import_filetype_error);
//    }
//
//    return (List)content;
//  }
//
//  public static String getSrtext(String fileName) throws Exception {
//    File tempFile = new File(fileName);
//    String tabName = tempFile.getName().split("\\.")[0];
//    String prjFilePath = fileName.substring(0, fileName.indexOf(tempFile.getName())) + tabName.split("\\.")[0] + ".prj";
//    String srtext = (new BufferedReader(new InputStreamReader(new FileInputStream(prjFilePath)))).readLine();
//    return srtext;
//  }
//
//  private List<String[]> CsvParseToList(String fileName, ParseOption parseOption, int lineNum) throws Exception {
//    File file = new File(fileName);
//    String charset = IConstants.CHARSET_ARRAY[parseOption.getCharset()];
//    List<String[]> content = new ArrayList();
//    new ArrayList();
//    InputStreamReader freader = null;
//    ICsvListReader csvReader = null;
//
//    try {
//      freader = new InputStreamReader(new FileInputStream(file), charset);
//      csvReader = new CsvListReader(freader, CsvPreference.STANDARD_PREFERENCE);
//      if (parseOption.isHasHeader()) {
//        this.headers = csvReader.getHeader(true);
//      }
//
//      List line;
//      int num;
//      for(num = 0; (line = csvReader.read()) != null; ++num) {
//        if (lineNum == -1) {
//          content.add((String[])line.toArray(new String[0]));
//        } else if (num <= lineNum) {
//          content.add((String[])line.toArray(new String[0]));
//        }
//      }
//
//      Iterator var12 = content.iterator();
//
//      while(var12.hasNext()) {
//        String[] grp_str = (String[])var12.next();
//
//        for(int i = 0; i < grp_str.length; ++i) {
//          if (grp_str[i] != null && grp_str[i].length() > 200) {
//            grp_str[i] = grp_str[i].substring(0, 200);
//          }
//        }
//      }
//
//      if (this.headers != null) {
//        this.colSize = this.headers.length;
//      } else if (content.size() > 0) {
//        this.colSize = ((String[])content.get(0)).length;
//      } else {
//        this.colSize = 0;
//      }
//
//      this.rowSize = num;
//    } catch (Exception var17) {
//      Exception e = var17;
//      throw e;
//    } finally {
//      if (freader != null) {
//        freader.close();
//      }
//
//      if (csvReader != null) {
//        csvReader.close();
//      }
//
//    }
//
//    return content;
//  }
//
//  private List<String[]> TxtParseToList(String fileName, ParseOption parseOption, int lineNum, Composite container) throws Exception {
//    File file = new File(fileName);
//    String charset = IConstants.CHARSET_ARRAY[parseOption.getCharset()];
//    List<String[]> content = new ArrayList();
//    RandomAccessFile raf = new RandomAccessFile(file, "r");
//    byte[] bytes = new byte[1000];
//    String lineData = "";
//    String sepaData = "";
//    String enclose = parseOption.getEnclose() == null ? "" : parseOption.getEnclose();
//    String fieldTerminate = parseOption.getFieldTerminate();
//    String lineTerminate = parseOption.getLineTerminate();
//    String split_fieldTerminate = this.escapeSpecialWord(fieldTerminate);
//    String split_line_field_Terminate = this.escapeSpecialWord(enclose + lineTerminate);
//    String split_enclose = this.ChangeSplitChar(enclose);
//
//    try {
//      int num = 0;
//      boolean head_flag = true;
//      boolean line_end_flag = false;
//      boolean byteread;
//      int byte_len;
//      int lineTerminateLen;
//      byte start;
//      String[] lineDatas;
//      int i;
//      int byteread;
//      if (fieldTerminate.isEmpty()) {
//        byteread = false;
//
//        label210:
//        while(true) {
//          do {
//            if ((byteread = raf.read(bytes)) == -1 && line_end_flag) {
//              break label210;
//            }
//
//            if (byteread == -1) {
//              lineData = "";
//              line_end_flag = true;
//            } else {
//              byte_len = 0;
//
//              for(lineTerminateLen = 0; lineTerminateLen < bytes.length && bytes[lineTerminateLen] != 0; ++lineTerminateLen) {
//                ++byte_len;
//              }
//
//              lineData = new String(bytes, 0, byte_len, charset);
//            }
//
//            byte_len = lineData.lastIndexOf(StringEscapeUtils.unescapeJava(lineTerminate));
//            lineTerminateLen = StringEscapeUtils.unescapeJava(lineTerminate).length();
//            if (byte_len != 0 && byte_len != -1 && byte_len + lineTerminateLen == lineData.length()) {
//              lineData = lineData.substring(0, byte_len);
//              raf.seek(raf.getFilePointer() - (long)lineTerminateLen);
//            }
//
//            sepaData = sepaData + lineData;
//            if ((long)sepaData.length() > 2000000L) {
//              MessageDialog.openWarning(container.getShell(), GBase8aMessages.import_properties_tip, GBase8aMessages.import_properties_import_max_byte_one_line);
//              content = new ArrayList();
//              break label210;
//            }
//          } while(sepaData.split(split_line_field_Terminate).length <= 1 && !line_end_flag);
//
//          start = 0;
//          lineDatas = sepaData.split(split_line_field_Terminate);
//          if (head_flag && parseOption.isHasHeader()) {
//            this.headers = new String[]{lineDatas[0]};
//            i = this.headers.length - 1;
//            this.headers[0] = this.headers[0].substring(enclose.length(), this.headers[0].length());
//            if (!split_enclose.isEmpty()) {
//              this.headers[i] = this.headers[i].endsWith(split_enclose) ? this.headers[i].substring(0, this.headers[i].length() - enclose.length()) : this.headers[i];
//            }
//
//            start = 1;
//            head_flag = false;
//          }
//
//          for(i = start; i < lineDatas.length; ++i) {
//            if (i == lineDatas.length - 1 && !line_end_flag) {
//              sepaData = lineDatas[lineDatas.length - 1];
//              break;
//            }
//
//            if (num <= lineNum) {
//              String data = lineDatas[i];
//              data = data.substring(enclose.length(), data.length());
//              if (!split_enclose.isEmpty()) {
//                data = data.endsWith(split_enclose) ? data.substring(0, data.length() - enclose.length()) : data;
//              }
//
//              content.add(new String[]{data});
//            }
//
//            ++num;
//          }
//
//          bytes = new byte[1000];
//        }
//      } else {
//        byteread = false;
//
//        label174:
//        while(true) {
//          do {
//            if ((byteread = raf.read(bytes)) == -1 && line_end_flag) {
//              break label174;
//            }
//
//            if (byteread == -1) {
//              lineData = "";
//              line_end_flag = true;
//            } else {
//              byte_len = 0;
//
//              for(lineTerminateLen = 0; lineTerminateLen < bytes.length && bytes[lineTerminateLen] != 0; ++lineTerminateLen) {
//                ++byte_len;
//              }
//
//              lineData = new String(bytes, 0, byte_len, charset);
//            }
//
//            byte_len = lineData.lastIndexOf(StringEscapeUtils.unescapeJava(lineTerminate));
//            lineTerminateLen = StringEscapeUtils.unescapeJava(lineTerminate).length();
//            if (byte_len != 0 && byte_len != -1 && byte_len + lineTerminateLen == lineData.length()) {
//              lineData = lineData.substring(0, byte_len);
//              raf.seek(raf.getFilePointer() - (long)lineTerminateLen);
//            }
//
//            sepaData = sepaData + lineData;
//            if ((long)sepaData.length() > 2000000L) {
//              MessageDialog.openWarning(container.getShell(), GBase8aMessages.import_properties_tip, GBase8aMessages.import_properties_import_max_byte_one_line);
//              content = new ArrayList();
//              break label174;
//            }
//          } while(sepaData.split(split_line_field_Terminate).length <= 1 && !line_end_flag);
//
//          start = 0;
//          lineDatas = sepaData.split(split_line_field_Terminate);
//          if (head_flag && parseOption.isHasHeader()) {
//            this.headers = lineDatas[0].split(split_enclose + split_fieldTerminate + split_enclose);
//            i = this.headers.length - 1;
//            this.headers[0] = this.headers[0].substring(enclose.length(), this.headers[0].length());
//            if (!split_enclose.isEmpty()) {
//              this.headers[i] = this.headers[i].endsWith(split_enclose) ? this.headers[i].substring(0, this.headers[i].length() - enclose.length()) : this.headers[i];
//            }
//
//            start = 1;
//            head_flag = false;
//          }
//
//          for(i = start; i < lineDatas.length; ++i) {
//            if (i == lineDatas.length - 1 && !line_end_flag) {
//              sepaData = lineDatas[lineDatas.length - 1];
//              break;
//            }
//
//            if (num <= lineNum) {
//              String[] datas = lineDatas[i].split(split_enclose + split_fieldTerminate + split_enclose);
//              int len = datas.length - 1;
//              datas[0] = datas[0].substring(enclose.length(), datas[0].length());
//              if (!split_enclose.isEmpty()) {
//                datas[len] = datas[len].endsWith(split_enclose) ? datas[len].substring(0, datas[len].length() - enclose.length()) : datas[len];
//              }
//
//              content.add(datas);
//            }
//
//            ++num;
//          }
//
//          bytes = new byte[1000];
//        }
//      }
//
//      if (this.headers != null) {
//        this.colSize = this.headers.length;
//      } else if (content.size() > 0) {
//        this.colSize = ((String[])content.get(0)).length;
//      } else {
//        this.colSize = 0;
//      }
//
//      this.rowSize = num;
//      return content;
//    } catch (Exception var29) {
//      Exception e = var29;
//      throw e;
//    }
//  }
//
//  private String escapeSpecialWord(String keyword) {
//    String[] arr = new String[]{".", "|", "*", "]", "[", "$", "^", "(", ")"};
//    String[] var6 = arr;
//    int var5 = arr.length;
//
//    for(int var4 = 0; var4 < var5; ++var4) {
//      String key = var6[var4];
//      if (keyword.contains(key)) {
//        keyword = keyword.replace(key, "\\" + key);
//      }
//    }
//
//    return keyword;
//  }
//
//  private String ChangeSplitChar(String split_char) {
//    if (split_char.equals(".")) {
//      return "\\.";
//    } else if (split_char.equals("|")) {
//      return "\\|";
//    } else if (split_char.equals("*")) {
//      return "\\*";
//    } else if (split_char.equals("\\")) {
//      return "\\\\";
//    } else if (split_char.equals("]")) {
//      return "\\]";
//    } else {
//      return split_char.equals("[") ? "\\[" : split_char;
//    }
//  }
//
//  private List<String[]> XmlParseToList(String fileName, ParseOption parseOption, int lineNum) throws Exception {
//    String charset = IConstants.CHARSET_ARRAY[parseOption.getCharset()];
//    List<String[]> content = new ArrayList();
//
//    try {
//      File f = new File(fileName);
//      SAXReader saxReader = new SAXReader();
//      Document document = null;
//      if (charset.equals("UTF8")) {
//        document = saxReader.read(f);
//      } else {
//        document = saxReader.read(new InputStreamReader(new FileInputStream(f), charset));
//      }
//
//      Element root = document.getRootElement();
//      List<Element> childList = root.elements("DATA_RECORD");
//      int num = 0;
//      Element felement;
//      Iterator iterator;
//      if (parseOption.isHasHeader() && childList.size() > 0) {
//        ArrayList<String> head_list = new ArrayList();
//        felement = (Element)childList.get(0);
//        iterator = felement.elementIterator();
//
//        while(iterator.hasNext()) {
//          Element selement = (Element)iterator.next();
//          head_list.add(selement.getName());
//        }
//
//        String[] items = new String[head_list.size()];
//        this.headers = (String[])head_list.toArray(items);
//      }
//
//      for(int i = 0; i < childList.size(); ++i) {
//        if (num <= lineNum) {
//          felement = (Element)childList.get(i);
//          iterator = felement.elementIterator();
//          ArrayList<String> content_list = new ArrayList();
//
//          while(iterator.hasNext()) {
//            Element selement = (Element)iterator.next();
//            content_list.add(selement.getText());
//          }
//
//          String[] items = new String[content_list.size()];
//          content.add((String[])content_list.toArray(items));
//        }
//
//        ++num;
//      }
//
//      if (this.headers != null) {
//        this.colSize = this.headers.length;
//      } else if (content.size() > 0) {
//        this.colSize = ((String[])content.get(0)).length;
//      } else {
//        this.colSize = 0;
//      }
//
//      this.rowSize = num;
//      return content;
//    } catch (Exception var17) {
//      Exception e = var17;
//      throw e;
//    }
//  }
//
//  public List<String[]> dataToList(String fileName, ParseOption parseOption) throws Exception {
//    List<String[]> content = new ArrayList();
//    String fileType = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
//    if (fileType.toUpperCase().equals("TXT")) {
//      content = this.TxtDataToList(fileName, parseOption);
//    } else if (fileType.toUpperCase().equals("CSV")) {
//      content = this.CsvDataToList(fileName, parseOption);
//    } else if (fileType.toUpperCase().equals("XML")) {
//      content = this.XmlDataToList(fileName, parseOption);
//    } else if (fileType.toUpperCase().equals("XLS")) {
//      content = this.XlsParseToList(fileName, parseOption, 0);
//    } else if (fileType.toUpperCase().equals("XLSX")) {
//      ExcelXlsxReader excelXlsxReader = new ExcelXlsxReader(parseOption);
//      this.rowSize = excelXlsxReader.process(fileName);
//      content = excelXlsxReader.getRows();
//    }
//
//    return (List)content;
//  }
//
//  private List<String[]> TxtDataToList(String fileName, ParseOption parseOption) throws Exception {
//    File file = new File(fileName);
//    String charset = IConstants.CHARSET_ARRAY[parseOption.getCharset()];
//    List<String[]> content = new ArrayList();
//    RandomAccessFile raf = new RandomAccessFile(file, "r");
//    byte[] bytes = new byte[1000];
//    String lineData = "";
//    String sepaData = "";
//    String enclose = parseOption.getEnclose() == null ? "" : parseOption.getEnclose();
//    String fieldTerminate = parseOption.getFieldTerminate();
//    String lineTerminate = parseOption.getLineTerminate();
//    String split_fieldTerminate = this.escapeSpecialWord(fieldTerminate);
//    String split_line_field_Terminate = this.escapeSpecialWord(enclose + lineTerminate);
//    String split_enclose = this.ChangeSplitChar(enclose);
//
//    try {
//      int num = 0;
//      boolean head_flag = true;
//      boolean line_end_flag = false;
//      boolean all_end_flag = false;
//      long position = parseOption.getPosition();
//      int startLineNum = parseOption.getStartLineNum();
//      int endLineNum = parseOption.getEndLineNum();
//      long newPosition = 0L;
//      raf.seek(position);
//      boolean byteread;
//      int last_pos;
//      int lineTerminateLen;
//      byte start;
//      String[] lineDatas;
//      int i;
//      int byteread;
//      if (fieldTerminate.isEmpty()) {
//        byteread = false;
//
//        label223:
//        while(true) {
//          do {
//            if ((byteread = raf.read(bytes)) == -1 && line_end_flag || all_end_flag) {
//              break label223;
//            }
//
//            if (byteread == -1) {
//              lineData = "";
//              line_end_flag = true;
//            } else {
//              last_pos = 0;
//
//              for(lineTerminateLen = 0; lineTerminateLen < bytes.length && bytes[lineTerminateLen] != 0; ++lineTerminateLen) {
//                ++last_pos;
//              }
//
//              lineData = new String(bytes, 0, last_pos, charset);
//            }
//
//            sepaData = sepaData + lineData;
//            if (sepaData.isEmpty()) {
//              break label223;
//            }
//
//            last_pos = lineData.lastIndexOf(StringEscapeUtils.unescapeJava(lineTerminate));
//            lineTerminateLen = StringEscapeUtils.unescapeJava(lineTerminate).length();
//            if (last_pos != 0 && last_pos != -1 && last_pos + lineTerminateLen == lineData.length()) {
//              lineData = lineData.substring(0, last_pos);
//              raf.seek(raf.getFilePointer() - (long)lineTerminateLen);
//            }
//          } while(sepaData.split(split_line_field_Terminate).length <= 1 && !line_end_flag);
//
//          start = 0;
//          lineDatas = sepaData.split(split_line_field_Terminate);
//          if (startLineNum == 0 && head_flag && parseOption.isHasHeader()) {
//            head_flag = false;
//            start = 1;
//          }
//
//          for(i = start; i < lineDatas.length; ++i) {
//            if (i == lineDatas.length - 1 && !line_end_flag) {
//              sepaData = lineDatas[lineDatas.length - 1];
//              break;
//            }
//
//            String data = lineDatas[i];
//            data = data.substring(enclose.length(), data.length());
//            if (!split_enclose.isEmpty()) {
//              data = data.endsWith(split_enclose) ? data.substring(0, data.length() - enclose.length()) : data;
//            }
//
//            content.add(new String[]{data});
//            ++num;
//          }
//
//          if (startLineNum + num >= endLineNum) {
//            newPosition = raf.getFilePointer() - (long)(lineData.length() - last_pos);
//            line_end_flag = true;
//            all_end_flag = true;
//            sepaData = "";
//            lineData = "";
//          }
//
//          bytes = new byte[1000];
//        }
//      } else {
//        byteread = false;
//
//        label185:
//        while(true) {
//          do {
//            if ((byteread = raf.read(bytes)) == -1 && line_end_flag || all_end_flag) {
//              break label185;
//            }
//
//            if (byteread == -1) {
//              lineData = "";
//              line_end_flag = true;
//            } else {
//              last_pos = 0;
//
//              for(lineTerminateLen = 0; lineTerminateLen < bytes.length && bytes[lineTerminateLen] != 0; ++lineTerminateLen) {
//                ++last_pos;
//              }
//
//              lineData = new String(bytes, 0, last_pos, charset);
//            }
//
//            sepaData = sepaData + lineData;
//            if (sepaData.isEmpty()) {
//              break label185;
//            }
//
//            last_pos = lineData.lastIndexOf(StringEscapeUtils.unescapeJava(lineTerminate));
//            lineTerminateLen = StringEscapeUtils.unescapeJava(lineTerminate).length();
//            if (last_pos != 0 && last_pos != -1 && last_pos + lineTerminateLen == lineData.length()) {
//              lineData = lineData.substring(0, last_pos);
//              raf.seek(raf.getFilePointer() - (long)lineTerminateLen);
//            }
//          } while(sepaData.split(split_line_field_Terminate).length <= 1 && !line_end_flag);
//
//          start = 0;
//          lineDatas = sepaData.split(split_line_field_Terminate);
//          if (startLineNum == 0 && head_flag && parseOption.isHasHeader()) {
//            start = 1;
//            head_flag = false;
//          }
//
//          for(i = start; i < lineDatas.length; ++i) {
//            if (!lineDatas[i].isEmpty()) {
//              if (i == lineDatas.length - 1 && !line_end_flag) {
//                sepaData = lineDatas[lineDatas.length - 1];
//                break;
//              }
//
//              String[] datas = lineDatas[i].split(split_enclose + split_fieldTerminate + split_enclose);
//              int len = datas.length - 1;
//              datas[0] = datas[0].substring(enclose.length(), datas[0].length());
//              if (!split_enclose.isEmpty()) {
//                datas[len] = datas[len].endsWith(split_enclose) ? datas[len].substring(0, datas[len].length() - enclose.length()) : datas[len];
//              }
//
//              content.add(datas);
//              ++num;
//            }
//          }
//
//          if (startLineNum + num >= endLineNum) {
//            newPosition = !sepaData.equals("") && lineData.substring(last_pos, lineData.length()).contains(sepaData) ? raf.getFilePointer() - (long)(lineData.length() - last_pos) : raf.getFilePointer();
//            line_end_flag = true;
//            all_end_flag = true;
//            sepaData = "";
//            lineData = "";
//          }
//
//          bytes = new byte[1000];
//        }
//      }
//
//      newPosition = newPosition == 0L ? raf.getFilePointer() : newPosition;
//      parseOption.setPosition(newPosition);
//      if (this.headers != null) {
//        this.colSize = this.headers.length;
//      } else if (content.size() > 0) {
//        this.colSize = ((String[])content.get(0)).length;
//      } else {
//        this.colSize = 0;
//      }
//
//      this.rowSize = num;
//      return content;
//    } catch (Exception var34) {
//      Exception e = var34;
//      throw e;
//    }
//  }
//
//  private List<String[]> CsvDataToList(String fileName, ParseOption parseOption) throws Exception {
//    File file = new File(fileName);
//    String charset = IConstants.CHARSET_ARRAY[parseOption.getCharset()];
//    List<String[]> content = new ArrayList();
//    new ArrayList();
//    InputStreamReader freader = null;
//    ICsvListReader csvReader = null;
//
//    try {
//      int startLineNum = parseOption.getStartLineNum();
//      int endLineNum = parseOption.getEndLineNum();
//      freader = new InputStreamReader(new FileInputStream(file), charset);
//      csvReader = new CsvListReader(freader, CsvPreference.STANDARD_PREFERENCE);
//      if (parseOption.isHasHeader()) {
//        this.headers = csvReader.getHeader(true);
//      }
//
//      List line;
//      int num;
//      for(num = 0; (line = csvReader.read()) != null; ++num) {
//        if (num >= startLineNum && num < endLineNum) {
//          content.add((String[])line.toArray(new String[0]));
//        } else if (num >= endLineNum) {
//          break;
//        }
//      }
//
//      if (this.headers != null) {
//        this.colSize = this.headers.length;
//      } else if (content.size() > 0) {
//        this.colSize = ((String[])content.get(0)).length;
//      } else {
//        this.colSize = 0;
//      }
//
//      this.rowSize = num;
//    } catch (Exception var15) {
//      Exception e = var15;
//      throw e;
//    } finally {
//      if (freader != null) {
//        freader.close();
//      }
//
//      if (csvReader != null) {
//        csvReader.close();
//      }
//
//    }
//
//    return content;
//  }
//
//  private List<String[]> XmlDataToList(String fileName, ParseOption parseOption) throws Exception {
//    String charset = IConstants.CHARSET_ARRAY[parseOption.getCharset()];
//    List<String[]> content = new ArrayList();
//
//    try {
//      int startLineNum = parseOption.getStartLineNum();
//      int endLineNum = parseOption.getEndLineNum();
//      int num = 0;
//      File f = new File(fileName);
//      SAXReader saxReader = new SAXReader();
//      Document document = null;
//      if (charset.equals("UTF8")) {
//        document = saxReader.read(f);
//      } else {
//        document = saxReader.read(new InputStreamReader(new FileInputStream(f), charset));
//      }
//
//      Element root = document.getRootElement();
//      List<Element> childList = root.elements("DATA_RECORD");
//
//      for(int i = 0; i < childList.size(); ++i) {
//        if (num >= startLineNum && num < endLineNum) {
//          Element felement = (Element)childList.get(i);
//          Iterator<Element> iterator = felement.elementIterator();
//          ArrayList<String> content_list = new ArrayList();
//
//          while(iterator.hasNext()) {
//            Element selement = (Element)iterator.next();
//            content_list.add(selement.getText());
//          }
//
//          String[] items = new String[content_list.size()];
//          content.add((String[])content_list.toArray(items));
//        } else if (num >= endLineNum) {
//          break;
//        }
//
//        ++num;
//      }
//
//      if (content.size() > 0) {
//        this.colSize = ((String[])content.get(0)).length;
//      } else {
//        this.colSize = 0;
//      }
//
//      this.rowSize = num;
//      return content;
//    } catch (Exception var18) {
//      Exception e = var18;
//      throw e;
//    }
//  }
//
//  public String[] getHeaders() {
//    return this.headers;
//  }
//
//  public int getColSize() {
//    return this.colSize;
//  }
//
//  public int getRowSize() {
//    return this.rowSize;
//  }
//
//  private List<String[]> XlsParseToList(String fileName, ParseOption parseOption, int lineNum) throws Exception {
//    new ArrayList();
//    Workbook workBook = null;
//    File file = new File(fileName);
//    if (file.getName().endsWith(".xls")) {
//      workBook = new HSSFWorkbook(new FileInputStream(file));
//    } else if (file.getName().endsWith(".xlsx")) {
//      workBook = new XSSFWorkbook(new FileInputStream(file));
//    }
//
//    if (lineNum > 0) {
//      parseOption.setEndLineNum(lineNum);
//    }
//
//    List<String[]> content = this.readXlsRow((Workbook)workBook, parseOption);
//    return content;
//  }
//
//  private List<String[]> readXlsRow(Workbook workBook, ParseOption parseOption) {
//    List<String[]> content = new ArrayList();
//    int startLineNum = parseOption.getStartLineNum();
//    int endLineNum = parseOption.getEndLineNum();
//    int num = 0;
//    boolean head_flag = true;
//    this.rowSize = 0;
//    this.colSize = parseOption.getColumnNum();
//    int numberOfSheets = workBook.getNumberOfSheets();
//
//    for(int i = 0; i < numberOfSheets; ++i) {
//      Sheet sheet = workBook.getSheetAt(i);
//      int start = 0;
//      if (startLineNum == 0 && head_flag && parseOption.isHasHeader() && i == 0) {
//        this.rowSize += sheet.getLastRowNum();
//        Row header = sheet.getRow(0);
//        if (header != null) {
//          this.colSize = header.getLastCellNum();
//          this.headers = new String[this.colSize];
//
//          for(int j = 0; j < this.colSize; ++j) {
//            header.getCell(j).setCellType(CellType.STRING);
//            this.headers[j] = header.getCell(j).getStringCellValue();
//          }
//        }
//
//        head_flag = false;
//        start = 1;
//      } else {
//        this.rowSize += sheet.getLastRowNum() + 1;
//      }
//
//      for(int k = start; k <= sheet.getLastRowNum(); ++k) {
//        if ((num < startLineNum || num >= endLineNum) && (content.size() < startLineNum || content.size() >= endLineNum)) {
//          if (num >= endLineNum) {
//            break;
//          }
//        } else {
//          Row row = sheet.getRow(k);
//          if (row != null) {
//            row.getLastCellNum();
//            String[] list = new String[this.colSize];
//            int t = 0;
//
//            label147:
//            while(true) {
//              if (t >= this.colSize) {
//                t = 0;
//
//                while(true) {
//                  if (t >= list.length) {
//                    break label147;
//                  }
//
//                  if (!list[t].equals("")) {
//                    content.add(list);
//                    break label147;
//                  }
//
//                  ++t;
//                }
//              }
//
//              Cell cell = row.getCell(t);
//              if (cell == null) {
//                list[t] = "";
//              } else if (CellType.NUMERIC == cell.getCellTypeEnum()) {
//                short format;
//                double d;
//                SimpleDateFormat sdf;
//                if (HSSFDateUtil.isCellDateFormatted(cell)) {
//                  format = cell.getCellStyle().getDataFormat();
//                  cell.getCellStyle().getDataFormatString();
//                  d = cell.getNumericCellValue();
//                  sdf = null;
//                  if (format != 14 && format != 31 && format != 57 && format != 58) {
//                    if (format != 20 && format != 32) {
//                      if (format == 17) {
//                        sdf = new SimpleDateFormat("yyyy-MM");
//                        list[t] = sdf.format(DateUtil.getJavaDate(d));
//                      } else if (format == 22) {
//                        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
//                        list[t] = sdf.format(DateUtil.getJavaDate(d));
//                      } else if (format == 47) {
//                        sdf = new SimpleDateFormat("mm:ss");
//                        list[t] = sdf.format(DateUtil.getJavaDate(d));
//                      } else if (format == 21) {
//                        sdf = new SimpleDateFormat("HH:mm:ss");
//                        list[t] = sdf.format(DateUtil.getJavaDate(d));
//                      } else {
//                        list[t] = cell.toString();
//                      }
//                    } else {
//                      sdf = new SimpleDateFormat("HH:mm");
//                      list[t] = sdf.format(DateUtil.getJavaDate(d));
//                    }
//                  } else {
//                    sdf = new SimpleDateFormat("yyyy-MM-dd");
//                    list[t] = sdf.format(DateUtil.getJavaDate(d));
//                  }
//                } else {
//                  format = cell.getCellStyle().getDataFormat();
//                  d = cell.getNumericCellValue();
//                  sdf = null;
//                  if (format != 14 && format != 31 && format != 57 && format != 58) {
//                    if (format != 20 && format != 32) {
//                      cell.setCellType(CellType.STRING);
//                      String ds = cell.getStringCellValue();
//                      list[t] = ds.trim();
//                    } else {
//                      sdf = new SimpleDateFormat("HH:mm");
//                      list[t] = sdf.format(DateUtil.getJavaDate(d));
//                    }
//                  } else {
//                    sdf = new SimpleDateFormat("yyyy-MM-dd");
//                    list[t] = sdf.format(DateUtil.getJavaDate(d));
//                  }
//                }
//              } else if (CellType.STRING == cell.getCellTypeEnum()) {
//                list[t] = cell.getStringCellValue();
//              } else if (CellType.BOOLEAN == cell.getCellTypeEnum()) {
//                list[t] = String.valueOf(cell.getBooleanCellValue());
//              } else if (CellType.FORMULA == cell.getCellTypeEnum()) {
//                list[t] = cell.getCellFormula();
//              } else if (CellType.BLANK == cell.getCellTypeEnum()) {
//                list[t] = "";
//              } else if (CellType.ERROR == cell.getCellTypeEnum()) {
//                list[t] = "";
//              } else {
//                list[t] = "";
//              }
//
//              ++t;
//            }
//          }
//        }
//
//        ++num;
//      }
//
//      if (this.colSize == 0 && content.size() > 0) {
//        this.colSize = ((String[])content.get(0)).length;
//      }
//    }
//
//    return content;
//  }
//}
