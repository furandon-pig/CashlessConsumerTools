# CashlessConsumerTools

## 概要

[キャッシュレス消費者還元事業](https://cashless.go.jp/consumer/index.html)のWebサイトで公開されている[PDF](https://cashless.go.jp/assets/doc/kameiten_touroku_list.pdf)をCSVにコンバートするツールです。

## 使い方

以下はLinux環境での使い方です。あらかじめJDK(Java Development Kit)をインストールしておいてください。

```
$ git clone https://github.com/furandon-pig/CashlessConsumerTools.git
$ cd CashlessConsumerTools
$ ./setup.sh
$ export CLASSPATH=src:lib/pdfbox-2.0.16.jar:lib/fontbox-2.0.16.jar:lib/commons-logging-1.2.jar
$ javac src/CashlessConsumerTools.java
$ java CashlessConsumerTools
```

以下のようなCSVが生成されます。

```
$ ls *.csv
ec_other.csv  ec_rakukten.csv  ec_yahoo.csv  kotei_tempo.csv
$
$ head kotei_tempo.csv
# 固定店舗（EC・通信販売を除く）
# <No.>,<都道府県>,<市区町村>,<事業所名（屋号）>,<業種>,<還元率>
1,北海道,愛別町,セブン－イレブン愛別町店,小売業,食料品,2
2,北海道,愛別町,伊藤新聞販売所,小売業,その他小売,5
3,北海道,愛別町,三愛自動車工業株式会社,その他業種,ー,5
4,北海道,赤平市,赤平　ＳＳ,小売業,ガソリンスタンド,2
5,北海道,赤平市,赤平平岸ＳＳ,小売業,ガソリンスタンド,2
6,北海道,赤平市,セブン－イレブン赤平文京町店,小売業,食料品,2
7,北海道,赤平市,セブン－イレブン赤平茂尻店,小売業,食料品,2
8,北海道,赤平市,出光茂尻ＳＳ,小売業,ガソリンスタンド,2
```

CSVからDBに投入したり、JSONフォーマットに変換することを想定し、PDFからCSVに変換する際に以下の処理を加えています。

 *「No.」の3桁区切りカンマを除去しています。
 * 「還元率」の単位として付与されていたパーセント( `%` )文字を除去しています。

