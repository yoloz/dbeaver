package org.jkiss.dbeaver.ext.gbase8a;

import java.util.StringTokenizer;

public class GBase8aQueryTokenizer {
    private static String _alternateQuerySeparator;
    private static String _querySeparator;
    private String _sQuerys;
    private String _sNextQuery;
    private String _sDelimiterCommand;
    private String _solComment;
    private String multiCommentPrefix = "/*";
    private String multiCommentRegex;

    public GBase8aQueryTokenizer(String sql, String querySeparator, String alternateSeparator, String solComment, String multiCommentRegex) {
        if (querySeparator != null && querySeparator.trim().length() > 0) {
            _querySeparator = querySeparator.trim();
        } else {
            _querySeparator = ";";
        }

        if (alternateSeparator != null && alternateSeparator.trim().length() > 0) {
            _alternateQuerySeparator = alternateSeparator;
        } else {
            _alternateQuerySeparator = null;
        }

        if (solComment != null && solComment.trim().length() > 0) {
            this._solComment = solComment;
        } else {
            this._solComment = null;
        }

        this.multiCommentRegex = multiCommentRegex;
        this._sDelimiterCommand = "DELIMITER ";
        if (sql != null) {
            this._sQuerys = this.prepareSQL(sql);
            this._sNextQuery = this.parse();
        } else {
            this._sQuerys = "";
        }

    }

    public boolean hasQuery() {
        return this._sNextQuery != null;
    }

    public String nextQuery() {
        String sReturnQuery = this._sNextQuery;
        this._sNextQuery = this.parse();
        return sReturnQuery;
    }

    private int findFirstSeparator() {
        String separator = _querySeparator;
        int separatorLength = _querySeparator.length();
        int iQuoteCount = 1;
        int iIndex1 = 0 - separatorLength;

        while (iQuoteCount % 2 != 0) {
            iQuoteCount = 0;
            iIndex1 = this._sQuerys.indexOf(separator, iIndex1 + separatorLength);
            if (iIndex1 <= -1) {
                return -1;
            }

            for (int iIndex2 = this._sQuerys.lastIndexOf(39, iIndex1 + separatorLength - 1); iIndex2 != -1; iIndex2 = this._sQuerys.lastIndexOf(39, iIndex2 - 1)) {
                ++iQuoteCount;
            }
        }

        return iIndex1;
    }

    private int findFirstAlternateSeparator() {
        if (_alternateQuerySeparator == null) {
            return -1;
        } else {
            String separator = _alternateQuerySeparator;
            int separatorLength = _alternateQuerySeparator.length();
            int iQuoteCount = 1;
            int iIndex1 = 0 - separatorLength;

            while (iQuoteCount % 2 != 0) {
                iQuoteCount = 0;
                iIndex1 = this._sQuerys.indexOf(separator, iIndex1 + separatorLength);
                if (iIndex1 <= -1) {
                    return -1;
                }

                for (int iIndex2 = this._sQuerys.lastIndexOf(39, iIndex1 + separatorLength - 1); iIndex2 != -1; iIndex2 = this._sQuerys.lastIndexOf(39, iIndex2 - 1)) {
                    ++iQuoteCount;
                }
            }

            return iIndex1;
        }
    }

    private boolean validateDelimiter(String delimiter) {
        return delimiter.length() > 0;
    }

