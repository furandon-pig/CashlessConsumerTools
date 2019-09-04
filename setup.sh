#!/bin/sh

# キャッシュレス消費者還元事業のWebサイトからPDFをダウンロードする。
curl -L -O https://cashless.go.jp/assets/doc/kameiten_touroku_list.pdf

# PDF処理ライブラリを用意する。
[ ! -d lib ] && mkdir lib

# PDFBoxをダウンロードする。
[ ! -f lib/pdfbox-2.0.16.jar  ] && curl -L -o lib/pdfbox-2.0.16.jar  http://ftp.kddilabs.jp/infosystems/apache/pdfbox/2.0.16/pdfbox-2.0.16.jar
[ ! -f lib/fontbox-2.0.16.jar ] && curl -L -o lib/fontbox-2.0.16.jar http://ftp.jaist.ac.jp/pub/apache/pdfbox/2.0.16/fontbox-2.0.16.jar

# PDFBoxはApache Commonsも必要とするのでこちらもダウンロードする。
# tarで固められた状態で配布されているので、必要なJarファイルのみlibディレクトリにコピーする。
if [ ! -f libs/commons-logging-1.2.jar ]; then
  mkdir -p tmp
  pushd tmp
  curl -L -O http://ftp.riken.jp/net/apache//commons/logging/binaries/commons-logging-1.2-bin.tar.gz
  tar zxvf commons-logging-1.2-bin.tar.gz commons-logging-1.2/commons-logging-1.2.jar
  mv commons-logging-1.2/commons-logging-1.2.jar ../lib
  popd
fi
