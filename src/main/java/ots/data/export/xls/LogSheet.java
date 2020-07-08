package ots.data.export.xls;

import com.gh.mygreen.xlsmapper.annotation.*;
import lombok.Getter;
import lombok.Setter;
import ots.data.export.bean.Record;

import java.util.List;

/**
 * Excel書き込み用オブジェクト.
 */
@XlsSheet(name="TableData")
@Setter
@Getter
public class LogSheet {

    /** 開始時間. */
    @XlsLabelledCell(label="TableName", type= LabelledCellType.Right)
    private String tableName;

    /** データ一覧. */
    @XlsHorizontalRecords(tableLabel="Data")
    @XlsRecordOption(overOperation= XlsRecordOption.OverOperation.Insert)
    private List<Record> data;
}
