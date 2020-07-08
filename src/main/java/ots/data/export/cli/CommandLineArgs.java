package ots.data.export.cli;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * コマンドライン引数.
 */
@Getter
@Setter
public class CommandLineArgs {

    /** インスタンス名. */
    private String instanceName;

    /** テーブル名. */
    private String tableName;

    /** リージョン名. */
    private String region;

    /** アクセスキー. */
    private String accessKey;

    /** シークレットキー. */
    private String secretKey;

    /** テンプレートパス. */
    private String path;

    /** 取得最大数. */
    private int limit;

    /** 降順かどうか. */
    private boolean desc;

    /** 出力先フォルダ. */
    private String output;

    /** 検索開始行. */
    private Map<String, Object> startColumn;

    /** 検索終了行. */
    private Map<String, Object> endColumn;

}
