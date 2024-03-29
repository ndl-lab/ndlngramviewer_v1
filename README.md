# NDL Ngram Viewer(Version 1)

## 概要
著作権保護期間が満了した図書資料28万点を対象に、2022年5月から12月までNDLラボの実験サービスとして公開していた[NDL Ngram Viewer](https://lab.ndl.go.jp/ngramviewer/)のソースコードです。

2023年1月にリニューアルしたNDL Ngram Viewerは、対象範囲を大きく拡大することに伴って大幅な改修を行ったため、[Version 2](https://github.com/ndl-lab/ndlngramviewer_v2)としてリポジトリを分けて公開しています。


## このリポジトリについて

このREADMEはローカル環境(http://localhost:9981/ngramviewer/
)にアプリケーションを構築する手順を説明しています。

自分でビルドする場合は、appディレクトリ内のREADME.mdを参考にしてください。

なお、リリース時のアプリケーションのURLやElasticsearchのエンドポイントの設定は
[app/src/main/resources/config/application.yml](app/src/main/resources/config/application.yml)を編集してビルドすることで変更可能です。


## サービス起動手順
### 1. アプリケーションの準備
自分でビルドする場合は、appディレクトリ内のREADME.mdを参考にしてください。

本リポジトリが配布するパッケージを利用する場合にはjavaのリリースバイナリを下記からダウンロードして適当な場所に展開してください。

### 2. サービス提供用Elasticsearchの準備
infra-docker/es_dockerに、サービス提供用Elasticsearchのdocker-composeの設定ファイル一式があります。infra-docker/README.mdの指示に従ってElasticsearchコンテナを起動してください。

### 3. インデックスの初期化
動作確認を兼ねて、ビルド済アプリケーションを置いたディレクトリで下記のコマンドを実行してください。

**【注意！】Elasticsearchコンテナのデータが初期化されますので気を付けてください**
```
java -jar ngramviewer-0.1.jar batch create-index all
```

### 4. 投入用データのダウンロード
自分で投入用データの作成を行う場合には、datacreationtoolsディレクトリ内のREADME.mdを参考にしてください。


当館が用意したデータを投入する場合、NDL Ngram Viewer version1の全データを次のURLから公開しています。

https://lab.ndl.go.jp/dataset/ngramviewer/sorted-tosho-pdmngram.json.gz
(20.7GB)

例えば以下のようにして、データをダウンロードしてください。
```
curl -OL https://lab.ndl.go.jp/dataset/ngramviewer/sorted-tosho-pdmngram.json.gz
```

投入用データのライセンス情報については次のリポジトリを参照してください。

https://github.com/ndl-lab/ndlngramdata


### 5. 投入用データのインデックス
下記のコマンドを実行してください。
```
java -jar ngramviewer-0.1.jar batch index-gzip sorted-tosho-pdmngram.json.gz
```

### 6. サービスの起動
下記のコマンドを実行してサービスを起動してください。
```
java -jar ngramviewer-0.1.jar web
```

実行後、

http://localhost:9981/ngramviewer/

から起動したサービスにアクセスできます。


## 参考文献
技術的な詳細や機能については、下記の文献を参照してください。

青池亨. 日本語資料の全文テキストデータ分析ツールNDL Ngram Viewerの開発について. じんもんこん2022.

青池亨. E2533 - NDL Ngram Viewerの公開：全文テキストデータ可視化サービス カレントアウェアネス-E, No.442, 国立国会図書館(https://current.ndl.go.jp/e2533)

