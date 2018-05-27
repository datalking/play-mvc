//package com.github.datalking.jdbc.datasource;
//
//import com.github.datalking.util.Assert;
//
//import javax.sql.DataSource;
//import java.lang.reflect.InvocationHandler;
//import java.lang.reflect.InvocationTargetException;
//import java.lang.reflect.Method;
//import java.lang.reflect.Proxy;
//import java.sql.Connection;
//import java.sql.SQLException;
//import java.sql.Statement;
//
///**
// * JDBC {@link javax.sql.DataSource}能的代理类，能够处理spring托管的事务
// *
// * @author yaoo on 5/26/18
// */
//public class TransactionAwareDataSourceProxy extends DelegatingDataSource {
//
//    private boolean reobtainTransactionalConnections = false;
//
//    public TransactionAwareDataSourceProxy() {
//    }
//
//    public TransactionAwareDataSourceProxy(DataSource targetDataSource) {
//        super(targetDataSource);
//    }
//
//    public void setReobtainTransactionalConnections(boolean reobtainTransactionalConnections) {
//        this.reobtainTransactionalConnections = reobtainTransactionalConnections;
//    }
//
//    @Override
//    public Connection getConnection() throws SQLException {
//        DataSource ds = getTargetDataSource();
//        Assert.state(ds != null, "'targetDataSource' is required");
//        return getTransactionAwareConnectionProxy(ds);
//    }
//
//    protected Connection getTransactionAwareConnectionProxy(DataSource targetDataSource) {
//        return (Connection) Proxy.newProxyInstance(
//                ConnectionProxy.class.getClassLoader(),
//                new Class[]{ConnectionProxy.class},
//                new TransactionAwareInvocationHandler(targetDataSource));
//    }
//
//    protected boolean shouldObtainFixedConnection(DataSource targetDataSource) {
//        return (!TransactionSynchronizationManager.isSynchronizationActive() ||
//                !this.reobtainTransactionalConnections);
//    }
//
//    private class TransactionAwareInvocationHandler implements InvocationHandler {
//
//        private final DataSource targetDataSource;
//
//        private Connection target;
//
//        private boolean closed = false;
//
//        public TransactionAwareInvocationHandler(DataSource targetDataSource) {
//            this.targetDataSource = targetDataSource;
//        }
//
//        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
//            // Invocation on ConnectionProxy interface coming in...
//
//            if (method.getName().equals("equals")) {
//                // Only considered as equal when proxies are identical.
//                return (proxy == args[0]);
//            } else if (method.getName().equals("hashCode")) {
//                // Use hashCode of Connection proxy.
//                return System.identityHashCode(proxy);
//            } else if (method.getName().equals("toString")) {
//                // Allow for differentiating between the proxy and the raw Connection.
//                StringBuilder sb = new StringBuilder("Transaction-aware proxy for target Connection ");
//                if (this.target != null) {
//                    sb.append("[").append(this.target.toString()).append("]");
//                } else {
//                    sb.append(" from DataSource [").append(this.targetDataSource).append("]");
//                }
//                return sb.toString();
//            } else if (method.getName().equals("unwrap")) {
//                if (((Class) args[0]).isInstance(proxy)) {
//                    return proxy;
//                }
//            } else if (method.getName().equals("isWrapperFor")) {
//                if (((Class) args[0]).isInstance(proxy)) {
//                    return true;
//                }
//            } else if (method.getName().equals("close")) {
//                // Handle close method: only close if not within a transaction.
//                DataSourceUtils.doReleaseConnection(this.target, this.targetDataSource);
//                this.closed = true;
//                return null;
//            } else if (method.getName().equals("isClosed")) {
//                return this.closed;
//            }
//
//            if (this.target == null) {
//                if (this.closed) {
//                    throw new SQLException("Connection handle already closed");
//                }
//                if (shouldObtainFixedConnection(this.targetDataSource)) {
//                    this.target = DataSourceUtils.doGetConnection(this.targetDataSource);
//                }
//            }
//            Connection actualTarget = this.target;
//            if (actualTarget == null) {
//                actualTarget = DataSourceUtils.doGetConnection(this.targetDataSource);
//            }
//
//            if (method.getName().equals("getTargetConnection")) {
//                // Handle getTargetConnection method: return underlying Connection.
//                return actualTarget;
//            }
//
//            // Invoke method on target Connection.
//            try {
//                Object retVal = method.invoke(actualTarget, args);
//
//                // If return value is a Statement, apply transaction timeout.
//                // Applies to createStatement, prepareStatement, prepareCall.
//                if (retVal instanceof Statement) {
//                    DataSourceUtils.applyTransactionTimeout((Statement) retVal, this.targetDataSource);
//                }
//
//                return retVal;
//            } catch (InvocationTargetException ex) {
//                throw ex.getTargetException();
//            } finally {
//                if (actualTarget != this.target) {
//                    DataSourceUtils.doReleaseConnection(actualTarget, this.targetDataSource);
//                }
//            }
//        }
//    }
//
//}
