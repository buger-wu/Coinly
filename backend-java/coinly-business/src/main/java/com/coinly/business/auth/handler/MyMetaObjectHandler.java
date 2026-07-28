package com.coinly.business.auth.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 字段自动填充处理器。
 *
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        if (metaObject.hasGetter("createTime")) {
            strictInsertFill(metaObject, "createTime", LocalDateTime::now, LocalDateTime.class);
        }
        if (metaObject.hasGetter("updateTime")) {
            strictInsertFill(metaObject, "updateTime", LocalDateTime::now, LocalDateTime.class);
        }
        if (metaObject.hasGetter("deleted")) {
            strictInsertFill(metaObject, "deleted", () -> 0, Integer.class);
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        if (metaObject.hasGetter("updateTime")) {
            strictUpdateFill(metaObject, "updateTime", LocalDateTime::now, LocalDateTime.class);
        }
    }
}