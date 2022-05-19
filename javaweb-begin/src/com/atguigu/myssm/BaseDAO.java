package com.atguigu.myssm;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseDAO<T> {
//    public final String URL = "jdbc:sqlserver://10.46.1.55:1433;DatabaseName=Ignition_SCADA_test?useUnicode=true&characterEncoding=uft-8";
    public final String URL = "jdbc:sqlserver://10.46.1.55:1433;DatabaseName=Ignition_SCADA_test";
    public final String USER = "scadatest";
    public final String PWD = "Cqst.2000";
    public final String DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    Connection conn;
    PreparedStatement ps;
    ResultSet rs;
    //T的Class对象
    private Class entityClass;

    public BaseDAO() {
        Type genericType = getClass().getGenericSuperclass();
        //ParameterizedType参数化类型
        Type[] actualTypeArguments = ((ParameterizedType) genericType).getActualTypeArguments();
        //获取T的真实类型
        Type actualType = actualTypeArguments[0];
        try {
            entityClass = Class.forName(actualType.getTypeName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取连接
     * @return 连接
     */
    private Connection getConn() {
        try {
            Class.forName(DRIVER);
            return DriverManager.getConnection(URL, USER, PWD);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 关闭资源
     * @param rs rs
     * @param ps ps
     * @param conn conn
     */
    protected void close(ResultSet rs, PreparedStatement ps, Connection conn) {
        try {
            if (rs != null) {
                rs.close();
            }
            if(ps != null) {
                ps.close();
            }
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected void setParams(PreparedStatement ps, Object...params) throws SQLException {
        if (params != null && params.length != 0) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
        }
    }
    /**
     * 执行更新，返回影响行数
     * @param sql sql
     * @param params params
     * @return int
     */
    protected int executeUpdate(String sql, Object...params) {
        try {
            conn = getConn();
            ps = conn.prepareStatement(sql);
            setParams(ps, params);
            return ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(rs, ps, conn);
        }
        return 0;
    }

    private void setValue(Object obj, String property, Object propertyValue) {
        Class<?> clazz = obj.getClass();
        try {
            //获取property字符串对应的属性名，比如"fid"去找obj中的fid属性
            Field field = clazz.getDeclaredField(property);
            if(field != null) {
                field.setAccessible(true);
                field.set(obj, propertyValue);
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    protected List<T> executeQuery(String sql, Object...params) {
        List<T> list = new ArrayList<>();
        try {
            conn = getConn();
            ps = conn.prepareStatement(sql);
            setParams(ps, params);
            rs = ps.executeQuery();
            //通过rs可以获取结果集的元数据
            ResultSetMetaData rsmd = rs.getMetaData();
            //获取结果集列数
            int columnCount = rsmd.getColumnCount();
            while (rs.next()) {
                T entity = (T) entityClass.getDeclaredConstructor().newInstance();

                for (int i = 0; i < columnCount; i++) {
                    String columnName = rsmd.getColumnName(i + 1);
                    Object columnValue = rs.getObject(i + 1);
                    setValue(entity, columnName, columnValue);
                }
                list.add(entity);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return list;
    }
}

