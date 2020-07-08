package ots.data.export.store;

import com.alicloud.openservices.tablestore.SyncClient;
import com.alicloud.openservices.tablestore.TableStoreException;
import com.alicloud.openservices.tablestore.core.utils.Base64;
import com.alicloud.openservices.tablestore.model.Column;
import com.alicloud.openservices.tablestore.model.DescribeTableRequest;
import com.alicloud.openservices.tablestore.model.DescribeTableResponse;
import com.alicloud.openservices.tablestore.model.Direction;
import com.alicloud.openservices.tablestore.model.GetRangeRequest;
import com.alicloud.openservices.tablestore.model.GetRangeResponse;
import com.alicloud.openservices.tablestore.model.PrimaryKeyBuilder;
import com.alicloud.openservices.tablestore.model.PrimaryKeyColumn;
import com.alicloud.openservices.tablestore.model.PrimaryKeyType;
import com.alicloud.openservices.tablestore.model.PrimaryKeyValue;
import com.alicloud.openservices.tablestore.model.RangeRowQueryCriteria;
import com.alicloud.openservices.tablestore.model.Row;
import org.apache.http.HttpStatus;
import ots.data.export.bean.Record;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * TableStore操作用クラス.
 */
public class TableStoreManager {

    /**
     * クライアント.
     */
    private final SyncClient client;

    private final String instanceName;

    /**
     * ローカル確認用コンストラクタ.
     *
     * @param endPoint     エンドポイント
     * @param accessKey    アクセスキー
     * @param secretKey    シークレットキー
     * @param instanceName インスタンス名
     */
    public TableStoreManager(String endPoint, String accessKey, String secretKey,
                             String instanceName) {
        client = new SyncClient(endPoint, accessKey, secretKey, instanceName);
        this.instanceName = instanceName;
    }

    /**
     * TableStoreクライアントをシャットダウンする.
     */
    public void clientShutDown() {
        client.shutdown();
    }

    /**
     * Table情報の取得.
     *
     * @param tableName テーブル名
     * @return テーブル情報
     */
    public DescribeTableResponse describeTable(String tableName) {
        DescribeTableRequest request = new DescribeTableRequest();
        request.setTableName(tableName);

        DescribeTableResponse response = null;

        try {
            response = client.describeTable(request);
        } catch (TableStoreException e) {
            if (e.getHttpStatus() == HttpStatus.SC_NOT_FOUND) {
                System.err.println("テーブルが見つかりませんでした。 instance-name:" + instanceName + " table-name:" + tableName);
                System.exit(404);
            } else {
                throw e;
            }
        }
        return response;
    }

    /**
     * テーブルレコード情報取得.
     *
     * @param tableName テーブル名
     * @param primaryKeyInfo プライマリキーじょうほう
     * @param limit 取得最大数
     * @param desc 降順かどうか
     * @param startColumn 範囲選択最小値カラム
     * @param endColumn 範囲選択最大値カラム
     * @return テーブルレコードリスト
     */
    public List<Record> getTableRecord(String tableName, Map<String, PrimaryKeyType> primaryKeyInfo, int limit, boolean desc
            , Map<String, Object> startColumn, Map<String, Object> endColumn) {
        RangeRowQueryCriteria criteria = new RangeRowQueryCriteria(tableName);

        // 最小値・最大値設定
        PrimaryKeyBuilder minBuilder = PrimaryKeyBuilder.createPrimaryKeyBuilder();
        PrimaryKeyBuilder maxBuilder = PrimaryKeyBuilder.createPrimaryKeyBuilder();

        for (Map.Entry<String, PrimaryKeyType> primaryKey : primaryKeyInfo.entrySet()) {
            String keyName = primaryKey.getKey();
            PrimaryKeyType keyType = primaryKey.getValue();

            // 最小値設定.範囲選択に指定されているキーの場合、指定した値を入れる。未指定の場合INF_MIN
            if (startColumn != null && startColumn.containsKey(keyName)) {
                switch (keyType) {
                    case STRING:
                        minBuilder.addPrimaryKeyColumn(keyName, PrimaryKeyValue.fromString((String) startColumn.get(keyName)));
                        break;
                    case INTEGER:
                        minBuilder.addPrimaryKeyColumn(keyName, PrimaryKeyValue.fromLong((Long) startColumn.get(keyName)));
                        break;
                    case BINARY:
                        minBuilder.addPrimaryKeyColumn(keyName, PrimaryKeyValue.fromBinary(Base64.fromBase64String((String) startColumn.get(keyName))));
                        break;
                    default:
                        throw new RuntimeException("予期しない例外が発生しました");
                }
            } else {
                minBuilder.addPrimaryKeyColumn(keyName, PrimaryKeyValue.INF_MIN);
            }

            // 最大値設定.範囲選択に指定されているキーの場合、指定した値を入れる。未指定の場合INF_MAX
            if (endColumn != null && endColumn.containsKey(keyName)) {
                switch (keyType) {
                    case STRING:
                        maxBuilder.addPrimaryKeyColumn(keyName, PrimaryKeyValue.fromString((String) endColumn.get(keyName)));
                        break;
                    case INTEGER:
                        maxBuilder.addPrimaryKeyColumn(keyName, PrimaryKeyValue.fromLong((Long) endColumn.get(keyName)));
                        break;
                    case BINARY:
                        maxBuilder.addPrimaryKeyColumn(keyName, PrimaryKeyValue.fromBinary(Base64.fromBase64String((String) endColumn.get(keyName))));
                        break;
                    default:
                        throw new RuntimeException("予期しない例外が発生しました");
                }
            } else {
                maxBuilder.addPrimaryKeyColumn(keyName, PrimaryKeyValue.INF_MAX);
            }
        }

        // 昇順・降順処理
        if (desc) {
            criteria.setInclusiveStartPrimaryKey(maxBuilder.build());
            criteria.setExclusiveEndPrimaryKey(minBuilder.build());
            criteria.setDirection(Direction.BACKWARD);
        } else {
            criteria.setInclusiveStartPrimaryKey(minBuilder.build());
            criteria.setExclusiveEndPrimaryKey(maxBuilder.build());
            criteria.setDirection(Direction.FORWARD);
        }

        criteria.setMaxVersions(1);

        // 取得制限が設定されていない場合、全取得(INTの最大値とする)
        if (limit == 0) {
            limit = Integer.MAX_VALUE;
        }

        int index = 1;
        List<Record> records = new ArrayList<>();

        while (true) {
            // レコード取得
            GetRangeResponse response = client.getRange(new GetRangeRequest(criteria));
            for (Row row : response.getRows()) {
                Map<String, String> dataMap = new LinkedHashMap<>();
                for (Map.Entry<String, PrimaryKeyColumn> primaryKey : row.getPrimaryKey().getPrimaryKeyColumnsMap().entrySet()) {
                    dataMap.put(primaryKey.getKey(), primaryKey.getValue().getValue().toString());
                }

                for (Column column : row.getColumns()) {
                    dataMap.put(column.getName(), column.getValue().toString());
                }

                Record record = new Record();
                record.setIndex(String.valueOf(index++));
                record.setDataMap(dataMap);
                records.add(record);

                // 取得制限を超えたら終了
                if (index > limit) {
                    break;
                }
            }

            // 一度のリクエストで取得制限となった場合は次のプライマリーキーから取得する
            if (response.getNextStartPrimaryKey() != null) {
                criteria.setInclusiveStartPrimaryKey(response.getNextStartPrimaryKey());
            } else {
                break;
            }
        }

        return records;
    }

}