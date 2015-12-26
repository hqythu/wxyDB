package me.hqythu.manager;

import me.hqythu.object.Table;
import me.hqythu.exception.SQLExecException;
import me.hqythu.exception.SQLRecordException;
import me.hqythu.exception.SQLTableException;
import me.hqythu.util.SetValue;
import me.hqythu.util.Where;

import java.util.List;


//recordLength 记录的长度
//recordSizePerPage 每一页记录的个数
//size         文件中记录总数
//fieldSize    记录的字段个数
//recordFieldPos 记录的每个字段的位置
/**
 * RecordManager
 * 记录管理器
 * 记录的插入、删除、修改
 */
public class RecordManager {

    private static RecordManager manager = null;

    public static RecordManager getInstance() {
        if (manager == null) {
            manager = new RecordManager();
        }
        return manager;
    }

    public void insert(String tableName, String[] fields, Object[] values) throws SQLExecException, SQLTableException, SQLRecordException {
        Table table = SystemManager.getInstance().getTable(tableName);
        if (table == null) throw new SQLExecException("not have table: "+tableName);
        table.insert(fields,values);
    }

    public void insert(String tableName, Object[] values) throws SQLExecException, SQLTableException, SQLRecordException {
        Table table = SystemManager.getInstance().getTable(tableName);
        if (table == null) throw new SQLExecException("not have table: "+tableName);
        table.insert(values);
    }

    public void remove(String tableName, Where where) throws SQLExecException, SQLTableException {
        Table table = SystemManager.getInstance().getTable(tableName);
        if (table == null) throw new SQLExecException("not have table: "+tableName);
        table.remove(where);
    }

    public void update(String tableName, Where where, List<SetValue> setValues) throws SQLExecException, SQLTableException {
        Table table = SystemManager.getInstance().getTable(tableName);
        if (table == null) throw new SQLExecException("not have table: "+tableName);
        table.update(where,setValues);
    }
    
    private RecordManager() {

    }
}

