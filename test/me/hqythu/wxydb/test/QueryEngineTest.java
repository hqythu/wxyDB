package me.hqythu.wxydb.test;

import me.hqythu.wxydb.manager.QueryEngine;
import me.hqythu.wxydb.manager.RecordManager;
import me.hqythu.wxydb.manager.SystemManager;
import me.hqythu.wxydb.object.Column;
import me.hqythu.wxydb.object.DataType;
import me.hqythu.wxydb.object.Table;
import me.hqythu.wxydb.util.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static java.lang.Math.abs;

/**
 * Created by apple on 15/12/26.
 */
public class QueryEngineTest {


    public static final String TEST_DB = "test_query.db";
    public static final String TEST_TABLE1 = "Student";
    public static final String TEST_TABLE2 = "Customer";
    public static final int BIG_NUM = 10000;
    public static final int NORMAL_NUM = 100;
    public static final int NUM1 = 100;
    public static final int NUM2 = 200;

    @Before
    public void setUp() throws Exception {
        SystemManager.getInstance().createDatabase(TEST_DB);
        SystemManager.getInstance().useDatabase(TEST_DB);

        List<Column> columns = new ArrayList<>();
        // 创建表
        columns.clear();
        columns.add(new Column("name",DataType.VARCHAR,(short)20));
        columns.add(new Column("age",DataType.INT,(short)4));
        Assert.assertTrue(SystemManager.getInstance().createTable("Student",columns));

        // 创建表
        columns.clear();
        columns.add(new Column("id",DataType.INT,(short)4));
        columns.add(new Column("name",DataType.VARCHAR,(short)100));
        columns.add(new Column("sex",DataType.VARCHAR,(short)1));
        Assert.assertTrue(SystemManager.getInstance().createTable("Customer",columns));

    }

    @After
    public void tearDown() throws Exception {
        SystemManager.getInstance().dropDatabase(TEST_DB);
    }

    /**
     * 测试多表连接,小数据
     */
    @Test
    public void testTableJoin() throws Exception {
        List<Map<Table,Object[]>> results;
        Set<String> tableNames;

        // 初始化插入数据
        // 表1
        List<Object> record = new ArrayList<>();
        record.add("LiuXiaoHong");
        record.add(0);
        for (int i = 0; i < NUM1; i++) {
            record.set(1,i);
            RecordManager.getInstance().insert(TEST_TABLE1,record);
        }
        // 表2
        record.clear();
        record.add(0);
        record.add("LiuXiaoHong");
        record.add("F");
        for (int i = 0; i < NUM2; i++) {
            record.set(0,i);
            RecordManager.getInstance().insert(TEST_TABLE2,record);
        }

        tableNames = new HashSet<>();

        tableNames.add(TEST_TABLE1);
        results = QueryEngine.getInstance().tableJoinRecords(tableNames);
        Assert.assertEquals(NUM1,results.size());

        tableNames.add(TEST_TABLE2);
        results = QueryEngine.getInstance().tableJoinRecords(tableNames);
        Assert.assertEquals(NUM1*NUM2,results.size());

    }

    /**
     * 测试多表连接,一个表为空
     */
    @Test
    public void testNormal() throws Exception {
        List<Map<Table,Object[]>> results;
        Set<String> tableNames;

        // 初始化插入数据
        // 表1
        List<Object> record = new ArrayList<>();
        record.add("LiuXiaoHong");
        record.add(0);
        for (int i = 0; i < BIG_NUM; i++) {
            record.set(1,i);
            RecordManager.getInstance().insert(TEST_TABLE1,record);
        }

        tableNames = new HashSet<>();

        tableNames.add(TEST_TABLE1);
        results = QueryEngine.getInstance().tableJoinRecords(tableNames);
        Assert.assertEquals(BIG_NUM,results.size());

        tableNames.add(TEST_TABLE2);
        results = QueryEngine.getInstance().tableJoinRecords(tableNames);
        Assert.assertEquals(0,results.size());
    }

