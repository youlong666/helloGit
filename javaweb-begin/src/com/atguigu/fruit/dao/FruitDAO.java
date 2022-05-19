package com.atguigu.fruit.dao;

import com.atguigu.fruit.pojo.Fruit;
import com.atguigu.myssm.BaseDAO;

import java.util.List;

public interface FruitDAO{
    public List<Fruit> queryAll(String sql);
}
