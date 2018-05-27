package com.github.datalking.jdbc.transaction;

import java.sql.Connection;

/**
 * @author yaoo on 5/27/18
 */
public interface TransactionDefinition {

    /// 事务的传播级别，7级，定义的是事务的控制范围
    // 支持当前事务，如果当前没有事务，就新建一个事务，spring默认的事务传播级别
    int PROPAGATION_REQUIRED = 0;
    // 支持当前事务，如果当前没有事务，就以非事务方式执行
    int PROPAGATION_SUPPORTS = 1;
    // 支持当前事务，如果当前没有事务，就抛出异常
    int PROPAGATION_MANDATORY = 2;
    // 新建事务，如果当前存在事务，把当前事务挂起
    int PROPAGATION_REQUIRES_NEW = 3;
    // 以非事务方式执行操作，如果当前存在事务，就把当前事务挂起
    int PROPAGATION_NOT_SUPPORTED = 4;
    // 以非事务方式执行，如果当前存在事务，则抛出异
    int PROPAGATION_NEVER = 5;
    // 如果当前上下文存在事务，则嵌套事务执行，如果不存在事务，则新建事务
    // 嵌套是子事务套在父事务中执行，子事务是父事务的一部分，在进入子事务之前，父事务建立一个回滚点，叫save point，然后执行子事务，
    // 这个子事务的执行也算是父事务的一部分，然后子事务执行结束，父事务继续执行
    int PROPAGATION_NESTED = 6;

    /// 事务的数据隔离级别，4级，定义的是事务在数据库读写方面的控制
    int ISOLATION_DEFAULT = -1;
    // 读取未提交，保证了读取过程中不会读取到非法数据
    int ISOLATION_READ_UNCOMMITTED = Connection.TRANSACTION_READ_UNCOMMITTED;
    // 读取已提交，保证了一个事务不会读到另一个并行事务已修改但未提交的数据，避免了脏读
    int ISOLATION_READ_COMMITTED = Connection.TRANSACTION_READ_COMMITTED;
    // 可重复读，mysql默认级别，保证了一个事务不会修改已经由另一个事务读取但未提交的数据，避免了脏读和不可重复读的情况
    int ISOLATION_REPEATABLE_READ = Connection.TRANSACTION_REPEATABLE_READ;
    // 可序列化，最严格的级别，事务串行执行，资源消耗最大
    int ISOLATION_SERIALIZABLE = Connection.TRANSACTION_SERIALIZABLE;

    // 事务超时时间
    int TIMEOUT_DEFAULT = -1;

    int getPropagationBehavior();

    int getIsolationLevel();

    int getTimeout();

    boolean isReadOnly();

    // 获取事务名，可以返回null
    String getName();


}
