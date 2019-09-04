#!/bin/sh

# $B%-%c%C%7%e%l%9>CHq<T4T85;v6H$N(BWeb$B%5%$%H$+$i(BPDF$B$r%@%&%s%m!<%I$9$k!#(B
curl -L -O https://cashless.go.jp/assets/doc/kameiten_touroku_list.pdf

# PDF$B=hM}%i%$%V%i%j$rMQ0U$9$k!#(B
[ ! -d lib ] && mkdir lib

# PDFBox$B$r%@%&%s%m!<%I$9$k!#(B
[ ! -f lib/pdfbox-2.0.16.jar  ] && curl -L -o lib/pdfbox-2.0.16.jar  http://ftp.kddilabs.jp/infosystems/apache/pdfbox/2.0.16/pdfbox-2.0.16.jar
[ ! -f lib/fontbox-2.0.16.jar ] && curl -L -o lib/fontbox-2.0.16.jar http://ftp.jaist.ac.jp/pub/apache/pdfbox/2.0.16/fontbox-2.0.16.jar

# PDFBox$B$O(BApache Commons$B$bI,MW$H$9$k$N$G$3$A$i$b%@%&%s%m!<%I$9$k!#(B
# tar$B$G8G$a$i$l$?>uBV$GG[I[$5$l$F$$$k$N$G!"I,MW$J(BJar$B%U%!%$%k$N$_(Blib$B%G%#%l%/%H%j$K%3%T!<$9$k!#(B
if [ ! -f libs/commons-logging-1.2.jar ]; then
  mkdir -p tmp
  pushd tmp
  curl -L -O http://ftp.riken.jp/net/apache//commons/logging/binaries/commons-logging-1.2-bin.tar.gz
  tar zxvf commons-logging-1.2-bin.tar.gz commons-logging-1.2/commons-logging-1.2.jar
  mv commons-logging-1.2/commons-logging-1.2.jar ../lib
  popd
fi
