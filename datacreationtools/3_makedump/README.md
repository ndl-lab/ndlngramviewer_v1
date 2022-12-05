# ngram集計結果のダンプデータ作成・インデックス


## 1.ダンプデータの作成
次のコマンドでKeyDBコンテナ内に入り、ダンプデータの出力を行ってください。(コンテナ名keydb_redis_1の箇所は、docker psコマンド等で起動中のコンテナから確認してください。)
```
docker exec -it keydb_redis_1 bash
```
(dockerコンテナ内で)
```
#keydb-cli save
```
実行後、redisdataディレクトリ以下にdump.rdbファイルが出力されます。

## 2.ダンプデータのインデックス
この手順ではNDL Ngram Viewerのビルド済アプリケーションを利用します。

自分でビルドするか、又はリリースパッケージから取得してください。

ダンプファイル（rdbファイル）から直接Elasticsearchにインデックスする場合は次のコマンドを実行してください。
```
java -jar ngramviewer-0.1.jar batch parse-rdb (rdbファイルのパス)
```

当館が配布しているngramの全件データのように、一旦json.gzとして出力することも可能です。

この場合は以下のコマンドを実行してください。
```
java -jar ngramviewer-0.1.jar batch rdb2json (rdbファイルのパス) (出力したいjson.gzのパス) (※オプション　ngramの総頻度の切り捨ての閾値)
```