    public String parse() {
        if (this._sQuerys.length() == 0) {
            return null;
        } else {
            int pos = this._sQuerys.indexOf("\n");
            String lineStr = this._sQuerys;
            if (pos > -1) {
                lineStr = this._sQuerys.substring(0, pos).trim();
            }

            String separator;
            if (lineStr.length() >= this._sDelimiterCommand.length() && lineStr.substring(0, this._sDelimiterCommand.length()).equalsIgnoreCase(this._sDelimiterCommand)) {
                separator = lineStr.substring(this._sDelimiterCommand.length()).trim();
                if (this.validateDelimiter(separator)) {
                    _querySeparator = separator;
                    if (pos > -1) {
                        this._sQuerys = this._sQuerys.substring(pos + 1);
                    } else {
                        this._sQuerys = "";
                    }

                    return this.parse();
                }
            }

            separator = _querySeparator;
            int indexSep = this.findFirstSeparator();
            int indexAltSep = this.findFirstAlternateSeparator();
            if (indexAltSep > -1 && (indexSep < 0 || indexAltSep < indexSep)) {
                separator = _alternateQuerySeparator;
            }

            int separatorLength = separator.length();
            int iQuoteCount = 1;
            int iIndex1 = 0 - separatorLength;

            String sNextQuery;
            while (iQuoteCount % 2 != 0) {
                iQuoteCount = 0;
                iIndex1 = this._sQuerys.indexOf(separator, iIndex1 + separatorLength);
                if (iIndex1 <= -1) {
                    sNextQuery = this._sQuerys;
                    this._sQuerys = "";
                    if (this._solComment != null && sNextQuery.startsWith(this._solComment)) {
                        return this.parse();
                    }

                    return this.replaceLineFeeds(sNextQuery);
                }

                for (int iIndex2 = this._sQuerys.lastIndexOf(39, iIndex1 + separatorLength - 1); iIndex2 != -1; iIndex2 = this._sQuerys.lastIndexOf(39, iIndex2 - 1)) {
                    ++iQuoteCount;
                }
            }

            sNextQuery = this._sQuerys.substring(0, iIndex1);
            this._sQuerys = this._sQuerys.substring(iIndex1 + separatorLength).trim();
            if (this._sQuerys.trim().startsWith(this.multiCommentPrefix)) {
                this._sQuerys = this._sQuerys.trim().replaceFirst(this.multiCommentRegex, "").trim();
            }

            if (this._solComment != null && this._sQuerys.startsWith(this._solComment)) {
                this._sQuerys = this.prepareSQL(this._sQuerys);
            }

            if (this._solComment != null && sNextQuery.startsWith(this._solComment)) {
                return this.parse();
            } else {
                return this.replaceLineFeeds(sNextQuery);
            }
        }
    }

    private String prepareSQL(String sql) {
        StringBuffer results = new StringBuffer(1024);
        boolean isSqlStart = true;

        while (isSqlStart) {
            int j = 0;
            if (sql.trim().startsWith(this.multiCommentPrefix)) {
                sql = sql.trim().replaceFirst(this.multiCommentRegex, "");
            }

            String[] lines = sql.trim().split("\n");
            String[] var9 = lines;
            int var8 = lines.length;

            for (int var7 = 0; var7 < var8; ++var7) {
                String line = var9[var7];
                if (j == 0) {
                    if (line.trim().startsWith(this._solComment) || line.trim().length() == 0) {
                        sql = sql.substring(line.trim().length());
                        continue;
                    }

                    if (line.startsWith(this.multiCommentPrefix)) {
                        break;
                    }
                }

                results.append(line).append('\n');
                ++j;
                isSqlStart = false;
            }

            StringTokenizer tok = new StringTokenizer(sql.trim(), "\n", false);
            if (!tok.hasMoreTokens()) {
                isSqlStart = false;
            }
        }

        return results.toString();
    }

    private String replaceLineFeeds(String sql) {
        StringBuffer sbReturn = new StringBuffer();
        int iPrev = 0;
        int linefeed = sql.indexOf(10);
        int iQuote = -1;

        while (true) {
            while (linefeed != -1) {
                iQuote = sql.indexOf(39, iQuote + 1);
                if (iQuote != -1 && iQuote < linefeed) {
                    int iNextQute = sql.indexOf(39, iQuote + 1);
                    if (iNextQute > linefeed) {
                        sbReturn.append(sql.substring(iPrev, linefeed));
                        sbReturn.append('\n');
                        iPrev = linefeed + 1;
                        linefeed = sql.indexOf(10, iPrev);
                    }
                } else {
                    linefeed = sql.indexOf(10, linefeed + 1);
                }
            }

            sbReturn.append(sql.substring(iPrev));
            return sbReturn.toString();
        }
    }
}