    /**
     * 测试查询
     */
    @Test
    public void testQuery() throws Exception {
        List<Map<Table,Object[]>> results;
        List<Object[]> queryset;
        Set<String> tableNames;
        SelectOption select;
        Where where;

        // 初始化插入数据
        // 表1
        List<Object> record = new ArrayList<>();
        record.add("LiuXiaoHong");
        record.add(0);
        for (int i = 0; i < 10; i++) {
            record.set(1,i);
            RecordManager.getInstance().insert(TEST_TABLE1,record);
        }

        record.clear();
        record.add(0);
        record.add("LiuXiaoHong");
        record.add("F");
        for (int i = 0; i < 2; i++) {
            record.set(0,i);
            RecordManager.getInstance().insert(TEST_TABLE2,record);
        }

        select = new SelectOption();
        select.add(TEST_TABLE1,"age");
        select.add(TEST_TABLE2,"id");
        select.addFromTable(TEST_TABLE1);
        select.addFromTable(TEST_TABLE2);
        where = new Where();
        where.boolExprsAndOps.add(new BoolExpr(TEST_TABLE1,"age", CompareOp.GEQ, 7, true));
        where.isExprs.add(true);
        queryset = QueryEngine.getInstance().query(select, where);
        Assert.assertEquals(6,queryset.size());
    }

    /**
     * 测试聚集查询
     */
    @Test
    public void testFunc() throws Exception {
        double temp;
        double avg,sum,min,max;
        Func func;

        avg = 0;
        sum = 0;
        // 初始化插入数据
        // 表1
        List<Object> record = new ArrayList<>();
        record.add("LiuXiaoHong");
        record.add(0);
        for (int i = 0; i < 10; i++) {
            avg += i;
            sum += i;
            record.set(1,i);
            RecordManager.getInstance().insert(TEST_TABLE1,record);
        }
        avg /= 10;

        record.clear();
        record.add(0);
        record.add("LiuXiaoHong");
        record.add("F");
        RecordManager.getInstance().insert(TEST_TABLE2,record);

        func = Func.AVG;
        temp = QueryEngine.getInstance().func(func,TEST_TABLE1, "age");
        Assert.assertTrue(abs(avg-temp) < 1e6);
        func = Func.SUM;
        temp = QueryEngine.getInstance().func(func,TEST_TABLE1, "age");
        Assert.assertTrue(abs(sum-temp) < 1e6);
        max = 9;
        func = Func.MAX;
        temp = QueryEngine.getInstance().func(func,TEST_TABLE1, "age");
        Assert.assertTrue(abs(max-temp) < 1e6);
        min = 0;
        func = Func.MIN;
        temp = QueryEngine.getInstance().func(func,TEST_TABLE1, "age");
        Assert.assertTrue(abs(min-temp) < 1e6);
    }

    @Test
    public void testSelectAll() throws Exception {
        List<Map<Table,Object[]>> results;
        List<Object[]> queryset;
        Set<String> tableNames;
        SelectOption select;
        Where where;


        // 初始化插入数据
        // 表1
        List<Object> record = new ArrayList<>();
        record.add("LiuXiaoHong");
        record.add(0);
        for (int i = 0; i < 10; i++) {
            record.set(1,i);
            RecordManager.getInstance().insert(TEST_TABLE1,record);
        }
        // 表2
        record.clear();
        record.add(0);
        record.add("LiuXiaoHong");
        record.add("F");
        RecordManager.getInstance().insert(TEST_TABLE2,record);
        select = new SelectOption(true);
        select.addFromTable(TEST_TABLE1);
        select.addFromTable(TEST_TABLE2);
        where = new Where();
        where.boolExprsAndOps.add(new BoolExpr(TEST_TABLE1,"age", CompareOp.GEQ, 7, true));
        where.isExprs.add(true);
        queryset = QueryEngine.getInstance().query(select, where);
        Assert.assertEquals(3,queryset.size());
        Assert.assertEquals(5,queryset.get(0).length);
    }
}
