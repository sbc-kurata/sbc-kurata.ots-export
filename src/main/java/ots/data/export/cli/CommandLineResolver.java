package ots.data.export.cli;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.cli.CommandLine;

import java.io.IOException;
import java.util.Map;

/**
 * コマンドライン解決.
 */
public class CommandLineResolver {

    /**
     * コマンドライン引数の解決をする.
     *
     * @param commandLine コマンドライン引数
     * @return コマンドライン引数解決オブジェクト
     */
    public static CommandLineArgs resolveArgs(CommandLine commandLine) {
        CommandLineArgs args = new CommandLineArgs();

        args.setTableName(commandLine.getOptionValue(CommandLineArgsOption.TABLE_NAME.getShortName()));
        args.setPath(commandLine.getOptionValue(CommandLineArgsOption.PATH.getShortName()));
        args.setRegion(commandLine.getOptionValue(CommandLineArgsOption.REGION_NAME.getShortName()));
        args.setAccessKey(commandLine.getOptionValue(CommandLineArgsOption.ACCESS_KEY.getShortName()));
        args.setSecretKey(commandLine.getOptionValue(CommandLineArgsOption.SECRET_KEY.getShortName()));
        args.setInstanceName(commandLine.getOptionValue(CommandLineArgsOption.INSTANCE_NAME.getShortName()));

        if (commandLine.hasOption(CommandLineArgsOption.DESC.getShortName())) {
            args.setDesc(true);
        }

        if (commandLine.hasOption(CommandLineArgsOption.LIMIT.getShortName())) {
            int limit = Integer.parseInt(commandLine.getOptionValue(CommandLineArgsOption.LIMIT.getShortName()));
            if (limit <= 0) {
                throw new RuntimeException("The minimum value of Limit is 1.  input value = " + limit);
            }
            args.setLimit(limit);
        } else {
            args.setLimit(0);
        }

        // 出力先は、デフォルト ./
        if (commandLine.hasOption(CommandLineArgsOption.OUTPUT.getShortName())) {
            args.setOutput(commandLine.getOptionValue(CommandLineArgsOption.OUTPUT.getShortName()));
        } else {
            args.setOutput("./");
        }

        ObjectMapper om = new ObjectMapper();

        if (commandLine.hasOption(CommandLineArgsOption.START_COLUMN.getShortName())) {
            try {
                args.setStartColumn(om.readValue(commandLine.getOptionValue(CommandLineArgsOption.START_COLUMN.getShortName()),
                        new TypeReference<Map<String, Object>>(){}));
            } catch (IOException e) {
                throw new RuntimeException("Jsonパースに失敗しました。StartColumnの入力値を確認してください。" );
            }
        }

        if (commandLine.hasOption(CommandLineArgsOption.END_COLUMN.getShortName())) {
            try {
                args.setEndColumn(om.readValue(commandLine.getOptionValue(CommandLineArgsOption.END_COLUMN.getShortName()),
                        new TypeReference<Map<String, Object>>(){}));
            } catch (IOException e) {
                throw new RuntimeException("Jsonパースに失敗しました。EndColumnの入力値を確認してください。" );
            }
        }

        return args;
    }
}
