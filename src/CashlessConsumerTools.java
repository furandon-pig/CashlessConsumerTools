/*
 * Copyright (c) 2019  furandon-pig  All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.*;

public class CashlessConsumerTools {
    public static final int REPLACE_FROM_HEAD = 1;
    public static final int REPLACE_FROM_TAIL = 2;

    public static final int NOT_PARSE_YET = 0; // 未パース状態
    public static final int KOTEI_TENPO = 1;  // 固定店舗（EC・通信販売を除く）
    public static final int EC_RAKUTEN = 2;   // EC・通信販売（楽天市場）
    public static final int EC_YAHOO = 3;     // EC・通信販売（Yahoo!ショッピング）
    public static final int EC_OTHER = 4;     // EC・通信販売（その他ＥＣサイト）

    private StringBuffer[] buf = new StringBuffer[EC_OTHER+1];
    private String[] headers = {
      "",
      "# 固定店舗（EC・通信販売を除く）\n# <No.>,<都道府県>,<市区町村>,<事業所名（屋号）>,<業種>,<還元率>\n",
      "# EC・通信販売（楽天市場）\n# <No.>,<事業所名（屋号）>,<還元率>\n",
      "# EC・通信販売（Yahoo!ショッピング）\n# <No.>,<事業所名（屋号）>,<還元率>\n",
      "# EC・通信販売（その他ＥＣサイト）\n# <No.>,<事業所名（屋号）>,<還元率>\n",
    };

    public CashlessConsumerTools() {
        for (int i = 0; i < buf.length; i++) {
            buf[i] = new StringBuffer(headers[i]);
        }
    }

    public String pdf2text(String file_path) throws IOException {
        // PDFをロードし、内容を展開する権限がなかったらエラーにする。
        PDDocument document = PDDocument.load(new File(file_path));
        AccessPermission ap = document.getCurrentAccessPermission();
        if (! ap.canExtractContent()) {
            throw new IOException("You do not have permission to extract text.");
        }

        // PDFを展開する準備。
        PDFTextStripper stripper = new PDFTextStripper();
        stripper.setSortByPosition(true);

        // PDFからテキストのみを抽出し、ファイルに書き込むための準備。
        String text_file_name = file_path + ".txt";
        File f = new File(file_path + ".txt");
        FileOutputStream fos = new FileOutputStream(f);
        PrintWriter pw = new PrintWriter(fos);

        // 100ページ単位でPDFからテキストを抽出し、ファイルに書き込んでゆく。
        final int parse_size = 100;
        StringBuffer buf = new StringBuffer();
        for (int p = 1; p <= document.getNumberOfPages(); p += parse_size) {
            stripper.setStartPage(p);
            stripper.setEndPage(p + (parse_size - 1));
            buf.setLength(0);

            buf.append(stripper.getText(document).trim());

            pw.println(buf.toString());
        }
        pw.close();
        fos.close();

        return text_file_name;
    }

    public StringBuffer replaceSpace(StringBuffer str, int replace_max, int replace_direction) {
        int replaced = 0;
        if (replace_direction == REPLACE_FROM_HEAD) {
            for (int i = 0; i < str.length(); i++) {
                if (str.charAt(i) == ' ') {
                    str.setCharAt(i, ',');
                    replaced++;
                }
                if (replaced >= replace_max) { break; }
            }
        } else {
            for (int i = str.length() - 1; i > 0; i--) {
                if (str.charAt(i) == ' ') {
                    str.setCharAt(i, ',');
                    replaced++;
                }
                if (replaced >= replace_max) { break; }
            }
        }

        return str;
    }

    public void saveFile(String file_name, StringBuffer buf) throws IOException {
        File f = new File(file_name);
        FileOutputStream fos = new FileOutputStream(f);
        PrintWriter pw = new PrintWriter(fos);

        pw.println(buf.toString());

        pw.close();
        fos.close();

        System.out.println(file_name + " を生成しました。");
    }

    public void parseText(String text_file) throws IOException {
        FileReader fr = new FileReader(text_file);
        BufferedReader br = new BufferedReader(fr);
        String readLine;

        // PDFで市区町村名と店名がくっついてパースされるデータを修正するためのデータ。
        Pattern patterns[] = {
                Pattern.compile("^(.*さいたま市浦和区)(.*)"),
                Pattern.compile("^(.*さいたま市岩槻区)(.*)"),
                Pattern.compile("^(.*さいたま市見沼区)(.*)"),
                Pattern.compile("^(.*さいたま市大宮区)(.*)"),
                Pattern.compile("^(.*さいたま市中央区)(.*)"),
                Pattern.compile("^(.*横浜市保土ケ谷区)(.*)"),
                Pattern.compile("^(.*北九州市小倉南区)(.*)"),
                Pattern.compile("^(.*北九州市小倉北区)(.*)"),
                Pattern.compile("^(.*北九州市八幡西区)(.*)"),
                Pattern.compile("^(.*北九州市八幡東区)(.*)")
        };

        String oldstr = "";
        String curstr = "";

        int current_data_type = NOT_PARSE_YET;
        while ((readLine = br.readLine()) != null) {
            String line = readLine.toString();

            if (line.matches("②固定店舗（EC・通信販売を除く） 令和元年\\d+月\\d+日　現在")) {
                current_data_type = KOTEI_TENPO;
                curstr = "②固定店舗（EC・通信販売を除く）";
            } else if (line.matches("③EC・通信販売（楽天市場） 令和元年\\d+月\\d+日　現在")) {
                current_data_type = EC_RAKUTEN;
                curstr = "③EC・通信販売（楽天市場）";
            } else if (line.matches("④EC・通信販売（Yahoo!ショッピング） 令和元年\\d+月\\d+日　現在")) {
                current_data_type = EC_YAHOO;
                curstr = "④EC・通信販売（Yahoo!ショッピング）";
            } else if (line.matches("⑤EC・通信販売（その他ＥＣサイト） 令和元年\\d+月\\d+日　現在")) {
                current_data_type = EC_OTHER;
                curstr = "⑤EC・通信販売（その他ＥＣサイト）";
            }

            if (! oldstr.equals(curstr)) {
                oldstr = curstr;
                System.out.println(curstr);
            }

            if (line.matches(".*\\d+%$")) {
                line = line.replaceFirst(",", ""); // 連番に付いているカンマ(",")を除去する。
                line = line.replaceFirst("%$", ""); //
                StringBuffer line2 = new StringBuffer(line);

                for (Pattern pattern : patterns) {
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.matches()) {
                        // 市区町村名と店名がくっついているケースにマッチしたら空白で分離した文字列にする。
                        line2 = new StringBuffer(matcher.group(1) + " " + matcher.group(2));
                    }
                }

                switch (current_data_type) {
                    case KOTEI_TENPO: // 「①固定店舗（EC・通信販売を除く）」の場合は6カラム
                        // 文字列の先頭と末尾から3カラムをカンマ区切りに変換する。
                        // 変換前： 8195 岩手県 花巻市 賢治最中本舗 サービス 飲食業 5%
                        // 変換後： 8195,岩手県,花巻市,賢治最中本舗,サービス,飲食業,5%
                        line2 = replaceSpace(line2, 3, REPLACE_FROM_HEAD);
                        line2 = replaceSpace(line2, 3, REPLACE_FROM_TAIL);

                        buf[KOTEI_TENPO].append(line2);
                        buf[KOTEI_TENPO].append("\n");
                        break;
                    case EC_RAKUTEN:
                    case EC_YAHOO:
                    case EC_OTHER:  // それ以外の場合は「②EC・通信販売（楽天市場）」「③EC・通信販売（Yahoo!ショッピング）」「④EC・通信販売（その他ＥＣサイト）」
                        line2 = replaceSpace(line2, 1, REPLACE_FROM_HEAD);
                        line2 = replaceSpace(line2, 1, REPLACE_FROM_TAIL);

                        buf[current_data_type].append(line2);
                        buf[current_data_type].append("\n");
                        break;
                    default:
                        break;
                }
            }
        }

        // 加盟店の分類毎にCSVを生成する。
        saveFile("kotei_tempo.csv", buf[KOTEI_TENPO]);
        saveFile("ec_rakukten.csv", buf[EC_RAKUTEN]);
        saveFile("ec_yahoo.csv", buf[EC_YAHOO]);
        saveFile("ec_other.csv", buf[EC_OTHER]);
    }

    public static void main(String... args) throws IOException {
        CashlessConsumerTools cct= new CashlessConsumerTools();

        cct.pdf2text("kameiten_touroku_list.pdf");
        cct.parseText("kameiten_touroku_list.pdf.txt");
    }
}
