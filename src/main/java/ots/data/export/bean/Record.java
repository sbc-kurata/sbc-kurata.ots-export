package ots.data.export.bean;

import com.gh.mygreen.xlsmapper.annotation.XlsColumn;
import com.gh.mygreen.xlsmapper.annotation.XlsMapColumns;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * TableStoreレコード.
 */
@Getter
@Setter
public class Record {

    /** インデックス. */
    @XlsColumn(columnName="No.")
    private String index;

    /** 行データ. */
    @XlsMapColumns(previousColumnName="No.")
    private Map<String, String> dataMap;

}