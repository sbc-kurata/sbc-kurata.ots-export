package ots.data.export.cli;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.util.EnumSet;

/**
 * コマンドライン引数定義.
 */
@AllArgsConstructor
@Getter
public enum CommandLineArgsOption {

    /** インスタンス名. */
    INSTANCE_NAME("i", "InstanceName", true, true, "(必須)インスタンス名"),

    /** テーブル名. */
    TABLE_NAME("t", "TableName", true, true, "(必須)テーブル名"),

    /** リージョン名. */
    REGION_NAME("r", "RegionName", true, true, "(必須)リージョン名"),

    /** アクセスキー. */
    ACCESS_KEY("ak", "AccessKey", true, true, "(必須)APIアクセスキー"),

    /** シークレットキー. */
    SECRET_KEY("sk", "SecretKey", true, true, "(必須)APIシークレットキー"),

    /** テンプレートパス. */
    PATH("p", "Path", true, true, "(必須)テンプレートパス"),

    /** 出力先フォルダ. */
    OUTPUT("o", "OutPut", false, true, "結果出力ディレクトリ"),

    /** 降順指定. */
    DESC("d", "Desc", false, false, "降順にするかどうか(引数なし)"),

    /** 取得数. */
    LIMIT("l", "Limit", false, true, "取得最大数"),

    /** 検索開始行. */
    START_COLUMN("sc", "StartColumn", false, true, "範囲選択開始位置(最小値)"),

    /** 検索終了行. */
    END_COLUMN("ec", "EndColumn", false, true, "範囲選択終了位置(最大値)"),

    /** ヘルプ. */
    HELP("h", "Help", false, false, "ヘルプ出力");

    /** 引数名. */
    private final String shortName;

    /** 引数名(ロング). */
    private final String longName;

    /** 必須パラメータかどうか. */
    private final boolean required;

    /** 値を持つか. */
    private final boolean hasArgs;

    /** 説明. */
    private final String description;

    /**
     * コマンドライン引数定義作成.
     *
     * @return コマンドライン引数
     */
    public static Options createOption() {
        Options options = new Options();

        EnumSet.allOf(CommandLineArgsOption.class)
                .forEach(args ->
                    options.addOption(Option.builder(args.shortName)
                            .longOpt(args.longName)
                            .required(args.required)
                            .hasArg(args.hasArgs)
                            .desc(args.description)
                            .build())
                );

        return options;
    }
}
