package ots.data.export;

import com.alicloud.openservices.tablestore.model.DescribeTableResponse;
import com.alicloud.openservices.tablestore.model.PrimaryKeyType;
import com.gh.mygreen.xlsmapper.XlsMapper;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import ots.data.export.bean.Record;
import ots.data.export.cli.CommandLineArgs;
import ots.data.export.cli.CommandLineArgsOption;
import ots.data.export.store.TableStoreManager;
import ots.data.export.cli.CommandLineResolver;
import ots.data.export.xls.LogSheet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * メインクラス.
 */
public class ExportDataMain {

    /**
     * メイン関数.
     *
     * @param args コマンドライン引数
     * @throws IOException IOエラー
     */
    public static void main(String[] args) throws IOException {

        // コマンドライン引数定義取得
        Options options = CommandLineArgsOption.createOption();

        // 第一引数が -h か --Help ならHELP出力して終了
        if (args.length >= 1 && (args[0].equals("-h") || args[0].equals("--Help"))) {
            HelpFormatter f = new HelpFormatter();
            f.printHelp("OptionsTip", options);
            System.exit(0);
        }

        // コマンドライン引数の解決
        CommandLineParser parser = new DefaultParser();
        CommandLineArgs commandLineArgs;
        try {
            CommandLine commandLine = parser.parse(options, args);
            commandLineArgs = CommandLineResolver.resolveArgs(commandLine);
        } catch (ParseException e) {
            System.err.println(e);
            HelpFormatter f = new HelpFormatter();
            f.printHelp("OptionsTip", options);
            return;
        }

        final String instanceName = commandLineArgs.getInstanceName();
        final String tableName = commandLineArgs.getTableName();

        // TableStoreエンドポイントのURL文字列生成
        final String endPoint = "https://" + instanceName + "." + commandLineArgs.getRegion() + ".ots.aliyuncs.com";

        TableStoreManager tableStoreManager = new TableStoreManager(endPoint, commandLineArgs.getAccessKey(), commandLineArgs.getSecretKey(), instanceName);

        // テーブル情報取得 PrimaryKey取得
        System.out.println("テーブル情報の取得を開始します");
        DescribeTableResponse tableResponse = tableStoreManager.describeTable(tableName);
        Map<String, PrimaryKeyType> primaryKeyMap = new LinkedHashMap<>(tableResponse.getTableMeta().getPrimaryKeyMap());
        System.out.println("テーブル情報の取得が完了しました");

        checkPrimaryKey(primaryKeyMap, commandLineArgs.getStartColumn(), commandLineArgs.getEndColumn());

        // テーブルレコード取得
        System.out.println("テーブルレコードの取得を開始します");
        List<Record> records = tableStoreManager.getTableRecord(tableName, primaryKeyMap, commandLineArgs.getLimit(),
                commandLineArgs.isDesc(), commandLineArgs.getStartColumn(), commandLineArgs.getEndColumn());
        System.out.println("テーブルレコードの取得が完了しました");

        tableStoreManager.clientShutDown();

        if (!records.isEmpty()) {

            System.out.println("Excel出力を開始します");

            // Excel書き込み用オブジェクト
            LogSheet sheet = new LogSheet();
            sheet.setTableName(tableName);
            sheet.setData(records);

            File file = new File(commandLineArgs.getOutput(), tableName + ".xlsx");

            // Excel書き込み
            XlsMapper xlsMapper = new XlsMapper();
            InputStream inputStream = new FileInputStream(commandLineArgs.getPath());
            xlsMapper.save(inputStream, new FileOutputStream(file), sheet);

            System.out.println("Excel出力が完了しました : " + file.getPath());
        } else {
            System.out.println("テーブルレコードが存在しないため終了します");
        }
    }

    /**
     * 範囲選択使用時に、指定したキーがテーブルのプライマリキーと一致しているかを確認する.
     * 不一致またはバイナリ型のプライマリキーが指定してある場合は、例外を出力し終了sる.
     *
     * @param primaryKey プライマリキー情報
     * @param startColumn 開始カラム情報
     * @param endColumn 終了カラム情報
     */
    public static void checkPrimaryKey(Map<String, PrimaryKeyType> primaryKey, Map<String, Object> startColumn, Map<String, Object> endColumn) {
        if (startColumn != null) {
            startColumn.keySet().forEach(key -> {
                if (!primaryKey.containsKey(key)) {
                    throw new RuntimeException("StartColumnのプライマリキー名がテーブル情報と一致しません。 : " + key);
                }
            });
        }

        if (endColumn != null) {
            endColumn.keySet().forEach(key -> {
                if (!primaryKey.containsKey(key)) {
                    throw new RuntimeException("EndColumnのプライマリキー名がテーブル情報と一致しません。 : " + key);
                }
            });
        }
    }
}
